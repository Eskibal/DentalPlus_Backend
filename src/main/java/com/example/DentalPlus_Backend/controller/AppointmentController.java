package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.Appointment;
import com.example.DentalPlus_Backend.model.Patient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping("/{patientId}")
    @Transactional
    public ResponseEntity<?> createAppointment(
            @PathVariable Long patientId,
            @RequestBody Appointment appointmentData) {

        Patient patient = entityManager.find(Patient.class, patientId);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        String validationError = validateAppointmentData(appointmentData);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        Appointment appointment = new Appointment(
                patient,
                appointmentData.getScheduledAt(),
                appointmentData.getReason()
        );

        appointment.setNotes(appointmentData.getNotes());

        entityManager.persist(appointment);

        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to) {

        StringBuilder jpql = new StringBuilder("FROM Appointment a WHERE 1=1");

        if (patientId != null) {
            jpql.append(" AND a.patient.id = :patientId");
        }

        if (from != null) {
            jpql.append(" AND a.scheduledAt >= :from");
        }

        if (to != null) {
            jpql.append(" AND a.scheduledAt <= :to");
        }

        jpql.append(" ORDER BY a.scheduledAt ASC");

        TypedQuery<Appointment> query = entityManager.createQuery(jpql.toString(), Appointment.class);

        if (patientId != null) {
            query.setParameter("patientId", patientId);
        }

        if (from != null) {
            query.setParameter("from", from);
        }

        if (to != null) {
            query.setParameter("to", to);
        }

        return ResponseEntity.ok(query.getResultList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = entityManager.find(Appointment.class, id);

        if (appointment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
        }

        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateAppointment(
            @PathVariable Long id,
            @RequestBody Appointment updatedAppointment) {

        Appointment appointment = entityManager.find(Appointment.class, id);

        if (appointment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
        }

        String validationError = validateAppointmentData(updatedAppointment);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        appointment.setScheduledAt(updatedAppointment.getScheduledAt());
        appointment.setReason(updatedAppointment.getReason());
        appointment.setNotes(updatedAppointment.getNotes());

        entityManager.merge(appointment);

        return ResponseEntity.ok(appointment);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        Appointment appointment = entityManager.find(Appointment.class, id);

        if (appointment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
        }

        entityManager.remove(appointment);

        return ResponseEntity.ok("Appointment deleted successfully");
    }

    private String validateAppointmentData(Appointment appointment) {
        if (!Appointment.isScheduledAtValid(appointment.getScheduledAt())) {
            return "Invalid scheduledAt";
        }

        if (!Appointment.isReasonValid(appointment.getReason())) {
            return "Invalid reason";
        }

        if (!Appointment.isNotesValid(appointment.getNotes())) {
            return "Invalid notes";
        }

        return null;
    }
}