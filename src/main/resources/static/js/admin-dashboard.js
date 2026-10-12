(() => {
    const dashboard = document.getElementById('adminDashboard');
    if (!dashboard) return;

    const data = {
        alunos: [],
        professores: [],
        turmas: [],
        matriculas: [],
        responsaveis: [],
        vinculos: [],
        diarios: []
    };

    const endpoints = {
        alunos: dashboard.dataset.alunosUrl,
        professores: dashboard.dataset.professoresUrl,
        turmas: dashboard.dataset.turmasUrl,
        matriculas: dashboard.dataset.matriculasUrl,
        responsaveis: dashboard.dataset.responsaveisUrl,
        vinculos: dashboard.dataset.vinculosUrl,
        diarios: dashboard.dataset.diariosUrl
    };

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    function cell(value, className = '') {
        const element = document.createElement('td');
        element.textContent = value == null || value === '' ? '—' : String(value);
        if (className) element.className = className;
        return element;
    }

    function appendRow(tbody, values) {
        const row = document.createElement('tr');
        values.forEach(value => row.append(cell(value)));
        tbody.append(row);
    }

    function setTableState(tbody, columnCount, message, error = false) {
        tbody.replaceChildren();
        const row = document.createElement('tr');
        const emptyCell = cell(message, `text-center py-4${error ? ' text-danger' : ' text-muted'}`);
        emptyCell.colSpan = columnCount;
        row.append(emptyCell);
        tbody.append(row);
    }

    function formatDate(value) {
        if (!value) return '—';
        const date = new Date(`${value.slice(0, 10)}T00:00:00`);
        return Number.isNaN(date.getTime()) ? value : new Intl.DateTimeFormat('pt-BR').format(date);
    }

    function maskCpf(value) {
        const digits = String(value || '').replace(/\D/g, '');
        return digits.length === 11 ? `***.***.***-${digits.slice(-2)}` : '—';
    }

    function maskPhone(value) {
        const digits = String(value || '').replace(/\D/g, '');
        if (digits.length < 10) return '—';
        return `(${digits.slice(0, 2)}) ${digits.slice(2, -4).replace(/\d/g, '*')}-${digits.slice(-4)}`;
    }

    async function request(url, options = {}) {
        const headers = new Headers(options.headers || {});
        headers.set('Accept', 'application/json');
        if (options.body) headers.set('Content-Type', 'application/json');
        if (options.method && options.method !== 'GET') {
            if (!csrfToken || !csrfHeader) {
                throw new Error('Não foi possível obter o token de segurança da sessão.');
            }
            headers.set(csrfHeader, csrfToken);
        }

        const response = await fetch(url, { ...options, headers });
        const contentType = response.headers.get('content-type') || '';
        let body = null;
        if (contentType.includes('application/json')) {
            body = await response.json();
        } else if (!response.ok && contentType.includes('text/plain')) {
            body = await response.text();
        }

        if (!response.ok) {
            const message = body && typeof body === 'object'
                ? body.mensagem || body.message || body.error || body.detail
                : body;
            throw new Error(message || `Falha na comunicação com o servidor (HTTP ${response.status}).`);
        }
        return body;
    }

    async function loadCollection(name) {
        const response = await request(endpoints[name]);
        if (!Array.isArray(response)) {
            throw new Error(`O servidor retornou um formato inesperado para ${name}.`);
        }
        data[name] = response;
    }

    function renderCount(name, count) {
        const element = document.getElementById(name);
        if (element) element.textContent = count;
    }

    function renderProfessorTable() {
        const tbody = document.querySelector('#cadastro-professor tbody');
        tbody.replaceChildren();
        if (!data.professores.length) return setTableState(tbody, 4, 'Nenhum professor cadastrado.');
        data.professores.forEach(professor => appendRow(tbody, [
            professor.nome,
            professor.formacao,
            formatDate(professor.dataContratacao),
            Number(professor.status) === 1 ? 'Ativo' : 'Inativo'
        ]));
    }

    function renderTurmas() {
        const grid = document.getElementById('turmasGrid');
        grid.replaceChildren();
        if (!data.turmas.length) {
            const empty = document.createElement('div');
            empty.className = 'col-12 text-center text-muted py-4';
            empty.textContent = 'Nenhuma turma cadastrada.';
            grid.append(empty);
            return;
        }

        data.turmas.forEach(turma => {
            const matriculas = data.matriculas.filter(item => item.turma?.codigo === turma.codigo);
            const column = document.createElement('div');
            column.className = 'col-md-6 col-xl-4';
            const card = document.createElement('article');
            card.className = 'stat-card border-start border-4 border-primary';
            const title = document.createElement('h5');
            title.className = 'fw-bold text-dark-blue';
            title.textContent = turma.nomeTurma;
            const teacher = document.createElement('p');
            teacher.className = 'text-muted small mb-1';
            teacher.textContent = `Professor(a): ${turma.professor?.pessoa?.nome || '—'}`;
            const schoolYear = document.createElement('p');
            schoolYear.className = 'text-muted small mb-3';
            schoolYear.textContent = `${turma.ano ?? '—'} · ${turma.grau || '—'}`;
            const count = document.createElement('span');
            count.className = 'badge bg-light text-dark-blue rounded-pill px-3';
            count.textContent = `${matriculas.length} matrícula(s)`;
            card.append(title, teacher, schoolYear, count);
            column.append(card);
            grid.append(column);
        });
    }

    function renderMatriculas() {
        const tbody = document.querySelector('#matriculas tbody');
        tbody.replaceChildren();
        if (!data.matriculas.length) return setTableState(tbody, 5, 'Nenhuma matrícula cadastrada.');
        data.matriculas.forEach(matricula => appendRow(tbody, [
            matricula.nr,
            matricula.aluno?.nome,
            matricula.turma?.nomeTurma,
            formatDate(matricula.dataEnvio),
            matricula.status == null ? '—' : `Código ${matricula.status}`
        ]));
    }

    function renderAlunos() {
        const tbody = document.querySelector('#alunos-lista tbody');
        tbody.replaceChildren();
        if (!data.alunos.length) return setTableState(tbody, 3, 'Nenhum aluno cadastrado.');
        data.alunos.forEach(aluno => {
            const matricula = data.matriculas
                .filter(item => item.aluno?.codigo === aluno.codigo)
                .sort((left, right) => Number(right.nr) - Number(left.nr))[0];
            const turma = matricula?.turma?.nomeTurma;
            const vinculo = data.vinculos
                .filter(item => item.aluno?.codigo === aluno.codigo)
                .sort((left, right) => Number(left.ordemContato) - Number(right.ordemContato))[0];
            const responsavel = vinculo?.responsavel?.pessoa?.nome;
            appendRow(tbody, [aluno.nome, turma, responsavel]);
        });
    }

    function renderResponsaveis() {
        const tbody = document.querySelector('#responsaveis-lista tbody');
        tbody.replaceChildren();
        if (!data.responsaveis.length) return setTableState(tbody, 4, 'Nenhum responsável cadastrado.');
        data.responsaveis.forEach(responsavel => appendRow(tbody, [
            responsavel.pessoa?.nome,
            maskCpf(responsavel.pessoa?.cpf),
            maskPhone(responsavel.pessoa?.telefone),
            responsavel.pessoa?.email
        ]));
    }

    function renderDiarios() {
        const tbody = document.querySelector('#diarios-lista tbody');
        tbody.replaceChildren();
        if (!data.diarios.length) return setTableState(tbody, 5, 'Nenhum registro no diário de bordo.');
        [...data.diarios]
            .sort((left, right) => String(right.dataRegistro).localeCompare(String(left.dataRegistro)))
            .forEach(diario => appendRow(tbody, [
            formatDate(diario.dataRegistro),
            diario.matricula?.aluno?.nome,
            diario.humor,
            diario.alimentacao,
            diario.matricula?.turma?.professor?.pessoa?.nome
            ]));
    }

    function fillSelect(select, items, getValue, getLabel, placeholder) {
        select.replaceChildren();
        const firstOption = document.createElement('option');
        firstOption.value = '';
        firstOption.disabled = true;
        firstOption.selected = true;
        firstOption.textContent = placeholder;
        select.append(firstOption);
        items.forEach(item => {
            const option = document.createElement('option');
            option.value = getValue(item);
            option.textContent = getLabel(item);
            select.append(option);
        });
        select.disabled = items.length === 0;
    }

    function renderSelects() {
        fillSelect(
            document.getElementById('select-prof-turma'),
            data.professores.filter(professor => Number(professor.status) === 1),
            professor => professor.codigo,
            professor => professor.nome,
            'Selecione um professor'
        );
        fillSelect(
            document.getElementById('select-aluno-mat'),
            data.alunos,
            aluno => aluno.codigo,
            aluno => aluno.nome,
            'Selecione um aluno'
        );
        fillSelect(
            document.getElementById('select-turma-mat'),
            data.turmas,
            turma => turma.codigo,
            turma => turma.nomeTurma,
            'Selecione uma turma'
        );
    }

    function renderDashboard() {
        renderCount('stat-alunos', data.alunos.length);
        renderCount('stat-professores', data.professores.length);
        renderCount('stat-turmas', data.turmas.length);
        renderCount('stat-matriculas', data.matriculas.length);
        renderProfessorTable();
        renderTurmas();
        renderMatriculas();
        renderAlunos();
        renderResponsaveis();
        renderDiarios();
        renderSelects();
    }

    async function refreshDashboard() {
        const collections = Object.keys(endpoints);
        const results = await Promise.allSettled(collections.map(loadCollection));
        const failed = [];
        results.forEach((result, index) => {
            if (result.status === 'rejected') {
                failed.push(collections[index]);
            }
        });
        renderDashboard();
        failed.forEach(name => {
            const error = results[collections.indexOf(name)].reason;
            if (name === 'turmas') {
                const grid = document.getElementById('turmasGrid');
                grid.replaceChildren();
                const message = document.createElement('div');
                message.className = 'col-12 text-center text-danger py-4';
                message.textContent = error.message;
                grid.append(message);
            } else {
                const tbody = document.querySelector(`tbody[data-collection="${name}"]`);
                if (tbody) setTableState(tbody, Number(tbody.dataset.columns), error.message, true);
            }

            const statId = {
                alunos: 'stat-alunos',
                professores: 'stat-professores',
                turmas: 'stat-turmas',
                matriculas: 'stat-matriculas'
            }[name];
            if (statId) {
                const stat = document.getElementById(statId);
                stat.textContent = '!';
                stat.title = error.message;
            }
        });
        if (failed.length) {
            window.showAppAlert(`Não foi possível carregar: ${failed.join(', ')}. Confira os detalhes nas respectivas seções.`, 'danger');
        }
    }

    function todayLocalDateTime() {
        const now = new Date();
        const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
        return localDate.toISOString().slice(0, 19);
    }

    function formValue(id) {
        return document.getElementById(id).value.trim();
    }

    async function submitForm(form, url, payload, successMessage, afterSave) {
        if (!form.reportValidity()) return;
        const submitButton = form.querySelector('[type="submit"]');
        submitButton.disabled = true;
        try {
            await request(url, { method: 'POST', body: JSON.stringify(payload) });
            form.reset();
            await afterSave();
            window.showAppAlert(successMessage, 'success');
        } catch (error) {
            window.showAppAlert(error.message || 'Não foi possível concluir a operação.', 'danger');
        } finally {
            submitButton.disabled = false;
        }
    }

    function wireForms() {
        const turmaForm = document.getElementById('novaTurmaForm');
        turmaForm.addEventListener('submit', event => {
            event.preventDefault();
            const professorCodigo = Number(document.getElementById('select-prof-turma').value);
            submitForm(turmaForm, `${endpoints.turmas}/salvar`, {
                nomeTurma: formValue('nova-turma-nome'),
                ano: Number(formValue('nova-turma-ano')),
                grau: document.getElementById('nova-turma-grau').value,
                professor: { pessoaCodigo: professorCodigo }
            }, 'Turma cadastrada com sucesso.', refreshDashboard);
        });

        const matriculaForm = document.getElementById('novaMatriculaForm');
        matriculaForm.addEventListener('submit', event => {
            event.preventDefault();
            const alunoCodigo = Number(document.getElementById('select-aluno-mat').value);
            const turmaCodigo = Number(document.getElementById('select-turma-mat').value);
            submitForm(matriculaForm, `${endpoints.matriculas}/salvar`, {
                aluno: { codigo: alunoCodigo },
                turma: { codigo: turmaCodigo },
                dataEnvio: todayLocalDateTime(),
                status: 0
            }, 'Matrícula enviada para processamento com sucesso.', refreshDashboard);
        });
    }

    function wireNavigation() {
        const links = document.querySelectorAll('.nav-link-custom[data-section]');
        const sections = document.querySelectorAll('.dashboard-section');
        function activateSection(targetId) {
            const target = document.getElementById(targetId);
            if (!target) return;
            links.forEach(item => item.classList.toggle('active', item.dataset.section === targetId));
            sections.forEach(section => section.classList.toggle('active', section.id === targetId));
            if (window.AOS) window.AOS.refresh();
        }

        links.forEach(link => {
            link.addEventListener('click', () => {
                activateSection(link.dataset.section);
            });
        });

        const requestedSection = window.location.hash.slice(1);
        if (requestedSection) activateSection(requestedSection);
    }

    function initializeSessionGreeting() {
        const greeting = document.getElementById('saudacao-admin');
        try {
            const name = sessionStorage.getItem('cg_nome');
            if (name && greeting) greeting.textContent = `Olá, ${name}`;
        } catch (error) {
            if (greeting) greeting.textContent = 'Olá, Administrador';
        }
    }

    document.querySelectorAll('#cadastro-professor tbody, #matriculas tbody, #alunos-lista tbody, #responsaveis-lista tbody, #diarios-lista tbody')
        .forEach(tbody => setTableState(tbody, Number(tbody.dataset.columns), 'Carregando dados...'));

    wireForms();
    wireNavigation();
    initializeSessionGreeting();
    refreshDashboard();
})();
