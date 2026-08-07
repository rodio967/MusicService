package com.example.user.oauth.config;


import com.example.user.oauth.properties.SpotifyOAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        SpotifyOAuthProperties.class
})
public class OAuthPropertiesConfig {
}
