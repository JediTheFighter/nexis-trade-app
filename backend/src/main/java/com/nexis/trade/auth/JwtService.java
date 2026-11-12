package com.nexis.trade.auth;

import com.nexis.trade.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

	private final AppProperties properties;
	private SecretKey key;

	public JwtService(AppProperties properties) {
		this.properties = properties;
	}

	@PostConstruct
	void init() {
		byte[] secret = properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
		if (secret.length < 32) {
			throw new IllegalStateException("JWT_SECRET must be at least 32 bytes");
		}
		this.key = Keys.hmacShaKeyFor(secret);
	}

	public String issue(UUID userId, String email) {
		Instant now = Instant.now();
		Instant expires = now.plus(properties.getJwt().getExpirationHours(), ChronoUnit.HOURS);
		return Jwts.builder()
				.subject(userId.toString())
				.claim("email", email)
				.issuedAt(Date.from(now))
				.expiration(Date.from(expires))
				.signWith(key)
				.compact();
	}

	public UUID parseUserId(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return UUID.fromString(claims.getSubject());
	}
}
