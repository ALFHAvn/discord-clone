package com.alfha.discord_clone.server;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddServerMemberRequest(@NotNull @Positive Long userId) {
}
