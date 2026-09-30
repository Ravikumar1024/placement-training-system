package com.placement.serviceimpl;

import com.placement.dto.LoginRequest;
import com.placement.dto.LoginResponse;
import com.placement.entity.User;
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
            .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String studentId = user.getStudent() == null ? null : user.getStudent().getId();
        return new LoginResponse(user.getId(), user.getUsername(), user.getRole().name(), studentId);
    }
}
