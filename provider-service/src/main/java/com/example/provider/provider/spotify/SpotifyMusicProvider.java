package com.example.provider.provider.spotify;

import com.example.common.MusicPlatform;
import com.example.provider.exception.InvalidCursorException;
import com.example.provider.exception.ProviderUnavailableException;
import com.example.provider.model.CursorPage;
import com.example.provider.model.PlatformAccess;
import com.example.provider.model.PlaylistSummary;
import com.example.provider.provider.MusicProvider;
import com.example.provider.provider.spotify.dto.SpotifyPlaylist;
import com.example.provider.provider.spotify.dto.SpotifyPlaylistPage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;


@Component
public class SpotifyMusicProvider implements MusicProvider {

    private final RestClient restClient;

    public SpotifyMusicProvider(
            @Qualifier("spotifyRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    @Override
    public MusicPlatform getPlatform() {
        return MusicPlatform.SPOTIFY;
    }

    @Override
    public CursorPage<PlaylistSummary> getPlaylists(PlatformAccess access, String cursor, int limit) {
        int offset = toOffset(cursor);

        SpotifyPlaylistPage page = execute(() -> restClient.get()
                .uri("/me/playlists?limit={limit}&offset={offset}", limit, offset)
                .headers(headers -> headers.setBearerAuth(access.accessToken()))
                .retrieve()
                .body(SpotifyPlaylistPage.class));

        List<PlaylistSummary> playlists = page.items().stream()
                .map(playlist -> toSummary(playlist, access.accountId()))
                .toList();

        return new CursorPage<>(playlists, page.next() == null ? null : String.valueOf(offset + limit));
    }

    private PlaylistSummary toSummary(SpotifyPlaylist playlist, String accountId) {
        boolean owned = playlist.owner() != null && Objects.equals(playlist.owner().id(), accountId);

        return new PlaylistSummary(
                playlist.id(),
                playlist.name(),
                playlist.items() == null ? null : playlist.items().total(),
                playlist.images() == null || playlist.images().isEmpty() ? null : playlist.images().get(0).url(),
                owned || playlist.collaborative()
        );
    }


    private int toOffset(String cursor) {
        if (cursor == null || cursor.isBlank()) return 0;

        try {
            int offset = Integer.parseInt(cursor);
            if (offset < 0) throw new InvalidCursorException();
            return offset;
        } catch (NumberFormatException e) {
            throw new InvalidCursorException();
        }
    }


    private <T> T execute(Supplier<T> request) {
        T body;
        try {
            body = request.get();
        } catch (RestClientException ex) {
            throw new ProviderUnavailableException(MusicPlatform.SPOTIFY, ex);
        }

        if (body == null) {
            throw new ProviderUnavailableException(MusicPlatform.SPOTIFY);
        }
        return body;
    }
}
