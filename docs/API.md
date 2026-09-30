# API Documentation - Placement Training & Tracking System

## Base URL
```
http://localhost:8080/api
```

---

## Authentication

### POST /api/auth/login
Login to the system and receive authentication response.

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "userId": 1,
    "username": "admin",
    "role": "ADMIN",
    "studentId": null
  }
}
```

**Error Response (401):**
```json
{
  "code": 401,
  "message": "Unauthorized: Invalid username or password",
  "data": null
}
```

---

## Student Management

### GET /api/students
Retrieve all students.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Students retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john@college.com",
      "phone": "9876543210",
      "department": "CSE",
      "batch": "2026",
      "cgpa": 8.5,
      "backlogs": 0,
      "skills": "Java, Python, SQL"
    }
  ]
}
```

### GET /api/students/{id}
Retrieve a specific student by ID.

**Path Parameters:**
- `id` - Student ID (required)

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Student retrieved successfully",
  "data": { ...student object... }
}
```

**Error Response (404):**
```json
{
  "code": 404,
  "message": "Student not found: 999",
  "data": null
}
```

### POST /api/students
Create a new student.

**Request Body:**
```json
{
  "name": "Jane Doe",
  "email": "jane@college.com",
  "phone": "9876543211",
  "department": "CSE",
  "batch": "2026",
  "cgpa": 8.0,
  "backlogs": 0,
  "skills": "Java, React"
}
```

**Validation Rules:**
- `name`: Required, min 2 chars, max 100 chars
- `email`: Required, valid email format, unique
- `phone`: Optional, max 15 chars
- `department`: Optional, max 50 chars
- `batch`: Optional, max 10 chars
- `cgpa`: Optional, 0.0 - 10.0
- `backlogs`: Optional, 0 - 10
- `skills`: Optional, max 500 chars

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Student created successfully",
  "data": { ...saved student... }
}
```

**Error Response (400):**
```json
{
  "code": 400,
  "message": "Email already exists: jane@college.com",
  "data": null
}
```

### PUT /api/students/{id}
Update an existing student.

**Path Parameters:**
- `id` - Student ID (required)

**Request Body:**
```json
{
  "name": "Jane Smith",
  "cgpa": 8.5,
  "skills": "Java, React, Spring"
}
```

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Student updated successfully",
  "data": { ...updated student... }
}
```

### DELETE /api/students/{id}
Delete a student.

**Path Parameters:**
- `id` - Student ID (required)

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Student deleted successfully",
  "data": null
}
```

---

## Company Management

### GET /api/companies
Retrieve all companies.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Companies retrieved successfully",
  "data": [
    {
      "id": 1,
      "companyName": "Tech Corp",
      "jobRole": "Software Developer",
      "packageLpa": 6.5,
      "minCgpa": 7.0,
      "maxBacklogs": 2,
      "minAptitudeScore": 60.0,
      "minAttendance": 75.0,
      "eligibleDepartments": "CSE,IT",
      "requiredSkills": "Java, SQL"
    }
  ]
}
```

### GET /api/companies/{id}
Retrieve a specific company by ID.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Company retrieved successfully",
  "data": { ...company object... }
}
```

### POST /api/companies
Create a new company.

**Request Body:**
```json
{
  "companyName": "Finance Ltd",
  "jobRole": "Financial Analyst",
  "packageLpa": 5.0,
  "minCgpa": 7.5,
  "maxBacklogs": 1,
  "minAptitudeScore": 65.0,
  "minAttendance": 80.0,
  "eligibleDepartments": "CSE,IT,BCom",
  "requiredSkills": "Excel, SQL"
}
```

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Company created successfully",
  "data": { ...saved company... }
}
```

### PUT /api/companies/{id}
Update an existing company.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Company updated successfully",
  "data": { ...updated company... }
}
```

### DELETE /api/companies/{id}
Delete a company.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Company deleted successfully",
  "data": null
}
```

---

## Training Management

### GET /api/trainings
Retrieve all training programs.

Training records also include `mode` (`ONSITE` or `ONLINE`), `location` for physical venues, `meetingUrl` for online sessions, and an optional YouTube `videoUrl` lesson. Students see assigned lesson videos in their Trainings tab.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Trainings retrieved successfully",
  "data": [
    {
      "id": 1,
      "trainingName": "Java Advanced",
      "description": "Advanced Java programming course",
      "trainer": "Dr. Smith",
      "startDate": "2024-01-01",
      "endDate": "2024-03-31"
    }
  ]
}
```

### GET /api/trainings/{id}
Retrieve a specific training by ID.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Training retrieved successfully",
  "data": { ...training object... }
}
```

### POST /api/trainings
Create a new training.

**Request Body:**
```json
{
  "trainingName": "Python Programming",
  "description": "Python for Data Science",
  "trainer": "Prof. Johnson",
  "startDate": "2024-04-01",
  "endDate": "2024-06-30"
}
```

**Validation Rules:**
- `trainingName`: Required, min 2 chars, max 100 chars
- `description`: Optional, max 500 chars
- `trainer`: Optional, max 100 chars
- `startDate`: Required
- `endDate`: Optional

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Training created successfully",
  "data": { ...saved training... }
}
```

### PUT /api/trainings/{id}
Update an existing training.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Training updated successfully",
  "data": { ...updated training... }
}
```

### DELETE /api/trainings/{id}
Delete a training.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Training deleted successfully",
  "data": null
}
```

---

## Attendance Management

### GET /api/attendances
Retrieve all attendance records.

This endpoint is admin-only. Students retrieve their own records from `GET /api/attendances/student/{studentId}`; the server verifies the requested student matches the authenticated account.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Attendance records retrieved successfully",
  "data": [
    {
      "id": 1,
      "student": { "id": 1, "name": "John Doe" },
      "training": { "id": 1, "trainingName": "Java Advanced" },
      "date": "2024-01-01",
      "present": true
    }
  ]
}
```

### GET /api/attendances/{id}
Retrieve a specific attendance record by ID.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Attendance record retrieved successfully",
  "data": { ...attendance object... }
}
```

### POST /api/attendances
Create a new attendance record.

**Request Body:**
```json
{
  "student": { "id": "student-uuid" },
  "training": { "id": "training-uuid" },
  "date": "2024-01-01",
  "present": true
}
```

### POST /api/attendances/bulk
Create or update attendance for a training session and date. Existing student/training/date records are updated.

```json
{
  "trainingId": "training-uuid",
  "date": "2024-01-01",
  "entries": [
    { "studentId": "student-uuid-1", "present": true },
    { "studentId": "student-uuid-2", "present": false }
  ]
}
```

### POST /api/trainings/{id}/video-completion
Student-only endpoint called after the embedded YouTube player reports at least 90% watched. The server derives the student from the authenticated session and records Present for the server's current date, only while the training is in its configured date range. A repeated completion on the same day updates the existing attendance row. Playback progress is client-reported and is not tamper-proof proctoring.

**Validation Rules:**
- `student`: Required (reference to student)
- `training`: Required (reference to training)
- `date`: Required
- `present`: Required (boolean)

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Attendance record created successfully",
  "data": { ...saved attendance... }
}
```

### PUT /api/attendances/{id}
Update an existing attendance record.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Attendance record updated successfully",
  "data": { ...updated attendance... }
}
```

### DELETE /api/attendances/{id}
Delete an attendance record.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Attendance record deleted successfully",
  "data": null
}
```

---

## Aptitude Tests

Login with `POST /api/auth/login` first. The response establishes a server-side session cookie. Test creation/question management and score maintenance require an ADMIN session; submissions and score history require a STUDENT session. Student submissions and history are restricted to the student linked to that session.

The admin question page can import up to 10 multiple-choice questions from Open Trivia Database. The test name selects a matching category for common topics (for example, math and computers); unmatched names use General Knowledge. Imported questions should be reviewed before publishing. Open Trivia Database content is attributed under CC BY-SA 4.0.

### GET /api/aptitude-tests
Retrieve all aptitude tests.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Aptitude tests retrieved successfully",
  "data": [
    {
      "id": "test-uuid",
      "testName": "Quantitative Ability",
      "testDate": "2024-01-15",
      "totalMarks": 100.0,
      "questionCount": 10
    }
  ]
}
```

### GET /api/aptitude-tests/{id}
Retrieve a specific aptitude test.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Aptitude test retrieved successfully",
  "data": { ...test object... }
}
```

### POST /api/aptitude-tests
Create a new aptitude test.

**Request Body:**
```json
{
  "testName": "Logical Reasoning",
  "testDate": "2024-02-01",
  "totalMarks": 100.0
}
```

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Aptitude test created successfully",
  "data": { ...saved test... }
}
```

### DELETE /api/aptitude-tests/{id}
Delete an aptitude test. A test with scores or questions cannot be deleted; remove its questions first, and preserve test history by removing scores before deleting.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Aptitude test deleted successfully",
  "data": null
}
```

### GET /api/aptitude-tests/{id}/questions
Retrieve student-safe questions. Correct answers are never included.

### GET /api/aptitude-tests/{id}/questions/manage
Retrieve questions with answer keys for the admin authoring screen.

### POST /api/aptitude-tests/{id}/questions
Add a multiple-choice question. `correctOption` must be `A`, `B`, `C`, or `D`; `marks` must be positive.

```json
{
  "prompt": "What is 2 + 2?",
  "optionA": "3",
  "optionB": "4",
  "optionC": "5",
  "optionD": "6",
  "correctOption": "B",
  "marks": 1.0
}
```

Questions can be updated with `PUT /api/aptitude-tests/{id}/questions/{questionId}` and deleted with `DELETE` on the same path.

### POST /api/aptitude-tests/{id}/submit
Grade the submitted answers on the server and persist the percentage as an aptitude score. Every question must be answered exactly once. A student can submit a given test only once; later submissions return a conflict response.

```json
{
  "studentId": "student-uuid",
  "answers": [
    { "questionId": "question-uuid", "selectedOption": "B" }
  ]
}
```

The response includes `earnedMarks`, `totalMarks`, and normalized `score` (0-100). The normalized percentage is used by eligibility checks.

### GET /api/aptitude-tests/{id}/review
Retrieve the completed student's saved questions, selected options, correct options, and score. The server returns this only after that authenticated student has completed the test; the answer key is not included in the available-questions endpoint.

---

## Aptitude Scores

### GET /api/aptitude-scores
Retrieve all aptitude scores.

### GET /api/aptitude-scores/student/{studentId}
Retrieve a student's score history, newest test date first.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Aptitude scores retrieved successfully",
  "data": [
    {
      "id": 1,
      "student": { "id": 1, "name": "John Doe" },
      "test": { "id": 1, "testName": "Quantitative Ability" },
      "score": 85.5
    }
  ]
}
```

### GET /api/aptitude-scores/{id}
Retrieve a specific aptitude score.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Aptitude score retrieved successfully",
  "data": { ...score object... }
}
```

### POST /api/aptitude-scores
Record an aptitude score.

**Request Body:**
```json
{
  "student": { "id": "student-uuid" },
  "test": { "id": "test-uuid" },
  "score": 85.5
}
```

**Validation Rules:**
- `score`: 0.0 - 100.0

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Aptitude score recorded successfully",
  "data": { ...saved score... }
}
```

### PUT /api/aptitude-scores/{id}
Update an aptitude score.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Aptitude score updated successfully",
  "data": { ...updated score... }
}
```

### DELETE /api/aptitude-scores/{id}
Delete an aptitude score.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Aptitude score deleted successfully",
  "data": null
}
```

---

## Eligibility

### GET /api/eligibility/{studentId}
Check company eligibility for a student based on their profile, aptitude scores, and attendance.

**Path Parameters:**
- `studentId` - Student ID (required)

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Eligibility check completed successfully",
  "data": [
    {
      "companyId": 1,
      "companyName": "Tech Corp",
      "eligible": true,
      "reasons": []
    },
    {
      "companyId": 2,
      "companyName": "Finance Ltd",
      "eligible": false,
      "reasons": ["CGPA below requirement", "Backlogs exceed limit"]
    }
  ]
}
```

---

## Placement Management

### GET /api/placements
Retrieve all placements.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Placements retrieved successfully",
  "data": [
    {
      "id": 1,
      "student": { "id": 1, "name": "John Doe" },
      "company": { "id": 1, "companyName": "Tech Corp" },
      "status": "SELECTED",
      "appliedDate": "2024-01-15",
      "placementDate": "2024-02-20"
    }
  ]
}
```

### GET /api/placements/student/{studentId}
Retrieve placement status for a specific student.

**Path Parameters:**
- `studentId` - Student ID (required)

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Placement status retrieved successfully",
  "data": [
    {
      "placementId": 1,
      "studentId": 1,
      "companyName": "Tech Corp",
      "jobRole": "Software Developer",
      "packageLpa": 6.5,
      "status": "SELECTED",
      "appliedDate": "2024-01-15",
      "placementDate": "2024-02-20"
    }
  ]
}
```

### POST /api/placements
Create a new placement application.

**Request Body:**
```json
{
  "studentId": 1,
  "companyId": 1,
  "status": "APPLIED",
  "appliedDate": "2024-01-15"
}
```

**Validation Rules:**
- `studentId`: Required, must exist
- `companyId`: Required, must exist
- `status`: Optional, defaults to APPLIED
- `appliedDate`: Optional, defaults to today
- `placementDate`: Optional

**Error Response (400):**
```json
{
  "code": 400,
  "message": "Placement already exists for this student and company",
  "data": null
}
```

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Placement created successfully",
  "data": { ...saved placement... }
}
```

### PUT /api/placements/{id}
Update a placement.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Placement updated successfully",
  "data": { ...updated placement... }
}
```

### DELETE /api/placements/{id}
Delete a placement.

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Placement deleted successfully",
  "data": null
}
```

---

## Status Values

### Placement Status
| Value | Description |
|-------|-------------|
| `APPLIED` | Initial application submitted |
| `APTITUDE_CLEARED` | Aptitude test passed |
| `TECHNICAL_ROUND` | Technical interview completed |
| `HR_ROUND` | HR interview completed |
| `SELECTED` | Offer accepted |
| `REJECTED` | Application rejected |

---

## Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400 | Bad Request | Invalid input or validation error |
| 401 | Unauthorized | Authentication required or invalid |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server error occurred |

---

## Response Format

All API responses follow this consistent format:

```json
{
  "code": 200,
  "message": "Success message describing the operation",
  "data": { /* operation-specific data or null */ }
}
```

### Success Responses
- `code`: 200
- `message`: Descriptive success message
- `data`: Response data (object, array, or null)

### Error Responses
- `code`: 400, 401, 403, 404, or 500
- `message`: Descriptive error message
- `data`: null