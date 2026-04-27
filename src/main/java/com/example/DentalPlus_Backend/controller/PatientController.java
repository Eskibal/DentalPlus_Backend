package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.Person;
import com.example.DentalPlus_Backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patient")
public class PatientController {

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping
    @Transactional
    public ResponseEntity<?> createPatient(@RequestBody PatientRequest request) {

        if (request == null || request.getPersonId() == null) {
            return ResponseEntity.badRequest().body("personId is required");
        }

        Person person = entityManager.find(Person.class, request.getPersonId());

        if (person == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person not found");
        }

        if (findPatientByPersonId(request.getPersonId()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This person is already linked to a patient");
        }

        User user = null;
        if (request.getUserId() != null) {
            user = entityManager.find(User.class, request.getUserId());

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            Patient patientWithSameUser = findPatientByUserId(request.getUserId());
            if (patientWithSameUser != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("This user is already linked to a patient");
            }
        }

        if (!Patient.isNotesValid(request.getNotes())) {
            return ResponseEntity.badRequest().body("Invalid notes");
        }

        Patient patient = new Patient(
                person,
                user,
                request.getActive(),
                request.getNotes()
        );

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
    public ResponseEntity<?> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientRequest request
    ) {
        Patient patient = entityManager.find(Patient.class, id);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        if (request == null) {
            return ResponseEntity.badRequest().body("Request body is required");
        }

        if (request.getPersonId() != null) {
            Person person = entityManager.find(Person.class, request.getPersonId());

            if (person == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person not found");
            }

            Patient patientWithSamePerson = findPatientByPersonId(request.getPersonId());
            if (patientWithSamePerson != null && !patientWithSamePerson.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("This person is already linked to another patient");
            }

            patient.setPerson(person);
        }

        if (request.isUserIdProvided()) {
            if (request.getUserId() == null) {
                patient.setUser(null);
            } else {
                User user = entityManager.find(User.class, request.getUserId());

                if (user == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
                }

                Patient patientWithSameUser = findPatientByUserId(request.getUserId());
                if (patientWithSameUser != null && !patientWithSameUser.getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("This user is already linked to another patient");
                }

                patient.setUser(user);
            }
        }

        if (request.getActive() != null) {
            patient.setActive(request.getActive());
        }

        if (request.isNotesProvided()) {
            if (!Patient.isNotesValid(request.getNotes())) {
                return ResponseEntity.badRequest().body("Invalid notes");
            }
            patient.setNotes(request.getNotes());
        }

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

    private Patient findPatientByPersonId(Long personId) {
        return entityManager
                .createQuery("FROM Patient p WHERE p.person.id = :personId", Patient.class)
                .setParameter("personId", personId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private Patient findPatientByUserId(Long userId) {
        return entityManager
                .createQuery("FROM Patient p WHERE p.user.id = :userId", Patient.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public static class PatientRequest {
        private Long personId;
        private Long userId;
        private Boolean active;
        private String notes;

        private boolean userIdProvided;
        private boolean notesProvided;

        public Long getPersonId() {
            return personId;
        }

        public void setPersonId(Long personId) {
            this.personId = personId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
            this.userIdProvided = true;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
            this.notesProvided = true;
        }

        public boolean isUserIdProvided() {
            return userIdProvided;
        }

        public boolean isNotesProvided() {
            return notesProvided;
        }
    }
}