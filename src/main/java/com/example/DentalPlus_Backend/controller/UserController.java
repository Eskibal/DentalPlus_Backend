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

        if (!User.isTextValid(request.getName())
                || !User.isTextValid(request.getSurname())
                || !User.isEmailValid(request.getEmail())
                || !User.isPasswordValid(request.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid data");
        }

        String email = User.normalizeEmail(request.getEmail());

        if (findByEmail(email) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        User user = new User(
                request.getName(),
                request.getSurname(),
                email,
                encoder.encode(request.getPassword())
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

        return ResponseEntity.ok(buildSafeUser(user));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User request) {

        User user = entityManager.find(User.class, id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (!User.isTextValid(request.getName())
                || !User.isTextValid(request.getSurname())
                || !User.isEmailValid(request.getEmail())) {
            return ResponseEntity.badRequest().body("Invalid data");
        }

        String email = User.normalizeEmail(request.getEmail());

        User existing = findByEmail(email);
        if (existing != null && !existing.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }

        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setEmail(email);

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
            return ResponseEntity.notFound().build();
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

        String email = User.normalizeEmail(request.getEmail());
        User user = findByEmail(email);

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

    private User buildSafeUser(User user) {
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setName(user.getName());
        safeUser.setSurname(user.getSurname());
        safeUser.setEmail(user.getEmail());
        safeUser.setActive(user.getActive());
        return safeUser;
    }
}