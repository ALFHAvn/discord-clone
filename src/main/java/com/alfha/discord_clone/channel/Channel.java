package com.alfha.discord_clone.channel;

import com.alfha.discord_clone.server.Server;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "channels")
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType type;

    protected Channel() {
    }

    public Channel(Server server, String name, ChannelType type) {
        this.server = server;
        this.name = name;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public Server getServer() {
        return server;
    }

    public String getName() {
        return name;
    }

    public ChannelType getType() {
        return type;
    }

    public void rename(String name) {
        this.name = name;
    }
}
