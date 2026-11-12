package com.alfha.discord_clone.user;

public class UserHasMessagesException extends RuntimeException {

    public UserHasMessagesException() {
        super("A user who has authored messages cannot be deleted.");
    }
}
