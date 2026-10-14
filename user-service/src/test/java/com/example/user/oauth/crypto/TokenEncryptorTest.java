package com.example.user.oauth.crypto;

import com.example.user.oauth.properties.TokenEncryptionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenEncryptorTest {

    private TokenEncryptor encryptor;

    @BeforeEach
    void setUp() {
        encryptor = new TokenEncryptor(new TokenEncryptionProperties(randomKey()));
    }

    @Test
    void decryptsWhatWasEncrypted() {
        String encrypted = encryptor.encrypt("spotify-access-token");

        assertThat(encrypted).startsWith("v1:").doesNotContain("spotify-access-token");
        assertThat(encryptor.decrypt(encrypted)).isEqualTo("spotify-access-token");
    }

    @Test
    void sameTokenEncryptsDifferentlyEachTime() {
        String first = encryptor.encrypt("token");
        String second = encryptor.encrypt("token");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void rejectsTamperedCiphertext() {
        String encrypted = encryptor.encrypt("token");
        byte[] payload = Base64.getDecoder().decode(encrypted.substring("v1:".length()));
        payload[payload.length - 1] ^= 1;
        String tampered = "v1:" + Base64.getEncoder().encodeToString(payload);

        assertThatThrownBy(() -> encryptor.decrypt(tampered))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsTokenEncryptedWithAnotherKey() {
        String encrypted = new TokenEncryptor(new TokenEncryptionProperties(randomKey())).encrypt("token");

        assertThatThrownBy(() -> encryptor.decrypt(encrypted))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsPlaintextStoredBeforeEncryption() {
        assertThatThrownBy(() -> encryptor.decrypt("plain-token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("формат");
    }

    @Test
    void rejectsKeyOfWrongLength() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);

        assertThatThrownBy(() -> new TokenEncryptor(new TokenEncryptionProperties(shortKey)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }

    private static String randomKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
