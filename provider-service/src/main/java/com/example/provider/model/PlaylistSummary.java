package com.example.provider.model;

/**
 * @param itemCount    {@code null}, если платформа не сообщает число треков (у чужих плейлистов Spotify)
 * @param transferable можно ли прочитать треки плейлиста, то есть перенести его
 *
 * Описание одного плейлиста в нашем формате
 */
public record PlaylistSummary(
        String id,
        String name,
        Integer itemCount,
        String imageUrl,
        boolean transferable
) {}
