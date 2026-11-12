package com.alfha.discord_clone.server;

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
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @PostMapping
    public ResponseEntity<ServerDto> createServer(@Valid @RequestBody CreateServerRequest request) {
        ServerDto created = serverService.createServer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ServerDto>> getAllServers() {
        return ResponseEntity.ok(serverService.getAllServers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerDto> getServerById(@PathVariable Long id) {
        return ResponseEntity.ok(serverService.getServerById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServerDto> updateServer(@PathVariable Long id, @Valid @RequestBody UpdateServerRequest request) {
        return ResponseEntity.ok(serverService.updateServer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServer(@PathVariable Long id) {
        serverService.deleteServer(id);
        return ResponseEntity.noContent().build();
    }
}
