package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @PersistenceContext
    private EntityManager entityManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/dentist")
    @Transactional
    public ResponseEntity<?> createDentist(@RequestBody Dentist request) {

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
    public List<User> getAll() {
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
            return ResponseEntity.notFound().build();
        }

        if (!Dentist.isTextValid(request.getSpeciality())) {
            return ResponseEntity.badRequest().body("Invalid speciality");
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

        User existingUser = entityManager
                .createQuery("FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        User newUser = new User(username, encoder.encode(requestUser.getPassword()));
        entityManager.persist(newUser);

        Dentist newDentist = new Dentist(
                newUser,
                request.getSpeciality(),
                request.getVisitWeekday(),
                request.getCity(),
                request.getDirection()
        );

        entityManager.persist(newDentist);

        return ResponseEntity.status(HttpStatus.CREATED).body(buildSafeDentistResponse(newDentist));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {

        if (!User.isUsernameValid(request.getUsername())
                || request.getPassword() == null
                || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        String normalizedEmail = User.normalizeEmail(request.getEmail());
        User user = findByEmail(normalizedEmail);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        Dentist dentist = entityManager.find(Dentist.class, user.getId());

        if (dentist == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dentist profile not found");
        }

        return ResponseEntity.ok(buildSafeDentistResponse(dentist));
    }

    private User findByUsername(String username) {
        return entityManager
                .createQuery("FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private User buildSafeUser(User user) {
        User safeUser = new User();
        safeUser.setUsername(user.getUsername());
        safeUser.setEmail(user.getEmail());
        safeUser.setThemePreference(user.getThemePreference());
        safeUser.setLanguagePreference(user.getLanguagePreference());
        safeUser.setActive(user.getActive());
        safeUser.setNotes(user.getNotes());
        return safeUser;
    }
}