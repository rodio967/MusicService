package com.example.user.oauth.crypto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EncryptedTokenConverterTest {

    private final TokenEncryptor encryptor = mock(TokenEncryptor.class);
    private final EncryptedTokenConverter converter = new EncryptedTokenConverter(encryptor);

    @Test
    void encryptsOnWriteAndDecryptsOnRead() {
        when(encryptor.encrypt("token")).thenReturn("v1:cipher");
        when(encryptor.decrypt("v1:cipher")).thenReturn("token");

        assertThat(converter.convertToDatabaseColumn("token")).isEqualTo("v1:cipher");
        assertThat(converter.convertToEntityAttribute("v1:cipher")).isEqualTo("token");
    }

    @Test
    void keepsNullAsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        verifyNoInteractions(encryptor);
    }
}
