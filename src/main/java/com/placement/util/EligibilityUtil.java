package com.placement.util;

import com.placement.entity.Company;
import com.placement.entity.Student;
import java.util.ArrayList;
import java.util.List;

public final class EligibilityUtil {
    private EligibilityUtil() {}

    public static List<String> reasons(Student s, Company c, double aptitude, double attendance) {
        List<String> reasons = new ArrayList<>();
        if (s.getCgpa() == null || s.getCgpa() < safe(c.getMinCgpa())) reasons.add(ApiMessages.get("eligibility.reason.cgpa"));
        if (s.getBacklogs() == null || s.getBacklogs() > safeInt(c.getMaxBacklogs())) reasons.add(ApiMessages.get("eligibility.reason.backlogs"));
        if (aptitude < safe(c.getMinAptitudeScore())) reasons.add(ApiMessages.get("eligibility.reason.aptitude"));
        if (attendance < safe(c.getMinAttendance())) reasons.add(ApiMessages.get("eligibility.reason.attendance"));
        if (c.getEligibleDepartments() != null && !c.getEligibleDepartments().isBlank()) {
            boolean deptOk = java.util.Arrays.stream(c.getEligibleDepartments().split(","))
                    .map(String::trim).anyMatch(d -> d.equalsIgnoreCase(s.getDepartment()));
            if (!deptOk) reasons.add(ApiMessages.get("eligibility.reason.department"));
        }
        return reasons;
    }

    private static double safe(Double v) { return v == null ? 0.0 : v; }
    private static int safeInt(Integer v) { return v == null ? Integer.MAX_VALUE : v; }
}
