package com.placement.service;

import com.placement.dto.LoginRequest;
import com.placement.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
