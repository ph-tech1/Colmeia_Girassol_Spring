document.addEventListener('click', event => {
    const link = event.target.closest('a[href^="#"]');
    if (!link || event.defaultPrevented || event.button !== 0
        || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey
        || link.target || link.hasAttribute('download')) {
        return;
    }

    const target = document.getElementById(link.hash.slice(1));
    if (!target) {
        return;
    }

    event.preventDefault();
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    target.scrollIntoView({
        behavior: prefersReducedMotion ? 'instant' : 'smooth',
        block: 'start'
    });

    if (window.location.hash !== link.hash) {
        window.history.pushState(null, '', link.hash);
    }
});
