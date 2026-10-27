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

    function blankCell(className = '') {
        const element = document.createElement('td');
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

    function normalizeSearch(value) {
        return String(value ?? '')
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLocaleLowerCase('pt-BR')
            .trim();
    }

    function matchesSearch(query, values) {
        const normalizedQuery = normalizeSearch(query);
        if (!normalizedQuery) return true;
        return normalizeSearch(values.filter(value => value != null).join(' ')).includes(normalizedQuery);
    }

    function renderProfessorTable() {
        const tbody = document.querySelector('#cadastro-professor tbody');
        tbody.replaceChildren();
        if (!data.professores.length) return setTableState(tbody, 4, 'Nenhum professor cadastrado.');
        const filtrados = data.professores.filter(professor => matchesSearch(
            document.getElementById('professorSearchInput').value,
            [professor.nome, professor.formacao, formatDate(professor.dataContratacao),
                Number(professor.status) === 1 ? 'Ativo' : 'Inativo']
        ));
        if (!filtrados.length) return setTableState(tbody, 4, 'Nenhum professor encontrado para essa busca.');
        filtrados.forEach(professor => appendRow(tbody, [
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
            const matriculas = data.matriculas.filter(item => Number(item.status) === 2 && item.turma?.codigo === turma.codigo);
            const column = document.createElement('div');
            column.className = 'col-md-6 col-xl-4';
            const card = document.createElement('article');
            card.className = 'stat-card classroom-dashboard-card';
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
            count.className = 'badge classroom-enrollment-count rounded-pill px-3';
            count.textContent = `${matriculas.length} matrícula(s)`;
            const rosterButton = document.createElement('a');
            rosterButton.className = 'btn btn-sm btn-outline-dark-blue rounded-pill';
            rosterButton.href = `${dashboard.dataset.classroomRosterUrl}?codigo=${encodeURIComponent(turma.codigo)}`;
            rosterButton.textContent = 'Ver alunos';
            const editButton = document.createElement('a');
            editButton.className = 'btn btn-sm btn-outline-dark-blue rounded-pill';
            editButton.href = `${dashboard.dataset.classroomEditUrl}?codigo=${encodeURIComponent(turma.codigo)}`;
            editButton.innerHTML = '<i class="fa-solid fa-pen-to-square me-1" aria-hidden="true"></i>Editar';
            const deleteButton = document.createElement('button');
            deleteButton.type = 'button';
            deleteButton.className = 'btn btn-sm btn-outline-danger rounded-pill';
            deleteButton.dataset.deleteClassroom = turma.codigo;
            deleteButton.dataset.classroomName = turma.nomeTurma || 'esta turma';
            deleteButton.innerHTML = '<i class="fa-solid fa-trash-can me-1" aria-hidden="true"></i>Excluir';
            const actions = document.createElement('div');
            actions.className = 'classroom-card-actions';
            actions.append(count, rosterButton);
            const managementActions = document.createElement('div');
            managementActions.className = 'classroom-management-actions';
            managementActions.append(editButton, deleteButton);
            card.append(title, teacher, schoolYear, actions, managementActions);
            column.append(card);
            grid.append(column);
        });
    }

    function openClassroomDeletion(button) {
        const matriculasVinculadas = data.matriculas.filter(item =>
            Number(item.turma?.codigo) === Number(button.dataset.deleteClassroom)
        ).length;
        document.getElementById('deleteClassroomName').textContent = button.dataset.classroomName;
        document.getElementById('deleteClassroomImpact').textContent = matriculasVinculadas
            ? `${matriculasVinculadas} matrícula(s) permanecerão no sistema, mas ficarão sem turma atribuída.`
            : 'Nenhuma matrícula está vinculada a esta turma.';
        document.getElementById('confirmDeleteClassroom').dataset.classroomCodigo = button.dataset.deleteClassroom;
        window.bootstrap.Modal.getOrCreateInstance(document.getElementById('deleteClassroomModal')).show();
    }

    async function deleteClassroom(button) {
        const codigo = button.dataset.classroomCodigo;
        const originalText = button.innerHTML;
        button.disabled = true;
        button.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Excluindo...';
        try {
            await request(`${endpoints.turmas}/${encodeURIComponent(codigo)}`, { method: 'DELETE' });
            window.bootstrap.Modal.getOrCreateInstance(document.getElementById('deleteClassroomModal')).hide();
            await refreshDashboard();
            window.showAppAlert('Turma excluída com sucesso.', 'success');
        } catch (error) {
            window.showAppAlert(error.message || 'Não foi possível excluir a turma.', 'danger');
        } finally {
            button.disabled = false;
            button.innerHTML = originalText;
        }
    }

    function renderMatriculas() {
        const tbody = document.querySelector('#matriculas tbody');
        tbody.replaceChildren();
        const solicitacoes = data.matriculas.filter(matricula => Number(matricula.status) >= 0 && Number(matricula.status) <= 4);
        const filtro = document.getElementById('matriculaFiltro').value;
        const termoBusca = document.getElementById('matriculaSearchInput').value;
        const filtradas = filtro === 'todos'
            ? solicitacoes
            : solicitacoes.filter(matricula => Number(matricula.status) === Number(filtro));
        const statusInfo = {
            0: { label: 'Aguardando análise', badge: 'text-bg-primary' },
            1: { label: 'Em análise', badge: 'text-bg-warning' },
            2: { label: 'Aprovada', badge: 'text-bg-success' },
            3: { label: 'Rejeitada', badge: 'text-bg-danger' },
            4: { label: 'Cancelada', badge: 'text-bg-secondary' }
        };
        const filtradasPorBusca = filtradas.filter(matricula => matchesSearch(termoBusca, [
            matricula.nr,
            matricula.aluno?.nome,
            matricula.turma?.nomeTurma,
            formatDate(matricula.dataEnvio),
            statusInfo[Number(matricula.status)]?.label,
            matricula.motivoRejeicao
        ]));
        renderCount('matriculas-aguardando', solicitacoes.filter(item => Number(item.status) === 0).length);
        renderCount('matriculas-pendentes', solicitacoes.filter(item => Number(item.status) === 1).length);
        renderCount('matriculas-aprovadas', solicitacoes.filter(item => Number(item.status) === 2).length);
        renderCount('matriculas-rejeitadas', solicitacoes.filter(item => Number(item.status) === 3).length);
        renderCount('matriculas-canceladas', solicitacoes.filter(item => Number(item.status) === 4).length);
        if (!filtradasPorBusca.length) {
            return setTableState(
                tbody,
                6,
                !solicitacoes.length ? 'Ainda não há solicitações de matrícula.'
                    : !filtradas.length ? 'Nenhuma solicitação encontrada para este status.'
                        : 'Nenhuma matrícula encontrada para essa busca.'
            );
        }

        filtradasPorBusca.forEach(matricula => {
            const row = document.createElement('tr');
            [matricula.nr, matricula.aluno?.nome, matricula.turma?.nomeTurma, formatDate(matricula.dataEnvio)]
                .forEach(value => row.append(cell(value)));

            const statusCell = blankCell();
            const badge = document.createElement('span');
            const info = statusInfo[Number(matricula.status)];
            badge.className = `badge rounded-pill ${info.badge}`;
            badge.textContent = info.label;
            statusCell.append(badge);
            if (Number(matricula.status) === 3 && matricula.motivoRejeicao) {
                const reason = document.createElement('div');
                reason.className = 'small text-muted mt-1';
                reason.textContent = `Motivo: ${matricula.motivoRejeicao}`;
                statusCell.append(reason);
            }
            row.append(statusCell);

            const actions = blankCell('text-nowrap');
            if (Number(matricula.status) === 0) {
                actions.append(createMatriculaAction(matricula.nr, 1, 'Iniciar análise', 'btn-outline-primary'));
            } else if (Number(matricula.status) === 1) {
                actions.append(createMatriculaAction(matricula.nr, 2, 'Aprovar', 'btn-success'));
                actions.append(document.createTextNode(' '));
                actions.append(createMatriculaAction(matricula.nr, 3, 'Rejeitar', 'btn-outline-danger'));
            } else if (Number(matricula.status) === 2) {
                const assignButton = document.createElement('button');
                assignButton.type = 'button';
                assignButton.className = 'btn btn-sm rounded-pill btn-outline-primary';
                assignButton.dataset.assignMatricula = matricula.nr;
                assignButton.textContent = matricula.turma ? 'Trocar turma' : 'Atribuir turma';
                actions.append(assignButton);
            } else {
                actions.textContent = 'Sem ações disponíveis';
                actions.className = 'small text-muted';
            }
            row.append(actions);
            tbody.append(row);
        });
    }

    function createMatriculaAction(nr, status, label, buttonClass) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = `btn btn-sm rounded-pill ${buttonClass}`;
        button.dataset.matriculaNr = nr;
        button.dataset.matriculaStatus = status;
        button.dataset.matriculaAction = 'true';
        button.textContent = label;
        return button;
    }

    function renderAlunos() {
        const tbody = document.querySelector('#alunos-lista tbody');
        tbody.replaceChildren();
        if (!data.alunos.length) return setTableState(tbody, 3, 'Nenhum aluno cadastrado.');
        let encontrados = 0;
        data.alunos.forEach(aluno => {
            const matricula = data.matriculas
                .filter(item => item.aluno?.codigo === aluno.codigo)
                .sort((left, right) => Number(right.nr) - Number(left.nr))[0];
            const turma = Number(matricula?.status) === 2 ? matricula?.turma?.nomeTurma : null;
            const vinculo = data.vinculos
                .filter(item => item.aluno?.codigo === aluno.codigo)
                .sort((left, right) => Number(left.ordemContato) - Number(right.ordemContato))[0];
            const responsavel = vinculo?.responsavel?.pessoa?.nome;
            if (!matchesSearch(document.getElementById('alunoSearchInput').value, [aluno.nome, turma, responsavel])) return;
            appendRow(tbody, [aluno.nome, turma, responsavel]);
            encontrados += 1;
        });
        if (!encontrados) setTableState(tbody, 3, 'Nenhum aluno encontrado para essa busca.');
    }

    function renderResponsaveis() {
        const tbody = document.querySelector('#responsaveis-lista tbody');
        tbody.replaceChildren();
        if (!data.responsaveis.length) return setTableState(tbody, 4, 'Nenhum responsável cadastrado.');
        const filtrados = data.responsaveis.filter(responsavel => matchesSearch(
            document.getElementById('responsavelSearchInput').value,
            [responsavel.pessoa?.nome, responsavel.pessoa?.cpf, maskCpf(responsavel.pessoa?.cpf),
                responsavel.pessoa?.telefone, maskPhone(responsavel.pessoa?.telefone), responsavel.pessoa?.email]
        ));
        if (!filtrados.length) return setTableState(tbody, 4, 'Nenhum responsável encontrado para essa busca.');
        filtrados.forEach(responsavel => appendRow(tbody, [
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

    function openClassAssignment(nr) {
        const matricula = data.matriculas.find(item => Number(item.nr) === Number(nr));
        if (!matricula || Number(matricula.status) !== 2) {
            window.showAppAlert('A matrícula precisa estar aprovada para receber uma turma.', 'danger');
            return;
        }

        const select = document.getElementById('matriculaTurmaSelect');
        select.replaceChildren(new Option('Selecione uma turma', ''));
        data.turmas
            .slice()
            .sort((left, right) => String(left.nomeTurma || '').localeCompare(String(right.nomeTurma || ''), 'pt-BR'))
            .forEach(turma => {
                const professor = turma.professor?.pessoa?.nome || 'Professor não informado';
                const option = new Option(`${turma.nomeTurma} · ${turma.grau} · ${turma.ano} · ${professor}`, turma.codigo);
                option.selected = Number(turma.codigo) === Number(matricula.turma?.codigo);
                select.add(option);
            });
        document.getElementById('assignStudentName').textContent = matricula.aluno?.nome || 'Aluno';
        document.getElementById('matriculaTurmaForm').dataset.matriculaNr = matricula.nr;
        document.getElementById('saveMatriculaTurma').disabled = data.turmas.length === 0;
        window.bootstrap.Modal.getOrCreateInstance(document.getElementById('matriculaTurmaModal')).show();
    }

    async function saveClassAssignment(event) {
        event.preventDefault();
        const form = event.currentTarget;
        if (!form.reportValidity()) return;

        const button = document.getElementById('saveMatriculaTurma');
        const originalText = button.textContent;
        button.disabled = true;
        button.textContent = 'Salvando...';
        try {
            await request(`${endpoints.matriculas}/${form.dataset.matriculaNr}/turma`, {
                method: 'PATCH',
                body: JSON.stringify({ turmaCodigo: Number(document.getElementById('matriculaTurmaSelect').value) })
            });
            window.bootstrap.Modal.getOrCreateInstance(document.getElementById('matriculaTurmaModal')).hide();
            await refreshDashboard();
            window.showAppAlert('Turma atribuída à matrícula com sucesso.', 'success');
        } catch (error) {
            window.showAppAlert(error.message || 'Não foi possível atribuir a turma.', 'danger');
        } finally {
            button.disabled = false;
            button.textContent = originalText;
        }
    }

    async function atualizarStatusMatricula(nr, status, button) {
        let motivoRejeicao = null;
        if (status === 3) {
            motivoRejeicao = window.prompt('Informe o motivo da rejeição da matrícula:');
            if (motivoRejeicao === null) return;
            motivoRejeicao = motivoRejeicao.trim();
            if (!motivoRejeicao) {
                window.showAppAlert('Informe o motivo da rejeição.', 'danger');
                return;
            }
        }

        button.disabled = true;
        const textoOriginal = button.textContent;
        button.textContent = 'Atualizando...';
        try {
            await request(`${endpoints.matriculas}/${nr}/status`, {
                method: 'PATCH',
                body: JSON.stringify({ status, motivoRejeicao })
            });
            await refreshDashboard();
            window.showAppAlert('Status da matrícula atualizado com sucesso.', 'success');
        } catch (error) {
            window.showAppAlert(error.message || 'Não foi possível atualizar o status.', 'danger');
            button.disabled = false;
            button.textContent = textoOriginal;
        }
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

    document.querySelector('#matriculas tbody').addEventListener('click', event => {
        const assignButton = event.target.closest('[data-assign-matricula]');
        if (assignButton) {
            openClassAssignment(Number(assignButton.dataset.assignMatricula));
            return;
        }
        const button = event.target.closest('[data-matricula-action]');
        if (!button) return;
        atualizarStatusMatricula(Number(button.dataset.matriculaNr), Number(button.dataset.matriculaStatus), button);
    });
    document.getElementById('turmasGrid').addEventListener('click', event => {
        const button = event.target.closest('[data-delete-classroom]');
        if (button) openClassroomDeletion(button);
    });
    document.getElementById('confirmDeleteClassroom').addEventListener('click', event => {
        deleteClassroom(event.currentTarget);
    });
    document.getElementById('matriculaTurmaForm').addEventListener('submit', saveClassAssignment);
    document.getElementById('matriculaFiltro').addEventListener('change', renderMatriculas);
    document.getElementById('professorSearchForm').addEventListener('submit', event => {
        event.preventDefault();
        renderProfessorTable();
    });
    document.getElementById('matriculaSearchForm').addEventListener('submit', event => {
        event.preventDefault();
        renderMatriculas();
    });
    document.getElementById('alunoSearchForm').addEventListener('submit', event => {
        event.preventDefault();
        renderAlunos();
    });
    document.getElementById('responsavelSearchForm').addEventListener('submit', event => {
        event.preventDefault();
        renderResponsaveis();
    });
    wireNavigation();
    initializeSessionGreeting();
    refreshDashboard();
})();
