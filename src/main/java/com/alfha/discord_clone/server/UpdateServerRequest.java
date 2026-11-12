package com.alfha.discord_clone.server;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateServerRequest(@NotBlank @Size(max = 255) String name) {
}
