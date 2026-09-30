const scoreForm = document.getElementById('scoreForm');
const scoreRows = document.getElementById('scoreRows');
const scoreMessage = document.getElementById('scoreMessage');
const scoreStudent = document.getElementById('scoreStudent');
const scoreTest = document.getElementById('scoreTest');
let studentsForScores = [];
let testsForScores = [];
let scoreRecords = [];
let editingScoreId = null;

function scoreData(result) { return result.data ?? result; }

function showScoreMessage(message, type = 'info') {
    scoreMessage.textContent = message;
    scoreMessage.className = `alert d-block alert-${type}`;
}

async function scoreRequest(url, options = {}) {
    const response = await fetch(url, { ...options, headers: { 'Content-Type': 'application/json', ...options.headers } });
    const result = await response.json();
    if (!response.ok) throw new Error(result.message || 'The request could not be completed.');
    return scoreData(result);
}

function fillSelect(select, placeholder, records, label) {
    select.replaceChildren(new Option(placeholder, ''));
    records.forEach(record => select.add(new Option(label(record), record.id)));
}

async function loadScoreData() {
    try {
        [studentsForScores, testsForScores, scoreRecords] = await Promise.all([
            scoreRequest('/api/students'), scoreRequest('/api/aptitude-tests'), scoreRequest('/api/aptitude-scores')
        ]);
        fillSelect(scoreStudent, 'Choose a student', studentsForScores, student => student.name);
        fillSelect(scoreTest, 'Choose a test', testsForScores, test => `${test.testName} (${test.testDate || 'unscheduled'})`);
        renderScores();
    } catch (error) { showScoreMessage(error.message, 'danger'); }
}

function renderScores() {
    scoreRows.replaceChildren();
    if (scoreRecords.length === 0) {
        scoreRows.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">No scores recorded.</td></tr>';
        return;
    }
    scoreRecords.forEach(score => {
        const row = document.createElement('tr');
        [score.student?.name || '-', score.test?.testName || '-', score.test?.testDate || '—', `${Number(score.score).toFixed(2)}%`].forEach(value => {
            const cell = document.createElement('td'); cell.textContent = value; row.appendChild(cell);
        });
        const actions = document.createElement('td'); actions.className = 'text-nowrap';
        const edit = document.createElement('button'); edit.type = 'button'; edit.className = 'btn btn-sm btn-outline-secondary me-1'; edit.textContent = 'Edit';
        edit.addEventListener('click', () => editScore(score));
        const remove = document.createElement('button'); remove.type = 'button'; remove.className = 'btn btn-sm btn-outline-danger'; remove.textContent = 'Delete';
        remove.addEventListener('click', () => deleteScore(score));
        actions.append(edit, remove); row.appendChild(actions); scoreRows.appendChild(row);
    });
}

function resetScoreForm() {
    scoreForm.reset();
    editingScoreId = null;
    document.getElementById('scoreFormHeading').textContent = 'Record a score';
    document.getElementById('saveScore').textContent = 'Save score';
    document.getElementById('cancelScoreEdit').hidden = true;
}

function editScore(score) {
    editingScoreId = score.id;
    scoreStudent.value = score.student?.id || '';
    scoreTest.value = score.test?.id || '';
    document.getElementById('scoreValue').value = score.score;
    document.getElementById('scoreFormHeading').textContent = 'Edit recorded score';
    document.getElementById('saveScore').textContent = 'Update score';
    document.getElementById('cancelScoreEdit').hidden = false;
    scoreForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

scoreForm.addEventListener('submit', async event => {
    event.preventDefault();
    const record = {
        student: { id: scoreStudent.value },
        test: { id: scoreTest.value },
        score: Number(document.getElementById('scoreValue').value)
    };
    try {
        await scoreRequest('/api/aptitude-scores' + (editingScoreId ? `/${editingScoreId}` : ''), {
            method: editingScoreId ? 'PUT' : 'POST', body: JSON.stringify(record)
        });
        resetScoreForm();
        scoreRecords = await scoreRequest('/api/aptitude-scores');
        renderScores();
        showScoreMessage('Score saved.', 'success');
    } catch (error) { showScoreMessage(error.message, 'danger'); }
});

async function deleteScore(score) {
    if (!confirm('Delete this aptitude score?')) return;
    try {
        await scoreRequest(`/api/aptitude-scores/${score.id}`, { method: 'DELETE' });
        scoreRecords = await scoreRequest('/api/aptitude-scores');
        renderScores();
        showScoreMessage('Score deleted.', 'success');
    } catch (error) { showScoreMessage(error.message, 'danger'); }
}

document.getElementById('cancelScoreEdit').addEventListener('click', resetScoreForm);
loadScoreData();