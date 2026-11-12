package com.alfha.discord_clone.auth;

import com.alfha.discord_clone.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Base64;

@Configuration
class JwtConfiguration {

    static final String ISSUER = "discord-clone";
    static final String AUDIENCE = "discord-clone-api";

    @Bean
    SecretKey jwtSigningKey(@Value("${discord.auth.jwt-secret}") String encodedSecret) {
        byte[] key;
        try {
            key = Base64.getDecoder().decode(encodedSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("DISCORD_JWT_SECRET must be Base64-encoded.");
        }
        if (key.length < 32) {
            throw new IllegalStateException("DISCORD_JWT_SECRET must encode at least 32 random bytes.");
        }
        return new SecretKeySpec(key, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return NimbusJwtEncoder.withSecretKey(jwtSigningKey).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSigningKey, UserRepository userRepository) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtTimestampValidator timestamps = new JwtTimestampValidator(Duration.ZERO);
        timestamps.setAllowEmptyExpiryClaim(false);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                timestamps,
                new JwtIssuerValidator(ISSUER),
                new JwtAudienceValidator(AUDIENCE),
                new JwtClaimValidator<String>(JwtClaimNames.SUB, subject -> hasActiveAccount(subject, userRepository))
        ));
        return decoder;
    }

    private boolean hasActiveAccount(String subject, UserRepository userRepository) {
        try {
            long userId = Long.parseLong(subject);
            return userId > 0 && userRepository.existsById(userId);
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
