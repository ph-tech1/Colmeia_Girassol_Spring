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
        draggedPlanCode: null,
        diarySelection: null
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

    function sanitizeNumericValue(value) {
        return String(value ?? '').replace(/\D/g, '').slice(0, 3);
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

    function formatPhone(value) {
        const digits = String(value || '').replace(/\D/g, '').slice(0, 11);
        if (!digits) return '';
        if (digits.length <= 10) return digits.replace(/(\d{2})(\d{4})(\d{0,4})/, (_, ddd, prefix, suffix) =>
            suffix ? `(${ddd}) ${prefix}-${suffix}` : `(${ddd}) ${prefix}`);
        return digits.replace(/(\d{2})(\d{5})(\d{0,4})/, (_, ddd, prefix, suffix) =>
            suffix ? `(${ddd}) ${prefix}-${suffix}` : `(${ddd}) ${prefix}`);
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

    function renderTeacherInsights() {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const todayKey = dateKey(today);
        const periodStart = new Date(today);
        periodStart.setDate(periodStart.getDate() - 6);
        const periodStartKey = dateKey(periodStart);
        const days = Array.from({ length: 7 }, (_, index) => {
            const date = new Date(periodStart);
            date.setDate(periodStart.getDate() + index);
            return { key: dateKey(date), present: 0, registered: 0 };
        });
        const daysByKey = new Map(days.map(day => [day.key, day]));
        const eligiblePresent = new Set();
        const completedDiaries = new Set();

        state.dashboard.frequencias.forEach(frequency => {
            const plan = getPlano(frequency.aulaCodigo);
            const day = plan && daysByKey.get(plan.dataAula);
            if (!day || plan.dataAula > todayKey || !['P', 'F', 'J'].includes(frequency.statusPresenca)) return;
            day.registered++;
            if (frequency.statusPresenca === 'P') {
                day.present++;
                eligiblePresent.add(`${frequency.aulaCodigo}:${frequency.matriculaCodigo}`);
            }
        });

        state.dashboard.diarios.forEach(diary => {
            const key = `${diary.aulaCodigo}:${diary.matriculaCodigo}`;
            if (eligiblePresent.has(key)) completedDiaries.add(key);
        });

        const attendanceContainer = document.getElementById('prof-chart-presenca');
        const activeDays = days.filter(day => day.registered > 0);
        attendanceContainer.innerHTML = activeDays.length ? `
            <div class="professor-attendance-chart" role="img"
                 aria-label="Percentual de presença nas frequências registradas nos últimos sete dias">
                ${days.map(day => {
                    const percentage = day.registered
                        ? Math.round(day.present * 100 / day.registered)
                        : 0;
                    const date = new Date(`${day.key}T00:00:00`);
                    const label = new Intl.DateTimeFormat('pt-BR', { weekday: 'short' }).format(date)
                        .replace('.', '');
                    return `
                        <div class="professor-attendance-day" title="${formatDate(day.key)}: ${day.registered
                            ? `${day.present} de ${day.registered} presentes (${percentage}%)`
                            : 'Sem frequência registrada'}">
                            <span class="professor-attendance-value">${day.registered ? `${percentage}%` : '—'}</span>
                            <div class="professor-attendance-track">
                                <span style="height:${percentage}%"></span>
                            </div>
                            <span class="professor-attendance-label">${escapeHtml(label)}</span>
                        </div>`;
                }).join('')}
            </div>
            <p class="professor-chart-note mb-0">${activeDays.reduce((sum, day) => sum + day.present, 0)} presenças em ${activeDays.reduce((sum, day) => sum + day.registered, 0)} registros</p>`
            : '<p class="professor-chart-empty">As frequências registradas nos últimos 7 dias aparecerão aqui.</p>';

        const diaryContainer = document.getElementById('prof-chart-diarios');
        const totalEligible = eligiblePresent.size;
        const totalCompleted = completedDiaries.size;
        const completionRate = totalEligible ? Math.round(totalCompleted * 100 / totalEligible) : 0;
        if (totalEligible) {
            diaryContainer.innerHTML = `
                <div class="professor-diary-chart">
                    <div class="professor-diary-donut" role="img"
                         aria-label="${completionRate}% dos diários esperados foram preenchidos"
                         style="--diary-progress:${completionRate}%">
                        <div>
                            <strong>${completionRate}%</strong>
                            <span>preenchidos</span>
                        </div>
                    </div>
                    <div class="professor-diary-legend">
                        <div><span class="professor-legend-dot completed"></span><span>Preenchidos</span><strong>${totalCompleted}</strong></div>
                        <div><span class="professor-legend-dot pending"></span><span>Pendentes</span><strong>${totalEligible - totalCompleted}</strong></div>
                    </div>
                </div>
                <p class="professor-chart-note mb-0">Considera os alunos presentes de ${formatDate(periodStartKey)} a ${formatDate(todayKey)}.</p>`;
        } else {
            diaryContainer.innerHTML = '<p class="professor-chart-empty">Quando houver presenças registradas nesta semana, o andamento dos diários aparecerá aqui.</p>';
        }

        const moodContainer = document.getElementById('prof-chart-humor');
        const moods = [
            { label: 'Feliz', className: 'happy', icon: 'fa-face-smile' },
            { label: 'Tranquilo', className: 'calm', icon: 'fa-face-meh' },
            { label: 'Agitado', className: 'active', icon: 'fa-bolt' },
            { label: 'Choroso', className: 'tearful', icon: 'fa-face-sad-tear' }
        ];
        const moodCounts = new Map(moods.map(mood => [mood.label, 0]));
        state.dashboard.diarios.forEach(diary => {
            const plan = getPlano(diary.aulaCodigo);
            if (!plan || plan.dataAula < periodStartKey || plan.dataAula > todayKey) return;
            if (moodCounts.has(diary.humor)) {
                moodCounts.set(diary.humor, moodCounts.get(diary.humor) + 1);
            }
        });
        const moodTotal = [...moodCounts.values()].reduce((sum, count) => sum + count, 0);
        const largestMoodCount = Math.max(...moodCounts.values(), 0);
        moodContainer.innerHTML = moodTotal ? `
            <div class="professor-mood-chart" role="img"
                 aria-label="Distribuição dos registros de humor nos diários dos últimos sete dias">
                ${moods.map(mood => {
                    const count = moodCounts.get(mood.label);
                    const width = largestMoodCount ? Math.round(count * 100 / largestMoodCount) : 0;
                    return `
                        <div class="professor-mood-row ${mood.className}">
                            <span class="professor-mood-label"><i class="fa-solid ${mood.icon}" aria-hidden="true"></i>${mood.label}</span>
                            <span class="professor-mood-track"><span style="width:${width}%"></span></span>
                            <strong>${count}</strong>
                        </div>`;
                }).join('')}
            </div>
            <p class="professor-chart-note mb-0">${moodTotal} registro${moodTotal === 1 ? '' : 's'} de humor de ${formatDate(periodStartKey)} a ${formatDate(todayKey)}.</p>`
            : '<p class="professor-chart-empty">Os registros de humor dos diários desta semana aparecerão aqui.</p>';
    }

    function updateClassSelects() {
        const turmas = state.dashboard.turmas;
        const options = turmas.map(turma =>
            `<option value="${turma.codigo}">${escapeHtml(turma.nomeTurma)}</option>`).join('');
        document.getElementById('plano-turma').innerHTML =
            `<option value="">Selecione uma turma</option>${options}`;
        document.getElementById('abrir-atividade').disabled = turmas.length === 0;

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
        const turmas = [...state.dashboard.turmas]
            .sort((first, second) => first.nomeTurma.localeCompare(second.nomeTurma, 'pt-BR', { sensitivity: 'base' }));

        container.innerHTML = turmas.length ? turmas.map(turma => {
            const alunosOrdenados = [...turma.alunos].sort((first, second) =>
                first.nome.localeCompare(second.nome, 'pt-BR', { sensitivity: 'base' }));

            return `
            <article class="professor-class-card form-card">
                <div class="professor-class-heading">
                    <div class="professor-class-icon"><i class="fa-solid fa-people-group"></i></div>
                    <div>
                        <h5 class="fw-bold text-dark-blue mb-1">${escapeHtml(turma.nomeTurma)}</h5>
                        <p class="text-muted mb-0">${escapeHtml(turma.grau)} · ${turma.ano ?? 'Ano letivo'}</p>
                    </div>
                    <span class="professor-student-count">${alunosOrdenados.length} aluno${alunosOrdenados.length === 1 ? '' : 's'}</span>
                </div>
                ${alunosOrdenados.length ? `
                    <ul class="professor-student-list">
                        ${alunosOrdenados.map(aluno => {
                            const responsavel = aluno.nomeResponsavel ? `${escapeHtml(aluno.nomeResponsavel)}` : 'Responsável não informado';
                            const telefone = aluno.telefoneResponsavel ? ` · ${escapeHtml(formatPhone(aluno.telefoneResponsavel))}` : '';
                            return `
                                <li>
                                    <span class="professor-student-initial">${escapeHtml(initials(aluno.nome))}</span>
                                    <div class="d-flex flex-column">
                                        <span>${escapeHtml(aluno.nome)}</span>
                                        <small class="text-muted">${responsavel}${telefone}</small>
                                    </div>
                                </li>`;
                        }).join('')}
                    </ul>` : `
                    <div class="professor-class-empty">Ainda não há alunos ativos vinculados a esta turma.</div>`}
            </article>`;
        }).join('') : `
            <div class="professor-empty-state">
                <i class="fa-solid fa-chalkboard"></i>
                <h5>Nenhuma turma vinculada</h5>
                <p>Quando uma turma for atribuída à sua conta, ela aparecerá aqui.</p>
            </div>`;
    }

    function normalizeSearch(value) {
        return String(value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR');
    }

    function getDiarySessions(turmaCodigo, matriculaCodigo) {
        const today = getTodayKey();
        return state.dashboard.planejamentos
            .filter(plano => plano.turmaCodigo === turmaCodigo
                && getFrequencia(plano.codigo, matriculaCodigo)?.statusPresenca === 'P')
            .sort((first, second) =>
                (first.dataAula === today ? 0 : first.dataAula < today ? 1 : 2)
                    - (second.dataAula === today ? 0 : second.dataAula < today ? 1 : 2)
                || (first.dataAula < today && second.dataAula < today
                    ? second.dataAula.localeCompare(first.dataAula)
                    : first.dataAula.localeCompare(second.dataAula))
                || first.codigo - second.codigo);
    }

    function renderDiaryBrowser() {
        const container = document.getElementById('diario-turmas-lista');
        const search = normalizeSearch(document.getElementById('diario-busca').value.trim());
        const turmas = [...state.dashboard.turmas].sort((first, second) =>
            first.nomeTurma.localeCompare(second.nomeTurma, 'pt-BR', { sensitivity: 'base' }));
        const totalAlunos = turmas.reduce((total, turma) => total + turma.alunos.length, 0);
        document.getElementById('diario-total-alunos').textContent =
            `${totalAlunos} aluno${totalAlunos === 1 ? '' : 's'}`;

        if (!turmas.length) {
            container.innerHTML = `
                <div class="professor-empty-state">
                    <i class="fa-solid fa-chalkboard"></i>
                    <h5>Nenhuma turma vinculada</h5>
                    <p>Quando uma turma for atribuída à sua conta, ela aparecerá aqui.</p>
                </div>`;
            return;
        }

        const visiveis = turmas.map(turma => {
            const alunos = [...turma.alunos].sort((first, second) =>
                first.nome.localeCompare(second.nome, 'pt-BR', { sensitivity: 'base' }));
            const turmaCombina = normalizeSearch(turma.nomeTurma).includes(search);
            return {
                turma,
                alunos: search && !turmaCombina
                    ? alunos.filter(aluno => normalizeSearch(aluno.nome).includes(search))
                    : alunos
            };
        }).filter(item => !search
            || normalizeSearch(item.turma.nomeTurma).includes(search)
            || item.alunos.length);

        container.innerHTML = visiveis.length ? visiveis.map(({ turma, alunos }) => `
            <section class="diary-class-card">
                <div class="diary-class-heading">
                    <span class="diary-class-icon"><i class="fa-solid fa-people-group" aria-hidden="true"></i></span>
                    <div class="min-w-0">
                        <h4 class="h6 fw-bold text-dark-blue mb-1">${escapeHtml(turma.nomeTurma)}</h4>
                        <p class="text-muted small mb-0">${escapeHtml(turma.grau)} · ${turma.ano ?? 'Ano letivo'}</p>
                    </div>
                    <span class="diary-roster-total">${turma.alunos.length}</span>
                </div>
                ${alunos.length ? `
                    <ul class="diary-student-list">
                        ${alunos.map(aluno => {
                            const selected = state.diarySelection?.turmaCodigo === turma.codigo
                                && state.diarySelection?.matriculaCodigo === aluno.matriculaCodigo;
                            return `
                                <li>
                                    <button type="button" class="diary-student-button${selected ? ' active' : ''}"
                                            data-diary-student data-turma-codigo="${turma.codigo}"
                                            data-matricula-codigo="${aluno.matriculaCodigo}"
                                            aria-pressed="${selected}">
                                        <span class="professor-student-initial">${escapeHtml(initials(aluno.nome))}</span>
                                        <span class="diary-student-name">${escapeHtml(aluno.nome)}</span>
                                        <i class="fa-solid fa-chevron-right" aria-hidden="true"></i>
                                    </button>
                                </li>`;
                        }).join('')}
                    </ul>` : `
                    <p class="diary-class-empty mb-0">${search
                        ? 'Nenhum aluno corresponde à busca nesta turma.'
                        : 'Ainda não há alunos ativos vinculados a esta turma.'}</p>`}
            </section>`).join('') : `
            <div class="professor-empty-state">
                <i class="fa-solid fa-magnifying-glass"></i>
                <h5>Nenhum resultado</h5>
                <p>Tente buscar pelo nome da turma ou da criança.</p>
            </div>`;
    }

    function clearDiaryFields() {
        document.getElementById('diario-humor').value = 'Feliz';
        document.getElementById('diario-alimentacao').value = 'Comeu tudo';
        document.getElementById('diario-sono').value = '';
        document.getElementById('diario-fraldas').value = '';
        document.getElementById('diario-saude').value = '';
        document.getElementById('diario-obs').value = '';
    }

    function renderDiarySessions(sessions, selection) {
        const container = document.getElementById('diario-sessoes-lista');
        const historyButton = document.getElementById('diario-ver-mais');
        const cutoff = getHistoryCutoff();
        const today = getTodayKey();
        const recentSessions = sessions.filter(plano => plano.dataAula >= cutoff);
        const olderSessions = sessions.filter(plano => plano.dataAula < cutoff);
        const visibleSessions = selection.showHistory ? sessions : recentSessions;
        const aulaInput = document.getElementById('diario-aula');

        historyButton.hidden = olderSessions.length === 0;
        historyButton.innerHTML = selection.showHistory
            ? '<i class="fa-solid fa-chevron-up me-1" aria-hidden="true"></i>Mostrar menos'
            : `<i class="fa-solid fa-clock-rotate-left me-1" aria-hidden="true"></i>Ver mais aulas antigas (${olderSessions.length})`;

        if (!sessions.length) {
            aulaInput.value = '';
            container.innerHTML = '<p class="diary-session-empty mb-0">Nenhuma aula com presença registrada para esta criança.</p>';
            return;
        }

        if (!visibleSessions.length) {
            aulaInput.value = '';
            selection.selectedAulaCodigo = null;
            container.innerHTML = '<p class="diary-session-empty mb-0">Sem aulas recentes. Use “Ver mais aulas antigas” para consultar o histórico.</p>';
            return;
        }

        container.innerHTML = visibleSessions.map(plano => {
            const diario = getDiario(plano.codigo, selection.matriculaCodigo);
            const isToday = plano.dataAula === today;
            const isFuture = plano.dataAula > today;
            const selected = selection.selectedAulaCodigo === plano.codigo;
            const status = diario ? 'Registrado' : 'Pendente';
            const statusClass = diario ? 'complete' : 'pending';
            const date = new Date(`${plano.dataAula}T00:00:00`);
            const day = new Intl.DateTimeFormat('pt-BR', { day: '2-digit' }).format(date);
            const month = new Intl.DateTimeFormat('pt-BR', { month: 'short' }).format(date)
                .replace('.', '');
            const year = new Intl.DateTimeFormat('pt-BR', { year: 'numeric' }).format(date);

            return `
                <button type="button" class="diary-session-card${isToday ? ' is-today' : ''}${selected ? ' is-selected' : ''}"
                        data-diary-session="${plano.codigo}" aria-pressed="${selected}">
                    <span class="diary-session-date">
                        <strong>${day}</strong>
                        <span>${escapeHtml(month)}</span>
                        <small>${year}</small>
                    </span>
                    <span class="diary-session-copy">
                        <span class="diary-session-title">${escapeHtml(plano.atividadeDinamica)}</span>
                        <span class="diary-session-subtitle">${isToday ? 'Aula de hoje' : formatDate(plano.dataAula)}</span>
                    </span>
                    <span class="diary-session-badges">
                        ${isToday ? '<span class="diary-session-today">Hoje</span>' : ''}
                        ${isFuture ? '<span class="diary-session-upcoming">Próxima</span>' : ''}
                        <span class="diary-session-status ${statusClass}">
                            <i class="fa-solid ${diario ? 'fa-circle-check' : 'fa-clock'}" aria-hidden="true"></i>${status}
                        </span>
                    </span>
                </button>`;
        }).join('');

        const selectedSession = visibleSessions.find(plano => plano.codigo === selection.selectedAulaCodigo)
            || visibleSessions.find(plano => plano.dataAula === today)
            || visibleSessions.find(plano => plano.dataAula <= today && !getDiario(plano.codigo, selection.matriculaCodigo))
            || visibleSessions[0];
        selection.selectedAulaCodigo = selectedSession?.codigo ?? null;
        aulaInput.value = selectedSession ? String(selectedSession.codigo) : '';
        container.querySelectorAll('[data-diary-session]').forEach(button => {
            const selected = Number(button.dataset.diarySession) === selection.selectedAulaCodigo;
            button.classList.toggle('is-selected', selected);
            button.setAttribute('aria-pressed', String(selected));
        });
    }

    function renderDiarySelection() {
        const editor = document.getElementById('diario-editor');
        const selection = state.diarySelection;
        const turma = selection && getTurma(selection.turmaCodigo);
        const aluno = turma?.alunos.find(item => item.matriculaCodigo === selection.matriculaCodigo);
        if (!turma || !aluno) {
            state.diarySelection = null;
            editor.hidden = true;
            return;
        }

        editor.hidden = false;
        document.getElementById('select-aluno-diario').value = String(aluno.matriculaCodigo);
        document.getElementById('diario-aluno-inicial').textContent = initials(aluno.nome);
        document.getElementById('diario-aluno-nome').textContent = aluno.nome;
        document.getElementById('diario-aluno-turma').textContent = `${turma.nomeTurma} · ${turma.grau}`;

        const sessions = getDiarySessions(turma.codigo, aluno.matriculaCodigo);
        renderDiarySessions(sessions, selection);

        const emptyState = document.getElementById('diario-sem-aula');
        const fields = document.getElementById('diario-campos');
        const hasSelectedSession = sessions.length > 0 && selection.selectedAulaCodigo !== null;
        emptyState.hidden = hasSelectedSession;
        fields.hidden = !hasSelectedSession;
        if (!hasSelectedSession) {
            emptyState.innerHTML = `
                <i class="fa-solid fa-calendar-check" aria-hidden="true"></i>
                <div>
                    <strong>${sessions.length
                        ? 'Selecione uma aula antiga para abrir este diário.'
                        : 'Ainda não há um dia disponível para este diário.'}</strong>
                    <p class="mb-0">${sessions.length
                        ? 'As aulas com mais de sete dias ficam recolhidas para facilitar a consulta.'
                        : 'Registre a presença da criança em uma aula da turma. Assim que ela estiver marcada como presente, o acompanhamento poderá ser preenchido.'}</p>
                </div>`;
            clearDiaryFields();
            return;
        }
        fillDiaryFields();
    }

    function fillDiaryFields() {
        const aulaCodigo = Number(document.getElementById('diario-aula').value);
        const matriculaCodigo = Number(document.getElementById('select-aluno-diario').value);
        const diario = getDiario(aulaCodigo, matriculaCodigo);
        const observacoes = diario?.observacoes || '';
        const saude = observacoes.match(/^Saúde: ([^\n]*)\n?/);

        if (!aulaCodigo || !matriculaCodigo) {
            clearDiaryFields();
            return;
        }
        document.getElementById('diario-humor').value = diario?.humor || 'Feliz';
        document.getElementById('diario-alimentacao').value = diario?.alimentacao || 'Comeu tudo';
        document.getElementById('diario-sono').value = diario?.sono || '';
        document.getElementById('diario-fraldas').value = sanitizeNumericValue(diario?.banheiroFralda || '');
        document.getElementById('diario-saude').value = saude?.[1] || '';
        document.getElementById('diario-obs').value = saude
            ? observacoes.slice(saude[0].length)
            : observacoes;
        document.getElementById('diario-status').innerHTML = diario
            ? '<i class="fa-solid fa-circle-check" aria-hidden="true"></i> Este dia já tem um diário. Você pode atualizar as informações.'
            : '<i class="fa-solid fa-sparkles" aria-hidden="true"></i> Um novo registro ficará disponível para os responsáveis.';
        document.getElementById('salvar-diario').innerHTML = diario
            ? '<i class="fa-solid fa-floppy-disk me-2"></i>Atualizar diário'
            : '<i class="fa-solid fa-paper-plane me-2"></i>Enviar para os pais';
    }

    function render() {
        updateProfile();
        updateStats();
        renderTeacherInsights();
        updateClassSelects();
        renderPlans();
        renderClasses();
        renderDiaryBrowser();
        renderDiarySelection();
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
            document.getElementById('diario-turmas-lista').innerHTML = `
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

    document.getElementById('diario-busca').addEventListener('input', renderDiaryBrowser);
    document.getElementById('diario-turmas-lista').addEventListener('click', event => {
        const button = event.target.closest('[data-diary-student]');
        if (!button) return;
        state.diarySelection = {
            turmaCodigo: Number(button.dataset.turmaCodigo),
            matriculaCodigo: Number(button.dataset.matriculaCodigo),
            selectedAulaCodigo: null,
            showHistory: false
        };
        renderDiaryBrowser();
        renderDiarySelection();
    });
    document.getElementById('diario-sessoes-lista').addEventListener('click', event => {
        const button = event.target.closest('[data-diary-session]');
        if (!button || !state.diarySelection) return;
        state.diarySelection.selectedAulaCodigo = Number(button.dataset.diarySession);
        const turma = getTurma(state.diarySelection.turmaCodigo);
        if (turma) {
            renderDiarySessions(
                getDiarySessions(turma.codigo, state.diarySelection.matriculaCodigo),
                state.diarySelection);
        }
        fillDiaryFields();
    });
    document.getElementById('diario-ver-mais').addEventListener('click', () => {
        if (!state.diarySelection) return;
        state.diarySelection.showHistory = !state.diarySelection.showHistory;
        renderDiarySelection();
    });

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
        const sono = document.getElementById('diario-sono').value.trim();
        const banheiroFralda = sanitizeNumericValue(document.getElementById('diario-fraldas').value);
        const observacoesComSaude = [saude ? `Saúde: ${saude}` : '', observacoes]
            .filter(Boolean).join('\n');
        if (observacoesComSaude.length > 500) {
            showAlert('Saúde e observações juntas devem ter no máximo 500 caracteres.', 'warning');
            return;
        }
        if (banheiroFralda && !banheiroFralda.match(/^\d+$/)) {
            showAlert('O campo de banheiro / fralda deve conter apenas números.', 'warning');
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
                    sono,
                    humor: document.getElementById('diario-humor').value,
                    banheiroFralda,
                    observacoes: observacoesComSaude
                })
            });
            if (await loadDashboard()) {
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
