package com.alfha.discord_clone.server;

public class ServerNotFoundException extends RuntimeException {

    public ServerNotFoundException(long id) {
        super("Server " + id + " was not found.");
    }
}
