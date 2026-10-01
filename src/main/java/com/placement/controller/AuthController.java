package com.placement.controller;

import com.placement.dto.*;
import com.placement.exception.UnauthorizedException;
import com.placement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        LoginResponse response;
        try {
            response = authService.login(request);
        } catch (UnauthorizedException exception) {
            SecurityContextHolder.clearContext();
            var session = httpRequest.getSession(false);
            if (session != null) session.invalidate();
            throw exception;
        }
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
            response.username(), null, List.of(new SimpleGrantedAuthority("ROLE_" + response.role())));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        return ResponseEntity.ok(Result.success("api.success.auth.login", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        return ResponseEntity.ok(Result.success("api.success.auth.logout", null));
    }

    @GetMapping("/csrf")
    public ResponseEntity<Result<String>> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok(Result.success("api.success.read.auth.csrf", csrfToken.getToken()));
    }
}
