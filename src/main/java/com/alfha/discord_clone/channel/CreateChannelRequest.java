package com.alfha.discord_clone.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateChannelRequest(@NotBlank @Size(max = 255) String name, @NotNull ChannelType type) {
}
