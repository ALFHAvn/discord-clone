package com.alfha.discord_clone.server;

public class ServerHasChannelsException extends RuntimeException {

    public ServerHasChannelsException() {
        super("Delete the server's channels before deleting the server.");
    }
}
