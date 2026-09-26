/* Regras compartilhadas de mascara e validacao do portal. */
(function (window, $) {
    'use strict';

    if (!$) return;

    const digits = value => String(value || '').replace(/\D/g, '');

    function cpfValido(value) {
        const cpf = digits(value);
        if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) return false;
        let sum = 0;
        for (let i = 0; i < 9; i++) sum += Number(cpf[i]) * (10 - i);
        let check = (sum * 10) % 11;
        if (check === 10) check = 0;
        if (check !== Number(cpf[9])) return false;
        sum = 0;
        for (let i = 0; i < 10; i++) sum += Number(cpf[i]) * (11 - i);
        check = (sum * 10) % 11;
        if (check === 10) check = 0;
        return check === Number(cpf[10]);
    }

    function dataValida(value, { future = false, minAge = 0 } = {}) {
        if (!value) return false;
        const date = new Date(`${value}T00:00:00`);
        if (Number.isNaN(date.getTime())) return false;
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (future && date < today) return false;
        if (!future && date > today) return false;
        if (minAge) {
            const limit = new Date(today.getFullYear() - minAge, today.getMonth(), today.getDate());
            if (date > limit) return false;
        }
        return true;
    }

    function setFieldState(field, valid, message) {
        const $field = $(field);
        $field.toggleClass('is-invalid', !valid).toggleClass('is-valid', valid);
        let $feedback = $field.siblings('.invalid-feedback');
        if (!$feedback.length) $feedback = $field.closest('.form-floating, .mb-3, .mb-4, .col-md-6, .col-md-12, .col-12').find('.invalid-feedback').first();
        if (!$feedback.length) {
            $feedback = $('<div class="invalid-feedback"></div>').insertAfter($field);
        }
        $feedback.text(message || 'Preencha este campo corretamente.');
        return valid;
    }

    function validateField(field) {
        const $field = $(field);
        if (!$field.is(':visible') || $field.is(':disabled')) return true;
        const value = String($field.val() || '').trim();
        const id = $field.attr('id') || '';
        const type = $field.attr('type');
        let valid = true;
        let message = '';

        if ($field.prop('required') && !value) {
            valid = false;
            message = 'Este campo é obrigatório.';
        } else if (value && $field.attr('minlength') && value.length < Number($field.attr('minlength'))) {
            valid = false;
            message = `Informe pelo menos ${$field.attr('minlength')} caracteres.`;
        } else if (value && type === 'email' && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
            valid = false;
            message = 'Informe um e-mail válido.';
        } else if (value && (id.toLowerCase().includes('cpf') || $field.hasClass('cpf-mask')) && !cpfValido(value)) {
            valid = false;
            message = 'Informe um CPF válido.';
        } else if (value && $field.hasClass('phone-mask') && digits(value).length < 10) {
            valid = false;
            message = 'Informe um telefone válido.';
        } else if (value && $field.hasClass('cep-mask') && digits(value).length !== 8) {
            valid = false;
            message = 'Informe um CEP válido.';
        } else if (value && $field.hasClass('uf-field') && !/^[A-Za-z]{2}$/.test(value)) {
            valid = false;
            message = 'Informe a UF com duas letras.';
        } else if (value && $field.is('[type="date"]')) {
            const isBirth = /nasc|nascimento/i.test(id);
            if (!dataValida(value, { future: $field.hasClass('future-date'), minAge: isBirth && /resp|responsavel/i.test(id) ? 18 : 0 })) {
                valid = false;
                message = isBirth ? 'Informe uma data de nascimento válida.' : 'Informe uma data válida.';
            }
        }
        return setFieldState(field, valid, message);
    }

    function validateForm(form) {
        let valid = true;
        $(form).find('input, select, textarea').each(function () {
            if (!validateField(this)) valid = false;
        });
        if (!valid) {
            const first = $(form).find('.is-invalid').first()[0];
            if (first) first.focus({ preventScroll: true });
        }
        return valid;
    }

    window.FormValidator = { cpfValido, dataValida, validateField, validateForm };

    $(function () {
        $('.cpf-mask').mask('000.000.000-00', { clearIfNotMatch: false });
        $('.phone-mask').mask('(00) 00000-0000');
        $('.cep-mask').mask('00000-000');
        $('.rg-mask').mask('00.000.00-0');
        $('.uf-field').on('input', function () { this.value = this.value.replace(/[^a-zA-Z]/g, '').toUpperCase().slice(0, 2); });

        $('form').on('submit', function (event) {
            if (!validateForm(this)) event.preventDefault();
        });

        $('input, select, textarea').on('blur change', function () { validateField(this); });
        $('input, select, textarea').on('input', function () {
            if ($(this).hasClass('is-invalid')) validateField(this);
        });

        $('#cpfRecuperar').prop('required', true).addClass('cpf-mask');
        $('#emailRecuperar').prop('required', true);
        $('#cpfRecuperar').mask('000.000.000-00');
    });

    
})(window, window.jQuery);
