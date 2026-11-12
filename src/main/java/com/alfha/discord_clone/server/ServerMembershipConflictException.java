package com.alfha.discord_clone.server;

public class ServerMembershipConflictException extends RuntimeException {

    public ServerMembershipConflictException(String message) {
        super(message);
    }
}
