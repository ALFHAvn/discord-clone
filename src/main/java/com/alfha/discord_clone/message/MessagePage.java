package com.alfha.discord_clone.message;

import java.util.List;

public record MessagePage(List<MessageDto> messages, Long nextAfterId) {
}
