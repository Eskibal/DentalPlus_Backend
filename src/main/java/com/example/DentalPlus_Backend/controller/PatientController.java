package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.Patient;
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
@RequestMapping("/patient")
public class PatientController {

    @PersistenceContext
    private EntityManager entityManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping
    @Transactional
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {

        if (patient.getUser() == null) {
            return ResponseEntity.badRequest().body("User data required");
        }

        User newUser = patient.getUser();

        if (!User.isTextValid(newUser.getName())) {
            return ResponseEntity.badRequest().body("Invalid name");
        }

        if (!User.isTextValid(newUser.getSurname())) {
            return ResponseEntity.badRequest().body("Invalid surname");
        }

        if (!User.isEmailValid(newUser.getEmail())) {
            return ResponseEntity.badRequest().body("Invalid email");
        }

        if (!User.isPasswordValid(newUser.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password");
        }

        String normalizedEmail = User.normalizeEmail(newUser.getEmail());

        User existingUser = entityManager
                .createQuery("FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", normalizedEmail)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }

        String validationError = validatePatientData(patient);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        if (findPatientByNationalId(patient.getNationalId()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("National ID already in use");
        }

        newUser.setEmail(normalizedEmail);
        newUser.setPassword(encoder.encode(newUser.getPassword()));

        entityManager.persist(newUser);

        Patient newPatient = new Patient(newUser, patient.getNationalId());
        applyOptionalPatientFields(newPatient, patient);

        entityManager.persist(newPatient);

        return ResponseEntity.status(HttpStatus.CREATED).body(newPatient);
    }

    @PostMapping("/{userId}")
    @Transactional
    public ResponseEntity<?> createPatientFromUser(@PathVariable Long userId,
                                                   @RequestBody Patient patientData) {

        User existingUser = entityManager.find(User.class, userId);

        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (entityManager.find(Patient.class, userId) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Patient already exists");
        }

        String validationError = validatePatientData(patientData);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        if (findPatientByNationalId(patientData.getNationalId()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("National ID already in use");
        }

        Patient patient = new Patient(existingUser, patientData.getNationalId());
        applyOptionalPatientFields(patient, patientData);

        entityManager.persist(patient);

        return ResponseEntity.status(HttpStatus.CREATED).body(patient);
    }

    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = entityManager
                .createQuery("FROM Patient", Patient.class)
                .getResultList();

        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        Patient patient = entityManager.find(Patient.class, id);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        return ResponseEntity.ok(patient);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updatePatient(@PathVariable Long id,
                                           @RequestBody Patient updatedPatient) {

        Patient patient = entityManager.find(Patient.class, id);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        String validationError = validatePatientData(updatedPatient);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        Patient patientWithSameNationalId = findPatientByNationalId(updatedPatient.getNationalId());
        if (patientWithSameNationalId != null && !patientWithSameNationalId.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("National ID already in use");
        }

        patient.setNationalId(updatedPatient.getNationalId());
        applyOptionalPatientFields(patient, updatedPatient);

        entityManager.merge(patient);

        return ResponseEntity.ok(patient);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        Patient patient = entityManager.find(Patient.class, id);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        entityManager.remove(patient);

        return ResponseEntity.ok("Patient deleted successfully");
    }

    private String validatePatientData(Patient patient) {
        if (!Patient.isNationalIdValid(patient.getNationalId())) {
            return "Invalid nationalId";
        }

        if (!Patient.isOptionalTextValid(patient.getPhone(), 20)) {
            return "Invalid phone";
        }

        if (!Patient.isBirthDateValid(patient.getBirthDate())) {
            return "Invalid birth date";
        }

        if (!Patient.isOptionalTextValid(patient.getGender(), 20)) {
            return "Invalid gender";
        }

        if (!Patient.isOptionalTextValid(patient.getAddress(), 150)) {
            return "Invalid address";
        }

        if (!Patient.isOptionalTextValid(patient.getCity(), 100)) {
            return "Invalid city";
        }

        if (!Patient.isOptionalTextValid(patient.getConsultationReason(), 255)) {
            return "Invalid consultation reason";
        }

        return null;
    }

    private void applyOptionalPatientFields(Patient target, Patient source) {
        target.setPhone(source.getPhone());
        target.setBirthDate(source.getBirthDate());
        target.setGender(source.getGender());
        target.setAddress(source.getAddress());
        target.setCity(source.getCity());
        target.setConsultationReason(source.getConsultationReason());
    }

    private Patient findPatientByNationalId(String nationalId) {
        return entityManager
                .createQuery("FROM Patient p WHERE p.nationalId = :nationalId", Patient.class)
                .setParameter("nationalId", Patient.normalizeText(nationalId))
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}