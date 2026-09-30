# Workflow Documentation - Placement Training & Tracking System

## System Workflow Overview

This document describes the business workflows for the Placement Training & Tracking System.

---

## 1. User Authentication Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                        USER AUTHENTICATION FLOW                     │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Landing   │────▶│   Login Page    │────▶│   Authentication│
│   Page      │     │   /login.html   │     │   /api/auth/login │
└─────────────┘     └─────────────────┘     └────────┬────────┘
                                                      │
                                        ┌─────────────┴─────────────┐
                                        │   Success / Error        │
                                        │                          │
                    ┌───────────────────┐     ┌───────────────────┐
                    │   ADMIN ROLE      │     │   STUDENT ROLE    │
                    │   /admin/...      │     │   /student/...    │
                    └───────────────────┘     └───────────────────┘
```

### Steps:

1. **User visits the application**
   - URL: `http://localhost:8080`
   - Redirected to landing page

2. **User clicks Login**
   - Redirected to `/webapp/login.html`

3. **User enters credentials**
   - Username: Required
   - Password: Required
   - Email format validation
   - Password field type: password

4. **POST /api/auth/login**
   ```json
   {
     "username": "admin",
     "password": "admin123"
   }
   ```

5. **Backend processes login**
   - Validates username exists in database
   - Validates password using BCrypt
   - Retrieves user role and student ID (if applicable)

6. **Response handling**
   - Success: Store user info in localStorage, redirect to role-specific dashboard
   - Error: Display error message on login page

---

## 2. Student Eligibility Checking Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
│                      STUDENT ELIGIBILITY WORKFLOW                   │
└─────────────────────────────────────────────────────────────────────┘

Student Dashboard
    │
    ├─▶ GET /api/students/{studentId}
    │   └─▶ Returns student profile
    │
    ├─▶ GET /api/aptitude-scores (filtered by student)
    │   └─▶ Calculates max aptitude score
    │
    ├─▶ GET /api/attendances (filtered by student)
    │   └─▶ Calculates attendance percentage
    │
    ├─▶ GET /api/companies
    │   └─▶ Retrieves all companies
    │
    └─▶ Eligibility Check (EligibilityUtil)
        └─▶ For each company:
            ├─▶ Check CGPA >= minCgpa
            ├─▶ Check Backlogs <= maxBacklogs
            ├─▶ Check Aptitude Score >= minAptitudeScore
            ├─▶ Check Attendance >= minAttendance
            ├─▶ Check Department in eligibleDepartments
            └─▶ Return eligibility result with reasons
```

### Eligibility Rules:

The [`EligibilityUtil`](../src/main/java/com/placement/util/EligibilityUtil.java) checks:

| Rule | Condition | Failure Reason |
|------|-----------|----------------|
| CGPA | `student.cgpa >= company.minCgpa` | "CGPA below requirement" |
| Backlogs | `student.backlogs <= company.maxBacklogs` | "Backlogs exceed limit" |
| Aptitude | `maxScore >= company.minAptitudeScore` | "Aptitude score below requirement" |
| Attendance | `attendance% >= company.minAttendance` | "Attendance below requirement" |
| Department | `student.department IN company.eligibleDepartments` | "Department not eligible" |

### Example API Call:
```
GET /api/eligibility/1
```

### Example Response:
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

## 3. Placement Application Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
│                       PLACEMENT APPLICATION FLOW                    │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ 1. Student Views Eligible Companies                                 │
└─────────────────────────────────────────────────────────────────────┘
            │
            ├─▶ GET /api/eligibility/{studentId}
            │   └─▶ Show companies where eligible=true
            │
            └─▶ Student selects a company to apply

┌─────────────────────────────────────────────────────────────────────┐
│ 2. Submit Placement Application                                     │
└─────────────────────────────────────────────────────────────────────┘
            │
            ├─▶ POST /api/placements
            │   {
            │     "studentId": 1,
            │     "companyId": 1,
            │     "status": "APPLIED"
            │   }
            │
            ├─▶ Backend validates:
            │   ├─ Student exists
            │   ├─ Company exists
            │   └─ No active placement for this student-company pair
            │
            └─▶ Creates placement with status=APPLIED

┌─────────────────────────────────────────────────────────────────────┐
│ 3. Placement Status Progression                                     │
└─────────────────────────────────────────────────────────────────────┘

APPLIED
    │
    ├─▶ After Aptitude Test
    │   PUT /api/placements/{id}
    │   { "status": "APTITUDE_CLEARED" }
    │
    ├─▶ After Technical Round
    │   PUT /api/placements/{id}
    │   { "status": "TECHNICAL_ROUND" }
    │
    ├─▶ After HR Round
    │   PUT /api/placements/{id}
    │   { "status": "HR_ROUND" }
    │
    ├─▶ Offer Accepted
    │   PUT /api/placements/{id}
    │   { "status": "SELECTED", "placementDate": "2024-02-20" }
    │
    └─▶ Offer Rejected
        PUT /api/placements/{id}
        { "status": "REJECTED" }
```

### Status Flow:
```
APPLIED → APTITUDE_CLEARED → TECHNICAL_ROUND → HR_ROUND → SELECTED
                                                    ↘ REJECTED
```

---

## 4. Training & Attendance Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
│                    TRAINING & ATTENDANCE WORKFLOW                   │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ 1. Admin Creates Training Program                                   │
└─────────────────────────────────────────────────────────────────────┘
            │
            ├─▶ POST /api/trainings
            │   {
            │     "trainingName": "Java Advanced",
            │     "description": "Advanced Java programming",
            │     "trainer": "Dr. Smith",
            │     "mode": "ONSITE",
            │     "location": "Computer Lab 2",
            │     "videoUrl": "https://www.youtube.com/watch?v=abcdefghijk",
            │     "startDate": "2024-01-01",
            │     "endDate": "2024-03-31"
            │   }

┌─────────────────────────────────────────────────────────────────────┐
│ 2. Admin Adds a Lesson Video and Takes Attendance                   │
└─────────────────────────────────────────────────────────────────────┘
            │
            ├─▶ Admin training form: Find video searches YouTube by training name
            ├─▶ Admin reviews a result and saves its YouTube URL on the training
            ├─▶ Admin Dashboard → Attendance → Mark attendance for manual session marks
            └─▶ Students watch the assigned lesson; after 90% playback, today's attendance is marked Present

┌─────────────────────────────────────────────────────────────────────┐
│ 3. Attendance Calculation for Eligibility                          │
└─────────────────────────────────────────────────────────────────────┘
            │
            ├─▶ GET all attendance records for student
            │
            ├─▶ Calculate: (present days / total days) * 100
            │
            └─▶ Used in eligibility check against company.minAttendance
```

---

Training records store mode, location, meeting URL, and an optional YouTube lesson URL. Enrollment is not modeled separately: the attendance screen currently uses all registered students as its roster. Video progress is measured by the browser's YouTube IFrame player and the server records attendance for the logged-in student; it is not equivalent to tamper-proof proctoring.

---

## 5. Aptitude Assessment Workflow

1. Admin opens **Aptitude tests** from the admin dashboard and creates a test with a date and total marks.
2. Admin selects the test and adds questions manually or imports up to 10 topic-matched questions from Open Trivia Database, then reviews the answer key and marks.
3. Students open **Aptitude tests** in their dashboard, answer each question, and submit once.
4. The server grades answers, scales the result to a percentage from 0 to 100, and records the answer snapshot.
5. The completed test row changes to **View results**; only then can the student see their selected answers and the correct answers. Retakes are blocked by the API.
6. Admins can review or correct manually recorded scores from **Aptitude scores**.
7. Eligibility uses the student's highest recorded percentage across tests.

Student question responses omit the answer key. Admin question management responses include it. Deleting a test with questions or recorded scores is blocked to protect history.

---

## 6. Admin Dashboard Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
│                       ADMIN DASHBOARD WORKFLOW                      │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ Dashboard Components                                                │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ Students    │ │ Companies   │ │ Trainings   │ │ Placements  │
│    0        │ │    0        │ │    0        │ │    0        │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘

│
├─▶ Quick Links (Navigation)
│   ├─ Students Management
│   ├─ Companies Management
│   ├─ Training Programs
│   ├─ Attendance
│   ├─ Aptitude Tests
│   └─ Placements
```

### Dashboard Data Loading:

```javascript
// Admin Dashboard (admin.js)
document.addEventListener('DOMContentLoaded', () => {
    count('/api/students', 'students');
    count('/api/companies', 'companies');
    count('/api/trainings', 'trainings');
    count('/api/placements', 'placements');
});
```

### CRUD Operations:

| Action | HTTP Method | Endpoint | Description |
|--------|-------------|----------|-------------|
| Create | POST | `/api/{entity}` | Create new entity |
| Read (all) | GET | `/api/{entity}` | List all entities |
| Read (one) | GET | `/api/{entity}/{id}` | Get entity by ID |
| Update | PUT | `/api/{entity}/{id}` | Update entity |
| Delete | DELETE | `/api/{entity}/{id}` | Delete entity |

---

## 7. Student Dashboard Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
│                      STUDENT DASHBOARD WORKFLOW                     │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ Profile Section                                                     │
└─────────────────────────────────────────────────────────────────────┘
            │
            ├─▶ GET /api/students/{studentId}
            │   └─▶ Display: Name, Department, Batch, CGPA, Email, Phone, Skills

┌─────────────────────────────────────────────────────────────────────┐
│ Eligible Companies Table                                            │
└─────────────────────────────────────────────────────────────────────┘
            │
            ├─▶ GET /api/eligibility/{studentId}
            │   ├─▶ Company Name
            │   ├─▶ Eligible (YES/NO badge)
            │   └─▶ Details (reasons for ineligibility)

┌─────────────────────────────────────────────────────────────────────┐
│ Placement Status Table                                              │
└─────────────────────────────────────────────────────────────────────┘
            │
            ├─▶ GET /api/placements/student/{studentId}
            │   ├─▶ Company Name
            │   ├─▶ Job Role
            │   ├─▶ Package (₹ X LPA)
            │   ├─▶ Status (badge with color coding)
            │   ├─▶ Applied Date
            │   └─▶ Placement Date
```

### Status Badge Colors:

| Status | Color | Badge Class |
|--------|-------|-------------|
| SELECTED | Green | `bg-success` |
| APPLIED | Warning | `bg-warning` |
| REJECTED | Danger | `bg-danger` |
| Other | Info | `bg-info` |

---

## 7. Data Validation Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
|                       VALIDATION WORKFLOW                           │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ Request Validation Flow                                             │
└─────────────────────────────────────────────────────────────────────┘

Client Request
    │
    ├─▶ @Valid annotation in Controller
    │
    ├─▶ @NotBlank - Field cannot be empty
    │
    ├─▶ @Size(min, max) - Length validation
    │
    ├─▶ @DecimalMin, @DecimalMax - Range validation
    │
    ├─▶ @NotNull - Cannot be null
    │
    ├─▶ @Email - Email format validation
    │
    └─▶ Service Layer Business Validation
        ├─▶ Email uniqueness check
        ├─▶ Company name uniqueness check
        ├─▶ Entity existence check
        └─▶ Business rule validation

Result:
    ├─▶ Valid → Process request
    └─▶ Invalid → Return 400 with error message
```

### Example Validation Errors:

```json
{
  "code": 400,
  "message": "Email already exists: john@college.com",
  "data": null
}
```

---

## 8. Security Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                        SECURITY WORKFLOW                            │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ Authentication                                                      │
└─────────────────────────────────────────────────────────────────────┘

Request
    │
    ├─▶ Is public endpoint (/api/auth/**)?
    │   ├─▶ YES → Allow
    │   └─▶ NO → Check authentication
    │
    ├─▶ Is user authenticated (has valid session/token)?
    │   ├─▶ NO → Return 401 Unauthorized
    │   └─▶ YES → Continue
    │
    ├─▶ Does user have required role?
    │   ├─▶ NO → Return 403 Forbidden
    │   └─▶ YES → Allow access

┌─────────────────────────────────────────────────────────────────────┐
│ Password Security                                                   │
└─────────────────────────────────────────────────────────────────────┘

Password
    │
    ├─▶ User enters password (plain text)
    │
    ├─▶ BCryptPasswordEncoder.encode()
    │   └─▶ Salt + Hashing (10 rounds default)
    │
    ├─▶ Stored in database (hashed)
    │
    └─▶ On login:
        ├─▶ Retrieve hashed password
        ├─▶BCryptPasswordEncoder.matches(input, stored)
        └─▶ Return true/false
```

---

## 9. Error Handling Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                       ERROR HANDLING WORKFLOW                       │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ Exception Handling                                                  │
└─────────────────────────────────────────────────────────────────────┘

Exception
    │
    ├─▶ ResourceNotFoundException
    │   └─▶ Handler: 404 Not Found
    │       { "code": 404, "message": "Entity not found", "data": null }
    │
    ├─▶ IllegalArgumentException
    │   └─▶ Handler: 400 Bad Request
    │       { "code": 400, "message": "Validation error", "data": null }
    │
    ├─▶ UnauthorizedException
    │   └─▶ Handler: 401 Unauthorized
    │       { "code": 401, "message": "Unauthorized", "data": null }
    │
    └─▶ Exception (generic)
        └─▶ Handler: 500 Internal Server Error
            { "code": 500, "message": "Unexpected error", "data": null }
```

---

## 10. Setup & Initialization Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SETUP & INITIALIZATION FLOW                      │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ Application Startup                                                 │
└─────────────────────────────────────────────────────────────────────┘

Spring Boot Application
    │
    ├─▶ Load application.yml
    │   ├─▶ Database connection
    │   ├─▶ Server port
    │   └─▶ Other configuration
    │
    ├─▶ Run DataInitializer (CommandLineRunner)
    │   ├─▶ Check if admin user exists
    │   │   ├─▶ NO → Create admin with password
    │   │   └─▶ YES → Skip
    │   │
    │   └─▶ Check if student user exists
    │       ├─▶ NO → Create student user
    │       └─▶ YES → Skip
    │
    └─▶ Start Tomcat server on port 8080

┌─────────────────────────────────────────────────────────────────────┐
│ Database Initialization                                             │
└─────────────────────────────────────────────────────────────────────┘

Spring Data JPA (Hibernate)
    │
    ├─▶ entity classes detected
    │
    ├─▶ Create tables based on @Entity annotations
    │   ├─▶ users
    │   ├─▶ students
    │   ├─▶ companies
    │   ├─▶ placements
    │   ├─▶ trainings
    │   ├─▶ attendance
    │   ├─▶ aptitude_tests
    │   └─▶ aptitude_scores
    │
    └─▶ Foreign key relationships established
```

---

## API Request Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                     API REQUEST FLOW (Sample)                       │
└─────────────────────────────────────────────────────────────────────┘

Client
    │
    ├─▶ POST /api/students (Create)
    │
    └─▶ Controller: StudentController.create()
        │
        ├─▶ @Valid annotation triggers validation
        │
        ├─▶ Service: StudentServiceImpl.save()
        │   │
        │   ├─▶ Validate email uniqueness
        │   │
        │   └─▶ Repository.save()
        │       │
        │       ├─▶ Hibernate creates INSERT statement
        │       └─▶ Database returns generated ID
        │
        └─▶ Return Result.success(savedStudent)
            │
            └─▶ JSON Response:
                {
                  "code": 200,
                  "message": "Student created successfully",
                  "data": { ...saved student with ID... }
                }
```

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        DATA FLOW DIAGRAM                            │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────┐     ┌───────────────────────────────────────────┐
│   CLIENT    │────▶│              CONTROLLER                   │
│   (Browser) │     │  - Request Mapping                        │
│             │     │  - Validation (@Valid)                    │
└─────────────┘     │  - Request Handling                       │
                    └───────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────┐     ┌───────────────────────────────────────────┐
│   SERVICE   │◀────│           SERVICE LAYER                   │
│   LAYER     │     │  - Business Logic                         │
│             │     │  - Validation Rules                       │
└─────────────┘     │  - Transaction Management                 │
                    └───────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────┐     ┌───────────────────────────────────────────┐
│   DATABASE  │◀────│            REPOSITORY                     │
│   (JPA)     │     │  - CRUD Operations                        │
│             │     │  - Custom Queries                         │
└─────────────┘     └───────────────────────────────────────────┘
```

---

## System Components Interaction

```
┌─────────────────────────────────────────────────────────────────────┐
│                 COMPONENT INTERACTION DIAGRAM                       │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    Browser      │────▶│   Security      │────▶│   Spring        │
│   (Client)      │     │   Filter        │     │   MVC           │
└─────────────────┘     └─────────────────┘     └────────┬────────┘
                                                        │
                                     ┌──────────────────▼─────────────────┐
                                     │           CONTROLLER               │
                                     │  - @RestController               │
                                     │  - @RequestMapping                 │
                                     └──────────────────┬─────────────────┘
                                                        │
                                     ┌──────────────────▼─────────────────┐
                                     │          SERVICE LAYER             │
                                     │  - @Service                        │
                                     │  - @Transactional                  │
                                     └──────────────────┬─────────────────┘
                                                        │
                                     ┌──────────────────▼─────────────────┐
                                     │        REPOSITORY (JPA)            │
                                     │  - @Repository                     │
                                     │  - JpaRepository                   │
                                     └──────────────────┬─────────────────┘
                                                        │
                                     ┌──────────────────▼─────────────────┐
                                     │        DATABASE (PostgreSQL)       │
                                     │  - Tables, Indexes, FKs            │
                                     └────────────────────────────────────┘