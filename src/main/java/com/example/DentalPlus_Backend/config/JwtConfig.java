package com.example.DentalPlus_Backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

	@Value("${auth.token.private-key}")
	private String privateKey;

	@Value("${auth.token.public-key}")
	private String publicKey;

	@Value("${auth.token.expiration-ms}")
	private Long expirationMs;

	public String getPrivateKey() {
		return privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public Long getExpirationMs() {
		return expirationMs;
	}
}