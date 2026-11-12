package com.alfha.discord_clone.message;

import java.time.Instant;

public record MessageDto(Long id, Long channelId, Long authorId, String content, Instant createdAt) {
}
