package com.example.user.oauth.client;

import com.example.user.oauth.dto.OAuthTokenResponse;
import com.example.user.oauth.dto.ProviderAccountInfo;
import com.example.user.oauth.exception.ReauthorizationRequiredException;
import com.example.user.oauth.properties.YoutubeOAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class YoutubeOAuthClientTest {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String PROFILE_URI = "https://openidconnect.googleapis.com/v1/userinfo";
    private static final String BASIC_AUTH =
            "Basic " + Base64.getEncoder().encodeToString("client-id:client-secret".getBytes());

    private MockRestServiceServer server;
    private YoutubeOAuthClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        YoutubeOAuthProperties properties = new YoutubeOAuthProperties(
                "client-id",
                "client-secret",
                URI.create("http://127.0.0.1:8081/api/oauth/callback/youtube"),
                URI.create("https://accounts.google.com/o/oauth2/v2/auth"),
                URI.create(TOKEN_URI),
                URI.create(PROFILE_URI),
                Set.of("openid", "https://www.googleapis.com/auth/youtube")
        );
        client = new YoutubeOAuthClient(properties, builder.build());
    }

    @Test
    void authorizationUriRequestsOfflineAccessAndConsent() {
        MultiValueMap<String, String> params = UriComponentsBuilder
                .fromUri(client.buildAuthorizationUri("state-123"))
                .build()
                .getQueryParams();

        assertThat(params.getFirst("access_type")).isEqualTo("offline");
        assertThat(params.getFirst("prompt")).isEqualTo("consent");
        assertThat(params.getFirst("state")).isEqualTo("state-123");
        assertThat(params.getFirst("response_type")).isEqualTo("code");
        assertThat(params.getFirst("scope")).contains("openid", "https://www.googleapis.com/auth/youtube");
    }

    @Test
    void exchangesCodeForTokens() {
        MultiValueMap<String, String> expectedForm = new LinkedMultiValueMap<>();
        expectedForm.add("grant_type", "authorization_code");
        expectedForm.add("code", "auth-code");
        expectedForm.add("redirect_uri", "http://127.0.0.1:8081/api/oauth/callback/youtube");

        server.expect(requestTo(TOKEN_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, BASIC_AUTH))
                .andExpect(content().formData(expectedForm))
                .andRespond(withSuccess("""
                        {"access_token":"ya29.access","expires_in":3599,"refresh_token":"1//refresh",
                         "scope":"openid https://www.googleapis.com/auth/youtube","token_type":"Bearer",
                         "id_token":"ignored"}
                        """, MediaType.APPLICATION_JSON));

        OAuthTokenResponse response = client.exchangeCode("auth-code");

        assertThat(response.accessToken()).isEqualTo("ya29.access");
        assertThat(response.refreshToken()).isEqualTo("1//refresh");
        assertThat(response.expiresIn()).isEqualTo(3599L);
        server.verify();
    }

    @Test
    void mapsGoogleSubjectToProviderAccountId() {
        server.expect(requestTo(PROFILE_URI))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer ya29.access"))
                .andRespond(withSuccess("""
                        {"sub":"110248495921238986420","name":"Test User"}
                        """, MediaType.APPLICATION_JSON));

        ProviderAccountInfo account = client.fetchProviderAccount("ya29.access");

        assertThat(account.id()).isEqualTo("110248495921238986420");
        assertThat(account.displayName()).isEqualTo("Test User");
    }

    @Test
    void refreshWithRevokedTokenRequiresReauthorization() {
        server.expect(requestTo(TOKEN_URI))
                .andExpect(content().formData(refreshForm("1//revoked")))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"error":"invalid_grant","error_description":"Token has been expired or revoked."}
                                """));

        assertThatThrownBy(() -> client.refreshAccessToken("1//revoked"))
                .isInstanceOf(ReauthorizationRequiredException.class);
    }

    @Test
    void refreshWithoutNewRefreshTokenKeepsItNull() {
        server.expect(requestTo(TOKEN_URI))
                .andExpect(content().formData(refreshForm("1//refresh")))
                .andRespond(withSuccess("""
                        {"access_token":"ya29.new","expires_in":3599,"token_type":"Bearer"}
                        """, MediaType.APPLICATION_JSON));

        OAuthTokenResponse response = client.refreshAccessToken("1//refresh");

        assertThat(response.accessToken()).isEqualTo("ya29.new");
        assertThat(response.refreshToken()).isNull();
    }

    private static MultiValueMap<String, String> refreshForm(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        return form;
    }
}
