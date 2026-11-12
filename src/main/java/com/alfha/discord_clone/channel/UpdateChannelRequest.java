package com.alfha.discord_clone.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateChannelRequest(@NotBlank @Size(max = 255) String name) {
}
