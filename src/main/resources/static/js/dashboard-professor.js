document.addEventListener('DOMContentLoaded', () => {
    const root = document.getElementById('professor-dashboard');
    if (!root) return;

    const now = new Date();
    const state = {
        dashboard: null,
        returnSection: 'planejamento-section',
        calendarMonth: new Date(now.getFullYear(), now.getMonth(), 1),
        showHistory: false,
        editingPlanCode: null,
        draggedPlanCode: null
    };
    const alertBox = document.getElementById('professor-alert');
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const dashboardUrl = root.dataset.dashboardUrl;

    function escapeHtml(value) {
        return String(value ?? '').replace(/[&<>"']/g, character => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        })[character]);
    }

    function showAlert(message, type = 'success') {
        alertBox.className = `alert alert-${type} professor-alert`;
        alertBox.textContent = message;
        alertBox.classList.remove('d-none');
        alertBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function hideAlert() {
        alertBox.classList.add('d-none');
        alertBox.textContent = '';
    }

    function showCallAlert(message, type = 'warning') {
        const callAlert = document.getElementById('chamada-alert');
        callAlert.className = `alert alert-${type} professor-workspace-alert`;
        callAlert.textContent = message;
        callAlert.classList.remove('d-none');
    }

    async function request(url, options = {}) {
        const headers = new Headers(options.headers || {});
        headers.set('Accept', 'application/json');
        if (options.body) headers.set('Content-Type', 'application/json');
        if (options.method && options.method !== 'GET') {
            if (!csrfToken || !csrfHeader) {
                throw new Error('Não foi possível validar a sessão. Atualize a página e tente novamente.');
            }
            headers.set(csrfHeader, csrfToken);
        }

        const response = await fetch(url, {
            ...options,
            headers,
            credentials: 'same-origin'
        });
        if (response.redirected || response.headers.get('content-type')?.includes('text/html')) {
            throw new Error('Sua sessão pode ter expirado. Entre novamente e tente de novo.');
        }
        const responseText = await response.text();
        let body = null;
        if (responseText) {
            try {
                body = JSON.parse(responseText);
            } catch {
                body = null;
            }
        }
        if (!response.ok) {
            const message = body?.detail || body?.message || body?.mensagem || responseText
                || `Não foi possível concluir a operação (erro ${response.status}).`;
            throw new Error(message);
        }
        if (!body && response.status !== 204) {
            throw new Error('O servidor não retornou os dados esperados. Tente novamente.');
        }
        return body;
    }

    function formatDate(date) {
        if (!date) return 'Data não informada';
        const [year, month, day] = date.split('-').map(Number);
        return new Intl.DateTimeFormat('pt-BR').format(new Date(year, month - 1, day));
    }

    function dateKey(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    function getTodayKey() {
        return dateKey(new Date());
    }

    function getHistoryCutoff() {
        const cutoff = new Date();
        cutoff.setHours(0, 0, 0, 0);
        cutoff.setDate(cutoff.getDate() - 7);
        return dateKey(cutoff);
    }

    function getPlanStatus(date) {
        const today = getTodayKey();
        if (date === today) return { label: 'Hoje', className: 'today' };
        if (date < today) return { label: 'Aula dada', className: 'done' };
        return { label: 'Ainda não dada', className: 'pending' };
    }

    function showPlanMonth(date) {
        const [year, month] = date.split('-').map(Number);
        state.calendarMonth = new Date(year, month - 1, 1);
        if (date < getHistoryCutoff()) state.showHistory = true;
    }

    function getTurma(turmaCodigo) {
        return state.dashboard.turmas.find(turma => turma.codigo === turmaCodigo);
    }

    function getPlano(aulaCodigo) {
        return state.dashboard.planejamentos.find(plano => plano.codigo === aulaCodigo);
    }

    function getFrequencia(aulaCodigo, matriculaCodigo) {
        return state.dashboard.frequencias.find(item =>
            item.aulaCodigo === aulaCodigo && item.matriculaCodigo === matriculaCodigo);
    }

    function getDiario(aulaCodigo, matriculaCodigo) {
        return state.dashboard.diarios.find(item =>
            item.aulaCodigo === aulaCodigo && item.matriculaCodigo === matriculaCodigo);
    }

    function initials(name) {
        return String(name || 'Professor')
            .trim()
            .split(/\s+/)
            .slice(0, 2)
            .map(part => part.charAt(0))
            .join('')
            .toLocaleUpperCase('pt-BR');
    }

    function updateProfile() {
        const professor = state.dashboard;
        const nome = professor.nome || 'Professor(a)';
        const saudacao = document.getElementById('saudacao-prof');
        const nomeHeader = document.getElementById('header-prof-nome');
        const infoHeader = document.getElementById('header-prof-info');
        const avatar = document.getElementById('header-prof-avatar');

        saudacao.textContent = `Olá, Prof. ${nome.trim().split(/\s+/)[0]}`;
        nomeHeader.textContent = nome;
        infoHeader.textContent = professor.formacao || 'Equipe pedagógica';
        avatar.textContent = initials(nome);
        avatar.setAttribute('aria-label', `Perfil de ${nome}`);
    }

    function updateStats() {
        document.getElementById('stat-prof-alunos').textContent = state.dashboard.turmas
            .reduce((total, turma) => total + turma.alunos.length, 0);
        document.getElementById('stat-prof-planos').textContent = state.dashboard.planejamentos.length;
        document.getElementById('stat-prof-diarios').textContent = state.dashboard.diarios.length;
    }

    function updateClassSelects() {
        const turmas = state.dashboard.turmas;
        const options = turmas.map(turma =>
            `<option value="${turma.codigo}">${escapeHtml(turma.nomeTurma)}</option>`).join('');
        document.getElementById('plano-turma').innerHTML =
            `<option value="">Selecione uma turma</option>${options}`;
        document.getElementById('abrir-atividade').disabled = turmas.length === 0;

        const aulaSelect = document.getElementById('diario-aula');
        const previousValue = aulaSelect.value;
        const aulas = state.dashboard.planejamentos;
        aulaSelect.innerHTML = `<option value="">Selecione uma aula</option>${aulas.map(aula =>
            `<option value="${aula.codigo}">${escapeHtml(aula.turmaNome)} · ${formatDate(aula.dataAula)} · ${escapeHtml(aula.atividadeDinamica)}</option>`
        ).join('')}`;
        if (aulas.some(aula => String(aula.codigo) === previousValue)) aulaSelect.value = previousValue;
        aulaSelect.dispatchEvent(new Event('change'));
    }

    function renderPlans() {
        const container = document.getElementById('planejamentos-lista-prof');
        const plans = state.dashboard.planejamentos;
        const cutoff = getHistoryCutoff();
        const archivedPlans = plans.filter(plano => plano.dataAula < cutoff);
        const visiblePlans = plans.filter(plano => state.showHistory || plano.dataAula >= cutoff);
        const monthTitle = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' })
            .format(state.calendarMonth);
        document.getElementById('calendario-mes-titulo').textContent =
            monthTitle.charAt(0).toLocaleUpperCase('pt-BR') + monthTitle.slice(1);
        const historyButton = document.getElementById('calendario-mostrar-historico');
        historyButton.hidden = archivedPlans.length === 0;
        historyButton.innerHTML = state.showHistory
            ? '<i class="fa-solid fa-clock-rotate-left me-2"></i>Ocultar histórico'
            : '<i class="fa-solid fa-clock-rotate-left me-2"></i>Mostrar histórico';
        document.getElementById('calendario-historico-resumo').textContent = archivedPlans.length
            ? `${archivedPlans.length} aula${archivedPlans.length === 1 ? '' : 's'} anterior${archivedPlans.length === 1 ? '' : 'es'} a ${formatDate(cutoff)} ${state.showHistory ? 'exibida' + (archivedPlans.length === 1 ? '' : 's') : 'no histórico'}.`
            : 'O histórico de aulas com mais de 7 dias aparecerá aqui.';

        const firstOfMonth = new Date(state.calendarMonth.getFullYear(), state.calendarMonth.getMonth(), 1);
        const firstWeekday = (firstOfMonth.getDay() + 6) % 7;
        const daysInMonth = new Date(state.calendarMonth.getFullYear(), state.calendarMonth.getMonth() + 1, 0).getDate();
        const cellCount = Math.ceil((firstWeekday + daysInMonth) / 7) * 7;
        const startDate = new Date(firstOfMonth);
        startDate.setDate(startDate.getDate() - firstWeekday);
        const plansByDate = new Map();
        visiblePlans.forEach(plan => {
            if (!plansByDate.has(plan.dataAula)) plansByDate.set(plan.dataAula, []);
            plansByDate.get(plan.dataAula).push(plan);
        });
        plansByDate.forEach(dayPlans => dayPlans.sort((left, right) =>
            left.turmaNome.localeCompare(right.turmaNome, 'pt-BR')));

        container.innerHTML = Array.from({ length: cellCount }, (_, index) => {
            const date = new Date(startDate);
            date.setDate(startDate.getDate() + index);
            const key = dateKey(date);
            const isCurrentMonth = date.getMonth() === state.calendarMonth.getMonth();
            const isToday = key === getTodayKey();
            const dayPlans = plansByDate.get(key) || [];
            return `
                <div class="professor-calendar-day${isCurrentMonth ? '' : ' is-outside-month'}${isToday ? ' is-today' : ''}"
                     data-date="${key}" aria-label="${formatDate(key)}${isToday ? ', hoje' : ''}">
                    <div class="professor-calendar-day-heading">
                        <span class="professor-calendar-day-number">${date.getDate()}</span>
                        ${isToday ? '<span class="professor-calendar-today-label">Hoje</span>' : ''}
                    </div>
                    <div class="professor-calendar-day-plans">
                        ${dayPlans.map(plano => {
                            const status = getPlanStatus(plano.dataAula);
                            const turma = getTurma(plano.turmaCodigo);
                            const registered = state.dashboard.frequencias.filter(item =>
                                item.aulaCodigo === plano.codigo).length;
                            const alunoCount = turma?.alunos.length || 0;
                            return `
                                <article class="professor-calendar-plan is-${status.className}" draggable="true"
                                         data-plan-card="${plano.codigo}" aria-label="${escapeHtml(plano.atividadeDinamica)}; ${status.label}">
                                    <div class="professor-calendar-plan-top">
                                        <span class="professor-calendar-status">${status.label}</span>
                                        <button type="button" class="professor-calendar-drag" aria-label="Arrastar ${escapeHtml(plano.atividadeDinamica)} para outra data" title="Arraste para outra data">
                                            <i class="fa-solid fa-grip-vertical" aria-hidden="true"></i>
                                        </button>
                                    </div>
                                    <strong>${escapeHtml(plano.atividadeDinamica)}</strong>
                                    <span class="professor-calendar-plan-class">${escapeHtml(plano.turmaNome)}</span>
                                    <div class="professor-calendar-plan-actions">
                                        <button type="button" data-plan-edit="${plano.codigo}" aria-label="Editar ${escapeHtml(plano.atividadeDinamica)}">
                                            <i class="fa-solid fa-pen" aria-hidden="true"></i><span>Editar</span>
                                        </button>
                                        <button type="button" data-plan-attendance="${plano.codigo}" aria-label="Fazer chamada de ${escapeHtml(plano.atividadeDinamica)}">
                                            <i class="fa-solid fa-list-check" aria-hidden="true"></i><span>Chamada</span>
                                        </button>
                                    </div>
                                    <small class="professor-calendar-attendance">${registered} de ${alunoCount} presenças registradas</small>
                                </article>`;
                        }).join('')}
                    </div>
                </div>`;
        }).join('');
    }

    function renderClasses() {
        const container = document.getElementById('minhas-turmas-lista');
        const turmas = state.dashboard.turmas;
        container.innerHTML = turmas.length ? turmas.map(turma => `
            <article class="professor-class-card form-card">
                <div class="professor-class-heading">
                    <div class="professor-class-icon"><i class="fa-solid fa-people-group"></i></div>
                    <div>
                        <h5 class="fw-bold text-dark-blue mb-1">${escapeHtml(turma.nomeTurma)}</h5>
                        <p class="text-muted mb-0">${escapeHtml(turma.grau)} · ${turma.ano ?? 'Ano letivo'}</p>
                    </div>
                    <span class="professor-student-count">${turma.alunos.length} aluno${turma.alunos.length === 1 ? '' : 's'}</span>
                </div>
                ${turma.alunos.length ? `
                    <ul class="professor-student-list">
                        ${turma.alunos.map(aluno => `
                            <li>
                                <span class="professor-student-initial">${escapeHtml(initials(aluno.nome))}</span>
                                <span>${escapeHtml(aluno.nome)}</span>
                            </li>`).join('')}
                    </ul>` : `
                    <div class="professor-class-empty">Ainda não há alunos ativos vinculados a esta turma.</div>`}
            </article>`).join('') : `
            <div class="professor-empty-state">
                <i class="fa-solid fa-chalkboard"></i>
                <h5>Nenhuma turma vinculada</h5>
                <p>Quando uma turma for atribuída à sua conta, ela aparecerá aqui.</p>
            </div>`;
    }

    function fillDiaryStudents() {
        const aulaCodigo = Number(document.getElementById('diario-aula').value);
        const alunoSelect = document.getElementById('select-aluno-diario');
        const previousValue = alunoSelect.value;
        const plano = getPlano(aulaCodigo);
        const turma = plano && getTurma(plano.turmaCodigo);
        const presentes = turma?.alunos.filter(aluno =>
            getFrequencia(aulaCodigo, aluno.matriculaCodigo)?.statusPresenca === 'P') || [];

        alunoSelect.disabled = !plano || presentes.length === 0;
        alunoSelect.innerHTML = presentes.length
            ? `<option value="">Selecione um aluno</option>${presentes.map(aluno =>
                `<option value="${aluno.matriculaCodigo}">${escapeHtml(aluno.nome)}</option>`).join('')}`
            : `<option value="">${plano ? 'Registre a presença antes do diário' : 'Selecione primeiro uma aula'}</option>`;
        if (presentes.some(aluno => String(aluno.matriculaCodigo) === previousValue)) {
            alunoSelect.value = previousValue;
        }
        fillDiaryFields();
    }

    function fillDiaryFields() {
        const aulaCodigo = Number(document.getElementById('diario-aula').value);
        const matriculaCodigo = Number(document.getElementById('select-aluno-diario').value);
        const diario = getDiario(aulaCodigo, matriculaCodigo);
        const observacoes = diario?.observacoes || '';
        const saude = observacoes.match(/^Saúde: ([^\n]*)\n?/);

        document.getElementById('diario-humor').value = diario?.humor || 'Feliz';
        document.getElementById('diario-alimentacao').value = diario?.alimentacao || 'Comeu tudo';
        document.getElementById('diario-sono').value = diario?.sono || '';
        document.getElementById('diario-fraldas').value = diario?.banheiroFralda || '';
        document.getElementById('diario-saude').value = saude?.[1] || '';
        document.getElementById('diario-obs').value = saude
            ? observacoes.slice(saude[0].length)
            : observacoes;
    }

    function render() {
        updateProfile();
        updateStats();
        updateClassSelects();
        renderPlans();
        renderClasses();
    }

    async function loadDashboard() {
        try {
            hideAlert();
            state.dashboard = await request(dashboardUrl);
            render();
            return true;
        } catch (error) {
            document.getElementById('planejamentos-lista-prof').innerHTML = `
                <div class="col-12"><div class="alert alert-danger mb-0">${escapeHtml(error.message)}</div></div>`;
            document.getElementById('minhas-turmas-lista').innerHTML = `
                <div class="alert alert-danger mb-0">${escapeHtml(error.message)}</div>`;
            showAlert(`Não foi possível carregar os dados do painel: ${error.message}`, 'danger');
            return false;
        }
    }

    function setActiveSection(sectionId) {
        document.querySelectorAll('.dashboard-section').forEach(section => {
            section.classList.toggle('active', section.id === sectionId);
        });
        document.querySelectorAll('[data-section]').forEach(link => {
            const activeSection = sectionId === 'atividade-editor-section' || sectionId === 'chamada-section'
                ? 'planejamento-section'
                : sectionId;
            const active = link.dataset.section === activeSection;
            link.classList.toggle('active', active);
            if (active) link.setAttribute('aria-current', 'page');
            else link.removeAttribute('aria-current');
        });
    }

    function navigateToSection(sectionId, hash = sectionId) {
        setActiveSection(sectionId);
        if (window.location.hash !== `#${hash}`) {
            window.history.pushState({ sectionId }, '', `#${hash}`);
        }
        document.querySelector('.main-content').scrollTo({ top: 0, behavior: 'smooth' });
    }

    function openActivityScreen(pushHistory = true, planCode = null) {
        state.returnSection = 'planejamento-section';
        const form = document.getElementById('form-planejamento');
        const plan = planCode === null ? null : getPlano(planCode);
        state.editingPlanCode = plan?.codigo ?? null;
        form.reset();
        const turmaSelect = document.getElementById('plano-turma');
        const dateInput = document.getElementById('plano-data');
        const isEditing = Boolean(plan);
        turmaSelect.disabled = isEditing;
        document.getElementById('plano-turma-ajuda').classList.toggle('d-none', !isEditing);
        dateInput.min = isEditing ? '' : getTodayKey();
        document.getElementById('atividade-kicker').textContent = isEditing ? 'Editar aula' : 'Nova atividade';
        document.getElementById('atividade-titulo').textContent = isEditing
            ? 'Ajuste os detalhes da aula'
            : 'Vamos planejar uma aula?';
        document.getElementById('atividade-subtitulo').textContent = isEditing
            ? 'Atualize a proposta ou a data. A turma desta aula permanece a mesma.'
            : 'Organize a proposta e os detalhes da atividade para sua turma.';
        document.getElementById('salvar-planejamento').innerHTML = isEditing
            ? '<i class="fa-solid fa-floppy-disk me-2"></i>Salvar alterações'
            : '<i class="fa-solid fa-floppy-disk me-2"></i>Salvar planejamento';
        if (plan) {
            turmaSelect.value = String(plan.turmaCodigo);
            document.getElementById('plano-titulo').value = plan.atividadeDinamica;
            document.getElementById('plano-desc').value = plan.descricao;
            dateInput.value = plan.dataAula;
        }
        if (pushHistory) navigateToSection('atividade-editor-section', plan ? `editar-planejamento-${plan.codigo}` : 'nova-atividade');
        else setActiveSection('atividade-editor-section');
        document.getElementById('plano-turma').focus();
    }

    document.querySelectorAll('[data-section]').forEach(link => {
        link.addEventListener('click', event => {
            event.preventDefault();
            navigateToSection(link.dataset.section);
            const dropdown = link.closest('.dropdown-menu');
            if (dropdown) {
                const toggle = dropdown.parentElement.querySelector('[data-bs-toggle="dropdown"]');
                bootstrap.Dropdown.getOrCreateInstance(toggle).hide();
            }
        });
    });

    document.getElementById('abrir-atividade').addEventListener('click', () => openActivityScreen());
    document.querySelectorAll('[data-workspace-back]').forEach(button => {
        button.addEventListener('click', () => navigateToSection(button.dataset.workspaceBack || state.returnSection));
    });

    document.getElementById('diario-aula').addEventListener('change', fillDiaryStudents);
    document.getElementById('select-aluno-diario').addEventListener('change', fillDiaryFields);

    document.getElementById('planejamentos-lista-prof').addEventListener('click', event => {
        const target = event.target.closest('button');
        if (!target) return;
        if (target.dataset.planAttendance) window.abrirChamada(Number(target.dataset.planAttendance));
        if (target.dataset.planEdit) openActivityScreen(true, Number(target.dataset.planEdit));
    });

    document.getElementById('calendario-mes-anterior').addEventListener('click', () => {
        state.calendarMonth.setMonth(state.calendarMonth.getMonth() - 1);
        renderPlans();
    });
    document.getElementById('calendario-mes-proximo').addEventListener('click', () => {
        state.calendarMonth.setMonth(state.calendarMonth.getMonth() + 1);
        renderPlans();
    });
    document.getElementById('calendario-hoje').addEventListener('click', () => {
        const today = new Date();
        state.calendarMonth = new Date(today.getFullYear(), today.getMonth(), 1);
        renderPlans();
    });
    document.getElementById('calendario-mostrar-historico').addEventListener('click', () => {
        state.showHistory = !state.showHistory;
        renderPlans();
    });

    const calendar = document.getElementById('planejamentos-lista-prof');
    calendar.addEventListener('dragstart', event => {
        const planCard = event.target.closest('[data-plan-card]');
        if (!planCard) return;
        state.draggedPlanCode = Number(planCard.dataset.planCard);
        planCard.classList.add('is-dragging');
        event.dataTransfer.effectAllowed = 'move';
        event.dataTransfer.setData('text/plain', String(state.draggedPlanCode));
    });
    calendar.addEventListener('dragend', event => {
        event.target.closest('[data-plan-card]')?.classList.remove('is-dragging');
        calendar.querySelectorAll('.is-drop-target').forEach(day => day.classList.remove('is-drop-target'));
        state.draggedPlanCode = null;
    });
    calendar.addEventListener('dragover', event => {
        const day = event.target.closest('.professor-calendar-day[data-date]');
        if (!day || state.draggedPlanCode === null) return;
        event.preventDefault();
        event.dataTransfer.dropEffect = 'move';
        calendar.querySelectorAll('.is-drop-target').forEach(target => target.classList.remove('is-drop-target'));
        day.classList.add('is-drop-target');
    });
    calendar.addEventListener('dragleave', event => {
        const day = event.target.closest('.professor-calendar-day[data-date]');
        if (day && !day.contains(event.relatedTarget)) day.classList.remove('is-drop-target');
    });
    calendar.addEventListener('drop', async event => {
        const day = event.target.closest('.professor-calendar-day[data-date]');
        if (!day || state.draggedPlanCode === null) return;
        event.preventDefault();
        day.classList.remove('is-drop-target');
        await updatePlanDate(state.draggedPlanCode, day.dataset.date);
    });

    document.getElementById('salvar-chamada').addEventListener('click', () => window.salvarChamada());
    document.getElementById('form-planejamento').addEventListener('submit', event => {
        event.preventDefault();
        window.salvarPlanejamento();
    });
    document.getElementById('plano-data').min = getTodayKey();

    window.abrirChamada = function(aulaCodigo, pushHistory = true) {
        const plano = getPlano(aulaCodigo);
        const turma = plano && getTurma(plano.turmaCodigo);
        if (!plano || !turma) {
            showAlert('Não foi possível localizar a turma desta aula.', 'danger');
            return;
        }

        state.returnSection = 'planejamento-section';
        document.getElementById('chamada-titulo').textContent = plano.atividadeDinamica;
        document.getElementById('chamada-subtitulo').textContent =
            'Marque a situação de cada criança para registrar a chamada.';
        document.getElementById('chamada-resumo-turma').textContent = plano.turmaNome;
        document.getElementById('chamada-resumo-aula').textContent =
            `${formatDate(plano.dataAula)} · ${plano.atividadeDinamica}`;
        document.getElementById('chamada-resumo-alunos').textContent =
            `${turma.alunos.length} aluno${turma.alunos.length === 1 ? '' : 's'}`;
        const callAlert = document.getElementById('chamada-alert');
        callAlert.classList.add('d-none');
        callAlert.textContent = '';
        const tbody = document.getElementById('listaChamadaAlunos');
        tbody.innerHTML = turma.alunos.length ? turma.alunos.map(aluno => {
            const frequencia = getFrequencia(aulaCodigo, aluno.matriculaCodigo);
            const status = frequencia?.statusPresenca || '';
            const diario = getDiario(aulaCodigo, aluno.matriculaCodigo);
            return `
                <article class="professor-roster-row">
                    <span class="professor-student-initial">${escapeHtml(initials(aluno.nome))}</span>
                    <div class="professor-roster-student">
                        <strong>${escapeHtml(aluno.nome)}</strong>
                        ${diario ? '<small><i class="fa-solid fa-book-open me-1"></i>Diário preenchido</small>' : ''}
                    </div>
                    <label class="visually-hidden" for="presenca-${aluno.matriculaCodigo}">Presença de ${escapeHtml(aluno.nome)}</label>
                    <select class="form-select attendance-select" id="presenca-${aluno.matriculaCodigo}" data-matricula="${aluno.matriculaCodigo}">
                            <option value="" ${status ? '' : 'selected'}>Não registrada</option>
                            <option value="P" ${status === 'P' ? 'selected' : ''}>Presente</option>
                            <option value="F" ${status === 'F' ? 'selected' : ''}>Falta</option>
                            <option value="J" ${status === 'J' ? 'selected' : ''}>Justificada</option>
                    </select>
                </article>`;
        }).join('') : `
            <div class="professor-empty-state">
                <i class="fa-solid fa-user-check"></i>
                <h5>Não há alunos ativos nesta turma</h5>
                <p>Quando houver matrículas ativas, elas aparecerão nesta chamada.</p>
            </div>`;

        document.getElementById('salvar-chamada').dataset.aulaCodigo = aulaCodigo;
        if (pushHistory) navigateToSection('chamada-section', `chamada-${aulaCodigo}`);
        else setActiveSection('chamada-section');
    };

    window.salvarChamada = async function() {
        const button = document.getElementById('salvar-chamada');
        const aulaCodigo = Number(button.dataset.aulaCodigo);
        const selects = [...document.querySelectorAll('#listaChamadaAlunos [data-matricula]')];
        const pending = selects.filter(select => select.value);
        if (selects.some(select => !select.value)) {
            showCallAlert('Selecione uma situação de presença para cada aluno antes de salvar.');
            return;
        }
        if (!pending.length) {
            showCallAlert('Não há presenças para registrar nesta turma.');
            return;
        }

        button.disabled = true;
        button.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Salvando...';
        let saved = 0;
        try {
            for (const select of pending) {
                await request(root.dataset.frequencyUrl, {
                    method: 'POST',
                    body: JSON.stringify({
                        aulaPlanejamentosCodigo: aulaCodigo,
                        matriculaCodigo: Number(select.dataset.matricula),
                        statusPresenca: select.value
                    })
                });
                saved++;
            }
            if (await loadDashboard()) {
                navigateToSection('planejamento-section');
                showAlert('Chamada salva com sucesso.');
            }
        } catch (error) {
            if (await loadDashboard()) window.abrirChamada(aulaCodigo, false);
            showCallAlert(saved
                ? `A chamada foi salva para ${saved} aluno(s), mas houve uma falha: ${error.message}`
                : `Não foi possível salvar a chamada: ${error.message}`, 'danger');
        } finally {
            button.disabled = false;
            button.innerHTML = '<i class="fa-solid fa-check me-2"></i>Salvar chamada';
        }
    };

    window.salvarDiario = async function() {
        const aulaCodigo = Number(document.getElementById('diario-aula').value);
        const matriculaCodigo = Number(document.getElementById('select-aluno-diario').value);
        if (!aulaCodigo || !matriculaCodigo) {
            showAlert('Selecione a aula e um aluno presente para continuar.', 'warning');
            return;
        }

        const observacoes = document.getElementById('diario-obs').value.trim();
        const saude = document.getElementById('diario-saude').value.trim();
        const observacoesComSaude = [saude ? `Saúde: ${saude}` : '', observacoes]
            .filter(Boolean).join('\n');
        if (observacoesComSaude.length > 500) {
            showAlert('Saúde e observações juntas devem ter no máximo 500 caracteres.', 'warning');
            return;
        }

        const button = document.getElementById('salvar-diario');
        button.disabled = true;
        try {
            await request(root.dataset.diaryUrl, {
                method: 'POST',
                body: JSON.stringify({
                    aulaPlanejamentosCodigo: aulaCodigo,
                    matriculaCodigo,
                    alimentacao: document.getElementById('diario-alimentacao').value,
                    sono: document.getElementById('diario-sono').value.trim(),
                    humor: document.getElementById('diario-humor').value,
                    banheiroFralda: document.getElementById('diario-fraldas').value.trim(),
                    observacoes: observacoesComSaude
                })
            });
            if (await loadDashboard()) {
                document.getElementById('diario-aula').value = String(aulaCodigo);
                fillDiaryStudents();
                document.getElementById('select-aluno-diario').value = String(matriculaCodigo);
                fillDiaryFields();
                showAlert('Diário de bordo salvo e compartilhado com os responsáveis.');
            }
        } catch (error) {
            showAlert(`Não foi possível salvar o diário: ${error.message}`, 'danger');
        } finally {
            button.disabled = false;
        }
    };

    async function updatePlanDate(planCode, newDate) {
        const plan = getPlano(planCode);
        if (!plan || plan.dataAula === newDate) return;

        try {
            await request(`${root.dataset.planUrl}/${planCode}`, {
                method: 'PUT',
                body: JSON.stringify({
                    turmaCodigo: plan.turmaCodigo,
                    dataAula: newDate,
                    atividadeDinamica: plan.atividadeDinamica,
                    descricao: plan.descricao
                })
            });
            showPlanMonth(newDate);
            if (await loadDashboard()) showAlert(`Aula reagendada para ${formatDate(newDate)}.`);
        } catch (error) {
            showAlert(`Não foi possível reagendar a aula: ${error.message}`, 'danger');
        }
    }

    window.salvarPlanejamento = async function() {
        const turmaCodigo = Number(document.getElementById('plano-turma').value);
        const titulo = document.getElementById('plano-titulo').value.trim();
        const descricao = document.getElementById('plano-desc').value.trim();
        const dataAula = document.getElementById('plano-data').value;
        const editing = state.editingPlanCode !== null;
        if (!turmaCodigo || titulo.length < 3 || titulo.length > 200 || descricao.length < 5 || !dataAula
                || (!editing && dataAula < getTodayKey())) {
            showAlert('Informe a turma, o título, a descrição e a data do planejamento.', 'warning');
            return;
        }

        const button = document.getElementById('salvar-planejamento');
        button.disabled = true;
        try {
            await request(editing ? `${root.dataset.planUrl}/${state.editingPlanCode}` : root.dataset.planUrl, {
                method: editing ? 'PUT' : 'POST',
                body: JSON.stringify({
                    turmaCodigo,
                    dataAula,
                    atividadeDinamica: titulo,
                    descricao
                })
            });
            if (editing) showPlanMonth(dataAula);
            state.editingPlanCode = null;
            document.getElementById('form-planejamento').reset();
            document.getElementById('plano-turma').disabled = false;
            document.getElementById('plano-turma-ajuda').classList.add('d-none');
            document.getElementById('plano-data').min = getTodayKey();
            if (await loadDashboard()) {
                navigateToSection('planejamento-section');
                showAlert(editing ? 'Aula atualizada com sucesso.' : 'Planejamento criado com sucesso.');
            }
        } catch (error) {
            showAlert(`Não foi possível ${editing ? 'atualizar' : 'salvar'} o planejamento: ${error.message}`, 'danger');
        } finally {
            button.disabled = false;
        }
    };

    function restoreSectionFromHash() {
        const hash = window.location.hash.slice(1);
        const attendanceMatch = hash.match(/^chamada-(\d+)$/);
        if (attendanceMatch) {
            window.abrirChamada(Number(attendanceMatch[1]), false);
            return;
        }
        if (hash === 'nova-atividade') {
            openActivityScreen(false);
            return;
        }
        const editMatch = hash.match(/^editar-planejamento-(\d+)$/);
        if (editMatch) {
            openActivityScreen(false, Number(editMatch[1]));
            return;
        }
        const target = document.getElementById(hash);
        if (target?.classList.contains('dashboard-section')) setActiveSection(hash);
    }

    window.addEventListener('popstate', restoreSectionFromHash);
    loadDashboard().then(loaded => {
        if (loaded && window.location.hash) restoreSectionFromHash();
    });
});
