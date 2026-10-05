document.addEventListener('DOMContentLoaded', () => {
    if (window.AOS) window.AOS.init();

    const form = document.getElementById('matriculaForm');
    if (!form) return;

    const button = document.getElementById('btnConcluirMatricula');
    const feedback = document.getElementById('cadastroFeedback');
    const buttonLabel = button.innerHTML;
    const steps = Array.from(form.querySelectorAll('.matricula-step'));
    const stepCounter = document.getElementById('stepCounter');
    const stepName = document.getElementById('stepName');
    const progress = document.getElementById('matriculaProgress');
    const progressBar = document.getElementById('matriculaProgressBar');
    let activeStep = 0;

    form.querySelectorAll('[data-toggle-password]').forEach(toggle => {
        toggle.addEventListener('click', () => {
            const password = document.getElementById(toggle.dataset.togglePassword);
            const isVisible = password.type === 'password';
            password.type = isVisible ? 'text' : 'password';
            toggle.setAttribute('aria-label', isVisible ? 'Ocultar senha' : 'Mostrar senha');
            toggle.setAttribute('aria-pressed', String(isVisible));
            toggle.querySelector('i').classList.toggle('fa-eye', !isVisible);
            toggle.querySelector('i').classList.toggle('fa-eye-slash', isVisible);
        });
    });

    function showStep(index, scroll = true) {
        activeStep = index;
        steps.forEach((step, stepIndex) => {
            step.hidden = stepIndex !== index;
        });

        stepCounter.textContent = `Etapa ${index + 1} de ${steps.length}`;
        stepName.textContent = steps[index].dataset.stepTitle;
        progress.setAttribute('aria-valuenow', String(index + 1));
        progressBar.style.width = `${((index + 1) / steps.length) * 100}%`;

        if (scroll) {
            const heading = steps[index].querySelector('.step-header');
            heading.focus({ preventScroll: true });
            heading.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }

    function validateStep(step) {
        const invalidFields = Array.from(step.querySelectorAll('input, select, textarea'))
            .filter(field => !window.FormValidator.validateField(field));

        if (!invalidFields.length) return true;

        invalidFields[0].focus({ preventScroll: true });
        invalidFields[0].scrollIntoView({ behavior: 'smooth', block: 'center' });
        return false;
    }

    form.addEventListener('click', event => {
        const nextButton = event.target.closest('[data-step-next]');
        const previousButton = event.target.closest('[data-step-prev]');

        if (nextButton && validateStep(steps[activeStep])) {
            showStep(Math.min(activeStep + 1, steps.length - 1));
        } else if (previousButton) {
            showStep(Math.max(activeStep - 1, 0));
        }
    });

    showStep(0, false);

    form.addEventListener('submit', async event => {
        event.preventDefault();

        if (!window.FormValidator.validateForm(form)) return;

        const dados = {
            nomeResponsavel: document.getElementById('nomeResp').value.trim(),
            cpfResponsavel: document.getElementById('cpfResp').value,
            rgResponsavel: document.getElementById('rgResp').value,
            dataNascimentoResponsavel: document.getElementById('dataNascResp').value,
            senha: document.getElementById('senhaResp').value,
            cep: document.getElementById('cepResp').value,
            cidade: document.getElementById('cidadeResp').value.trim(),
            uf: document.getElementById('ufResp').value.trim().toUpperCase(),
            endereco: document.getElementById('enderecoResp').value.trim(),
            telefone: document.getElementById('celResp').value,
            localTrabalho: document.getElementById('localTrabResp').value.trim(),
            telefoneTrabalho: document.getElementById('telTrabResp').value,
            estadoCivil: document.getElementById('estadoCivilResp').value,
            email: document.getElementById('emailResp').value.trim(),
            nomeAluno: document.getElementById('nomeAluno').value.trim(),
            dataNascimentoAluno: document.getElementById('dataNascAluno').value,
            grauParentesco: document.getElementById('grauParentesco').value,
            alergias: document.getElementById('alergias').value.trim(),
            restricoesAlimentares: document.getElementById('restricoes').value.trim(),
            necessidadesEspeciais: document.getElementById('necessidades').value.trim()
        };

        button.disabled = true;
        button.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Enviando...';
        feedback.className = 'alert d-none';

        try {
            const response = await fetch(form.action, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(dados)
            });
            const resultado = await response.json().catch(() => ({}));
            if (!response.ok) throw new Error(resultado.mensagem || 'Não foi possível concluir o cadastro.');

            feedback.textContent = resultado.mensagem || 'Pré-matrícula cadastrada com sucesso.';
            feedback.className = 'alert alert-success';
            form.reset();
            form.querySelectorAll('.is-valid').forEach(field => field.classList.remove('is-valid'));
            showStep(0, false);
        } catch (error) {
            feedback.textContent = error.message;
            feedback.className = 'alert alert-danger';
        } finally {
            button.disabled = false;
            button.innerHTML = buttonLabel;
            feedback.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    });
});