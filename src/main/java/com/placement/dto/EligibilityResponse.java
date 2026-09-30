package com.placement.dto;

import java.util.List;

public record EligibilityResponse(
	String companyId,
    String companyName,
    boolean eligible,
    List<String> reasons
) {}
