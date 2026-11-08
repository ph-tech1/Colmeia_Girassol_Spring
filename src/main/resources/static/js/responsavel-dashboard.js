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

    const lerResposta = async response => {
        const contentType = response.headers.get('content-type') || '';
        if (!contentType.includes('application/json')) {
            throw new Error('A resposta do servidor não pôde ser processada. Atualize a página e tente novamente.');
        }
        return response.json();
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

    const autorizadoEdicaoForm = document.getElementById('autorizadoEdicaoForm');
    if (autorizadoEdicaoForm) {
        const codigo = autorizadoEdicaoForm.dataset.autorizadoCodigo;
        enviarFormulario(
            autorizadoEdicaoForm,
            `/api/portal-responsavel/autorizados/${codigo}`,
            'PUT',
            'Dados da pessoa autorizada atualizados.'
        );
    }

    document.querySelectorAll('[data-matricula-action]').forEach((button) => {
        button.addEventListener('click', async () => {
            const cancelar = button.dataset.matriculaAction === 'cancelar';
            const tipoCancelamento = button.dataset.matriculaTipo || 'solicitação';
            if (cancelar && !window.confirm(`Deseja realmente cancelar esta ${tipoCancelamento}?`)) return;

            if (!csrfToken || !csrfHeader) {
                mostrarFeedback(
                    button.closest('article').querySelector('[data-matricula-feedback]'),
                    'Não foi possível validar sua sessão. Atualize a página e tente novamente.'
                );
                return;
            }
            const textoOriginal = button.innerHTML;
            const cabecalhos = { 'Accept': 'application/json' };
            cabecalhos[csrfHeader] = csrfToken;
            button.disabled = true;
            button.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Atualizando...';

            try {
                const resposta = await fetch(button.dataset.matriculaUrl, {
                    method: 'POST',
                    headers: cabecalhos
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
                if (!resposta.ok) {
                    throw new Error(corpo?.mensagem || 'Não foi possível atualizar a matrícula. Tente novamente.');
                }
                window.showAppAlert(
                    corpo?.mensagem || (
                        cancelar
                            ? 'Matrícula cancelada.'
                            : button.dataset.matriculaAction === 'retomar'
                                ? 'Matrícula retomada.'
                                : 'Solicitação enviada.'
                    ),
                    'success'
                );
                window.setTimeout(() => window.location.reload(), 700);
            } catch (error) {
                const feedback = button.closest('article').querySelector('[data-matricula-feedback]');
                mostrarFeedback(feedback, error.message);
                button.disabled = false;
                button.innerHTML = textoOriginal;
            }
        });
    });

    const autorizadoBuscaForm = document.getElementById('autorizadoBuscaForm');
    if (autorizadoBuscaForm) {
        const aluno = document.getElementById('autorizadoCrianca');
        const cpf = document.getElementById('autorizadoCpf');
        const consultar = document.getElementById('consultarAutorizadoCpf');
        const existente = document.getElementById('autorizadoExistente');
        const nomeMascarado = document.getElementById('autorizadoNomeMascarado');
        const confirmarExistente = document.getElementById('confirmarAutorizadoExistente');
        const feedbackConfirmacao = document.getElementById('confirmacaoAutorizadoFeedback');
        const confirmacaoSucesso = document.getElementById('confirmacaoAutorizadoSucesso');
        const dadosNovos = document.getElementById('dadosNovoAutorizado');
        const nome = document.getElementById('autorizadoNome');
        const rg = document.getElementById('autorizadoRg');
        const telefone = document.getElementById('autorizadoTelefone');
        const parentesco = document.getElementById('autorizadoParentesco');
        const vinculo = document.getElementById('vinculoAutorizado');
        const salvar = document.getElementById('salvarAutorizacao');
        const feedback = document.getElementById('autorizadoBuscaFeedback');
        let consultaAtual = 0;

        const limparConsulta = () => {
            consultaAtual++;
            existente.hidden = true;
            dadosNovos.hidden = true;
            vinculo.hidden = true;
            salvar.hidden = true;
            salvar.disabled = false;
            confirmarExistente.checked = false;
            confirmarExistente.setAttribute('aria-invalid', 'false');
            feedbackConfirmacao.hidden = true;
            confirmacaoSucesso.hidden = true;
            nome.required = false;
            telefone.required = false;
            parentesco.required = false;
            nomeMascarado.textContent = '';
            feedback.className = 'alert d-none mt-4 mb-0';
            feedback.textContent = '';
        };

        const cabecalhosJson = () => {
            const headers = { 'Content-Type': 'application/json', Accept: 'application/json' };
            if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
            return headers;
        };

        aluno.addEventListener('change', limparConsulta);
        cpf.addEventListener('input', limparConsulta);
        confirmarExistente.addEventListener('change', () => {
            feedbackConfirmacao.hidden = true;
            confirmacaoSucesso.hidden = existente.hidden || !confirmarExistente.checked;
            confirmarExistente.setAttribute('aria-invalid', 'false');
            salvar.disabled = !existente.hidden && !confirmarExistente.checked;
        });

        consultar.addEventListener('click', async () => {
            const cpfNormalizado = cpf.value.replace(/\D/g, '');
            if (!aluno.value || cpfNormalizado.length !== 11) {
                mostrarFeedback(feedback, 'Selecione a criança e informe um CPF com 11 números.');
                return;
            }
            const alunoCodigoConsultado = aluno.value;
            if (!csrfToken || !csrfHeader) {
                mostrarFeedback(feedback, 'Não foi possível validar sua sessão. Atualize a página e tente novamente.');
                return;
            }

            const original = consultar.innerHTML;
            consultar.disabled = true;
            consultar.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Consultando...';
            limparConsulta();
            const requestNumber = consultaAtual;

            try {
                const response = await fetch('/api/portal-responsavel/autorizados/consultar', {
                    method: 'POST',
                    headers: cabecalhosJson(),
                    body: JSON.stringify({ alunoCodigo: Number(aluno.value), cpf: cpf.value })
                });
                if (response.url.includes('/html/login.html')) {
                    window.location.assign(response.url);
                    return;
                }
                const body = await lerResposta(response);
                if (!response.ok) throw new Error(body.mensagem || 'Não foi possível consultar o CPF.');
                if (requestNumber !== consultaAtual
                        || cpf.value.replace(/\D/g, '') !== cpfNormalizado
                        || aluno.value !== alunoCodigoConsultado) return;

                if (body.encontrado) {
                    existente.hidden = false;
                    nomeMascarado.textContent = body.nomeMascarado;
                    salvar.disabled = true;
                } else {
                    dadosNovos.hidden = false;
                    nome.required = true;
                    telefone.required = true;
                    salvar.disabled = false;
                }
                vinculo.hidden = false;
                parentesco.required = true;
                salvar.hidden = false;
            } catch (error) {
                mostrarFeedback(feedback, error.message);
            } finally {
                consultar.disabled = false;
                consultar.innerHTML = original;
            }
        });

        autorizadoBuscaForm.addEventListener('submit', async event => {
            event.preventDefault();
            if (!dadosNovos.hidden && (nome.value.trim().length < 3 || telefone.value.replace(/\D/g, '').length < 10)) {
                mostrarFeedback(feedback, 'Informe nome e telefone válidos para a nova pessoa autorizada.');
                return;
            }
            if (!existente.hidden && !confirmarExistente.checked) {
                feedbackConfirmacao.hidden = false;
                confirmacaoSucesso.hidden = true;
                confirmarExistente.setAttribute('aria-invalid', 'true');
                confirmarExistente.focus();
                return;
            }
            if (!window.FormValidator.validateForm(autorizadoBuscaForm)) return;
            if (!csrfToken || !csrfHeader) {
                mostrarFeedback(feedback, 'Não foi possível validar sua sessão. Atualize a página e tente novamente.');
                return;
            }

            const original = salvar.innerHTML;
            salvar.disabled = true;
            salvar.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Salvando...';
            try {
                const response = await fetch('/api/portal-responsavel/autorizados', {
                    method: 'POST',
                    headers: cabecalhosJson(),
                    body: JSON.stringify({
                        alunoCodigo: Number(aluno.value),
                        cpf: cpf.value,
                        nome: dadosNovos.hidden ? null : nome.value.trim(),
                        rg: dadosNovos.hidden || !rg.value.trim() ? null : rg.value.trim(),
                        telefone: dadosNovos.hidden ? null : telefone.value,
                        parentescoCodigo: Number(parentesco.value),
                        confirmarPessoaExistente: confirmarExistente.checked
                    })
                });
                if (response.url.includes('/html/login.html')) {
                    window.location.assign(response.url);
                    return;
                }
                const body = await lerResposta(response);
                if (!response.ok) throw new Error(body.mensagem || 'Não foi possível salvar a autorização.');
                mostrarFeedback(feedback, body.mensagem || 'Pessoa autorizada vinculada com sucesso.', true);
                window.setTimeout(() => window.location.assign(autorizadoBuscaForm.dataset.successUrl), 900);
            } catch (error) {
                mostrarFeedback(feedback, error.message);
            } finally {
                salvar.disabled = false;
                salvar.innerHTML = original;
            }
        });
    }

    document.querySelectorAll('[data-autorizado-desativar]').forEach(button => {
        button.addEventListener('click', async () => {
            if (!window.confirm('Deseja desativar esta autorização para esta criança?')) return;
            if (!csrfToken || !csrfHeader) {
                mostrarFeedback(button.closest('tr').querySelector('[data-autorizado-feedback]'),
                    'Não foi possível validar sua sessão. Atualize a página e tente novamente.');
                return;
            }
            const original = button.innerHTML;
            button.disabled = true;
            button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span>';
            try {
                const response = await fetch(`/api/portal-responsavel/autorizados/${button.dataset.autorizadoDesativar}`, {
                    method: 'DELETE',
                    headers: { Accept: 'application/json', [csrfHeader]: csrfToken }
                });
                if (response.url.includes('/html/login.html')) {
                    window.location.assign(response.url);
                    return;
                }
                const body = await lerResposta(response);
                if (!response.ok) throw new Error(body.mensagem || 'Não foi possível desativar a autorização.');
                window.location.reload();
            } catch (error) {
                const row = button.closest('tr');
                const feedbackElement = row.querySelector('[data-autorizado-feedback]');
                feedbackElement.textContent = error.message;
                feedbackElement.hidden = false;
                button.disabled = false;
                button.innerHTML = original;
            }
        });
    });
})();
