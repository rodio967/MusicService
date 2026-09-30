package com.example.user.oauth.crypto;

import com.example.user.oauth.properties.TokenEncryptionProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;


@Component
public class TokenEncryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH_BYTES = 32;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final String VERSION_PREFIX = "v1:";

    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenEncryptor(TokenEncryptionProperties properties) {
        this.key = parseKey(properties.tokenSecret());
    }

    public String encrypt(String plaintext) {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] payload = ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();

            return VERSION_PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Не удалось зашифровать токен", ex);
        }
    }

    public String decrypt(String encrypted) {
        if (!encrypted.startsWith(VERSION_PREFIX)) {
            throw new IllegalStateException("Неизвестный формат зашифрованного токена");
        }

        byte[] payload;
        try {
            payload = Base64.getDecoder().decode(encrypted.substring(VERSION_PREFIX.length()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Зашифрованный токен повреждён", ex);
        }

        if (payload.length <= IV_LENGTH_BYTES) {
            throw new IllegalStateException("Зашифрованный токен повреждён");
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, payload, 0, IV_LENGTH_BYTES));
            byte[] plaintext = cipher.doFinal(payload, IV_LENGTH_BYTES, payload.length - IV_LENGTH_BYTES);

            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Не удалось расшифровать токен: неверный ключ или данные изменены", ex);
        }
    }

    private static SecretKey parseKey(String base64Key) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Key);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("encryption.token-secret должен быть в Base64", ex);
        }

        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "encryption.token-secret должен содержать " + KEY_LENGTH_BYTES + " байта (AES-256)"
            );
        }

        return new SecretKeySpec(keyBytes, "AES");
    }
}
