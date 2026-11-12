package com.alfha.discord_clone.auth;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
