package com.example.provider.provider.spotify.config;

import com.example.provider.provider.spotify.SpotifyErrorHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SpotifyProperties.class)
public class SpotifyClientConfig {

    @Bean
    public RestClient spotifyRestClient(RestClient.Builder builder, SpotifyProperties properties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(properties.connectTimeout())
                .withReadTimeout(properties.readTimeout());

        return builder
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .defaultStatusHandler(HttpStatusCode::isError, new SpotifyErrorHandler())
                .build();
    }
}
