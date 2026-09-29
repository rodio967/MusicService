package com.example.user.oauth.config;


import com.example.user.oauth.properties.OAuthHttpProperties;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient oauthRestClient(RestClient.Builder builder, OAuthHttpProperties httpProperties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(httpProperties.connectTimeout())
                .withReadTimeout(httpProperties.readTimeout());

        return builder
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .build();
    }
}
