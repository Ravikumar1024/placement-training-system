// Login form handler
document.getElementById('loginForm').addEventListener('submit', async e => {
    e.preventDefault();
    const msg = document.getElementById('msg');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: usernameInput.value,
                password: passwordInput.value
            })
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || 'Login failed');
        }

        // Store user info
        const user = result.data || result;
        localStorage.setItem('user', JSON.stringify(user));
        localStorage.setItem('role', user.role);
        localStorage.setItem('userId', user.userId);
        localStorage.setItem('studentId', user.studentId || '');

        const dashboardResponse = await fetch('/api/dashboard');
        const dashboardResult = await dashboardResponse.json();
        if (!dashboardResponse.ok) {
            throw new Error(dashboardResult.message || 'Unable to load dashboard data.');
        }

        // Navigate based on role
        if (user.role === 'ADMIN') {
            location.href = '/webapp/admin/dashboard.html';
        } else {
            location.href = '/webapp/student/dashboard.html';
        }

    } catch (err) {
        msg.textContent = err.message;
        msg.className = 'alert alert-danger d-none mt-3';
        setTimeout(() => msg.classList.remove('d-none'), 100);
    }
});
