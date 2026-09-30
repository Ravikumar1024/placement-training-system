// Student Dashboard JavaScript
const user = JSON.parse(localStorage.getItem('user') || 'null');

if (!user || user.role !== 'STUDENT') {
    location.href = '/login.html';
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function formatStatus(status) {
    if (!status) return 'NOT AVAILABLE';
    return status.replace(/_/g, ' ');
}

function showStatusBadge(status) {
    if (!status) return '<span class="badge bg-secondary">N/A</span>';
    const statusLower = status.toLowerCase();
    if (statusLower.includes('selected') || statusLower.includes('placed')) {
        return '<span class="badge bg-success">' + escapeHtml(status) + '</span>';
    } else if (statusLower.includes('rejected')) {
        return '<span class="badge bg-danger">' + escapeHtml(status) + '</span>';
    } else if (statusLower.includes('cleared')) {
        return '<span class="badge bg-info">' + escapeHtml(status) + '</span>';
    } else {
        return '<span class="badge bg-warning">' + escapeHtml(status) + '</span>';
    }
}

function loadStudentProfile(student) {
    const studentId = user.studentId;
    if (!student && !studentId) return;

    const request = student ? Promise.resolve({ data: student }) : fetch('/api/students/' + studentId).then(r => r.json());
    request
        .then(result => {
            const student = result.data || result;
            document.getElementById('studentInfo').innerHTML = `
                <strong>Name:</strong> ${escapeHtml(student.name)}<br>
                <strong>Department:</strong> ${escapeHtml(student.department || '-')}<br>
                <strong>Batch:</strong> ${escapeHtml(student.batch || '-')}<br>
                <strong>CGPA:</strong> ${student.cgpa || '-'}<br>
                <strong>Email:</strong> ${escapeHtml(student.email || '-')}<br>
                <strong>Phone:</strong> ${escapeHtml(student.phone || '-')}<br>
                <strong>Skills:</strong> ${escapeHtml(student.skills || '-') || 'Not specified'}
            `;
        })
        .catch(err => console.error('Error loading profile:', err));
}

function loadPlacements(placements) {
    const studentId = user.studentId;
    const noPlacement = document.getElementById('noPlacement');
    const placementRows = document.getElementById('placementRows');

    const request = placements ? Promise.resolve({ data: placements }) : fetch('/api/placements/student/' + studentId).then(r => r.json());
    request
        .then(result => {
            const placementRecords = result.data || result;

            if (!Array.isArray(placementRecords) || placementRecords.length === 0) {
                placementRows.innerHTML = '';
                noPlacement.hidden = false;
            } else {
                noPlacement.hidden = true;
                placementRows.innerHTML = placementRecords.map(p => `
                    <tr>
                        <td>${escapeHtml(p.companyName)}</td>
                        <td>${escapeHtml(p.jobRole || '-')}</td>
                        <td>${p.packageLpa != null ? '₹' + p.packageLpa + ' LPA' : '-'}</td>
                        <td>${showStatusBadge(p.status)}</td>
                        <td>${escapeHtml(p.appliedDate || '-')}</td>
                    </tr>
                `).join('');
            }
        })
        .catch(err => {
            console.error('Error loading placements:', err);
            placementRows.innerHTML = `<tr><td colspan="5" class="text-danger text-center">Error loading placements</td></tr>`;
        });
}

function loadAttendance(records) {
    const studentId = user.studentId;
    const table = document.getElementById('attendanceTable');

    const request = records ? Promise.resolve({ data: records }) : fetch('/api/attendances/student/' + encodeURIComponent(studentId)).then(r => r.json());
    request
        .then(result => {
            const attendanceRecords = result.data || result;

            if (!Array.isArray(attendanceRecords) || attendanceRecords.length === 0) {
                table.innerHTML = '<tr><td colspan="3" class="text-center">No attendance records found</td></tr>';
            } else {
                table.innerHTML = attendanceRecords.map(r => `
                    <tr>
                        <td>${escapeHtml(r.attendanceDate || r.date || '-')}</td>
                        <td>${escapeHtml(r.training?.trainingName || '-')}</td>
                        <td>${(r.present ?? r.isPresent) ? '<span class="badge bg-success">Present</span>' : '<span class="badge bg-danger">Absent</span>'}</td>
                    </tr>
                `).join('');
            }
        })
        .catch(err => {
            console.error('Error loading attendance:', err);
            table.innerHTML = '<tr><td colspan="3" class="text-danger text-center">Error loading attendance</td></tr>';
        });
}

function loadEligibility(eligibility) {
    const studentId = user.studentId;
    const companiesTable = document.getElementById('companiesTable');

    const request = eligibility ? Promise.resolve({ data: eligibility }) : fetch('/api/eligibility/' + studentId).then(r => r.json());
    request
        .then(result => {
            const eligibilityRecords = result.data || result;

            if (!Array.isArray(eligibilityRecords) || eligibilityRecords.length === 0) {
                companiesTable.innerHTML = '<tr><td colspan="3" class="text-center">No companies found</td></tr>';
            } else {
                companiesTable.innerHTML = eligibilityRecords.map(c => `
                    <tr>
                        <td>${escapeHtml(c.companyName)}</td>
                        <td>${c.eligible ? 
                            '<span class="badge bg-success">YES</span>' : 
                            '<span class="badge bg-danger">NO</span>'}</td>
                        <td>${escapeHtml(c.reasons && c.reasons.length > 0 ? c.reasons.join(', ') : 'All requirements satisfied')}</td>
                    </tr>
                `).join('');
            }
        })
        .catch(err => {
            console.error('Error loading eligibility:', err);
            companiesTable.innerHTML = '<tr><td colspan="3" class="text-danger text-center">Error loading eligibility</td></tr>';
        });
}

let activeAptitudeTest = null;
let activeTrainingVideo = null;
let youtubePlayer = null;
let youtubeApiPromise = null;
let trainingWatchTimer = null;
let trainingVideoDuration = 0;
let trainingSecondsWatched = 0;
let lastTrainingVideoTime = null;
let trainingAttendanceSent = false;

async function loadStudentTrainings() {
    const rows = document.getElementById('studentTrainingRows');
    if (!rows) return;
    try {
        const response = await fetch('/api/trainings/current');
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Unable to load training sessions.');
        const trainings = result.data || result;
        rows.replaceChildren();
        if (!trainings.length) {
            rows.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No training sessions are active today.</td></tr>';
            return;
        }
        trainings.forEach(training => {
            const row = document.createElement('tr');
            [training.trainingName || '-', (training.mode || 'ONSITE') === 'ONLINE' ? 'Online' : 'On-site', training.trainer || '-', `${training.startDate || '-'}${training.endDate ? ` to ${training.endDate}` : ''}`].forEach(value => {
                const cell = document.createElement('td'); cell.textContent = value; row.appendChild(cell);
            });
            const locationCell = document.createElement('td');
            if ((training.mode || 'ONSITE') === 'ONLINE' && training.meetingUrl) {
                const link = document.createElement('a');
                link.href = training.meetingUrl; link.target = '_blank'; link.rel = 'noopener noreferrer'; link.textContent = 'Join session';
                locationCell.appendChild(link);
            } else {
                locationCell.textContent = training.location || (training.mode === 'ONLINE' ? 'Link not provided' : 'Location not provided');
            }
            row.appendChild(locationCell);
            const lessonCell = document.createElement('td');
            if (training.videoUrl) {
                const watchButton = document.createElement('button');
                watchButton.type = 'button';
                watchButton.className = 'btn btn-sm btn-outline-primary';
                watchButton.textContent = 'Watch lesson';
                watchButton.addEventListener('click', () => startTrainingVideo(training));
                lessonCell.appendChild(watchButton);
            } else {
                lessonCell.textContent = 'Not assigned';
            }
            row.appendChild(lessonCell);
            rows.appendChild(row);
        });
    } catch (error) {
        rows.innerHTML = `<tr><td colspan="6" class="text-center text-danger">${escapeHtml(error.message)}</td></tr>`;
    }
}

function youtubeVideoId(videoUrl) {
    try {
        const url = new URL(videoUrl);
        if (url.hostname === 'youtu.be') return url.pathname.slice(1).split('/')[0];
        if (url.hostname === 'youtube.com' || url.hostname === 'www.youtube.com') {
            return url.searchParams.get('v') || url.pathname.match(/^\/embed\/([\w-]{11})/)?.[1] || null;
        }
    } catch (error) {
        console.error('Invalid training video URL:', error);
    }
    return null;
}

function loadYouTubePlayerApi() {
    if (window.YT?.Player) return Promise.resolve();
    if (!youtubeApiPromise) {
        youtubeApiPromise = new Promise((resolve, reject) => {
            const previousCallback = window.onYouTubeIframeAPIReady;
            window.onYouTubeIframeAPIReady = () => {
                if (typeof previousCallback === 'function') previousCallback();
                resolve();
            };
            const script = document.createElement('script');
            script.src = 'https://www.youtube.com/iframe_api';
            script.onerror = () => reject(new Error('Could not load the YouTube player.'));
            document.head.appendChild(script);
        });
    }
    return youtubeApiPromise;
}

function stopTrainingWatchTimer() {
    if (trainingWatchTimer !== null) window.clearInterval(trainingWatchTimer);
    trainingWatchTimer = null;
    lastTrainingVideoTime = null;
}

function renderTrainingProgress() {
    const progress = document.getElementById('trainingVideoProgress');
    if (!trainingVideoDuration) return;
    const percent = Math.min(100, Math.floor(trainingSecondsWatched * 100 / trainingVideoDuration));
    progress.value = percent;
    if (percent >= 90 && !trainingAttendanceSent) recordTrainingVideoCompletion();
}

function beginTrainingWatchTimer() {
    if (trainingWatchTimer !== null) return;
    lastTrainingVideoTime = youtubePlayer.getCurrentTime();
    trainingWatchTimer = window.setInterval(() => {
        if (!youtubePlayer || youtubePlayer.getPlayerState() !== window.YT.PlayerState.PLAYING) return;
        const currentTime = youtubePlayer.getCurrentTime();
        const delta = currentTime - lastTrainingVideoTime;
        if (delta > 0 && delta <= 2.5) trainingSecondsWatched += delta;
        lastTrainingVideoTime = currentTime;
        renderTrainingProgress();
    }, 1000);
}

async function startTrainingVideo(training) {
    const section = document.getElementById('trainingVideoSection');
    const message = document.getElementById('trainingVideoMessage');
    const videoId = youtubeVideoId(training.videoUrl);
    if (!videoId) {
        message.textContent = 'This training has an invalid YouTube lesson URL.';
        message.className = 'alert d-block alert-danger mt-3';
        section.hidden = false;
        return;
    }
    stopTrainingWatchTimer();
    if (youtubePlayer) youtubePlayer.destroy();
    activeTrainingVideo = training;
    trainingVideoDuration = 0;
    trainingSecondsWatched = 0;
    trainingAttendanceSent = false;
    document.getElementById('trainingVideoProgress').value = 0;
    document.getElementById('trainingVideoTitle').textContent = training.trainingName;
    message.textContent = 'Watch at least 90% of the lesson to record attendance for today.';
    message.className = 'alert d-block alert-info mt-3';
    section.hidden = false;
    document.getElementById('trainingVideoPlayer').replaceWith(Object.assign(document.createElement('div'), { id: 'trainingVideoPlayer' }));
    section.scrollIntoView({ behavior: 'smooth', block: 'start' });
    try {
        await loadYouTubePlayerApi();
        youtubePlayer = new window.YT.Player('trainingVideoPlayer', {
            videoId,
            width: '100%',
            height: '100%',
            playerVars: { rel: 0, playsinline: 1, origin: window.location.origin },
            events: {
                onReady: event => { trainingVideoDuration = event.target.getDuration(); },
                onStateChange: event => {
                    if (event.data === window.YT.PlayerState.PLAYING) beginTrainingWatchTimer();
                    else stopTrainingWatchTimer();
                    if (event.data === window.YT.PlayerState.ENDED) renderTrainingProgress();
                },
                onError: () => {
                    message.textContent = 'This video cannot be played here. Contact the training admin.';
                    message.className = 'alert d-block alert-danger mt-3';
                }
            }
        });
    } catch (error) {
        message.textContent = error.message;
        message.className = 'alert d-block alert-danger mt-3';
    }
}

async function recordTrainingVideoCompletion() {
    if (!activeTrainingVideo || trainingAttendanceSent || !trainingVideoDuration
        || trainingSecondsWatched < trainingVideoDuration * 0.9) return;
    trainingAttendanceSent = true;
    const message = document.getElementById('trainingVideoMessage');
    message.textContent = 'Lesson completed. Recording attendance...';
    message.className = 'alert d-block alert-info mt-3';
    try {
        const response = await fetch(`/api/trainings/${encodeURIComponent(activeTrainingVideo.id)}/video-completion`, { method: 'POST' });
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Attendance could not be recorded.');
        const date = result.data?.date || new Date().toISOString().slice(0, 10);
        message.textContent = `Present recorded for ${date}.`;
        message.className = 'alert d-block alert-success mt-3';
        loadAttendance();
        refreshAttendanceStats();
    } catch (error) {
        trainingAttendanceSent = false;
        message.textContent = error.message;
        message.className = 'alert d-block alert-danger mt-3';
    }
}

async function refreshAttendanceStats() {
    const studentId = user.studentId || localStorage.getItem('studentId');
    try {
        const response = await fetch('/api/attendances/student/' + encodeURIComponent(studentId));
        const result = await response.json();
        if (!response.ok) return;
        const records = result.data || result;
        const present = records.filter(record => record.present ?? record.isPresent).length;
        const rate = records.length ? Math.round(present * 100 / records.length) : 0;
        document.getElementById('trainingsAttended').textContent = present;
        document.getElementById('attendanceRate').textContent = `${rate}%`;
    } catch (error) {
        console.error('Unable to refresh attendance stats:', error);
    }
}

async function loadAptitude() {
    const studentId = user.studentId || localStorage.getItem('studentId');
    const testRows = document.getElementById('availableTestRows');
    const scoreRows = document.getElementById('studentScoreRows');
    if (!studentId || !testRows || !scoreRows) return;
    try {
        const [testResponse, scoreResponse] = await Promise.all([
                fetch('/api/aptitude-tests'), fetch('/api/aptitude-scores/student/' + encodeURIComponent(studentId))
        ]);
        const [testResult, scoreResult] = await Promise.all([testResponse.json(), scoreResponse.json()]);
        if (!testResponse.ok || !scoreResponse.ok) throw new Error(testResult.message || scoreResult.message || 'Unable to load aptitude data.');
        const tests = testResult.data || testResult;
        const scores = scoreResult.data || scoreResult;
        const completedAttempts = new Map(scores
            .filter(score => score.completedAttempt)
            .map(score => [score.test?.id, score]));
        testRows.replaceChildren();
        if (tests.length === 0) {
            testRows.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No tests are available.</td></tr>';
        } else {
            tests.forEach(test => {
                const row = document.createElement('tr');
                [test.testName, test.testDate || 'Unscheduled', `${test.totalMarks ?? '-'} marks`].forEach(value => {
                    const cell = document.createElement('td'); cell.textContent = value; row.appendChild(cell);
                });
                const actionCell = document.createElement('td'); actionCell.className = 'text-end';
                const button = document.createElement('button');
                const completedAttempt = completedAttempts.get(test.id);
                button.type = 'button'; button.className = `btn btn-sm ${completedAttempt ? 'btn-outline-success' : 'btn-primary'}`;
                button.textContent = completedAttempt ? 'View results' : test.questionCount > 0 ? 'Take test' : 'Not ready';
                button.disabled = !completedAttempt && test.questionCount === 0;
                button.addEventListener('click', () => completedAttempt ? viewAptitudeReview(test) : beginAptitudeTest(test));
                actionCell.appendChild(button); row.appendChild(actionCell); testRows.appendChild(row);
            });
        }
        scoreRows.replaceChildren();
        if (scores.length === 0) {
            scoreRows.innerHTML = '<tr><td colspan="3" class="text-center text-muted">No test results recorded.</td></tr>';
        } else {
            scores.forEach(record => {
                const row = document.createElement('tr');
                [record.test?.testName || '-', record.test?.testDate || '—', `${Number(record.score).toFixed(2)}%`].forEach(value => {
                    const cell = document.createElement('td'); cell.textContent = value; row.appendChild(cell);
                });
                scoreRows.appendChild(row);
            });
        }
        document.getElementById('aptitudeMessage').classList.add('d-none');
    } catch (error) {
        const message = document.getElementById('aptitudeMessage');
        message.textContent = error.message;
        message.className = 'alert d-block alert-danger';
    }
}

async function beginAptitudeTest(test) {
    const message = document.getElementById('aptitudeMessage');
    try {
        const response = await fetch(`/api/aptitude-tests/${encodeURIComponent(test.id)}/questions`);
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Could not open this test.');
        const questions = result.data || result;
        if (questions.length === 0) throw new Error('This test does not have questions yet.');
        activeAptitudeTest = { test, questions };
        document.getElementById('assessmentReview').hidden = true;
        const title = document.getElementById('assessmentTitle');
        const form = document.getElementById('assessmentForm');
        title.textContent = `${test.testName} · ${test.totalMarks} marks`;
        form.replaceChildren();
        questions.forEach((question, index) => {
            const fieldset = document.createElement('fieldset');
            fieldset.className = 'border rounded p-3';
            const legend = document.createElement('legend');
            legend.className = 'h6'; legend.textContent = `${index + 1}. ${question.prompt} (${question.marks} marks)`;
            fieldset.appendChild(legend);
            ['A', 'B', 'C', 'D'].forEach(letter => {
                const wrapper = document.createElement('div'); wrapper.className = 'form-check';
                const input = document.createElement('input');
                input.type = 'radio'; input.className = 'form-check-input';
                input.name = `answer-${question.id}`; input.id = `answer-${question.id}-${letter}`;
                input.value = letter; input.dataset.questionId = question.id; input.required = true;
                const label = document.createElement('label');
                label.className = 'form-check-label'; label.htmlFor = input.id;
                label.textContent = `${letter}. ${question[`option${letter}`]}`;
                wrapper.append(input, label); fieldset.appendChild(wrapper);
            });
            form.appendChild(fieldset);
        });
        const submit = document.createElement('button');
        submit.type = 'submit'; submit.className = 'btn btn-primary align-self-start'; submit.textContent = 'Submit test';
        form.appendChild(submit);
        document.getElementById('assessmentContainer').hidden = false;
        message.classList.add('d-none');
        document.getElementById('assessmentContainer').scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) {
        message.textContent = error.message;
        message.className = 'alert d-block alert-danger';
    }
}

document.getElementById('assessmentForm')?.addEventListener('submit', async event => {
    event.preventDefault();
    if (!activeAptitudeTest) return;
    const userStudentId = user.studentId || localStorage.getItem('studentId');
    const answers = [...event.currentTarget.querySelectorAll('input[type="radio"]:checked')]
        .map(input => ({ questionId: input.dataset.questionId, selectedOption: input.value }));
    try {
        const response = await fetch(`/api/aptitude-tests/${encodeURIComponent(activeAptitudeTest.test.id)}/submit`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ studentId: userStudentId, answers })
        });
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Could not submit the test.');
        const submittedTest = activeAptitudeTest.test;
        document.getElementById('assessmentContainer').hidden = true;
        activeAptitudeTest = null;
        await loadAptitude();
        await viewAptitudeReview(submittedTest);
    } catch (error) {
        const message = document.getElementById('aptitudeMessage');
        message.textContent = error.message;
        message.className = 'alert d-block alert-danger';
    }
});

async function viewAptitudeReview(test) {
    const message = document.getElementById('aptitudeMessage');
    try {
        const response = await fetch(`/api/aptitude-tests/${encodeURIComponent(test.id)}/review`);
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Could not load the completed test review.');
        const review = result.data || result;
        document.getElementById('assessmentContainer').hidden = true;
        document.getElementById('reviewTitle').textContent = `${review.testName} · Answer review`;
        document.getElementById('reviewScore').textContent = `Score: ${review.earnedMarks} / ${review.totalMarks} marks (${Number(review.score).toFixed(2)}%).`;
        const reviewAnswers = document.getElementById('reviewAnswers');
        reviewAnswers.replaceChildren();
        review.answers.forEach((answer, index) => {
            const article = document.createElement('article');
            article.className = 'border rounded p-3';
            const prompt = document.createElement('h3');
            prompt.className = 'h6';
            prompt.textContent = `${index + 1}. ${answer.prompt} (${answer.marks} marks)`;
            const options = document.createElement('ol');
            options.type = 'A';
            answer.options.forEach((option, optionIndex) => {
                const letter = 'ABCD'[optionIndex];
                const item = document.createElement('li');
                item.textContent = option;
                if (letter === answer.correctOption) item.classList.add('aptitude-correct-answer');
                if (letter === answer.selectedOption) item.classList.add('aptitude-selected-answer');
                const markers = [];
                if (letter === answer.selectedOption) markers.push('Your answer');
                if (letter === answer.correctOption) markers.push('Correct answer');
                if (markers.length) item.append(` (${markers.join(' · ')})`);
                options.appendChild(item);
            });
            article.append(prompt, options);
            reviewAnswers.appendChild(article);
        });
        document.getElementById('assessmentReview').hidden = false;
        message.classList.add('d-none');
        document.getElementById('assessmentReview').scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) {
        message.textContent = error.message;
        message.className = 'alert d-block alert-danger';
    }
}

document.getElementById('closeReview')?.addEventListener('click', () => {
    document.getElementById('assessmentReview').hidden = true;
});

async function load() {
    try {
        const response = await fetch('/api/dashboard');
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Unable to load dashboard.');
        const dashboard = result.data || result;
        if (dashboard.role !== 'STUDENT') throw new Error('Student dashboard data is unavailable.');
        loadStudentProfile(dashboard.student);
        loadPlacements(dashboard.placements);
        loadAttendance(dashboard.attendance);
        loadEligibility(dashboard.eligibility);
    } catch (error) {
        console.error('Error loading student dashboard:', error);
        document.getElementById('studentInfo').textContent = error.message;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    const toggleSidebar = document.getElementById('toggleSidebar');
    const mobileSidebar = window.matchMedia('(max-width: 992px)');
    const backdrop = document.createElement('button');
    backdrop.type = 'button';
    backdrop.className = 'sidebar-backdrop';
    backdrop.setAttribute('aria-label', 'Close navigation menu');
    backdrop.hidden = true;
    document.body.appendChild(backdrop);

    const setSidebarOpen = open => {
        sidebar.classList.toggle('collapsed', !open);
        mainContent.classList.toggle('expanded', !open);
        toggleSidebar.setAttribute('aria-expanded', String(open));
        toggleSidebar.setAttribute('aria-label', open ? 'Close navigation menu' : 'Open navigation menu');
        backdrop.hidden = !open || !mobileSidebar.matches;
    };

    setSidebarOpen(!mobileSidebar.matches);
    mobileSidebar.addEventListener('change', event => setSidebarOpen(!event.matches));
    toggleSidebar.addEventListener('click', () => setSidebarOpen(sidebar.classList.contains('collapsed')));
    backdrop.addEventListener('click', () => setSidebarOpen(false));
    sidebar.querySelectorAll('[data-page]').forEach(link => {
        link.addEventListener('click', () => {
            if (mobileSidebar.matches) setSidebarOpen(false);
        });
    });
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && mobileSidebar.matches && !sidebar.classList.contains('collapsed')) {
            setSidebarOpen(false);
            toggleSidebar.focus();
        }
    });

    load();
});

async function logout() {
    try { await fetch('/api/auth/logout', { method: 'POST' }); } catch (err) { console.error('Error ending student session:', err); }
    localStorage.removeItem('user');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');
    localStorage.removeItem('studentId');
    location.href = '/';
}
