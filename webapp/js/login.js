// Login form handler
document.getElementById('loginForm').addEventListener('submit', async e => {
    e.preventDefault();
    const submitButton = document.getElementById('loginSubmit');
    const submitLabel = submitButton.innerHTML;
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    submitButton.disabled = true;
    submitButton.textContent = 'Signing in...';

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: usernameInput.value,
                password: passwordInput.value,
                expectedRole: document.body.dataset.loginRole || null
            })
        });

        const result = await response.json().catch(() => ({}));

        if (!response.ok) {
            throw window.createApiError(result, 'Unable to sign in right now. Please try again.');
        }

        const user = result.data || result;
        if (!user.userId || !user.role) {
            throw window.createApiError(result, 'The sign-in response was incomplete. Please try again.');
        }
        const expectedRole = document.body.dataset.loginRole;
        if (expectedRole && user.role !== expectedRole) {
            const roleMessage = expectedRole === 'ADMIN'
                ? 'This page is for admin accounts. Use the student sign-in for student accounts.'
                : 'This page is for student accounts. Use the admin sign-in for admin accounts.';
            throw window.createApiError({ code: 'PTSE002', message: roleMessage });
        }

        const dashboardResponse = await fetch('/api/dashboard');
        const dashboardResult = await dashboardResponse.json().catch(() => ({}));
        if (!dashboardResponse.ok) {
            throw window.createApiError(dashboardResult, 'Signed in, but unable to load your dashboard. Please try again.');
        }

        localStorage.setItem('user', JSON.stringify(user));
        localStorage.setItem('role', user.role);
        localStorage.setItem('userId', user.userId);
        localStorage.setItem('studentId', user.studentId || '');

        if (user.role === 'ADMIN') {
            location.href = '/webapp/admin/dashboard.html';
        } else {
            location.href = '/webapp/student/dashboard.html';
        }

    } catch (err) {
        if (err instanceof TypeError) {
            err.code = 'PTSE006';
            err.message = 'Unable to connect. Check your connection and try again.';
        }
        window.showSnackbar(err, 'Unable to sign in right now. Please try again.');
    } finally {
        submitButton.disabled = false;
        submitButton.innerHTML = submitLabel;
    }
});
