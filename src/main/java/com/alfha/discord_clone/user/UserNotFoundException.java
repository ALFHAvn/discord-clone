package com.alfha.discord_clone.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(long id) {
        super("User " + id + " was not found.");
    }
}
