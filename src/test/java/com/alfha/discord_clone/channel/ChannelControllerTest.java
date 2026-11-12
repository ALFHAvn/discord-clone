package com.alfha.discord_clone.channel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChannelController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChannelService channelService;

    @Test
    void createsATextChannelUnderTheRequestedServer() throws Exception {
        when(channelService.createChannel(2L, new CreateChannelRequest("general", ChannelType.TEXT)))
                .thenReturn(new ChannelDto(3L, 2L, "general", ChannelType.TEXT));

        mockMvc.perform(post("/api/servers/2/channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"general\",\"type\":\"TEXT\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/api/servers/2/channels/3"))
                .andExpect(jsonPath("$.serverId").value(2))
                .andExpect(jsonPath("$.type").value("TEXT"));
    }

    @Test
    void rejectsAnUnsupportedChannelTypeBeforeCallingTheService() throws Exception {
        mockMvc.perform(post("/api/servers/2/channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"general\",\"type\":\"VOICE\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(channelService);
    }

    @Test
    void returnsNotFoundWhenTheChannelDoesNotBelongToTheServer() throws Exception {
        when(channelService.getChannel(2L, 3L)).thenThrow(new ChannelNotFoundException(2L, 3L));

        mockMvc.perform(get("/api/servers/2/channels/3"))
                .andExpect(status().isNotFound());
    }
}
