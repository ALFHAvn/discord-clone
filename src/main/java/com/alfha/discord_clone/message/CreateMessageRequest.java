package com.alfha.discord_clone.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateMessageRequest(
        @NotNull @Positive Long authorId,
        @NotBlank @Size(max = 2000) String content
) {
}
