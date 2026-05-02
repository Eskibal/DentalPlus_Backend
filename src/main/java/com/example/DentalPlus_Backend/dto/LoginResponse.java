package com.example.DentalPlus_Backend.dto;

public class LoginResponse {

	private String token;
	private Long userId;
	private ProfileDto profile;

	public LoginResponse() {
	}

	public LoginResponse(String token, Long userId, ProfileDto profile) {
		this.token = token;
		this.userId = userId;
		this.profile = profile;
	}

	public String getToken() {
		return token;
	}

	public Long getUserId() {
		return userId;
	}

	public ProfileDto getProfile() {
		return profile;
	}
}