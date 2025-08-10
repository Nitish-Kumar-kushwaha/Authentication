package com.security.authentication.repository;

import com.security.authentication.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    Optional<RevokedToken> findByTokenId(String tokenId);

    @Modifying
    @Query("DELETE FROM RevokedToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Timestamp now);

    @Query("SELECT COUNT(rt) > 0 FROM RevokedToken rt WHERE rt.tokenId = :tokenId")
    boolean isTokenRevoked(@Param("tokenId") String tokenId);
} 