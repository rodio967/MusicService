package com.example.user.oauth.connection;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthConnectionService {

    private final OAuthConnectionRepository connectionRepository;


}
