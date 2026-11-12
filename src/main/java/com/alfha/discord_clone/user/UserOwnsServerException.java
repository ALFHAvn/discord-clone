package com.alfha.discord_clone.user;

public class UserOwnsServerException extends RuntimeException {

    public UserOwnsServerException() {
        super("A user who owns a server cannot be deleted.");
    }
}
