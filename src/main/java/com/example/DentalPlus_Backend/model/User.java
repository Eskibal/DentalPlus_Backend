package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({
    "id",
    "username"
})
@Entity
@Table(name = "User")
public class User {

    private static final int MAX_USERNAME_LENGTH = 100;
    private static final int MAX_PASSWORD_LENGTH = 255;
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{6,}$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(nullable = false, unique = true, length = MAX_USERNAME_LENGTH)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = MAX_PASSWORD_LENGTH)
    private String password;

    public User() {
    }

    public User(String username, String password) {
        this.username = normalizeText(username);
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = normalizeText(username);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static boolean isUsernameValid(String username) {
        return username != null
                && !username.isBlank()
                && username.trim().length() <= MAX_USERNAME_LENGTH;
    }

    public static boolean isPasswordValid(String password) {
        return password != null
                && !password.isBlank()
                && password.length() <= MAX_PASSWORD_LENGTH
                && password.matches(PASSWORD_REGEX);
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}