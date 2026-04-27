package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({
    "id",
    "username",
    "email",
    "themePreference",
    "languagePreference",
    "active",
    "notes"
})
@Entity
@Table(name = "user")
public class User {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{6,}$";

    private static final int MAX_USERNAME_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 255;
    private static final int MAX_PASSWORD_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(length = MAX_USERNAME_LENGTH, unique = true)
    private String username;

    @Column(nullable = false, unique = true, length = MAX_EMAIL_LENGTH)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = MAX_PASSWORD_LENGTH)
    private String password;

    @Column(length = 20)
    private String themePreference;

    @Column(length = 10)
    private String languagePreference;

    @Column(nullable = false)
    private Boolean active;

    @Column(length = 500)
    private String notes;

    public User() {
    }

    public User(
            String username,
            String email,
            String password,
            String themePreference,
            String languagePreference,
            Boolean active,
            String notes
    ) {
        this.username = normalizeText(username);
        this.email = normalizeEmail(email);
        this.password = password;
        this.themePreference = normalizeThemePreference(themePreference);
        this.languagePreference = normalizeLanguagePreference(languagePreference);
        this.active = active != null ? active : true;
        this.notes = normalizeText(notes);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = normalizeText(username);
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

    public String getThemePreference() {
        return themePreference;
    }

    public void setThemePreference(String themePreference) {
        this.themePreference = normalizeThemePreference(themePreference);
    }

    public String getLanguagePreference() {
        return languagePreference;
    }

    public void setLanguagePreference(String languagePreference) {
        this.languagePreference = normalizeLanguagePreference(languagePreference);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active != null ? active : true;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = normalizeText(notes);
    }

    public static boolean isUsernameValid(String username) {
        return username == null
                || username.isBlank()
                || username.trim().length() <= MAX_USERNAME_LENGTH;
    }

    public static boolean isEmailValid(String email) {
        return email != null
                && !email.isBlank()
                && email.trim().length() <= MAX_EMAIL_LENGTH
                && email.trim().toLowerCase().matches(EMAIL_REGEX);
    }

    public static boolean isPasswordValid(String password) {
        return password != null
                && !password.isBlank()
                && password.length() <= MAX_PASSWORD_LENGTH
                && password.matches(PASSWORD_REGEX);
    }

    public static boolean isThemePreferenceValid(String themePreference) {
        if (themePreference == null || themePreference.isBlank()) {
            return true;
        }

        String normalized = themePreference.trim().toUpperCase();

        return normalized.equals("LIGHT")
                || normalized.equals("DARK")
                || normalized.equals("SYSTEM");
    }

    public static boolean isLanguagePreferenceValid(String languagePreference) {
        return languagePreference == null
                || languagePreference.isBlank()
                || languagePreference.trim().length() <= 10;
    }

    public static boolean isNotesValid(String notes) {
        return notes == null
                || notes.isBlank()
                || notes.trim().length() <= 500;
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }

    public static String normalizeThemePreference(String themePreference) {
        return themePreference == null ? null : themePreference.trim().toUpperCase();
    }

    public static String normalizeLanguagePreference(String languagePreference) {
        return languagePreference == null ? null : languagePreference.trim().toLowerCase();
    }
}