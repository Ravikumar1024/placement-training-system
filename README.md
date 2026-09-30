# Web-Based Placement Training and Tracking System

## Stack
- Java 21
- Spring Boot 3.5
- Gradle
- PostgreSQL
- HTML/CSS/JavaScript
- Spring Data JPA
- Spring Security (password encoding + request security)

## Database
Create a PostgreSQL database:
`placement_db`

Run `database.sql` as a PostgreSQL superuser to create it, then update `src/main/resources/application.yml` with your PostgreSQL username/password. Flyway creates the application tables on a clean database and applies the aptitude-question migration to an existing schema.

## Run
```bash
./gradlew bootRun
```
Windows:
```bat
gradlew.bat bootRun
```

Open:
http://localhost:8080/

## Demo accounts
Admin:
- username: admin
- password: admin123

Student:
- username: student
- password: student123

The application creates these users automatically on first run. A Student entity must be linked to the student user before the student dashboard can display a profile. You can create students through the Students API/page and then link a User in the database if needed.

## Main APIs
- POST /api/auth/login
- GET/POST/PUT/DELETE /api/students
- GET/POST/PUT/DELETE /api/companys
- GET/POST/PUT/DELETE /api/trainings
- GET/POST/PUT/DELETE /api/attendances
- GET/POST/PUT/DELETE /api/aptitude-tests
- POST /api/aptitude-tests/{id}/questions
- POST /api/aptitude-tests/{id}/submit
- GET/POST/PUT/DELETE /api/aptitude-scores
- GET /api/aptitude-scores/student/{studentId}
- GET/POST/PUT/DELETE /api/placements
- GET /api/placements/student/{studentId}
- GET /api/eligibility/{studentId}

## Notes
The aptitude workflows use server-side sessions and role checks. Other legacy API routes remain permissive; add authorization for those routes before exposing the application publicly.


## Frontend architecture

The frontend is intentionally separated from the Java source/resources:

```text
webapp/
├── index.html
├── login.html
├── admin/
│   ├── dashboard.html
│   ├── students.html
│   ├── companies.html
│   ├── training.html
│   ├── attendance.html
│   ├── aptitude.html
│   ├── aptitude-scores.html
│   └── placements.html
├── student/
│   └── dashboard.html
├── css/
└── js/
```

Spring Boot serves the `webapp/` directory through `WebAppConfig`.

The admin dashboard links to attendance marking, aptitude test authoring, and score management. Students can take authored multiple-choice tests and view their recorded score history from their dashboard.

Frontend URL:
http://localhost:8080/

Direct login:
http://localhost:8080/webapp/login.html

## Student placement status
The student dashboard loads placement status from PostgreSQL through `GET /api/placements/student/{studentId}`.
The logged-in student's `studentId` comes from the login response, and the dashboard displays all placement records for that student.
If no placement record exists, the dashboard shows a clear "No placement application/status has been recorded yet" message.
