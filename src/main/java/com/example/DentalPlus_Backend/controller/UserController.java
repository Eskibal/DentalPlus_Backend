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

        if (request.getUser() == null) {
            return ResponseEntity.badRequest().body("User data required");
        }

        User requestUser = request.getUser();

        if (!User.isUsernameValid(requestUser.getUsername())) {
            return ResponseEntity.badRequest().body("Invalid username");
        }

        if (!User.isPasswordValid(requestUser.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password");
        }

        if (!Dentist.isTextValid(request.getSpeciality())) {
            return ResponseEntity.badRequest().body("Invalid speciality");
        }

        if (!Dentist.isWeekdayValid(request.getVisitWeekday())) {
            return ResponseEntity.badRequest().body("Invalid visitWeekday");
        }

        if (!Dentist.isTextValid(request.getCity())) {
            return ResponseEntity.badRequest().body("Invalid city");
        }

        if (!Dentist.isTextValid(request.getDirection())) {
            return ResponseEntity.badRequest().body("Invalid direction");
        }

        String username = User.normalizeText(requestUser.getUsername());

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

        String username = User.normalizeText(request.getUsername());

        User user = entityManager
                .createQuery("FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);

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

    private Object buildSafeDentistResponse(Dentist dentist) {
        return new Object() {
            public final Long userId = dentist.getUserId();
            public final String username = dentist.getUser().getUsername();
            public final String speciality = dentist.getSpeciality();
            public final String visitWeekday = dentist.getVisitWeekday();
            public final String city = dentist.getCity();
            public final String direction = dentist.getDirection();
        };
    }
}