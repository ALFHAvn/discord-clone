package com.alfha.discord_clone.server;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/servers/{serverId}/members")
public class ServerMemberController {

    private final ServerService serverService;

    public ServerMemberController(ServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping
    public ResponseEntity<List<ServerMemberDto>> getMembers(@PathVariable Long serverId) {
        return ResponseEntity.ok(serverService.getMembers(serverId));
    }

    @PostMapping
    public ResponseEntity<ServerMemberDto> addMember(@PathVariable Long serverId,
                                                     @Valid @RequestBody AddServerMemberRequest request) {
        ServerMemberDto added = serverService.addMember(serverId, request.userId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{userId}")
                .buildAndExpand(added.userId())
                .toUri();
        return ResponseEntity.created(location).body(added);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ServerMemberDto> getMember(@PathVariable Long serverId, @PathVariable Long userId) {
        return ResponseEntity.ok(serverService.getMember(serverId, userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long serverId, @PathVariable Long userId) {
        serverService.removeMember(serverId, userId);
        return ResponseEntity.noContent().build();
    }
}
