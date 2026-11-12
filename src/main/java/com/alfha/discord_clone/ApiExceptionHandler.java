package com.alfha.discord_clone;

import com.alfha.discord_clone.channel.ChannelHasMessagesException;
import com.alfha.discord_clone.channel.ChannelNotFoundException;
import com.alfha.discord_clone.message.MessageNotFoundException;
import com.alfha.discord_clone.server.ServerHasChannelsException;
import com.alfha.discord_clone.server.ServerMemberNotFoundException;
import com.alfha.discord_clone.server.ServerMembershipConflictException;
import com.alfha.discord_clone.server.ServerNotFoundException;
import com.alfha.discord_clone.user.DuplicateUserException;
import com.alfha.discord_clone.user.InvalidPasswordException;
import com.alfha.discord_clone.user.UserHasMessagesException;
import com.alfha.discord_clone.user.UserNotFoundException;
import com.alfha.discord_clone.user.UserOwnsServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPassword(InvalidPasswordException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({UserNotFoundException.class, ServerNotFoundException.class,
            ServerMemberNotFoundException.class, ChannelNotFoundException.class, MessageNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({DuplicateUserException.class, UserOwnsServerException.class,
            ServerMembershipConflictException.class, ServerHasChannelsException.class,
            ChannelHasMessagesException.class, UserHasMessagesException.class})
    public ProblemDetail handleConflict(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }
}
