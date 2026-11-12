package com.alfha.discord_clone.user;

import com.alfha.discord_clone.message.MessageRepository;
import com.alfha.discord_clone.server.ServerMemberRepository;
import com.alfha.discord_clone.server.ServerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

@Service
public class UserService {

    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final ServerMemberRepository serverMemberRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ServerRepository serverRepository,
                       ServerMemberRepository serverMemberRepository, MessageRepository messageRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.serverRepository = serverRepository;
        this.serverMemberRepository = serverMemberRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto createUser(CreateUserRequest request) {
        String password = request.getPassword();
        PasswordRules.validate(password);
        String passwordHash = passwordEncoder.encode(password);
        User user = new User(request.getUsername(), request.getEmail(), passwordHash);
        return saveUser(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return toDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = findUserForUpdate(id);
        user.setUsername(request.username());
        user.setEmail(request.email());
        return saveUser(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findUserForUpdate(id);
        if (serverRepository.existsByOwnerId(id)) {
            throw new UserOwnsServerException();
        }
        if (messageRepository.existsByAuthorId(id)) {
            throw new UserHasMessagesException();
        }
        serverMemberRepository.deleteByUserId(id);
        userRepository.delete(user);
    }

    private User findUserForUpdate(Long id) {
        return userRepository.findForUpdateById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserDto saveUser(User user) {
        try {
            return toDto(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            if (exception.getMostSpecificCause() instanceof SQLException sqlException
                    && UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
                throw new DuplicateUserException(exception);
            }
            throw exception;
        }
    }

    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail());
    }
}
