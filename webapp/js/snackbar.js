(() => {
    function responseError(result, fallback) {
        const error = new Error(result?.message || fallback || 'The request could not be completed.');
        error.code = result?.code || 'PTSE006';
        return error;
    }

    function showSnackbar(result, fallback, tone) {
        const response = typeof result === 'string' ? { message: result } : (result || {});
        let fallbackCode = 'PTSE006';
        if (tone === 'success') fallbackCode = 'PTSS002';
        else if (tone === 'info') fallbackCode = 'PTSS001';
        const code = response.code || fallbackCode;
        const successful = code.startsWith('PTSS');
        const variant = successful ? 'success' : 'error';
        let region = document.getElementById('appSnackbarRegion');
        if (!region) {
            region = document.createElement('div');
            region.id = 'appSnackbarRegion';
            region.className = 'app-snackbar-region';
            region.setAttribute('aria-live', 'polite');
            region.setAttribute('aria-atomic', 'false');
            document.body.appendChild(region);
        }

        const snackbar = document.createElement('div');
        snackbar.className = `app-snackbar app-snackbar-${variant}`;
        snackbar.setAttribute('role', successful ? 'status' : 'alert');

        const icon = document.createElement('i');
        icon.className = `fas ${successful ? 'fa-circle-check' : 'fa-circle-exclamation'}`;
        icon.setAttribute('aria-hidden', 'true');

        const content = document.createElement('div');
        content.className = 'app-snackbar-content';
        const codeElement = document.createElement('strong');
        codeElement.className = 'app-snackbar-code';
        codeElement.textContent = code;
        const messageElement = document.createElement('span');
        messageElement.className = 'app-snackbar-message';
        messageElement.textContent = response.message || fallback || code;
        content.append(codeElement, messageElement);

        const close = document.createElement('button');
        close.type = 'button';
        close.className = 'app-snackbar-close';
        close.setAttribute('aria-label', 'Dismiss notification');
        close.innerHTML = '<i class="fas fa-xmark" aria-hidden="true"></i>';

        let timer;
        const dismiss = () => {
            window.clearTimeout(timer);
            snackbar.classList.remove('is-visible');
            window.setTimeout(() => snackbar.remove(), 180);
        };
        close.addEventListener('click', dismiss);
        snackbar.append(icon, content, close);
        region.appendChild(snackbar);
        requestAnimationFrame(() => snackbar.classList.add('is-visible'));
        timer = window.setTimeout(dismiss, 5000);
        return snackbar;
    }

    window.createApiError = responseError;
    window.showSnackbar = showSnackbar;
})();
