(() => {
    'use strict';

    const form = document.getElementById('teacherRegisterForm');
    if (!form) return;

    const feedback = document.getElementById('teacherFeedback');
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    function showFeedback(message, type, linkUrl) {
        feedback.replaceChildren();
        feedback.className = `alert alert-${type}`;
        feedback.setAttribute('role', type === 'danger' ? 'alert' : 'status');
        feedback.append(document.createTextNode(message));
        if (linkUrl) {
            feedback.append(document.createTextNode(' '));
            const link = document.createElement('a');
            link.href = linkUrl;
            link.className = 'alert-link';
            link.textContent = 'Voltar à lista de professores';
            feedback.append(link);
        }
    }

    async function readResponse(response) {
        const contentType = response.headers.get('content-type') || '';
        if (contentType.includes('application/json')) return response.json();
        const text = await response.text();
        return text || null;
    }

    function errorMessage(body, status) {
        if (body && typeof body === 'object') {
            return body.mensagem || body.message || body.error || body.detail
                || `Não foi possível cadastrar o professor (HTTP ${status}).`;
        }
        return body || `Não foi possível cadastrar o professor (HTTP ${status}).`;
    }

    form.addEventListener('submit', async event => {
        event.preventDefault();
        if (!window.FormValidator?.validateForm) {
            showFeedback('Não foi possível carregar as validações do formulário. Atualize a página e tente novamente.', 'danger');
            return;
        }
        if (!window.FormValidator.validateForm(form)) {
            showFeedback('Revise os campos destacados e tente novamente.', 'warning');
            return;
        }
        if (!csrfToken || !csrfHeader) {
            showFeedback('Não foi possível validar sua sessão. Atualize a página e tente novamente.', 'danger');
            return;
        }

        const value = id => document.getElementById(id).value.trim();
        const payload = {
            nome: value('professorNome'),
            cpf: value('professorCpf'),
            rg: value('professorRg') || null,
            dataNascimento: value('professorNascimento'),
            email: value('professorEmail'),
            senha: document.getElementById('professorSenha').value,
            cep: value('professorCep'),
            cidade: value('professorCidade'),
            uf: value('professorUf'),
            endereco: value('professorEndereco'),
            telefone: value('professorTelefone'),
            formacao: value('professorFormacao'),
            dataContratacao: value('professorContratacao')
        };
        const submitButton = form.querySelector('[type="submit"]');
        const originalButtonContent = submitButton.innerHTML;
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Salvando...';
        feedback.classList.add('d-none');

        try {
            const response = await fetch(form.dataset.professoresUrl, {
                method: 'POST',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(payload)
            });
            if (response.url.includes('/html/login.html')) {
                window.location.assign(response.url);
                return;
            }
            const body = await readResponse(response);
            if (!response.ok) throw new Error(errorMessage(body, response.status));

            form.reset();
            form.querySelectorAll('.is-valid, .is-invalid').forEach(field => {
                field.classList.remove('is-valid', 'is-invalid');
            });
            showFeedback('Professor cadastrado com sucesso!', 'success', form.dataset.dashboardUrl);
            feedback.scrollIntoView({ behavior: 'smooth', block: 'center' });
        } catch (error) {
            showFeedback(error.message || 'Não foi possível cadastrar o professor.', 'danger');
        } finally {
            submitButton.disabled = false;
            submitButton.innerHTML = originalButtonContent;
        }
    });

    document.querySelectorAll('[data-password-toggle]').forEach(button => {
        button.addEventListener('click', () => {
            const input = document.getElementById(button.dataset.passwordToggle);
            const showing = input.type === 'password';
            input.type = showing ? 'text' : 'password';
            button.setAttribute('aria-pressed', String(showing));
            button.setAttribute('aria-label', showing ? 'Ocultar senha' : 'Mostrar senha');
            button.querySelector('i').className = `fa-solid ${showing ? 'fa-eye-slash' : 'fa-eye'}`;
        });
    });

    window.CepService?.vincular(form, {
        onErro({ mensagem }) {
            const cepField = document.getElementById('professorCep');
            const feedback = document.createElement('small');
            feedback.className = 'teacher-field-hint text-warning-emphasis d-block mt-1';
            feedback.textContent = mensagem;
            cepField.parentElement.querySelector('.teacher-cep-warning')?.remove();
            feedback.classList.add('teacher-cep-warning');
            cepField.parentElement.append(feedback);
        }
    });
})();
