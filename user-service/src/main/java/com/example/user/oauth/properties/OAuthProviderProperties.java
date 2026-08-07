package com.example.user.oauth.properties;

import java.net.URI;
import java.util.Set;

public interface OAuthProviderProperties {

    String clientId();
    String clientSecret();
    URI redirectUri();
    URI authUri();
    URI tokenUri();
    URI profileUri();
    Set<String> scopes();
}
