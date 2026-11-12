package com.alfha.discord_clone.channel;

public class ChannelHasMessagesException extends RuntimeException {

    public ChannelHasMessagesException() {
        super("Delete the channel's messages before deleting the channel.");
    }
}
