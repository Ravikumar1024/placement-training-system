const testsBody = document.getElementById('testRows');
const questionsBody = document.getElementById('questionRows');
const testForm = document.getElementById('testForm');
const questionForm = document.getElementById('questionForm');
const aptitudeMessage = document.getElementById('aptitudeMessage');
let tests = [];
let selectedTest = null;
let editingTestId = null;
let editingQuestionId = null;

function responseData(result) { return result.data ?? result; }

function showAptitudeMessage(message, type = 'info') {
    aptitudeMessage.textContent = message;
    aptitudeMessage.className = `alert d-block alert-${type}`;
}

async function request(url, options = {}) {
    const response = await fetch(url, { ...options, headers: { 'Content-Type': 'application/json', ...options.headers } });
    const result = await response.json();
    if (!response.ok) throw new Error(result.message || 'The request could not be completed.');
    return responseData(result);
}

function appendCell(row, value) {
    const cell = document.createElement('td');
    cell.textContent = value ?? '-';
    row.appendChild(cell);
    return cell;
}

async function loadTests() {
    try {
        tests = await request('/api/aptitude-tests');
        testsBody.replaceChildren();
        if (tests.length === 0) {
            testsBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No aptitude tests have been created.</td></tr>';
            return;
        }
        tests.forEach(test => {
            const row = document.createElement('tr');
            appendCell(row, test.testName);
            appendCell(row, test.testDate || 'Not scheduled');
            appendCell(row, test.totalMarks);
            const actions = document.createElement('td');
            actions.className = 'text-nowrap';
            const manage = document.createElement('button');
            manage.type = 'button'; manage.className = 'btn btn-sm btn-outline-primary me-1'; manage.textContent = 'Questions';
            manage.addEventListener('click', () => selectTest(test));
            const edit = document.createElement('button');
            edit.type = 'button'; edit.className = 'btn btn-sm btn-outline-secondary me-1'; edit.textContent = 'Edit';
            edit.addEventListener('click', () => editTest(test));
            const remove = document.createElement('button');
            remove.type = 'button'; remove.className = 'btn btn-sm btn-outline-danger'; remove.setAttribute('aria-label', `Delete ${test.testName}`); remove.textContent = 'Delete';
            remove.addEventListener('click', () => deleteTest(test));
            actions.append(manage, edit, remove);
            row.appendChild(actions);
            testsBody.appendChild(row);
        });
    } catch (error) { showAptitudeMessage(error.message, 'danger'); }
}

function resetTestForm() {
    testForm.reset();
    editingTestId = null;
    document.getElementById('saveTest').textContent = 'Create test';
    document.getElementById('cancelTestEdit').hidden = true;
}

function editTest(test) {
    editingTestId = test.id;
    document.getElementById('testName').value = test.testName || '';
    document.getElementById('testDate').value = test.testDate || '';
    document.getElementById('totalMarks').value = test.totalMarks ?? '';
    document.getElementById('saveTest').textContent = 'Update test';
    document.getElementById('cancelTestEdit').hidden = false;
    testForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

testForm.addEventListener('submit', async event => {
    event.preventDefault();
    const test = {
        testName: document.getElementById('testName').value.trim(),
        testDate: document.getElementById('testDate').value || null,
        totalMarks: Number(document.getElementById('totalMarks').value)
    };
    try {
        const saved = await request('/api/aptitude-tests' + (editingTestId ? `/${editingTestId}` : ''), {
            method: editingTestId ? 'PUT' : 'POST', body: JSON.stringify(test)
        });
        resetTestForm();
        await loadTests();
        if (!editingTestId && saved?.id) await selectTest(saved);
        else showAptitudeMessage('Test saved.', 'success');
    } catch (error) { showAptitudeMessage(error.message, 'danger'); }
});

async function deleteTest(test) {
    if (!confirm(`Delete “${test.testName}”? Tests with recorded scores cannot be deleted.`)) return;
    try {
        await request(`/api/aptitude-tests/${test.id}`, { method: 'DELETE' });
        if (selectedTest?.id === test.id) clearSelectedTest();
        await loadTests();
        showAptitudeMessage('Test deleted.', 'success');
    } catch (error) { showAptitudeMessage(error.message, 'danger'); }
}

function clearSelectedTest() {
    selectedTest = null;
    editingQuestionId = null;
    questionForm.hidden = true;
    document.getElementById('questionPrompt').hidden = false;
    document.getElementById('questionHeading').textContent = 'Test questions';
    questionsBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">Choose a test to view questions.</td></tr>';
}

async function selectTest(test) {
    selectedTest = test;
    document.getElementById('questionHeading').textContent = `Questions: ${test.testName}`;
    document.getElementById('questionPrompt').hidden = true;
    questionForm.hidden = false;
    resetQuestionForm();
    await loadQuestions();
    document.getElementById('questionWorkspace').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function loadQuestions() {
    if (!selectedTest) return;
    try {
        const questions = await request(`/api/aptitude-tests/${selectedTest.id}/questions/manage`);
        questionsBody.replaceChildren();
        if (questions.length === 0) {
            questionsBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">Add the first question to make this test available.</td></tr>';
            return;
        }
        questions.forEach(question => {
            const row = document.createElement('tr');
            appendCell(row, question.prompt);
            appendCell(row, question.correctOption);
            appendCell(row, question.marks);
            const actions = document.createElement('td');
            actions.className = 'text-nowrap';
            const edit = document.createElement('button');
            edit.type = 'button'; edit.className = 'btn btn-sm btn-outline-secondary me-1'; edit.textContent = 'Edit';
            edit.addEventListener('click', () => editQuestion(question));
            const remove = document.createElement('button');
            remove.type = 'button'; remove.className = 'btn btn-sm btn-outline-danger'; remove.textContent = 'Delete';
            remove.addEventListener('click', () => deleteQuestion(question));
            actions.append(edit, remove);
            row.appendChild(actions);
            questionsBody.appendChild(row);
        });
    } catch (error) { showAptitudeMessage(error.message, 'danger'); }
}

function questionPayload() {
    return {
        prompt: document.getElementById('questionPromptInput').value.trim(),
        optionA: document.getElementById('optionA').value.trim(),
        optionB: document.getElementById('optionB').value.trim(),
        optionC: document.getElementById('optionC').value.trim(),
        optionD: document.getElementById('optionD').value.trim(),
        correctOption: document.getElementById('correctOption').value,
        marks: Number(document.getElementById('questionMarks').value)
    };
}

function resetQuestionForm() {
    questionForm.reset();
    editingQuestionId = null;
    document.getElementById('questionMarks').value = '1';
    document.getElementById('saveQuestion').textContent = 'Add question';
    document.getElementById('cancelQuestionEdit').hidden = true;
}

function editQuestion(question) {
    editingQuestionId = question.id;
    document.getElementById('questionPromptInput').value = question.prompt;
    ['optionA', 'optionB', 'optionC', 'optionD', 'correctOption'].forEach(field => { document.getElementById(field).value = question[field]; });
    document.getElementById('questionMarks').value = question.marks;
    document.getElementById('saveQuestion').textContent = 'Update question';
    document.getElementById('cancelQuestionEdit').hidden = false;
    questionForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

questionForm.addEventListener('submit', async event => {
    event.preventDefault();
    if (!selectedTest) return;
    const base = `/api/aptitude-tests/${selectedTest.id}/questions`;
    try {
        await request(base + (editingQuestionId ? `/${editingQuestionId}` : ''), {
            method: editingQuestionId ? 'PUT' : 'POST', body: JSON.stringify(questionPayload())
        });
        resetQuestionForm();
        await loadQuestions();
        showAptitudeMessage('Question saved.', 'success');
    } catch (error) { showAptitudeMessage(error.message, 'danger'); }
});

async function deleteQuestion(question) {
    if (!confirm('Delete this question?')) return;
    try {
        await request(`/api/aptitude-tests/${selectedTest.id}/questions/${question.id}`, { method: 'DELETE' });
        await loadQuestions();
        showAptitudeMessage('Question deleted.', 'success');
    } catch (error) { showAptitudeMessage(error.message, 'danger'); }
}

function openTriviaCategory(testName) {
    const name = testName.toLowerCase();
    if (/math|quantitative|numerical|arithmetic/.test(name)) return 19;
    if (/computer|programming|coding|information technology/.test(name)) return 18;
    if (/science|physics|chemistry|biology/.test(name)) return 17;
    if (/history/.test(name)) return 23;
    if (/geography/.test(name)) return 22;
    if (/sport/.test(name)) return 21;
    if (/english|verbal|vocabulary|literature/.test(name)) return 10;
    return 9;
}

function decodeTriviaText(value) {
    const decoder = document.createElement('textarea');
    decoder.innerHTML = value;
    return decoder.value;
}

function shuffleOptions(options) {
    for (let index = options.length - 1; index > 0; index -= 1) {
        const swapIndex = Math.floor(Math.random() * (index + 1));
        [options[index], options[swapIndex]] = [options[swapIndex], options[index]];
    }
    return options;
}

async function importOnlineQuestions() {
    if (!selectedTest) return;
    const button = document.getElementById('importOnlineQuestions');
    button.disabled = true;
    button.innerHTML = '<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Importing';
    try {
        const category = openTriviaCategory(selectedTest.testName);
        const response = await fetch(`https://opentdb.com/api.php?amount=10&category=${category}&type=multiple&encode=url3986`);
        if (!response.ok) throw new Error('Open Trivia Database is unavailable right now.');
        const result = await response.json();
        if (result.response_code !== 0 || !Array.isArray(result.results) || result.results.length === 0) {
            throw new Error('No questions were returned for this topic. Wait a few seconds and try again.');
        }
        const questions = result.results.map(item => {
            const options = shuffleOptions([
                { text: decodeURIComponent(item.correct_answer), correct: true },
                ...item.incorrect_answers.map(answer => ({ text: decodeURIComponent(answer), correct: false }))
            ]);
            const [optionA, optionB, optionC, optionD] = options.map(option => option.text.trim());
            return {
                prompt: decodeURIComponent(item.question).trim(),
                optionA, optionB, optionC, optionD,
                correctOption: 'ABCD'[options.findIndex(option => option.correct)],
                marks: 0
            };
        }).filter(question => question.prompt && question.prompt.length <= 1000
            && [question.optionA, question.optionB, question.optionC, question.optionD].every(option => option && option.length <= 500));
        if (questions.length === 0) throw new Error('The returned questions did not fit the test field limits.');
        const questionMarks = Math.max(0.01, Number((Number(selectedTest.totalMarks) / questions.length).toFixed(2)));
        questions.forEach(question => { question.marks = questionMarks; });
        const imported = await request(`/api/aptitude-tests/${selectedTest.id}/questions/import`, {
            method: 'POST', body: JSON.stringify({ questions })
        });
        await loadQuestions();
        showAptitudeMessage(`Imported ${imported.length} questions from Open Trivia Database. Review the answer keys before publishing.`, 'success');
    } catch (error) {
        showAptitudeMessage(error.message, 'danger');
    } finally {
        button.disabled = false;
        button.innerHTML = '<i class="fas fa-cloud-arrow-down" aria-hidden="true"></i> Import online questions';
    }
}

document.getElementById('cancelTestEdit').addEventListener('click', resetTestForm);
document.getElementById('cancelQuestionEdit').addEventListener('click', resetQuestionForm);
document.getElementById('importOnlineQuestions').addEventListener('click', importOnlineQuestions);
loadTests();