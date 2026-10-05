(() => {
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