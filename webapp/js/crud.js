const crudForm = document.getElementById('form');
const crudRows = document.getElementById('rows');
const numericFields = new Set(['cgpa', 'backlogs', 'packageLpa', 'minCgpa', 'maxBacklogs', 'minAptitudeScore', 'minAttendance', 'totalMarks']);

async function apiRequest(url, options = {}) {
    const response = await fetch(url, options);
    const result = await response.json();
    if (!response.ok) throw window.createApiError(result, 'The request could not be completed.');
    if ((options.method || 'GET').toUpperCase() !== 'GET') window.showSnackbar(result);
    return result.data ?? result;
}

async function load() {
    try {
        const records = await apiRequest(window.API);
        const columns = window.DISPLAY_FIELDS || window.FIELDS;
        crudRows.replaceChildren();
        if (!Array.isArray(records) || records.length === 0) {
            const row = document.createElement('tr');
            const cell = document.createElement('td');
            cell.colSpan = columns.length + 1;
            cell.className = 'text-center text-muted';
            cell.textContent = 'No records found.';
            row.appendChild(cell);
            crudRows.appendChild(row);
            return;
        }
        records.forEach(record => {
            const row = document.createElement('tr');
            row.dataset.id = record.id;
            columns.forEach(field => {
                const cell = document.createElement('td');
                const meetingUrl = field === 'location' && record.mode === 'ONLINE' ? record.meetingUrl : record[field];
                if ((field === 'meetingUrl' || field === 'videoUrl' || (field === 'location' && record.mode === 'ONLINE')) && meetingUrl) {
                    const link = document.createElement('a');
                    link.href = meetingUrl;
                    link.target = '_blank';
                    link.rel = 'noopener noreferrer';
                    link.textContent = field === 'videoUrl' ? 'Watch lesson' : 'Join meeting';
                    cell.appendChild(link);
                } else {
                    cell.textContent = record[field] ?? '';
                }
                row.appendChild(cell);
            });
            const actions = document.createElement('td');
            const editButton = document.createElement('button');
            editButton.type = 'button'; editButton.className = 'btn btn-sm btn-outline-primary me-1'; editButton.textContent = 'Edit';
            editButton.addEventListener('click', () => edit(record.id));
            const deleteButton = document.createElement('button');
            deleteButton.type = 'button'; deleteButton.className = 'btn btn-sm btn-outline-danger'; deleteButton.textContent = 'Delete';
            deleteButton.addEventListener('click', () => del(record.id));
            actions.append(editButton, deleteButton);
            row.appendChild(actions);
            crudRows.appendChild(row);
        });
    } catch (error) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = (window.DISPLAY_FIELDS || window.FIELDS).length + 1;
        cell.className = 'text-center text-danger';
        cell.textContent = error.message;
        row.appendChild(cell);
        crudRows.replaceChildren(row);
        window.showSnackbar(error);
    }
}

async function edit(id) {
    try {
        const record = await apiRequest(`${window.API}/${encodeURIComponent(id)}`);
        window.FIELDS.forEach(field => {
            const element = document.getElementById(field);
            if (element) element.value = record[field] ?? '';
        });
        crudForm.dataset.id = id;
        crudForm.querySelector('button[type="submit"], button:not([type])').textContent = 'Update';
    } catch (error) { window.showSnackbar(error); }
}

crudForm.addEventListener('submit', async event => {
    event.preventDefault();
    const payload = {};
    window.FIELDS.forEach(field => {
        const element = document.getElementById(field);
        if (!element) return;
        if (element.type === 'checkbox') payload[field] = element.checked;
        else if (numericFields.has(field)) payload[field] = element.value === '' ? null : Number(element.value);
        else payload[field] = element.value === '' ? null : element.value;
    });
    try {
        const recordId = crudForm.dataset.id;
        await apiRequest(window.API + (recordId ? `/${encodeURIComponent(recordId)}` : ''), {
            method: recordId ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        crudForm.reset();
        delete crudForm.dataset.id;
        crudForm.querySelector('button[type="submit"], button:not([type])').textContent = 'Add';
        await load();
    } catch (error) { window.showSnackbar(error); }
});

async function del(id) {
    if (!confirm('Delete this record?')) return;
    try {
        await apiRequest(`${window.API}/${encodeURIComponent(id)}`, { method: 'DELETE' });
        await load();
    } catch (error) { window.showSnackbar(error); }
}

load();
