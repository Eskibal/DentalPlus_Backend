package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

	private final JwtConfig jwtConfig;
	private final PrivateKey privateKey;
	private final PublicKey publicKey;

	public JwtService(JwtConfig jwtConfig) {
		this.jwtConfig = jwtConfig;
		this.privateKey = loadPrivateKey(jwtConfig.getPrivateKey());
		this.publicKey = loadPublicKey(jwtConfig.getPublicKey());
	}

	public String generateToken(Long userId) {
		Instant now = Instant.now();
		Instant expiration = now.plusMillis(jwtConfig.getExpirationMs());

		return Jwts.builder().subject(String.valueOf(userId)).issuedAt(Date.from(now)).expiration(Date.from(expiration))
				.signWith(privateKey, Jwts.SIG.RS256).compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Long extractUserId(String token) {
		Claims claims = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();

		return Long.parseLong(claims.getSubject());
	}

	public String extractToken(String authorizationHeader) {
		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			return null;
		}

		if (!authorizationHeader.startsWith("Bearer ")) {
			return null;
		}

		String token = authorizationHeader.substring(7).trim();
		return token.isBlank() ? null : token;
	}

	private PrivateKey loadPrivateKey(String pem) {
		try {
			String normalized = normalizePem(pem).replace("-----BEGIN PRIVATE KEY-----", "")
					.replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");

			byte[] decoded = Base64.getDecoder().decode(normalized);
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			return keyFactory.generatePrivate(spec);
		} catch (Exception e) {
			throw new IllegalStateException("Invalid private key configuration", e);
		}
	}

	private PublicKey loadPublicKey(String pem) {
		try {
			String normalized = normalizePem(pem).replace("-----BEGIN PUBLIC KEY-----", "")
					.replace("-----END PUBLIC KEY-----", "").replaceAll("\\s", "");

			byte[] decoded = Base64.getDecoder().decode(normalized);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			return keyFactory.generatePublic(spec);
		} catch (Exception e) {
			throw new IllegalStateException("Invalid public key configuration", e);
		}
	}

	private String normalizePem(String pem) {
		if (pem == null) {
			return "";
		}

		return pem.replace("\\n", "\n").trim();
	}
}