package com.alfha.discord_clone.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    private static final String MESSAGES = "/api/servers/2/channels/3/messages";
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T12:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @Test
    void createsAMessageAndReturnsItsLocation() throws Exception {
        when(messageService.createMessage(2L, 3L, new CreateMessageRequest(4L, "Hello!")))
                .thenReturn(new MessageDto(5L, 3L, 4L, "Hello!", CREATED_AT));

        mockMvc.perform(post(MESSAGES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":4,\"content\":\"Hello!\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost" + MESSAGES + "/5"))
                .andExpect(jsonPath("$.createdAt").value(CREATED_AT.toString()));
    }

    @Test
    void rejectsBlankMessageContentBeforeCallingTheService() throws Exception {
        mockMvc.perform(post(MESSAGES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":4,\"content\":\"  \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }

    @Test
    void listsMessagesUsingTheRequestedCursorAndLimit() throws Exception {
        when(messageService.getMessages(2L, 3L, 4L, 1))
                .thenReturn(new MessagePage(List.of(new MessageDto(5L, 3L, 4L, "Hello!", CREATED_AT)), 5L));

        mockMvc.perform(get(MESSAGES).param("afterId", "4").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].id").value(5))
                .andExpect(jsonPath("$.nextAfterId").value(5));
    }

    @ParameterizedTest
    @ValueSource(strings = {"?afterId=-1", "?limit=0", "?limit=101"})
    void rejectsInvalidHistoryParametersBeforeCallingTheService(String query) throws Exception {
        mockMvc.perform(get(MESSAGES + query)).andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }
}
