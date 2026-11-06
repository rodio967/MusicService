package com.example.provider.provider.spotify;

import com.example.provider.exception.InvalidCursorException;
import com.example.provider.exception.ProviderAccessDeniedException;
import com.example.provider.exception.ProviderUnavailableException;
import com.example.provider.exception.RateLimitedException;
import com.example.provider.exception.ReauthorizationRequiredException;
import com.example.provider.model.CursorPage;
import com.example.provider.model.PlatformAccess;
import com.example.provider.model.PlaylistSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SpotifyMusicProviderTest {

    private static final String BASE_URL = "https://api.spotify.test/v1";
    private static final PlatformAccess ACCESS = new PlatformAccess("spotify-token", "me");

    private MockRestServiceServer server;
    private SpotifyMusicProvider provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultStatusHandler(HttpStatusCode::isError, new SpotifyErrorHandler());
        server = MockRestServiceServer.bindTo(builder).build();

        provider = new SpotifyMusicProvider(builder.build());
    }

    @Test
    void mapsPlaylistsAndSendsBearerToken() {
        server.expect(requestTo(BASE_URL + "/me/playlists?limit=20&offset=0"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer spotify-token"))
                .andRespond(withSuccess("""
                        {
                          "href": "ignored",
                          "items": [
                            {
                              "id": "own", "name": "Свой", "collaborative": false, "public": true,
                              "owner": { "id": "me", "display_name": "Я" },
                              "images": [ { "url": "https://img/own.jpg", "width": 640 } ],
                              "items": { "total": 42, "href": "ignored" }
                            },
                            {
                              "id": "collab", "name": "Совместный", "collaborative": true,
                              "owner": { "id": "friend" },
                              "images": [],
                              "items": { "total": 7 }
                            },
                            {
                              "id": "foreign", "name": "Чужой", "collaborative": false,
                              "owner": { "id": "spotify" },
                              "images": null
                            }
                          ],
                          "next": null
                        }
                        """, MediaType.APPLICATION_JSON));

        CursorPage<PlaylistSummary> page = provider.getPlaylists(ACCESS, null, 20);

        assertThat(page.items()).containsExactly(
                new PlaylistSummary("own", "Свой", 42, "https://img/own.jpg", true),
                new PlaylistSummary("collab", "Совместный", 7, null, true),
                new PlaylistSummary("foreign", "Чужой", null, null, false)
        );
        server.verify();
    }

    @Test
    void readsItemCountFromOldTracksField() {
        server.expect(requestTo(BASE_URL + "/me/playlists?limit=20&offset=0"))
                .andRespond(withSuccess("""
                        {"items": [{"id": "p", "name": "P", "owner": {"id": "me"}, "tracks": {"total": 5}}], "next": null}
                        """, MediaType.APPLICATION_JSON));

        assertThat(provider.getPlaylists(ACCESS, null, 20).items().get(0).itemCount()).isEqualTo(5);
    }

    @Test
    void returnsNextCursorWhenSpotifyHasMorePages() {
        server.expect(requestTo(BASE_URL + "/me/playlists?limit=20&offset=20"))
                .andRespond(withSuccess("""
                        {"items": [], "next": "https://api.spotify.com/v1/me/playlists?offset=40&limit=20"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(provider.getPlaylists(ACCESS, "20", 20).nextCursor()).isEqualTo("40");
    }

    @Test
    void returnsNullCursorOnLastPage() {
        server.expect(requestTo(BASE_URL + "/me/playlists?limit=20&offset=40"))
                .andRespond(withSuccess("""
                        {"items": [], "next": null}
                        """, MediaType.APPLICATION_JSON));

        assertThat(provider.getPlaylists(ACCESS, "40", 20).nextCursor()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "-1", "1.5"})
    void rejectsInvalidCursorWithoutCallingSpotify(String cursor) {
        assertThatThrownBy(() -> provider.getPlaylists(ACCESS, cursor, 20))
                .isInstanceOf(InvalidCursorException.class);
        server.verify();
    }

    @Test
    void unauthorizedMeansReauthorizationRequired() {
        respondWith(HttpStatus.UNAUTHORIZED);

        assertThatThrownBy(() -> provider.getPlaylists(ACCESS, null, 20))
                .isInstanceOf(ReauthorizationRequiredException.class);
    }

    @Test
    void forbiddenMeansAccessDenied() {
        respondWith(HttpStatus.FORBIDDEN);

        assertThatThrownBy(() -> provider.getPlaylists(ACCESS, null, 20))
                .isInstanceOf(ProviderAccessDeniedException.class);
    }

    @Test
    void tooManyRequestsKeepsRetryAfter() {
        server.expect(requestTo(BASE_URL + "/me/playlists?limit=20&offset=0"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).header(HttpHeaders.RETRY_AFTER, "30"));

        assertThatThrownBy(() -> provider.getPlaylists(ACCESS, null, 20))
                .isInstanceOfSatisfying(RateLimitedException.class,
                        ex -> assertThat(ex.getRetryAfter()).isEqualTo(Duration.ofSeconds(30)));
    }

    @Test
    void serverErrorMeansProviderUnavailable() {
        respondWith(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThatThrownBy(() -> provider.getPlaylists(ACCESS, null, 20))
                .isInstanceOf(ProviderUnavailableException.class);
    }

    @Test
    void timeoutMeansProviderUnavailable() {
        server.expect(requestTo(BASE_URL + "/me/playlists?limit=20&offset=0"))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> provider.getPlaylists(ACCESS, null, 20))
                .isInstanceOf(ProviderUnavailableException.class);
    }

    @Test
    void malformedJsonMeansProviderUnavailable() {
        server.expect(requestTo(BASE_URL + "/me/playlists?limit=20&offset=0"))
                .andRespond(withSuccess("{ not json", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.getPlaylists(ACCESS, null, 20))
                .isInstanceOf(ProviderUnavailableException.class);
    }

    @Test
    void emptyBodyMeansProviderUnavailable() {
        server.expect(requestTo(BASE_URL + "/me/playlists?limit=20&offset=0"))
                .andRespond(withSuccess());

        assertThatThrownBy(() -> provider.getPlaylists(ACCESS, null, 20))
                .isInstanceOf(ProviderUnavailableException.class);
    }

    private void respondWith(HttpStatus status) {
        server.expect(requestTo(BASE_URL + "/me/playlists?limit=20&offset=0"))
                .andRespond(withStatus(status));
    }
}
