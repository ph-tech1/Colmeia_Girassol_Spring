(() => {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    const mostrarFeedback = (elemento, mensagem, sucesso = false) => {
        if (typeof window.showAppAlert === 'function') {
            window.showAppAlert(mensagem, sucesso ? 'success' : 'danger');
            return;
        }

        elemento.textContent = mensagem;
        elemento.classList.remove('d-none', 'alert-danger', 'alert-success');
        elemento.classList.add(sucesso ? 'alert-success' : 'alert-danger');
    };

    const enviarFormulario = async (formulario, url, metodo, mensagemSucesso) => {
        formulario.addEventListener('submit', async (event) => {
            event.preventDefault();
            if (!window.FormValidator.validateForm(formulario)) return;

            const feedback = formulario.querySelector('[role="alert"]') || document.getElementById(`${formulario.id.replace('Form', '')}Feedback`);
            const botao = formulario.querySelector('[type="submit"]');
            const textoOriginal = botao.innerHTML;
            const cabecalhos = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
            if (csrfToken && csrfHeader) cabecalhos[csrfHeader] = csrfToken;

            botao.disabled = true;
            botao.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Salvando...';

            try {
                const resposta = await fetch(url, {
                    method: metodo,
                    headers: cabecalhos,
                    body: JSON.stringify(Object.fromEntries(new FormData(formulario)))
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

                mostrarFeedback(feedback, mensagemSucesso, true);
                window.setTimeout(() => window.location.assign(formulario.dataset.successUrl), 900);
            } catch (error) {
                mostrarFeedback(feedback, error.message);
            } finally {
                botao.disabled = false;
                botao.innerHTML = textoOriginal;
            }
        });
    };

    const perfilForm = document.getElementById('perfilForm');
    if (perfilForm) {
        window.CepService.vincular(perfilForm, {
            onErro({ mensagem }) {
                mostrarFeedback(document.getElementById('perfilFeedback'), mensagem);
            }
        });
        enviarFormulario(perfilForm, '/api/portal-responsavel/perfil', 'PUT', 'Perfil atualizado com sucesso.');
    }

    const criancaForm = document.getElementById('criancaForm');
    if (criancaForm) {
        const campoNascimento = document.getElementById('criancaNascimento');
        const hoje = new Date();
        hoje.setMinutes(hoje.getMinutes() - hoje.getTimezoneOffset());
        campoNascimento.max = hoje.toISOString().slice(0, 10);
        enviarFormulario(criancaForm, '/api/portal-responsavel/criancas', 'POST', 'Criança vinculada com sucesso.');
    }
})();
