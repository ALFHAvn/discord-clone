package com.alfha.discord_clone.server;

public class ServerMemberNotFoundException extends RuntimeException {

    public ServerMemberNotFoundException(long serverId, long userId) {
        super("User " + userId + " is not a member of server " + serverId + ".");
    }
}
