package com.placement.serviceimpl;

import com.placement.controller.AuthController;
import com.placement.dto.LoginRequest;
import com.placement.entity.User;
import com.placement.exception.UnauthorizedException;
import com.placement.repository.UserRepository;
import com.placement.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthRoleTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthService authService;
    @Mock private SecurityContextRepository securityContextRepository;

    @InjectMocks private AuthServiceImpl authServiceImpl;
    @InjectMocks private AuthController authController;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsStudentCredentialsWhenAdminRoleIsExpected() {
        User student = User.builder()
            .id("student-id")
            .username("student-code")
            .password("encoded-password")
            .role(User.Role.STUDENT)
            .build();
        when(userRepository.findByUsername("student-code")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        LoginRequest loginRequest = new LoginRequest("student-code", "password", "ADMIN");

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
            () -> authServiceImpl.login(loginRequest));

        assertEquals("This page is for admin accounts. Use the student sign-in for student accounts.", exception.getMessage());
    }

    @Test
    void failedRoleCheckClearsExistingSessionAndDoesNotSaveSecurityContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        LoginRequest loginRequest = new LoginRequest("student-code", "password", "ADMIN");
        when(authService.login(loginRequest)).thenThrow(new UnauthorizedException("api.error.authentication.adminOnly"));

        assertThrows(UnauthorizedException.class,
            () -> authController.login(loginRequest, request, response));

        assertNull(request.getSession(false));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(securityContextRepository, never()).saveContext(any(), any(), any());
    }
}