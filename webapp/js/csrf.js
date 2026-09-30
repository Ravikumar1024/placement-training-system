(function () {
    const originalFetch = window.fetch.bind(window);
    let csrfTokenPromise;

    async function getCsrfToken() {
        if (!csrfTokenPromise) {
            csrfTokenPromise = originalFetch('/api/auth/csrf', { credentials: 'same-origin' })
                .then(response => response.json())
                .then(result => result.data)
                .finally(() => { csrfTokenPromise = null; });
        }
        return csrfTokenPromise;
    }

    window.fetch = async function (input, init = {}) {
        const url = new URL(input instanceof Request ? input.url : input, window.location.href);
        const method = String(init.method || (input instanceof Request ? input.method : 'GET')).toUpperCase();
        const protectedMutation = url.origin === window.location.origin
            && !['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method);
        if (!protectedMutation) return originalFetch(input, init);

        const token = await getCsrfToken();
        const headers = new Headers(input instanceof Request ? input.headers : undefined);
        new Headers(init.headers).forEach((value, name) => headers.set(name, value));
        headers.set('X-XSRF-TOKEN', token);
        return originalFetch(input, { ...init, headers, credentials: 'same-origin' });
    };
})();