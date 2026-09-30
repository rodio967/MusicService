package com.example.user.oauth.crypto;

import jakarta.persistence.AttributeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class EncryptedTokenConverter implements AttributeConverter<String, String> {

    private final TokenEncryptor tokenEncryptor;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : tokenEncryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : tokenEncryptor.decrypt(dbData);
    }
}
