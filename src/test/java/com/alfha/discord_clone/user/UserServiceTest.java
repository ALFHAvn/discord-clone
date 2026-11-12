package com.alfha.discord_clone.user;

import com.alfha.discord_clone.message.MessageRepository;
import com.alfha.discord_clone.server.ServerMemberRepository;
import com.alfha.discord_clone.server.ServerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ServerRepository serverRepository;
    @Mock
    private ServerMemberRepository serverMemberRepository;
    @Mock
    private MessageRepository messageRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, serverRepository, serverMemberRepository,
                messageRepository, passwordEncoder);
    }

    @Test
    void storesABcryptHashThatMatchesTheSubmittedPassword() {
        String password = "\u00e9".repeat(36);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(createRequest(password));

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(user.capture());
        String storedHash = user.getValue().getPasswordHash();
        assertThat(storedHash).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, storedHash)).isTrue();
        assertThat(passwordEncoder.matches("wrong-password", storedHash)).isFalse();
    }

    @Test
    void rejectsAPasswordOver72Utf8BytesBeforeSaving() {
        assertThatThrownBy(() -> userService.createUser(createRequest("\u00e9".repeat(37))))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("Password must be at most 72 bytes in UTF-8.");

        verifyNoInteractions(userRepository);
    }

    private CreateUserRequest createRequest(String password) {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("demo-user");
        request.setEmail("demo-user@example.test");
        request.setPassword(password);
        return request;
    }
}
