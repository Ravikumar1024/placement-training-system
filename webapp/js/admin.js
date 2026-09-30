// API call with Result class support
async function apiCall(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    return fetch(url, { ...options, headers });
}

// Show message helper
function showMessage(elementId, message, type) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.textContent = message;
    el.className = `alert alert-${type} mt-3`;
    el.classList.remove('d-none');
    setTimeout(() => el.classList.add('d-none'), 3000);
}

function showToast(message, type = 'success') {
    const region = document.getElementById('toastRegion');
    if (!region) return;

    const variant = type === 'error' ? 'error' : 'success';
    const toast = document.createElement('div');
    toast.className = `admin-toast admin-toast-${variant}`;
    toast.setAttribute('role', variant === 'error' ? 'alert' : 'status');

    const icon = document.createElement('i');
    icon.className = `fas ${variant === 'error' ? 'fa-circle-exclamation' : 'fa-circle-check'}`;
    icon.setAttribute('aria-hidden', 'true');

    const text = document.createElement('span');
    text.className = 'admin-toast-message';
    text.textContent = message;

    const dismiss = document.createElement('button');
    dismiss.type = 'button';
    dismiss.className = 'admin-toast-dismiss';
    dismiss.setAttribute('aria-label', 'Dismiss notification');
    dismiss.innerHTML = '<i class="fas fa-xmark" aria-hidden="true"></i>';

    let removeTimer;
    const dismissToast = () => {
        if (toast.dataset.dismissed) return;
        toast.dataset.dismissed = 'true';
        window.clearTimeout(removeTimer);
        toast.classList.remove('is-visible');
        window.setTimeout(() => toast.remove(), 220);
    };

    dismiss.addEventListener('click', dismissToast);
    toast.append(icon, text, dismiss);
    region.appendChild(toast);
    requestAnimationFrame(() => toast.classList.add('is-visible'));
    removeTimer = window.setTimeout(dismissToast, 4500);
}

function validateStudentLength() {
    const fields = [
        { id: 'studentPhone', label: 'Phone number', maxLength: 15 },
        { id: 'studentDepartment', label: 'Department name', maxLength: 50 }
    ];
    const invalidFields = fields.filter(field => document.getElementById(field.id).value.length > field.maxLength);

    if (invalidFields.length === 0) return '';

    document.getElementById(invalidFields[0].id).focus();
    return invalidFields.map(field => `${field.label} must be at most ${field.maxLength} characters.`).join(' ');
}

function getApiErrorMessage(result, fallback) {
    if (result.errors && typeof result.errors === 'object') {
        const fieldErrors = Object.entries(result.errors)
            .map(([field, message]) => `${field.charAt(0).toUpperCase() + field.slice(1)}: ${message}`)
            .join('. ');
        if (fieldErrors) return fieldErrors;
    }
    return result.message || fallback;
}

async function loadAdminDashboard() {
    try {
        const response = await fetch('/api/dashboard');
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Unable to load dashboard.');
        const dashboard = result.data || result;
        if (dashboard.role !== 'ADMIN') throw new Error('Admin dashboard data is unavailable.');
        Object.entries(dashboard.stats || {}).forEach(([key, value]) => {
            const counter = document.getElementById(key);
            if (counter) counter.textContent = value;
        });
    } catch (error) {
        console.error('Error loading admin dashboard:', error);
    }
}

// Count data from API
async function count(url, id) {
    try {
        const r = await fetch(url);
        const result = await r.json();
        const data = result.data || result;
        document.getElementById(id).textContent = Array.isArray(data) ? data.length : (data.count || 0);
    } catch (err) {
        console.error('Error counting:', err);
    }
}

// Load students data
async function loadStudents() {
    try {
        const r = await fetch('/api/students');
        const result = await r.json();
        const students = result.data || result;
        const table = document.getElementById('studentsTable');
        table.innerHTML = '';
        if (Array.isArray(students) && students.length > 0) {
            students.forEach(student => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td class="text-start">${student.id || ''}</td>
                    <td class="text-start">${student.name || ''}</td>
                    <td class="text-start">${student.email || ''}</td>
                    <td class="text-start">${student.phone || ''}</td>
                    <td class="text-start">${student.department || ''}</td>
                    <td class="text-start">${student.batch || ''}</td>
                    <td class="text-start">${student.cgpa || ''}</td>
                    <td class="text-start">
                        <div class="d-flex gap-2">
                            <button class="btn btn-sm btn-primary" onclick="editStudent('${student.id}')">Edit</button>
                            <button class="btn btn-sm btn-danger" onclick="deleteStudent('${student.id}')">Delete</button>
                        </div>
                    </td>
                `;
                table.appendChild(row);
            });
        } else {
            table.innerHTML = '<tr><td colspan="8" class="text-center">No students found</td></tr>';
        }
    } catch (err) {
        console.error('Error loading students:', err);
        document.getElementById('studentsTable').innerHTML = '<tr><td colspan="8" class="text-center text-danger">Error loading students</td></tr>';
    }
}

// Save student
async function saveStudent() {
    const validationMessage = validateStudentLength();
    if (validationMessage) {
        showToast(validationMessage, 'error');
        return;
    }

    const student = {
        code: document.getElementById('studentCode').value.trim(),
        name: document.getElementById('studentName').value,
        email: document.getElementById('studentEmail').value,
        phone: document.getElementById('studentPhone').value,
        department: document.getElementById('studentDepartment').value,
        batch: document.getElementById('studentBatch').value,
        cgpa: document.getElementById('studentCgpa').value,
        backlogs: document.getElementById('studentBacklogs').value,
        skills: document.getElementById('studentSkills').value
    };
    
    try {
        const r = await fetch('/api/students', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(student)
        });
        const result = await r.json();
        
        if (r.ok) {
            bootstrap.Modal.getInstance(document.getElementById('studentFormModal')).hide();
            document.getElementById('studentForm').reset();
            loadStudents();
            showToast('Student saved successfully!');
        } else {
            showToast(getApiErrorMessage(result, 'Failed to save student'), 'error');
        }
    } catch (err) {
        console.error('Error saving student:', err);
        showToast('Error saving student', 'error');
    }
}

// Edit student
async function editStudent(id) {
    try {
        const r = await fetch(`/api/students/${id}`);
        const result = await r.json();
        const student = result.data || result;
        
        document.getElementById('studentCode').value = student.code || '';
        document.getElementById('studentName').value = student.name || '';
        document.getElementById('studentEmail').value = student.email || '';
        document.getElementById('studentPhone').value = student.phone || '';
        document.getElementById('studentDepartment').value = student.department || '';
        document.getElementById('studentBatch').value = student.batch || '';
        document.getElementById('studentCgpa').value = student.cgpa || '';
        document.getElementById('studentBacklogs').value = student.backlogs || '';
        document.getElementById('studentSkills').value = student.skills || '';
        
        new bootstrap.Modal(document.getElementById('studentFormModal')).show();
        
        // Override saveStudent to use PUT
        window.currentEditId = id;
        window.saveStudent = async function() {
            const validationMessage = validateStudentLength();
            if (validationMessage) {
                showToast(validationMessage, 'error');
                return;
            }

            const student = {
                code: document.getElementById('studentCode').value.trim(),
                name: document.getElementById('studentName').value,
                email: document.getElementById('studentEmail').value,
                phone: document.getElementById('studentPhone').value,
                department: document.getElementById('studentDepartment').value,
                batch: document.getElementById('studentBatch').value,
                cgpa: document.getElementById('studentCgpa').value,
                backlogs: document.getElementById('studentBacklogs').value,
                skills: document.getElementById('studentSkills').value
            };
            
            try {
                const r = await fetch(`/api/students/${id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(student)
                });
                const result = await r.json();
                
                if (r.ok) {
                    bootstrap.Modal.getInstance(document.getElementById('studentFormModal')).hide();
                    document.getElementById('studentForm').reset();
                    loadStudents();
                    showToast('Student updated successfully!');
                    // Restore original saveStudent
                    window.saveStudent = saveStudent;
                } else {
                    showToast(getApiErrorMessage(result, 'Failed to update student'), 'error');
                }
            } catch (err) {
                console.error('Error updating student:', err);
                showToast('Error updating student', 'error');
            }
        };
    } catch (err) {
        console.error('Error loading student:', err);
        showToast('Error loading student', 'error');
    }
}

// Delete student
async function deleteStudent(id) {
    if (!confirm('Are you sure you want to delete this student?')) return;
    
    try {
        const r = await fetch(`/api/students/${id}`, { method: 'DELETE' });
        if (r.ok) {
            loadStudents();
            showToast('Student deleted successfully!');
        } else {
            showToast('Failed to delete student', 'error');
        }
    } catch (err) {
        console.error('Error deleting student:', err);
        showToast('Error deleting student', 'error');
    }
}

// Load companies data
async function loadCompanies() {
    try {
        const r = await fetch('/api/companies');
        const result = await r.json();
        const companies = result.data || result;
        const table = document.getElementById('companiesTable');
        table.innerHTML = '';
        if (Array.isArray(companies) && companies.length > 0) {
            companies.forEach(company => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td class="text-start">${company.id || ''}</td>
                    <td class="text-start">${company.companyName || ''}</td>
                    <td class="text-start">${company.jobRole || ''}</td>
                    <td class="text-start">${company.packageLpa || ''}</td>
                    <td class="text-start">${company.minCgpa || ''}</td>
                    <td class="text-start">
                        <div class="d-flex gap-2">
                            <button class="btn btn-sm btn-primary" onclick="editCompany('${company.id}')">Edit</button>
                            <button class="btn btn-sm btn-danger" onclick="deleteCompany('${company.id}')">Delete</button>
                        </div>
                    </td>
                `;
                table.appendChild(row);
            });
        } else {
            table.innerHTML = '<tr><td colspan="6" class="text-center">No companies found</td></tr>';
        }
    } catch (err) {
        console.error('Error loading companies:', err);
        document.getElementById('companiesTable').innerHTML = '<tr><td colspan="6" class="text-center text-danger">Error loading companies</td></tr>';
    }
}

// Save company
async function saveCompany() {
    const company = {
        companyName: document.getElementById('companyName').value,
        jobRole: document.getElementById('companyJobRole').value,
        packageLpa: document.getElementById('companyPackage').value,
        minCgpa: document.getElementById('companyMinCgpa').value,
        maxBacklogs: document.getElementById('companyMaxBacklogs').value
    };
    
    try {
        const r = await fetch('/api/companies', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(company)
        });
        const result = await r.json();
        
        if (r.ok) {
            bootstrap.Modal.getInstance(document.getElementById('companyFormModal')).hide();
            document.getElementById('companyForm').reset();
            loadCompanies();
            showToast('Company saved successfully!');
        } else {
            showToast(result.message || 'Failed to save company', 'error');
        }
    } catch (err) {
        console.error('Error saving company:', err);
        showToast('Error saving company', 'error');
    }
}

// Edit company
async function editCompany(id) {
    try {
        const r = await fetch(`/api/companies/${id}`);
        const result = await r.json();
        const company = result.data || result;
        
        document.getElementById('companyName').value = company.companyName || '';
        document.getElementById('companyJobRole').value = company.jobRole || '';
        document.getElementById('companyPackage').value = company.packageLpa || '';
        document.getElementById('companyMinCgpa').value = company.minCgpa || '';
        document.getElementById('companyMaxBacklogs').value = company.maxBacklogs || '';
        
        new bootstrap.Modal(document.getElementById('companyFormModal')).show();
        
        window.currentEditId = id;
        window.saveCompany = async function() {
            const company = {
                companyName: document.getElementById('companyName').value,
                jobRole: document.getElementById('companyJobRole').value,
                packageLpa: document.getElementById('companyPackage').value,
                minCgpa: document.getElementById('companyMinCgpa').value,
                maxBacklogs: document.getElementById('companyMaxBacklogs').value
            };
            
            try {
                const r = await fetch(`/api/companies/${id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(company)
                });
                const result = await r.json();
                
                if (r.ok) {
                    bootstrap.Modal.getInstance(document.getElementById('companyFormModal')).hide();
                    document.getElementById('companyForm').reset();
                    loadCompanies();
                    showToast('Company updated successfully!');
                    window.saveCompany = saveCompany;
                } else {
                    showToast(result.message || 'Failed to update company', 'error');
                }
            } catch (err) {
                console.error('Error updating company:', err);
                showToast('Error updating company', 'error');
            }
        };
    } catch (err) {
        console.error('Error loading company:', err);
        showToast('Error loading company', 'error');
    }
}

// Delete company
async function deleteCompany(id) {
    if (!confirm('Are you sure you want to delete this company?')) return;
    
    try {
        const r = await fetch(`/api/companies/${id}`, { method: 'DELETE' });
        if (r.ok) {
            loadCompanies();
            showToast('Company deleted successfully!');
        } else {
            showToast('Failed to delete company', 'error');
        }
    } catch (err) {
        console.error('Error deleting company:', err);
        showToast('Error deleting company', 'error');
    }
}

// Load trainings data
async function loadTrainings() {
    try {
        const r = await fetch('/api/trainings');
        const result = await r.json();
        const trainings = result.data || result;
        const table = document.getElementById('trainingsTable');
        table.innerHTML = '';
        if (Array.isArray(trainings) && trainings.length > 0) {
            trainings.forEach(training => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td class="text-start">${training.id || ''}</td>
                    <td class="text-start">${training.trainingName || ''}</td>
                    <td class="text-start">${training.trainer || ''}</td>
                    <td class="text-start">${training.mode || 'ONSITE'}</td>
                    <td class="text-start">${training.mode === 'ONLINE' ? (training.meetingUrl ? `<a href="${training.meetingUrl}" target="_blank" rel="noopener noreferrer">Join meeting</a>` : 'Online') : (training.location || 'Location not set')}</td>
                    <td class="text-start">${training.videoUrl ? `<a href="${training.videoUrl}" target="_blank" rel="noopener noreferrer">Watch lesson</a>` : 'Not set'}</td>
                    <td class="text-start">${training.startDate || ''}</td>
                    <td class="text-start">${training.endDate || ''}</td>
                    <td class="text-start">
                        <div class="d-flex gap-2">
                            <button class="btn btn-sm btn-primary" onclick="editTraining('${training.id}')">Edit</button>
                            <button class="btn btn-sm btn-danger" onclick="deleteTraining('${training.id}')">Delete</button>
                        </div>
                    </td>
                `;
                table.appendChild(row);
            });
        } else {
            table.innerHTML = '<tr><td colspan="9" class="text-center">No trainings found</td></tr>';
        }
    } catch (err) {
        console.error('Error loading trainings:', err);
        document.getElementById('trainingsTable').innerHTML = '<tr><td colspan="9" class="text-center text-danger">Error loading trainings</td></tr>';
    }
}

// Save training
async function saveTraining() {
    const training = {
        trainingName: document.getElementById('trainingName').value,
        trainer: document.getElementById('trainingTrainer').value,
        mode: document.getElementById('trainingMode').value,
        location: document.getElementById('trainingLocation').value,
        meetingUrl: document.getElementById('trainingMeetingUrl').value,
        videoUrl: document.getElementById('trainingVideoUrl').value,
        startDate: document.getElementById('trainingStartDate').value,
        endDate: document.getElementById('trainingEndDate').value
    };
    
    try {
        const r = await fetch('/api/trainings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(training)
        });
        const result = await r.json();
        
        if (r.ok) {
            bootstrap.Modal.getInstance(document.getElementById('trainingFormModal')).hide();
            document.getElementById('trainingForm').reset();
            loadTrainings();
            showToast('Training saved successfully!');
        } else {
            showToast(result.message || 'Failed to save training', 'error');
        }
    } catch (err) {
        console.error('Error saving training:', err);
        showToast('Error saving training', 'error');
    }
}

// Edit training
async function editTraining(id) {
    try {
        const r = await fetch(`/api/trainings/${id}`);
        const result = await r.json();
        const training = result.data || result;
        
        document.getElementById('trainingName').value = training.trainingName || '';
        document.getElementById('trainingTrainer').value = training.trainer || '';
        document.getElementById('trainingMode').value = training.mode || 'ONSITE';
        document.getElementById('trainingLocation').value = training.location || '';
        document.getElementById('trainingMeetingUrl').value = training.meetingUrl || '';
        document.getElementById('trainingVideoUrl').value = training.videoUrl || '';
        document.getElementById('trainingStartDate').value = training.startDate || '';
        document.getElementById('trainingEndDate').value = training.endDate || '';
        
        new bootstrap.Modal(document.getElementById('trainingFormModal')).show();
        
        window.currentEditId = id;
        window.saveTraining = async function() {
            const training = {
                trainingName: document.getElementById('trainingName').value,
                trainer: document.getElementById('trainingTrainer').value,
                mode: document.getElementById('trainingMode').value,
                location: document.getElementById('trainingLocation').value,
                meetingUrl: document.getElementById('trainingMeetingUrl').value,
                videoUrl: document.getElementById('trainingVideoUrl').value,
                startDate: document.getElementById('trainingStartDate').value,
                endDate: document.getElementById('trainingEndDate').value
            };
            
            try {
                const r = await fetch(`/api/trainings/${id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(training)
                });
                const result = await r.json();
                
                if (r.ok) {
                    bootstrap.Modal.getInstance(document.getElementById('trainingFormModal')).hide();
                    document.getElementById('trainingForm').reset();
                    loadTrainings();
                    showToast('Training updated successfully!');
                    window.saveTraining = saveTraining;
                } else {
                    showToast(result.message || 'Failed to update training', 'error');
                }
            } catch (err) {
                console.error('Error updating training:', err);
                showToast('Error updating training', 'error');
            }
        };
    } catch (err) {
        console.error('Error loading training:', err);
        showToast('Error loading training', 'error');
    }
}

// Delete training
async function deleteTraining(id) {
    if (!confirm('Are you sure you want to delete this training?')) return;
    
    try {
        const r = await fetch(`/api/trainings/${id}`, { method: 'DELETE' });
        if (r.ok) {
            loadTrainings();
            showToast('Training deleted successfully!');
        } else {
            showToast('Failed to delete training', 'error');
        }
    } catch (err) {
        console.error('Error deleting training:', err);
        showToast('Error deleting training', 'error');
    }
}

// Load placements data
async function loadPlacements() {
    try {
        const r = await fetch('/api/placements');
        const result = await r.json();
        const placements = result.data || result;
        const table = document.getElementById('placementsTable');
        table.innerHTML = '';
        if (Array.isArray(placements) && placements.length > 0) {
            placements.forEach(p => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td class="text-start">${p.id || ''}</td>
                    <td class="text-start">${p.student?.name || ''}</td>
                    <td class="text-start">${p.company?.companyName || ''}</td>
                    <td class="text-start">${p.company?.jobRole || ''}</td>
                    <td class="text-start">${p.company?.packageLpa || ''}</td>
                    <td class="text-start"><span class="badge bg-${getBadgeColor(p.status)}">${p.status || ''}</span></td>
                    <td class="text-start">${p.appliedDate || ''}</td>
                `;
                table.appendChild(row);
            });
        } else {
            table.innerHTML = '<tr><td colspan="7" class="text-center">No placements found</td></tr>';
        }
    } catch (err) {
        console.error('Error loading placements:', err);
        document.getElementById('placementsTable').innerHTML = '<tr><td colspan="7" class="text-center text-danger">Error loading placements</td></tr>';
    }
}

async function generateEligiblePlacements() {
    try {
        const response = await fetch('/api/placements/generate-eligible', { method: 'POST' });
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || 'Unable to generate placements.');
        const summary = result.data || result;
        await loadPlacements();
        showToast(`Created ${summary.created} placement records; skipped ${summary.skipped} existing eligible records.`);
    } catch (error) {
        showToast(error.message || 'Unable to generate placements.', 'error');
    }
}

// Load attendance data
async function loadAttendance() {
    try {
        const r = await fetch('/api/attendances');
        const result = await r.json();
        const attendance = result.data || result;
        const table = document.getElementById('attendanceTable');
        table.innerHTML = '';
        if (Array.isArray(attendance) && attendance.length > 0) {
            attendance.forEach(a => {
                const row = document.createElement('tr');
                const present = a.present ?? a.isPresent;
                row.innerHTML = `
                    <td class="text-start">${a.id || ''}</td>
                    <td class="text-start">${a.student?.name || ''}</td>
                    <td class="text-start">${a.training?.trainingName || ''}</td>
                    <td class="text-start">${a.attendanceDate || a.date || ''}</td>
                    <td class="text-start"><span class="badge bg-${getAttendanceColor(present)}">${present ? 'Present' : 'Absent'}</span></td>
                `;
                table.appendChild(row);
            });
        } else {
            table.innerHTML = '<tr><td colspan="5" class="text-center">No attendance records found</td></tr>';
        }
    } catch (err) {
        console.error('Error loading attendance:', err);
        document.getElementById('attendanceTable').innerHTML = '<tr><td colspan="5" class="text-center text-danger">Error loading attendance</td></tr>';
    }
}

// Helper function for placement status badge color
function getBadgeColor(status) {
    const colors = {
        'APPLIED': 'secondary',
        'APTITUDE_CLEARED': 'info',
        'TECHNICAL_ROUND': 'warning',
        'HR_ROUND': 'primary',
        'SELECTED': 'success',
        'REJECTED': 'danger'
    };
    return colors[status] || 'secondary';
}

// Helper function for attendance badge color
function getAttendanceColor(isPresent) {
    return isPresent ? 'success' : 'danger';
}

// Initialize dashboard on load
document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    const mobileSidebar = window.matchMedia('(max-width: 992px)');
    const toggleSidebar = document.getElementById('toggleSidebar');
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

    mobileSidebar.addEventListener('change', event => {
        setSidebarOpen(!event.matches);
    });

    loadAdminDashboard();
    
    // Sidebar navigation
    document.querySelectorAll('[data-page]').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            document.querySelectorAll('[data-page]').forEach(l => l.classList.remove('active'));
            this.classList.add('active');
            showPage(this.dataset.page);

            if (mobileSidebar.matches) {
                setSidebarOpen(false);
            }
            
            // Load data when corresponding page is shown
            if (this.dataset.page === 'dashboard') loadAdminDashboard();
            if (this.dataset.page === 'students') loadStudents();
            if (this.dataset.page === 'companies') loadCompanies();
            if (this.dataset.page === 'trainings') loadTrainings();
            if (this.dataset.page === 'placements') loadPlacements();
            if (this.dataset.page === 'attendance') loadAttendance();
        });
    });

    // Toggle sidebar
    toggleSidebar.addEventListener('click', () => {
        setSidebarOpen(sidebar.classList.contains('collapsed'));
    });
    backdrop.addEventListener('click', () => setSidebarOpen(false));
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && mobileSidebar.matches && !sidebar.classList.contains('collapsed')) {
            setSidebarOpen(false);
            toggleSidebar.focus();
        }
    });
});

function showPage(page) {
    document.getElementById('dashboardView').style.display = page === 'dashboard' ? 'block' : 'none';
    document.getElementById('studentsView').style.display = page === 'students' ? 'block' : 'none';
    document.getElementById('companiesView').style.display = page === 'companies' ? 'block' : 'none';
    document.getElementById('trainingsView').style.display = page === 'trainings' ? 'block' : 'none';
    document.getElementById('placementsView').style.display = page === 'placements' ? 'block' : 'none';
    document.getElementById('attendanceView').style.display = page === 'attendance' ? 'block' : 'none';
    const pageTitles = {
        dashboard: 'Placement overview',
        students: 'Students',
        companies: 'Companies',
        trainings: 'Trainings',
        placements: 'Placements',
        attendance: 'Attendance'
    };
    document.getElementById('pageTitle').textContent = pageTitles[page] || 'Placement overview';
}

function showModal(modalId) {
    new bootstrap.Modal(document.getElementById(modalId)).show();
}

async function logout() {
    try { await fetch('/api/auth/logout', { method: 'POST' }); } catch (err) { console.error('Error ending admin session:', err); }
    localStorage.removeItem('user');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');
    localStorage.removeItem('studentId');
    location.href = '/';
}
