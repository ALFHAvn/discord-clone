package com.alfha.discord_clone.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final String VALID_REQUEST = """
            {
              "username": "demo-user",
              "email": "demo-user@example.test",
              "password": "dummy-password"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"username\":\"  \",\"email\":\"demo-user@example.test\",\"password\":\"dummy-password\"}",
            "{\"username\":\"demo-user\",\"email\":\"\",\"password\":\"dummy-password\"}",
            "{\"username\":\"demo-user\",\"email\":\"invalid-email\",\"password\":\"dummy-password\"}",
            "{\"username\":\"demo-user\",\"email\":\"demo-user@example.test\",\"password\":\"  \"}"
    })
    void rejectsInvalidRequestsBeforeCallingTheService(String body) throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void createsAUserAndReturnsItsLocation() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(new UserDto(1L, "demo-user", "demo-user@example.test"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/api/users/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("demo-user"))
                .andExpect(jsonPath("$.email").value("demo-user@example.test"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void returnsNotFoundForAMissingUser() throws Exception {
        when(userService.getUserById(42L)).thenThrow(new UserNotFoundException(42L));

        mockMvc.perform(get("/api/users/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returnsConflictForADuplicateUser() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new DuplicateUserException(new DataIntegrityViolationException("Database error")));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Username or email is already in use."));
    }

    @Test
    void updatesTheProfileWithoutRequiringAPassword() throws Exception {
        var request = new UpdateUserRequest("new-name", "new@example.test");
        when(userService.updateUser(1L, request))
                .thenReturn(new UserDto(1L, "new-name", "new@example.test"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"new-name","email":"new@example.test"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("new-name"))
                .andExpect(jsonPath("$.email").value("new@example.test"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void rejectsAnInvalidProfileBeforeCallingTheService() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"new-name","email":"invalid-email"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void deletesAUserWithAnEmptyResponse() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).deleteUser(1L);
    }
}
