package com.placement.dto;

/**
 * Login response DTO containing user authentication information.
 */
public record LoginResponse(
	String userId,
    String username,
    String role,
    String studentId
) {}
