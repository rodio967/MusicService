package com.example.provider.controller;

import com.example.common.MusicPlatform;
import com.example.provider.exception.RateLimitedException;
import com.example.provider.model.CursorPage;
import com.example.provider.model.PlaylistSummary;
import com.example.provider.service.ProviderFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProviderController.class, properties = "internal.api.secret=test-secret")
class ProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProviderFacade facade;

    @Test
    void returnsPlaylistPage() throws Exception {
        when(facade.fetchPlaylists(42L, "spotify", "20", 10)).thenReturn(new CursorPage<>(
                List.of(new PlaylistSummary("p1", "Для бега", 42, null, true)), "30"));

        mockMvc.perform(request("/api/providers/spotify/playlists?cursor=20&limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("p1"))
                .andExpect(jsonPath("$.items[0].transferable").value(true))
                .andExpect(jsonPath("$.nextCursor").value("30"));
    }

    @Test
    void usesFirstPageAndDefaultLimit() throws Exception {
        when(facade.fetchPlaylists(anyLong(), anyString(), any(), anyInt()))
                .thenReturn(new CursorPage<>(List.of(), null));

        mockMvc.perform(request("/api/providers/spotify/playlists"))
                .andExpect(status().isOk());

        verify(facade).fetchPlaylists(42L, "spotify", null, 20);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "51", "abc"})
    void rejectsInvalidLimit(String limit) throws Exception {
        mockMvc.perform(request("/api/providers/spotify/playlists?limit=" + limit))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        verifyNoInteractions(facade);
    }

    @Test
    void rateLimitReturnsRetryAfterHeader() throws Exception {
        when(facade.fetchPlaylists(anyLong(), anyString(), any(), anyInt()))
                .thenThrow(new RateLimitedException(MusicPlatform.SPOTIFY, Duration.ofSeconds(30)));

        mockMvc.perform(request("/api/providers/spotify/playlists"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "30"))
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
    }

    private static MockHttpServletRequestBuilder request(String url) {
        return get(url)
                .header("X-Internal-Secret", "test-secret")
                .header("X-User-Id", "42");
    }
}
