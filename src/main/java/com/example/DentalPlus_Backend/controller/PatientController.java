package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.Patient;
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
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {

        String validationError = validatePatientData(patient);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        if (findPatientByNationalId(patient.getNationalId()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("National ID already in use");
        }

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

        Patient existing = findPatientByNationalId(updatedPatient.getNationalId());
        if (existing != null && !existing.getId().equals(id)) {
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
        if (!Patient.isNationalIdValid(patient.getNationalId())) return "Invalid nationalId";
        if (!Patient.isOptionalTextValid(patient.getPhone(), 20)) return "Invalid phone";
        if (!Patient.isBirthDateValid(patient.getBirthDate())) return "Invalid birth date";
        if (!Patient.isOptionalTextValid(patient.getGender(), 20)) return "Invalid gender";
        if (!Patient.isOptionalTextValid(patient.getAddress(), 150)) return "Invalid address";
        if (!Patient.isOptionalTextValid(patient.getCity(), 100)) return "Invalid city";
        if (!Patient.isOptionalTextValid(patient.getConsultationReason(), 255)) return "Invalid consultation reason";
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