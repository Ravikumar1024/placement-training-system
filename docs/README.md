# Placement Training & Tracking System - Documentation

## Overview

This is a comprehensive Placement Training & Tracking System built with Spring Boot and modern web technologies. The system helps educational institutions manage student placements, track training programs, monitor attendance, and evaluate aptitude scores.

## Features

### For Administrators
- Manage students, companies, and training programs
- Track student attendance and aptitude scores
- Configure company eligibility criteria
- Monitor placement status and progress

### For Students
- View personal placement dashboard
- Check eligibility for companies
- Track placement application status
- View training program details

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: PostgreSQL (or MySQL, H2)
- **Security**: Spring Security + JWT (configured for token-based auth)
- **Validation**: Jakarta Bean Validation 3.x
- **Build Tool**: Gradle

### Frontend
- **HTML5/CSS3/JavaScript**
- **Bootstrap 5.3** (via CDN)
- **Vanilla JS** (no framework dependencies)

## Project Structure

```
placement-training-system/
├── src/main/java/com/placement/
│   ├── PlacementApplication.java         # Main application entry point
│   ├── config/                           # Configuration classes
│   │   ├── SecurityConfig.java          # Security configuration
│   │   ├── CorsConfig.java              # CORS configuration
│   │   ├── WebAppConfig.java            # Web app configuration
│   │   ├── DataInitializer.java         # Data seeding
│   ├── controller/                       # REST controllers
│   │   ├── AuthController.java          # Authentication endpoints
│   │   ├── StudentController.java       # Student management
│   │   ├── CompanyController.java       # Company management
│   │   ├── PlacementController.java     # Placement management
│   │   ├── TrainingController.java      # Training management
│   │   ├── AttendanceController.java    # Attendance tracking
│   │   ├── AptitudeTestController.java  # Aptitude tests
│   │   ├── AptitudeScoreController.java # Aptitude scores
│   │   ├── EligibilityController.java   # Eligibility checking
│   ├── service/                          # Service interfaces
│   ├── serviceimpl/                      # Service implementations
│   ├── repository/                       # JPA repositories
│   ├── entity/                           # JPA entities
│   ├── dto/                              # Data Transfer Objects
│   │   ├── Result.java                  # Generic result wrapper
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── PlacementRequest.java
│   │   ├── PlacementStatusResponse.java
│   │   └── EligibilityResponse.java
│   ├── exception/                        # Custom exceptions
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   └── UnauthorizedException.java
│   └── util/                             # Utility classes
│       ├── EligibilityUtil.java
│       └── PasswordUtil.java
├── webapp/                               # Frontend application
│   ├── index.html                        # Landing page
│   ├── login.html                        # Login page
│   ├── css/style.css                     # Custom styles
│   ├── js/                               # JavaScript files
│   │   ├── login.js
│   │   ├── admin.js
│   │   ├── student.js
│   │   └── crud.js
│   ├── admin/                            # Admin pages
│   │   ├── dashboard.html
│   │   ├── students.html
│   │   ├── companies.html
│   │   ├── training.html
│   │   ├── attendance.html
│   │   ├── aptitude.html
│   │   └── placements.html
│   └── student/                          # Student pages
│       └── dashboard.html
├── docs/                                 # Documentation
│   ├── README.md                         # This file
│   ├── API.md                            # API documentation
│   └── WORKFLOW.md                       # Workflow documentation
├── build.gradle                          # Gradle build config
├── database.sql                          # Database schema
└── README.md                             # Project overview
```

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication

#### Login
```
POST /api/auth/login
Content-Type: application/json

Request Body:
{
  "username": "admin",
  "password": "admin123"
}

Response (Success - 200):
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

Response (Error - 401):
{
  "code": 401,
  "message": "Unauthorized: Invalid username or password",
  "data": null
}
```

### Student Management

#### Get All Students
```
GET /api/students

Response (Success - 200):
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

#### Get Student by ID
```
GET /api/students/{id}

Response (Success - 200):
{
  "code": 200,
  "message": "Student retrieved successfully",
  "data": { ...student object... }
}

Response (Error - 404):
{
  "code": 404,
  "message": "Student not found: 999",
  "data": null
}
```

#### Create Student
```
POST /api/students
Content-Type: application/json

Request Body:
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

Response (Success - 201):
{
  "code": 200,
  "message": "Student created successfully",
  "data": { ...saved student... }
}
```

### Company Management

#### Get All Companies
```
GET /api/companies

Response (Success - 200):
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

### Eligibility Check

#### Check Student Eligibility
```
GET /api/eligibility/{studentId}

Response (Success - 200):
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

### Placement Management

#### Get Student Placements
```
GET /api/placements/student/{studentId}

Response (Success - 200):
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

## Error Responses

All error responses follow this format:
```json
{
  "code": 400,
  "message": "Error message",
  "data": null
}
```

| Code | Meaning | Description |
|------|---------|-------------|
| 400 | Bad Request | Invalid input or validation error |
| 401 | Unauthorized | Authentication required |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server error |

## Data Models

### Student
```java
{
  "id": Long,
  "name": String (required),
  "email": String (required, unique),
  "phone": String,
  "department": String,
  "batch": String,
  "cgpa": Double (0.0 - 10.0),
  "backlogs": Integer (0 - 10),
  "skills": String
}
```

### Company
```java
{
  "id": Long,
  "companyName": String (required),
  "jobRole": String,
  "packageLpa": Double,
  "minCgpa": Double,
  "maxBacklogs": Integer,
  "minAptitudeScore": Double (0.0 - 100.0),
  "minAttendance": Double (0.0 - 100.0),
  "eligibleDepartments": String,
  "requiredSkills": String
}
```

### Placement Status
- `APPLIED` - Initial application
- `APTITUDE_CLEARED` - Aptitude test passed
- `TECHNICAL_ROUND` - Technical interview completed
- `HR_ROUND` - HR interview completed
- `SELECTED` - Job offer accepted
- `REJECTED` - Application rejected

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Gradle 8.x or Maven 3.6+
- PostgreSQL 12+ or MySQL 8+ or H2 (for development)

### Database Setup

#### PostgreSQL
```sql
CREATE DATABASE placement_db;
```

#### H2 (Development)
Add to `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:placement
    driverClassName: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
```

### Build and Run

#### Using Gradle
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Or run the JAR
java -jar build/libs/placement-0.0.1-SNAPSHOT.jar
```

#### Using Maven
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Default Credentials

After first run, the system creates these accounts:

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| student | student123 | STUDENT |

## Security Configuration

### CORS Settings
Allowed origins:
- http://localhost:8080
- http://localhost:3000

Allowed methods:
- GET, POST, PUT, DELETE, OPTIONS

### Role-Based Access

| Endpoint Pattern | Allowed Roles |
|-----------------|---------------|
| /api/auth/** | All (public) |
| /api/admin/** | ADMIN only |
| /api/students/** | ADMIN, STUDENT |
| /api/companies/** | ADMIN, STUDENT |
| /api/trainings/** | ADMIN only |
| /api/aptitude-tests/** | ADMIN only |
| /api/aptitude-scores/** | ADMIN, STUDENT |
| /api/attendances/** | ADMIN, STUDENT |
| /api/placements/** | ADMIN, STUDENT |
| /api/eligibility/** | ADMIN, STUDENT |

## Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.placement.controller.*"
```

## Deployment

### Production Build
```bash
./gradlew clean build -x test
```

### Docker Deployment
Create `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Troubleshooting

### Common Issues

1. **Database connection failed**
   - Verify database credentials in `application.yml`
   - Ensure database server is running

2. **401 Unauthorized**
   - Check login credentials
   - Verify token is being sent in Authorization header

3. **Validation errors**
   - Check request body matches entity requirements
   - Review error message for specific field issues

## Support

For issues and questions, contact the development team.