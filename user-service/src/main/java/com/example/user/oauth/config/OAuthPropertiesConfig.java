package com.example.user.oauth.config;


import com.example.user.oauth.properties.OAuthFrontendProperties;
import com.example.user.oauth.properties.OAuthHttpProperties;
import com.example.user.oauth.properties.SpotifyOAuthProperties;
import com.example.user.oauth.properties.TokenEncryptionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        SpotifyOAuthProperties.class,
        OAuthFrontendProperties.class,
        OAuthHttpProperties.class,
        TokenEncryptionProperties.class
})
public class OAuthPropertiesConfig {
}
