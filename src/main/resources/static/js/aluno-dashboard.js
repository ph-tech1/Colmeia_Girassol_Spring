(() => {
    const form = document.getElementById('alunoForm');
    const painelEdicao = document.getElementById('editarDados');
    const botaoEditar = document.getElementById('editarDadosToggle');
    const feedback = document.getElementById('alunoFeedback');
    const campoNascimento = document.getElementById('alunoNascimento');
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    const alternarEdicao = (abrir) => {
        painelEdicao.classList.toggle('d-none', !abrir);
        botaoEditar.setAttribute('aria-expanded', String(abrir));
        if (abrir) painelEdicao.scrollIntoView({ behavior: 'smooth', block: 'start' });
    };

    botaoEditar.addEventListener('click', () => alternarEdicao(painelEdicao.classList.contains('d-none')));
    document.getElementById('fecharEdicao').addEventListener('click', () => alternarEdicao(false));
    document.getElementById('cancelarEdicao').addEventListener('click', () => alternarEdicao(false));

    const hoje = new Date();
    hoje.setMinutes(hoje.getMinutes() - hoje.getTimezoneOffset());
    campoNascimento.max = hoje.toISOString().slice(0, 10);

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!window.FormValidator.validateForm(form)) return;

        const cabecalhos = { 'Content-Type': 'application/json' };
        if (csrfToken && csrfHeader) cabecalhos[csrfHeader] = csrfToken;

        try {
            const resposta = await fetch(`/api/portal-responsavel/criancas/${form.dataset.alunoCodigo}`, {
                method: 'PUT',
                headers: cabecalhos,
                body: JSON.stringify(Object.fromEntries(new FormData(form)))
            });
            if (resposta.url.includes('/html/login.html')) {
                window.location.assign(resposta.url);
                return;
            }
            const tipoConteudo = resposta.headers.get('content-type') || '';
            if (!tipoConteudo.includes('application/json')) {
                throw new Error('A resposta do servidor não pôde ser processada. Atualize a página e tente novamente.');
            }
            const corpo = await resposta.json();
            if (!resposta.ok) throw new Error(corpo.mensagem || 'Não foi possível salvar os dados.');

            feedback.textContent = corpo.mensagem;
            feedback.classList.remove('d-none', 'alert-danger');
            feedback.classList.add('alert-success');
            window.setTimeout(() => window.location.reload(), 700);
        } catch (error) {
            feedback.textContent = error.message;
            feedback.classList.remove('d-none', 'alert-success');
            feedback.classList.add('alert-danger');
        }
    });
})();
