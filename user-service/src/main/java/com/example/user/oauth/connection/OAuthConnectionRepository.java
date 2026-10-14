package com.example.user.oauth.connection;

import com.example.user.enums.MusicPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthConnectionRepository extends JpaRepository<OAuthConnectionEntity, Long> {

    Optional<OAuthConnectionEntity> findByUserIdAndPlatform(Long userId, MusicPlatform platform);
}
