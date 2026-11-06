package com.example.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MusicPlatformTest {

    @Test
    void parsesValueIgnoringCase() {
        assertThat(MusicPlatform.toPlatform("spotify")).contains(MusicPlatform.SPOTIFY);
        assertThat(MusicPlatform.toPlatform("YouTube")).contains(MusicPlatform.YOUTUBE);
    }

    @Test
    void returnsEmptyForUnknownOrMissingValue() {
        assertThat(MusicPlatform.toPlatform("deezer")).isEmpty();
        assertThat(MusicPlatform.toPlatform("")).isEmpty();
        assertThat(MusicPlatform.toPlatform(null)).isEmpty();
    }

    @Test
    void pathValueIsLowercase() {
        assertThat(MusicPlatform.SPOTIFY.pathValue()).isEqualTo("spotify");
    }
}
