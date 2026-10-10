(() => {
    const alertDuration = 5200;
    const alertStates = new WeakMap();
    const alertIcons = {
        danger: 'fa-circle-exclamation',
        info: 'fa-circle-info',
        success: 'fa-circle-check',
        warning: 'fa-triangle-exclamation'
    };

    const clearAlertState = (alert) => {
        const state = alertStates.get(alert);
        if (!state) return;
        window.clearTimeout(state.dismissTimer);
        window.clearTimeout(state.removeTimer);
        alertStates.delete(alert);
    };

    const getAlertContainer = () => {
        let container = document.querySelector('.app-alert-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'app-alert-container';
            container.setAttribute('role', 'region');
            container.setAttribute('aria-label', 'Notificações');
            container.setAttribute('aria-live', 'polite');
            document.body.append(container);
        }
        return container;
    };

    const startAlert = (alert) => {
        if (!alert.classList.contains('alert') || alert.dataset.autoDismiss === 'false') return;
        if (alert.classList.contains('d-none')) {
            clearAlertState(alert);
            return;
        }

        const message = (alert.querySelector('[data-toast-content]') || alert).textContent.trim();
        const currentState = alertStates.get(alert);
        if (currentState && currentState.message === message) return;
        clearAlertState(alert);

        alert.classList.add('app-toast');
        getAlertContainer().append(alert);

        if (alert.dataset.toastReady !== 'true') {
            const content = document.createElement('span');
            content.className = 'app-toast-content';
            content.dataset.toastContent = '';
            while (alert.firstChild) content.append(alert.firstChild);

            const icon = document.createElement('span');
            icon.className = 'app-toast-icon';
            icon.setAttribute('aria-hidden', 'true');
            const iconElement = document.createElement('i');
            iconElement.className = `fa-solid ${alertIcons[alert.classList.contains('alert-danger') ? 'danger' : alert.classList.contains('alert-success') ? 'success' : alert.classList.contains('alert-info') ? 'info' : 'warning']}`;
            icon.append(iconElement);

            const closeButton = document.createElement('button');
            closeButton.className = 'app-toast-close';
            closeButton.type = 'button';
            closeButton.setAttribute('aria-label', 'Fechar notificação');
            closeButton.innerHTML = '<i class="fa-solid fa-xmark" aria-hidden="true"></i>';
            closeButton.addEventListener('click', () => {
                clearAlertState(alert);
                alert.remove();
            });

            alert.append(icon, content, closeButton);
            alert.dataset.toastReady = 'true';
        } else {
            const iconElement = alert.querySelector('.app-toast-icon i');
            const variant = alert.classList.contains('alert-danger') ? 'danger'
                : alert.classList.contains('alert-success') ? 'success'
                    : alert.classList.contains('alert-info') ? 'info' : 'warning';
            iconElement.className = `fa-solid ${alertIcons[variant]}`;
        }

        let progress = alert.querySelector('.alert-countdown');
        if (!progress) {
            const track = document.createElement('span');
            track.className = 'alert-countdown-track';
            track.setAttribute('aria-hidden', 'true');
            progress = document.createElement('span');
            progress.className = 'alert-countdown';
            track.append(progress);
            alert.append(track);
        }

        alert.classList.remove('alert-exiting');
        alert.classList.add('alert-animated', 'alert-entering');
        progress.style.animation = 'none';
        void progress.offsetWidth;
        progress.style.animation = `alert-countdown ${alertDuration}ms linear forwards`;

        const state = { message, dismissTimer: 0, removeTimer: 0 };
        alertStates.set(alert, state);
        window.requestAnimationFrame(() => alert.classList.remove('alert-entering'));
        state.dismissTimer = window.setTimeout(() => {
            alert.classList.add('alert-exiting');
            state.removeTimer = window.setTimeout(() => {
                clearAlertState(alert);
                alert.remove();
            }, 240);
        }, alertDuration);
    };

    window.showAppAlert = (message, variant = 'warning') => {
        const allowedVariants = ['danger', 'info', 'success', 'warning'];
        const alert = document.createElement('div');
        alert.className = `alert alert-${allowedVariants.includes(variant) ? variant : 'warning'} mb-0`;
        alert.setAttribute('role', 'alert');
        alert.textContent = message;
        getAlertContainer().append(alert);
        startAlert(alert);
        return alert;
    };

    document.querySelectorAll('.alert').forEach((alert) => startAlert(alert));
    new MutationObserver((mutations) => {
        const changedAlerts = new Set();
        mutations.forEach((mutation) => {
            const target = mutation.target.nodeType === Node.ELEMENT_NODE
                ? mutation.target
                : mutation.target.parentElement;
            const alert = target?.closest('.alert');
            if (alert) changedAlerts.add(alert);

            mutation.addedNodes?.forEach((node) => {
                if (node.nodeType !== Node.ELEMENT_NODE) return;
                if (node.matches('.alert')) changedAlerts.add(node);
                node.querySelectorAll?.('.alert').forEach((addedAlert) => changedAlerts.add(addedAlert));
            });
        });
        changedAlerts.forEach(startAlert);
    }).observe(document.body, {
        attributes: true,
        attributeFilter: ['class'],
        characterData: true,
        childList: true,
        subtree: true
    });

    const themeButtons = document.querySelectorAll('[data-theme-toggle]');
    const updateButtons = (isDark) => {
        themeButtons.forEach((button) => {
            const icon = button.querySelector('i');
            const label = isDark ? 'Ativar modo claro' : 'Ativar modo escuro';

            button.setAttribute('aria-label', label);
            button.setAttribute('aria-pressed', String(isDark));
            button.title = label;
            icon.classList.toggle('fa-moon', !isDark);
            icon.classList.toggle('fa-sun', isDark);
        });
    };

    let isDark = document.documentElement.dataset.theme === 'dark';
    updateButtons(isDark);

    themeButtons.forEach((button) => {
        button.addEventListener('click', () => {
            isDark = !isDark;
            document.documentElement.dataset.theme = isDark ? 'dark' : 'light';
            updateButtons(isDark);

            try {
                localStorage.setItem('colmeia-theme', isDark ? 'dark' : 'light');
            } catch (error) {}
        });
    });
})();