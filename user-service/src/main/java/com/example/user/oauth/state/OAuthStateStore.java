package com.example.user.oauth.state;

public interface OAuthStateStore {

    void save(String state, Long userId);

    Long getAndRemove(String state);
}
