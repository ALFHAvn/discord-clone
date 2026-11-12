package com.alfha.discord_clone.channel;

public class ChannelNotFoundException extends RuntimeException {

    public ChannelNotFoundException(long serverId, long channelId) {
        super("Channel " + channelId + " was not found in server " + serverId + ".");
    }
}
