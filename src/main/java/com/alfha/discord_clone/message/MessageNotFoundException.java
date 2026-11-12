package com.alfha.discord_clone.message;

public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException(long channelId, long messageId) {
        super("Message " + messageId + " was not found in channel " + channelId + ".");
    }
}
