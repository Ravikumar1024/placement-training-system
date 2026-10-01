const trainingSelect = document.getElementById('trainingSelect');
const attendanceDate = document.getElementById('attendanceDate');
const rosterRows = document.getElementById('rosterRows');
const historyRows = document.getElementById('attendanceHistory');
let students = [];
let trainings = [];
let attendanceRecords = [];

function apiData(result) {
    return result.data ?? result;
}

function setAttendanceMessage(message, type = 'info') {
    window.showSnackbar(message && typeof message === 'object' ? message : {
        code: type === 'success' || type === 'info' ? 'PTSS001' : 'PTSE001',
        message: String(message || '')
    });
}

async function fetchList(url) {
    const response = await fetch(url);
    const result = await response.json();
    if (!response.ok) throw window.createApiError(result, 'Unable to load data.');
    return apiData(result);
}

function option(label, value) {
    const element = document.createElement('option');
    element.value = value;
    element.textContent = label;
    return element;
}

function renderRoster() {
    const trainingId = trainingSelect.value;
    const date = attendanceDate.value;
    if (!trainingId || !date) {
        rosterRows.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">Choose a training and date to load the roster.</td></tr>';
        return;
    }
    const previous = new Map(attendanceRecords
        .filter(record => record.training?.id === trainingId && (record.date || record.attendanceDate) === date)
        .map(record => [record.student?.id, record.present ?? record.isPresent]));
    rosterRows.replaceChildren();
    if (students.length === 0) {
        rosterRows.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">No students are registered.</td></tr>';
        return;
    }
    students.forEach(student => {
        const row = document.createElement('tr');
        [student.name || 'Unnamed student', student.email || '-', student.department || '-'].forEach(value => {
            const cell = document.createElement('td');
            cell.textContent = value;
            row.appendChild(cell);
        });
        const statusCell = document.createElement('td');
        statusCell.className = 'text-end';
        const select = document.createElement('select');
        select.className = 'form-select form-select-sm ms-auto';
        select.style.maxWidth = '160px';
        select.dataset.studentId = student.id;
        select.setAttribute('aria-label', `Attendance for ${student.name || 'student'}`);
        select.append(option('Choose status', ''), option('Present', 'true'), option('Absent', 'false'));
        const savedStatus = previous.get(student.id);
        select.value = savedStatus == null ? '' : String(savedStatus);
        statusCell.appendChild(select);
        row.appendChild(statusCell);
        rosterRows.appendChild(row);
    });
}

async function loadRoster() {
    try {
        if (!trainingSelect.value || !attendanceDate.value) throw new Error('Choose both a training and a session date.');
        attendanceRecords = await fetchList('/api/attendances');
        renderRoster();
        setAttendanceMessage('Roster loaded. Existing marks for this session have been selected.', 'success');
    } catch (error) {
        setAttendanceMessage(error, 'warning');
    }
}

function renderHistory() {
    historyRows.replaceChildren();
    if (attendanceRecords.length === 0) {
        historyRows.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">No attendance records found.</td></tr>';
        return;
    }
    [...attendanceRecords]
        .sort((a, b) => String(b.date || b.attendanceDate || '').localeCompare(String(a.date || a.attendanceDate || '')))
        .forEach(record => {
            const row = document.createElement('tr');
            [record.date || record.attendanceDate || '-', record.training?.trainingName || '-', record.student?.name || '-'].forEach(value => {
                const cell = document.createElement('td');
                cell.textContent = value;
                row.appendChild(cell);
            });
            const status = document.createElement('td');
            const present = record.present ?? record.isPresent;
            const badge = document.createElement('span');
            badge.className = `badge text-bg-${present ? 'success' : 'danger'}`;
            badge.textContent = present ? 'Present' : 'Absent';
            status.appendChild(badge);
            row.appendChild(status);
            historyRows.appendChild(row);
        });
}

async function loadInitialData() {
    try {
        [students, trainings, attendanceRecords] = await Promise.all([
            fetchList('/api/students'), fetchList('/api/trainings'), fetchList('/api/attendances')
        ]);
        trainings.forEach(training => trainingSelect.append(option(training.trainingName, training.id)));
        renderHistory();
    } catch (error) {
        setAttendanceMessage(error, 'danger');
    }
}

async function saveAttendance() {
    const controls = [...rosterRows.querySelectorAll('select[data-student-id]')];
    if (controls.length === 0 || controls.some(control => control.value === '')) {
        setAttendanceMessage('Set Present or Absent for every student before saving.', 'warning');
        return;
    }
    try {
        const response = await fetch('/api/attendances/bulk', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                trainingId: trainingSelect.value,
                date: attendanceDate.value,
                entries: controls.map(control => ({ studentId: control.dataset.studentId, present: control.value === 'true' }))
            })
        });
        const result = await response.json();
        if (!response.ok) throw window.createApiError(result, 'Could not save attendance.');
        attendanceRecords = await fetchList('/api/attendances');
        renderRoster();
        renderHistory();
        setAttendanceMessage(result, 'success');
    } catch (error) {
        setAttendanceMessage(error, 'danger');
    }
}

document.getElementById('loadRoster').addEventListener('click', loadRoster);
document.getElementById('saveAttendance').addEventListener('click', saveAttendance);
document.getElementById('markAllPresent').addEventListener('click', () => rosterRows.querySelectorAll('select[data-student-id]').forEach(select => { select.value = 'true'; }));
document.getElementById('markAllAbsent').addEventListener('click', () => rosterRows.querySelectorAll('select[data-student-id]').forEach(select => { select.value = 'false'; }));
trainingSelect.addEventListener('change', renderRoster);
attendanceDate.addEventListener('change', renderRoster);
const today = new Date();
attendanceDate.value = [today.getFullYear(), String(today.getMonth() + 1).padStart(2, '0'), String(today.getDate()).padStart(2, '0')].join('-');
loadInitialData();