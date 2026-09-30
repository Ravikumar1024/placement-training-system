package com.placement.util;

import com.placement.entity.Company;
import com.placement.entity.Student;
import java.util.ArrayList;
import java.util.List;

public final class EligibilityUtil {
    private EligibilityUtil() {}

    public static List<String> reasons(Student s, Company c, double aptitude, double attendance) {
        List<String> reasons = new ArrayList<>();
        if (s.getCgpa() == null || s.getCgpa() < safe(c.getMinCgpa())) reasons.add("CGPA below requirement");
        if (s.getBacklogs() == null || s.getBacklogs() > safeInt(c.getMaxBacklogs())) reasons.add("Backlogs exceed limit");
        if (aptitude < safe(c.getMinAptitudeScore())) reasons.add("Aptitude score below requirement");
        if (attendance < safe(c.getMinAttendance())) reasons.add("Attendance below requirement");
        if (c.getEligibleDepartments() != null && !c.getEligibleDepartments().isBlank()) {
            boolean deptOk = java.util.Arrays.stream(c.getEligibleDepartments().split(","))
                    .map(String::trim).anyMatch(d -> d.equalsIgnoreCase(s.getDepartment()));
            if (!deptOk) reasons.add("Department not eligible");
        }
        return reasons;
    }

    private static double safe(Double v) { return v == null ? 0.0 : v; }
    private static int safeInt(Integer v) { return v == null ? Integer.MAX_VALUE : v; }
}
