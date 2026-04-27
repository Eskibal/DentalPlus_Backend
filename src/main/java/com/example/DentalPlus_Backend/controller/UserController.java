package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @PersistenceContext
    private EntityManager entityManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping
    @Transactional
    public ResponseEntity<?> createUser(@RequestBody User request) {

        if (!User.isUsernameValid(request.getUsername())) {
            return ResponseEntity.badRequest().body("Invalid username");
        }

        if (!User.isEmailValid(request.getEmail())) {
            return ResponseEntity.badRequest().body("Invalid email");
        }

        if (!User.isPasswordValid(request.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password");
        }

        if (!User.isThemePreferenceValid(request.getThemePreference())) {
            return ResponseEntity.badRequest().body("Invalid themePreference");
        }

        if (!User.isLanguagePreferenceValid(request.getLanguagePreference())) {
            return ResponseEntity.badRequest().body("Invalid languagePreference");
        }

        if (!User.isNotesValid(request.getNotes())) {
            return ResponseEntity.badRequest().body("Invalid notes");
        }

        String normalizedEmail = User.normalizeEmail(request.getEmail());

        if (findByEmail(normalizedEmail) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            User userWithSameUsername = findByUsername(request.getUsername().trim());
            if (userWithSameUsername != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
            }
        }

        User user = new User(
                request.getUsername(),
                normalizedEmail,
                encoder.encode(request.getPassword()),
                request.getThemePreference(),
                request.getLanguagePreference(),
                request.getActive(),
                request.getNotes()
        );

        entityManager.persist(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(buildSafeUser(user));
    }

    @GetMapping
    public List<UserResponse> getAll() {
        List<User> users = entityManager
                .createQuery("FROM User", User.class)
                .getResultList();

        return users.stream()
                .map(this::buildSafeUser)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        User user = entityManager.find(User.class, id);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok(buildSafeUser(user));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User request) {

        User user = entityManager.find(User.class, id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (!User.isUsernameValid(request.getUsername())) {
            return ResponseEntity.badRequest().body("Invalid username");
        }

        if (!User.isEmailValid(request.getEmail())) {
            return ResponseEntity.badRequest().body("Invalid email");
        }

        if (!User.isThemePreferenceValid(request.getThemePreference())) {
            return ResponseEntity.badRequest().body("Invalid themePreference");
        }

        if (!User.isLanguagePreferenceValid(request.getLanguagePreference())) {
            return ResponseEntity.badRequest().body("Invalid languagePreference");
        }

        if (!User.isNotesValid(request.getNotes())) {
            return ResponseEntity.badRequest().body("Invalid notes");
        }

        String normalizedEmail = User.normalizeEmail(request.getEmail());

        User existingByEmail = findByEmail(normalizedEmail);
        if (existingByEmail != null && !existingByEmail.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            User existingByUsername = findByUsername(request.getUsername().trim());
            if (existingByUsername != null && !existingByUsername.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already in use");
            }
        }

        user.setUsername(request.getUsername());
        user.setEmail(normalizedEmail);
        user.setThemePreference(request.getThemePreference());
        user.setLanguagePreference(request.getLanguagePreference());
        user.setNotes(request.getNotes());

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (!User.isPasswordValid(request.getPassword())) {
                return ResponseEntity.badRequest().body("Invalid password");
            }
            user.setPassword(encoder.encode(request.getPassword()));
        }

        entityManager.merge(user);

        return ResponseEntity.ok(buildSafeUser(user));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = entityManager.find(User.class, id);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        entityManager.remove(user);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {

        if (!User.isEmailValid(request.getEmail())
                || request.getPassword() == null
                || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        String normalizedEmail = User.normalizeEmail(request.getEmail());
        User user = findByEmail(normalizedEmail);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        if (!user.getActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Inactive user");
        }

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        return ResponseEntity.ok(buildSafeUser(user));
    }

    private User findByEmail(String email) {
        return entityManager
                .createQuery("FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private User findByUsername(String username) {
        return entityManager
                .createQuery("FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private UserResponse buildSafeUser(User user) {
        return new UserResponse(user);
    }

    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String themePreference;
        private String languagePreference;
        private Boolean active;
        private String notes;

        public UserResponse(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.themePreference = user.getThemePreference();
            this.languagePreference = user.getLanguagePreference();
            this.active = user.getActive();
            this.notes = user.getNotes();
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getThemePreference() {
            return themePreference;
        }

        public String getLanguagePreference() {
            return languagePreference;
        }

        public Boolean getActive() {
            return active;
        }

        public String getNotes() {
            return notes;
        }
    }
}