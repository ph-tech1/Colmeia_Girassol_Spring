(() => {
    'use strict';

    const form = document.getElementById('classroomRegisterForm');
    if (!form) return;

    const teacherSelect = document.getElementById('classroomTeacher');
    const teacherLoadHint = document.getElementById('teacherLoadHint');
    const submitButton = form.querySelector('[type="submit"]');
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const turmaCodigo = new URLSearchParams(window.location.search).get('codigo');
    const isEditing = Boolean(turmaCodigo);
    const title = document.getElementById('classroom-form-title');
    const eyebrow = document.getElementById('classroomEyebrow');
    const formKicker = document.getElementById('classroomFormKicker');
    const description = document.getElementById('classroomWelcomeDescription');
    const formDescription = document.getElementById('classroomFormDescription');
    const welcomeLead = document.getElementById('classroomWelcomeLead');
    const welcomeAccent = document.getElementById('classroomWelcomeAccent');
    const pageTitle = document.querySelector('title');
    const backLink = document.querySelector('.teacher-back-link');
    const cancelLink = document.querySelector('.classroom-cancel-button');
    const currentYear = new Date().getFullYear();
    document.getElementById('classroomYear').value = currentYear;

    if (isEditing) {
        pageTitle.textContent = 'Editar turma | Colmeia Girassol';
        eyebrow.innerHTML = '<i class="fa-solid fa-wand-magic-sparkles me-2" aria-hidden="true"></i>Atualizando nosso cantinho';
        welcomeLead.textContent = 'Cada detalhe ajuda a turma a';
        welcomeAccent.textContent = 'florescer.';
        description.textContent = 'Atualize o nome, a etapa, o ano letivo ou o(a) professor(a) que acompanha os alunos.';
        formKicker.textContent = 'Cuidando da nossa sala';
        title.textContent = 'Editar informações da turma';
        formDescription.textContent = 'As alterações serão refletidas no painel e nas matrículas vinculadas.';
        submitButton.innerHTML = '<i class="fa-solid fa-floppy-disk me-2" aria-hidden="true"></i>Salvar alterações';
        cancelLink.href = backLink.href;
        cancelLink.textContent = 'Cancelar edição';
    }

    async function readResponse(response) {
        const contentType = response.headers.get('content-type') || '';
        if (contentType.includes('application/json')) return response.json();
        const text = await response.text();
        return text || null;
    }

    function responseError(body, status) {
        if (body && typeof body === 'object') {
            return body.mensagem || body.message || body.error || body.detail
                || `Não foi possível concluir a operação (HTTP ${status}).`;
        }
        return body || `Não foi possível concluir a operação (HTTP ${status}).`;
    }

    async function fetchJson(url, options = {}) {
        const headers = new Headers(options.headers || {});
        headers.set('Accept', 'application/json');
        if (options.body) headers.set('Content-Type', 'application/json');
        if (options.method && options.method !== 'GET') {
            if (!csrfToken || !csrfHeader) {
                throw new Error('Não foi possível validar sua sessão. Atualize a página e tente novamente.');
            }
            headers.set(csrfHeader, csrfToken);
        }

        const response = await fetch(url, { ...options, headers });
        if (response.url.includes('/html/login.html')) {
            window.location.assign(response.url);
            throw new Error('Sua sessão expirou. Entre novamente para continuar.');
        }
        const body = await readResponse(response);
        if (!response.ok) throw new Error(responseError(body, response.status));
        return body;
    }

    async function loadTeachers() {
        teacherSelect.replaceChildren(new Option('Carregando professores...', ''));
        const teachers = await fetchJson(form.dataset.professoresUrl);
        if (!Array.isArray(teachers)) {
            throw new Error('O servidor retornou uma lista de professores inválida.');
        }

        const activeTeachers = teachers.filter(teacher => Number(teacher.status) === 1);
        teacherSelect.replaceChildren(new Option(
            activeTeachers.length ? 'Selecione o(a) professor(a)' : 'Nenhum professor ativo disponível',
            ''
        ));
        activeTeachers.forEach(teacher => {
            teacherSelect.add(new Option(teacher.nome, teacher.pessoaCodigo));
        });
        teacherSelect.disabled = activeTeachers.length === 0;
        submitButton.disabled = activeTeachers.length === 0;
        teacherLoadHint.textContent = activeTeachers.length
            ? 'Escolha quem vai guiar as descobertas dessa turma.'
            : 'Cadastre ou ative um professor antes de salvar a turma.';
        return activeTeachers.length > 0;
    }

    async function loadClassForEditing() {
        const turmas = await fetchJson(form.dataset.turmasUrl);
        if (!Array.isArray(turmas)) {
            throw new Error('O servidor retornou uma lista de turmas inválida.');
        }
        const turma = turmas.find(item => String(item.codigo) === turmaCodigo);
        if (!turma) throw new Error('A turma que você deseja editar não foi encontrada.');

        document.getElementById('classroomName').value = turma.nomeTurma || '';
        document.getElementById('classroomYear').value = turma.ano ?? '';
        document.getElementById('classroomLevel').value = turma.grau || '';
        const teacherCodigo = turma.professor?.pessoaCodigo;
        if (teacherCodigo && [...teacherSelect.options].some(option => option.value === String(teacherCodigo))) {
            teacherSelect.value = String(teacherCodigo);
        } else {
            teacherSelect.value = '';
            teacherLoadHint.textContent = 'O(a) professor(a) atual não está ativo. Selecione um professor ativo para salvar.';
        }
    }

    form.addEventListener('submit', async event => {
        event.preventDefault();
        if (!form.reportValidity()) return;

        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Salvando turma...';
        let saved = false;
        try {
            const professorCodigo = Number(teacherSelect.value);
            await fetchJson(isEditing
                ? `${form.dataset.turmasUrl}/${encodeURIComponent(turmaCodigo)}`
                : form.dataset.turmaSaveUrl, {
                method: isEditing ? 'PUT' : 'POST',
                body: JSON.stringify({
                    nomeTurma: document.getElementById('classroomName').value.trim(),
                    ano: Number(document.getElementById('classroomYear').value),
                    grau: document.getElementById('classroomLevel').value,
                    professor: { pessoaCodigo: professorCodigo }
                })
            });
            saved = true;
            window.showAppAlert(
                isEditing ? 'Turma atualizada com sucesso!' : 'Turma criada com sucesso! Ela já está disponível no painel.',
                'success'
            );
            if (isEditing) {
                window.setTimeout(() => window.location.assign(backLink.href), 800);
            } else {
                form.reset();
                document.getElementById('classroomYear').value = currentYear;
            }
        } catch (error) {
            window.showAppAlert(error.message || 'Não foi possível salvar a turma.', 'danger');
        } finally {
            submitButton.innerHTML = isEditing
                ? '<i class="fa-solid fa-floppy-disk me-2" aria-hidden="true"></i>Salvar alterações'
                : '<i class="fa-solid fa-wand-magic-sparkles me-2" aria-hidden="true"></i>Criar minha turma';
            submitButton.disabled = teacherSelect.disabled || (isEditing && saved);
        }
    });

    async function initialize() {
        const hasTeachers = await loadTeachers();
        if (isEditing) await loadClassForEditing();
        submitButton.disabled = !hasTeachers;
    }

    initialize().catch(error => {
        teacherSelect.replaceChildren(new Option('Não foi possível carregar professores', ''));
        teacherSelect.disabled = true;
        submitButton.disabled = true;
        teacherLoadHint.textContent = 'Tente atualizar a página. Se o problema continuar, confira o painel administrativo.';
        window.showAppAlert(error.message || 'Não foi possível preparar os dados da turma.', 'danger');
    });
})();
