(() => {
    const script = document.currentScript;
    const list = document.getElementById('rosterList');
    const params = new URLSearchParams(window.location.search);
    const turmaCodigo = Number(params.get('codigo'));

    function showState(message, isError = false) {
        const state = document.createElement('p');
        state.className = `roster-state${isError ? ' roster-state-error' : ''}`;
        state.textContent = message;
        list.replaceChildren(state);
    }

    async function getCollection(url, label) {
        const response = await fetch(url, { headers: { Accept: 'application/json' } });
        if (!response.ok) {
            throw new Error(`Não foi possível carregar ${label} (HTTP ${response.status}).`);
        }
        const result = await response.json();
        if (!Array.isArray(result)) {
            throw new Error(`O servidor retornou um formato inesperado para ${label}.`);
        }
        return result;
    }

    function calculateAge(birthDate) {
        if (!birthDate) return 'Idade não informada';
        const date = new Date(`${birthDate.slice(0, 10)}T00:00:00`);
        if (Number.isNaN(date.getTime())) return 'Idade não informada';
        const today = new Date();
        let age = today.getFullYear() - date.getFullYear();
        const beforeBirthday = today.getMonth() < date.getMonth()
            || (today.getMonth() === date.getMonth() && today.getDate() < date.getDate());
        if (beforeBirthday) age -= 1;
        return age < 0 ? 'Idade não informada' : `${age} ${age === 1 ? 'ano' : 'anos'}`;
    }

    function makeDetail(label, value, icon) {
        const detail = document.createElement('div');
        detail.className = 'roster-student-detail';
        const symbol = document.createElement('i');
        symbol.className = `fa-solid ${icon}`;
        symbol.setAttribute('aria-hidden', 'true');
        const content = document.createElement('span');
        const heading = document.createElement('small');
        heading.textContent = label;
        const text = document.createElement('strong');
        text.textContent = value || 'Não informado';
        content.append(heading, text);
        detail.append(symbol, content);
        return detail;
    }

    function renderStudent(matricula, index) {
        const aluno = matricula.aluno || {};
        const card = document.createElement('article');
        card.className = 'roster-student-card';

        const number = document.createElement('span');
        number.className = 'roster-student-number';
        number.textContent = String(index + 1).padStart(2, '0');

        const identity = document.createElement('div');
        identity.className = 'roster-student-identity';
        const avatar = document.createElement('span');
        avatar.className = 'roster-student-avatar';
        avatar.setAttribute('aria-hidden', 'true');
        const avatarIcon = document.createElement('i');
        avatarIcon.className = 'fa-solid fa-child-reaching';
        avatar.append(avatarIcon);
        const name = document.createElement('h3');
        name.textContent = aluno.nome || 'Aluno sem nome';
        const enrollment = document.createElement('small');
        enrollment.textContent = `Matrícula nº ${matricula.nr ?? '—'}`;
        identity.append(avatar, name, enrollment);

        const details = document.createElement('div');
        details.className = 'roster-student-details';
        details.append(
            makeDetail('Idade', calculateAge(aluno.dataNascimento), 'fa-cake-candles'),
            makeDetail('Alergias', aluno.alergias, 'fa-kit-medical'),
            makeDetail('Restrições alimentares', aluno.restricoesAlimentares, 'fa-utensils'),
            makeDetail('Necessidades especiais', aluno.necessidadesEspeciais, 'fa-heart')
        );
        card.append(number, identity, details);
        list.append(card);
    }

    async function loadRoster() {
        if (!Number.isInteger(turmaCodigo) || turmaCodigo <= 0) {
            throw new Error('Turma inválida. Volte ao painel e selecione uma turma.');
        }

        const [turmas, matriculas] = await Promise.all([
            getCollection(script.dataset.turmasUrl, 'turmas'),
            getCollection(script.dataset.matriculasUrl, 'matrículas')
        ]);
        const turma = turmas.find(item => Number(item.codigo) === turmaCodigo);
        if (!turma) throw new Error('Não foi possível encontrar a turma selecionada.');

        const alunos = matriculas
            .filter(item => Number(item.status) === 2 && Number(item.turma?.codigo) === turmaCodigo)
            .sort((left, right) => String(left.aluno?.nome || '').localeCompare(String(right.aluno?.nome || ''), 'pt-BR'));

        document.getElementById('rosterTitle').textContent = turma.nomeTurma || 'Turma';
        document.getElementById('rosterSubtitle').textContent =
            `${turma.grau || 'Etapa não informada'} · ${turma.ano ?? 'Ano não informado'} · Professor(a): ${turma.professor?.pessoa?.nome || 'Não informado'}`;
        document.getElementById('rosterCount').textContent =
            `${alunos.length} ${alunos.length === 1 ? 'aluno matriculado' : 'alunos matriculados'} nesta turma.`;
        const countBadge = document.getElementById('rosterCountBadge');
        const countIcon = document.createElement('i');
        countIcon.className = 'fa-solid fa-children';
        countIcon.setAttribute('aria-hidden', 'true');
        countBadge.replaceChildren(document.createTextNode(`${alunos.length} `), countIcon);

        if (!alunos.length) {
            showState('Esta turma ainda não possui alunos com matrícula aprovada.');
            return;
        }
        list.replaceChildren();
        alunos.forEach(renderStudent);
    }

    loadRoster().catch(error => {
        showState(error.message || 'Não foi possível carregar os alunos desta turma.', true);
        document.getElementById('rosterCount').textContent = 'A lista não pôde ser carregada.';
        const backLink = document.querySelector('.teacher-back-link');
        backLink.href = script.dataset.dashboardUrl;
    });
})();
