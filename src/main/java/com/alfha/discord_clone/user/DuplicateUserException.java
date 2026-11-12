package com.alfha.discord_clone.user;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(Throwable cause) {
        super("Username or email is already in use.", cause);
    }
}
