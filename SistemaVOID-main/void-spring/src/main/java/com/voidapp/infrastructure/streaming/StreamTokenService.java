package com.voidapp.infrastructure.streaming;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class StreamTokenService {

    @Value("${stream.token.secret:default_stream_secret_change_me_so_its_long_enough}")
    private String secret;

    @Value("${stream.token.ttlSeconds:120}")
    private long ttlSeconds;

    public record StreamTokenPayload(String token, String jti, Date expiresAt) {}

    public StreamTokenPayload generateOneTimeToken(UUID userId, UUID videoId, String sessionId, String ipAddress) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + (ttlSeconds * 1000));
        
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .id(jti)
                .claim("userId", userId.toString())
                .claim("videoId", videoId.toString())
                .claim("sessionId", sessionId)
                .claim("ipAddress", ipAddress)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(key)
                .compact();

        return new StreamTokenPayload(token, jti, expiresAt);
    }
}
