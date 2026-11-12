package com.alfha.discord_clone.user;

import java.nio.charset.StandardCharsets;

public final class PasswordRules {

    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private PasswordRules() {
    }

    public static void validate(String password) {
        if (password.indexOf('\0') >= 0 || !StandardCharsets.UTF_8.newEncoder().canEncode(password)) {
            throw new InvalidPasswordException("Password must be valid Unicode text without null characters.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new InvalidPasswordException("Password must be at most 72 bytes in UTF-8.");
        }
    }
}
