package com.placement.serviceimpl;

import com.placement.dto.LoginRequest;
import com.placement.dto.LoginResponse;
import com.placement.entity.User;
import com.placement.exception.UnauthorizedException;
import com.placement.repository.UserRepository;
import com.placement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new UnauthorizedException("api.error.authentication.invalidCredentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("api.error.authentication.invalidCredentials");
        }
        if (request.expectedRole() != null && !request.expectedRole().equals(user.getRole().name())) {
            String roleMessageKey = "ADMIN".equals(request.expectedRole())
                ? "api.error.authentication.adminOnly"
                : "api.error.authentication.studentOnly";
            throw new UnauthorizedException(roleMessageKey);
        }

        String studentId = user.getStudent() == null ? null : user.getStudent().getId();
        return new LoginResponse(user.getId(), user.getUsername(), user.getRole().name(), studentId);
    }
}
