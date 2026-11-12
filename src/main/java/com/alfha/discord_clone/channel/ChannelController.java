package com.alfha.discord_clone.channel;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/servers/{serverId}/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @PostMapping
    public ResponseEntity<ChannelDto> createChannel(@PathVariable Long serverId,
                                                    @Valid @RequestBody CreateChannelRequest request) {
        ChannelDto created = channelService.createChannel(serverId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{channelId}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ChannelDto>> getChannels(@PathVariable Long serverId) {
        return ResponseEntity.ok(channelService.getChannels(serverId));
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<ChannelDto> getChannel(@PathVariable Long serverId, @PathVariable Long channelId) {
        return ResponseEntity.ok(channelService.getChannel(serverId, channelId));
    }

    @PutMapping("/{channelId}")
    public ResponseEntity<ChannelDto> updateChannel(@PathVariable Long serverId, @PathVariable Long channelId,
                                                    @Valid @RequestBody UpdateChannelRequest request) {
        return ResponseEntity.ok(channelService.updateChannel(serverId, channelId, request));
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long serverId, @PathVariable Long channelId) {
        channelService.deleteChannel(serverId, channelId);
        return ResponseEntity.noContent().build();
    }
}
