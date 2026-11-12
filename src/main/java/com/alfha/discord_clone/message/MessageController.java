package com.alfha.discord_clone.message;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/servers/{serverId}/channels/{channelId}/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<MessageDto> createMessage(@PathVariable Long serverId, @PathVariable Long channelId,
                                                    @Valid @RequestBody CreateMessageRequest request) {
        MessageDto created = messageService.createMessage(serverId, channelId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{messageId}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<MessagePage> getMessages(@PathVariable Long serverId, @PathVariable Long channelId,
                                                   @RequestParam(defaultValue = "0") @PositiveOrZero long afterId,
                                                   @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return ResponseEntity.ok(messageService.getMessages(serverId, channelId, afterId, limit));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<MessageDto> getMessage(@PathVariable Long serverId, @PathVariable Long channelId,
                                                 @PathVariable Long messageId) {
        return ResponseEntity.ok(messageService.getMessage(serverId, channelId, messageId));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageDto> updateMessage(@PathVariable Long serverId, @PathVariable Long channelId,
                                                    @PathVariable Long messageId,
                                                    @Valid @RequestBody UpdateMessageRequest request) {
        return ResponseEntity.ok(messageService.updateMessage(serverId, channelId, messageId, request));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long serverId, @PathVariable Long channelId,
                                              @PathVariable Long messageId) {
        messageService.deleteMessage(serverId, channelId, messageId);
        return ResponseEntity.noContent().build();
    }
}
