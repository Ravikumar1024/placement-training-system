// Login form handler
document.getElementById('loginForm').addEventListener('submit', async e => {
    e.preventDefault();
    const msg = document.getElementById('msg');
    const submitButton = document.getElementById('loginSubmit');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    msg.textContent = '';
    msg.classList.add('d-none');
    submitButton.disabled = true;
    submitButton.textContent = 'Signing in...';

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: usernameInput.value,
                password: passwordInput.value
            })
        });

        const result = await response.json().catch(() => ({}));

        if (!response.ok) {
            const message = response.status === 400 || response.status === 401
                ? 'Invalid username or password. Please try again.'
                : result.message || 'Unable to sign in right now. Please try again.';
            throw new Error(message);
        }

        const user = result.data || result;
        if (!user.userId || !user.role) {
            throw new Error('The sign-in response was incomplete. Please try again.');
        }

        const dashboardResponse = await fetch('/api/dashboard');
        const dashboardResult = await dashboardResponse.json().catch(() => ({}));
        if (!dashboardResponse.ok) {
            throw new Error(dashboardResult.message || 'Signed in, but unable to load your dashboard. Please try again.');
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
        msg.textContent = err instanceof TypeError
            ? 'Unable to connect. Check your connection and try again.'
            : err.message || 'Unable to sign in right now. Please try again.';
        msg.classList.remove('d-none');
    } finally {
        submitButton.disabled = false;
        submitButton.textContent = 'Login';
    }
});
