package com.placement.util;

import org.springframework.security.crypto.password.PasswordEncoder;

public final class PasswordUtil {
    private PasswordUtil() {}
    public static String encode(PasswordEncoder encoder, String raw) {
        return encoder.encode(raw);
    }
}
