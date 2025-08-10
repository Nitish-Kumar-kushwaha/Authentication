package com.security.authentication.security;

import com.security.authentication.repository.RevokedTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final String issuer;
    private final SecretKey key;
    private final long accessTokenMinutes;
    private final RevokedTokenRepository revokedTokenRepository;

    public JwtService(
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-minutes}") long accessTokenMinutes,
            RevokedTokenRepository revokedTokenRepository
    ) {
        this.issuer = issuer;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessTokenMinutes;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(accessTokenMinutes * 60);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .subject(subject)
                .id(jti)
                .claims(claims == null ? Map.of() : claims)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getTokenId(String token) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload().getId();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenRevoked(String token) {
        String tokenId = getTokenId(token);
        return tokenId != null && revokedTokenRepository.isTokenRevoked(tokenId);
    }

    public boolean isValidAndNotRevoked(String token) {
        return isValid(token) && !isTokenRevoked(token);
    }

    public Timestamp getExpiration(String token) {
        try {
            Date expiration = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload().getExpiration();
            return new Timestamp(expiration.getTime());
        } catch (Exception e) {
            return null;
        }
    }
}
