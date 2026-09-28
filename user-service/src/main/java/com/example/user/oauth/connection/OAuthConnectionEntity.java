package com.example.user.oauth.connection;


import com.example.user.entity.User;
import com.example.user.enums.ConnectionStatus;
import com.example.user.enums.MusicPlatform;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "oauth_connection",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "platform"}))
@Getter
@Setter
@NoArgsConstructor
public class OAuthConnectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private MusicPlatform platform;


    @Column(nullable = false)
    private String providerAccountId;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String accessToken;


    @Column(columnDefinition = "TEXT")
    private String refreshToken;


    @Column(nullable = false)
    private Instant expiresAt;


    @Column(length = 500)
    private String scopes;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConnectionStatus status = ConnectionStatus.ACTIVE;


    @Column(nullable = false, updatable = false)
    private Instant createdAt;


    @Column(nullable = false)
    private Instant updatedAt;


    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isTokenValid() {
        return expiresAt.isAfter(Instant.now().plusSeconds(60));
    }

    public boolean isReauthorizationRequired() {
        return status == ConnectionStatus.REAUTHORIZATION_REQUIRED;
    }


}
