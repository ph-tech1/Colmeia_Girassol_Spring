(() => {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    const mostrarFeedback = (elemento, mensagem, sucesso = false) => {
        elemento.textContent = mensagem;
        elemento.classList.remove('d-none', 'alert-danger', 'alert-success');
        elemento.classList.add(sucesso ? 'alert-success' : 'alert-danger');
    };

    const enviar = async (url, metodo, dados) => {
        const cabecalhos = { 'Content-Type': 'application/json' };
        if (csrfToken && csrfHeader) cabecalhos[csrfHeader] = csrfToken;

        const resposta = await fetch(url, {
            method: metodo,
            headers: cabecalhos,
            body: JSON.stringify(dados)
        });

        if (resposta.url.includes('/html/login.html')) {
            window.location.assign(resposta.url);
            return null;
        }

        const tipoConteudo = resposta.headers.get('content-type') || '';
        if (!tipoConteudo.includes('application/json')) {
            throw new Error('A resposta do servidor não pôde ser processada. Atualize a página e tente novamente.');
        }
        const corpo = await resposta.json();
        if (!resposta.ok) throw new Error(corpo.mensagem || 'Não foi possível salvar os dados.');
        return corpo;
    };

    const perfilForm = document.getElementById('perfilForm');
    window.CepService.vincular(perfilForm, {
        onErro({ mensagem }) {
            mostrarFeedback(document.getElementById('perfilFeedback'), mensagem);
        }
    });

    perfilForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!window.FormValidator.validateForm(perfilForm)) return;
        const feedback = document.getElementById('perfilFeedback');
        const dados = Object.fromEntries(new FormData(perfilForm));

        try {
            const resultado = await enviar('/api/portal-responsavel/perfil', 'PUT', dados);
            if (!resultado) return;
            mostrarFeedback(feedback, 'Perfil atualizado com sucesso.', true);
            window.setTimeout(() => window.location.reload(), 800);
        } catch (error) {
            mostrarFeedback(feedback, error.message);
        }
    });

    const criancaForm = document.getElementById('criancaForm');
    const campoNascimento = document.getElementById('criancaNascimento');
    const hoje = new Date();
    hoje.setMinutes(hoje.getMinutes() - hoje.getTimezoneOffset());
    campoNascimento.max = hoje.toISOString().slice(0, 10);

    criancaForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!window.FormValidator.validateForm(criancaForm)) return;
        const feedback = document.getElementById('criancaFeedback');
        const dados = Object.fromEntries(new FormData(criancaForm));

        try {
            const resultado = await enviar('/api/portal-responsavel/criancas', 'POST', dados);
            if (!resultado) return;
            mostrarFeedback(feedback, 'Criança vinculada com sucesso.', true);
            window.setTimeout(() => window.location.reload(), 800);
        } catch (error) {
            mostrarFeedback(feedback, error.message);
        }
    });
})();
