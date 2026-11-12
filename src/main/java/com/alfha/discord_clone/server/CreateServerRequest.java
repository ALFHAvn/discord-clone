package com.alfha.discord_clone.server;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateServerRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull @Positive Long ownerId
) {
}
