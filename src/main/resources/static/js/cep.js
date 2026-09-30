(function (window) {
    'use strict';

    const digits = value => String(value || '').replace(/\D/g, '');

    async function consultar(cep) {
        const cepNumerico = digits(cep);
        if (!/^\d{8}$/.test(cepNumerico)) {
            return { ok: false, mensagem: 'CEP deve conter 8 dígitos.' };
        }

        try {
            const response = await fetch(`https://viacep.com.br/ws/${cepNumerico}/json/`);
            if (!response.ok) throw new Error('Falha na consulta ao ViaCEP.');

            const dados = await response.json();
            if (dados.erro) {
                return { ok: false, mensagem: 'CEP não localizado.' };
            }
            return { ok: true, dados };
        } catch (error) {
            console.error('Erro ao consultar o CEP:', error);
            return { ok: false, mensagem: 'Não foi possível consultar o CEP. Você pode preencher o endereço manualmente.' };
        }
    }

    function preencher(formulario, dados) {
        if (!formulario || !dados) return;

        formulario.querySelectorAll('[data-cep-field]').forEach(campo => {
            const propriedade = campo.dataset.cepField;
            if (!(propriedade in dados) || typeof dados[propriedade] !== 'string') return;

            campo.value = dados[propriedade];
            campo.dispatchEvent(new Event('input', { bubbles: true }));
            campo.dispatchEvent(new Event('change', { bubbles: true }));
        });
    }

    function vincular(formulario, opcoes = {}) {
        if (!formulario) return;

        formulario.addEventListener('focusout', async event => {
            const campoCep = event.target.closest('[data-cep]');
            if (!campoCep || !formulario.contains(campoCep)) return;

            const cepConsultado = digits(campoCep.value);
            const resultado = await consultar(cepConsultado);
            if (digits(campoCep.value) !== cepConsultado) return;

            if (!resultado.ok) {
                campoCep.dispatchEvent(new CustomEvent('cepError', {
                    bubbles: true,
                    detail: resultado
                }));
                if (typeof opcoes.onErro === 'function') {
                    opcoes.onErro({ campo: campoCep, ...resultado });
                }
                return;
            }

            preencher(formulario, resultado.dados);
        });
    }

    window.CepService = { consultar, preencher, vincular };
})(window);