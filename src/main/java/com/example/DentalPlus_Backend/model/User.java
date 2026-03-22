package com.example.DentalPlus_Backend.model;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
    "id",
    "name",
    "surname",
    "email",
    "active"
})
@Entity
@Table(name = "User")
public class User {

	private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

	private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{6,}$";

	private static final int MAX_TEXT_LENGTH = 150;
	private static final int MAX_EMAIL_LENGTH = 255;
	private static final int MAX_PASSWORD_LENGTH = 255;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@Column(nullable = false, length = MAX_TEXT_LENGTH)
	private String name;

	@Column(nullable = false, length = MAX_TEXT_LENGTH)
	private String surname;

	@Column(nullable = false, unique = true, length = MAX_EMAIL_LENGTH)
	private String email;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(nullable = false, length = MAX_PASSWORD_LENGTH)
	private String password;

	@Column(nullable = false)
	private Boolean active = true;

	public User() {
	}

	public User(String name, String surname, String email, String password) {
		this.name = normalizeText(name);
		this.surname = normalizeText(surname);
		this.email = normalizeEmail(email);
		this.password = password;
		this.active = true;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = normalizeText(name);
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = normalizeText(surname);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = normalizeEmail(email);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active != null ? active : true;
	}

	public static boolean isTextValid(String text) {
		return text != null && !text.isBlank() && text.trim().length() <= MAX_TEXT_LENGTH;
	}

	public static boolean isEmailValid(String email) {
		return email != null && !email.isBlank() && email.trim().length() <= MAX_EMAIL_LENGTH
				&& email.trim().toLowerCase().matches(EMAIL_REGEX);
	}

	public static boolean isPasswordValid(String password) {
	    return password != null
	            && !password.isBlank()
	            && password.length() <= MAX_PASSWORD_LENGTH
	            && password.matches(PASSWORD_REGEX);
	}

	public static String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

	public static String normalizeText(String text) {
		return text == null ? null : text.trim();
	}
}