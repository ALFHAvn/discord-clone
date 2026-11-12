package com.alfha.discord_clone.server;

import com.alfha.discord_clone.user.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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

@WebMvcTest({ServerController.class, ServerMemberController.class})
@AutoConfigureMockMvc(addFilters = false)
class ServerControllerTest {

    private static final String VALID_REQUEST = """
            {"name":"Study Group","ownerId":1}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServerService serverService;

    @Test
    void createsAServerAndReturnsItsLocation() throws Exception {
        when(serverService.createServer(new CreateServerRequest("Study Group", 1L)))
                .thenReturn(new ServerDto(2L, "Study Group", 1L));

        mockMvc.perform(post("/api/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/api/servers/2"))
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.owner").doesNotExist());
    }

    @Test
    void rejectsAnInvalidRequestBeforeCallingTheService() throws Exception {
        mockMvc.perform(post("/api/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"  ","ownerId":0}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(serverService);
    }

    @Test
    void returnsNotFoundWhenTheOwnerDoesNotExist() throws Exception {
        when(serverService.createServer(new CreateServerRequest("Study Group", 1L)))
                .thenThrow(new UserNotFoundException(1L));

        mockMvc.perform(post("/api/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsServers() throws Exception {
        when(serverService.getAllServers()).thenReturn(List.of(new ServerDto(2L, "Study Group", 1L)));

        mockMvc.perform(get("/api/servers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Study Group"));
    }

    @Test
    void renamesAServer() throws Exception {
        when(serverService.updateServer(2L, new UpdateServerRequest("Weekend Study Group")))
                .thenReturn(new ServerDto(2L, "Weekend Study Group", 1L));

        mockMvc.perform(put("/api/servers/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Weekend Study Group\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Weekend Study Group"))
                .andExpect(jsonPath("$.ownerId").value(1));
    }

    @Test
    void deletesAServerWithAnEmptyResponse() throws Exception {
        mockMvc.perform(delete("/api/servers/2"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(serverService).deleteServer(2L);
    }

    @Test
    void addsAMemberAndReturnsItsLocation() throws Exception {
        when(serverService.addMember(2L, 3L)).thenReturn(new ServerMemberDto(3L, "study-member"));

        mockMvc.perform(post("/api/servers/2/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":3}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/api/servers/2/members/3"))
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.username").value("study-member"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void rejectsAnInvalidMemberBeforeCallingTheService() throws Exception {
        mockMvc.perform(post("/api/servers/2/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":0}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(serverService);
    }

    @Test
    void removesAMemberWithAnEmptyResponse() throws Exception {
        mockMvc.perform(delete("/api/servers/2/members/3"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(serverService).removeMember(2L, 3L);
    }
}
