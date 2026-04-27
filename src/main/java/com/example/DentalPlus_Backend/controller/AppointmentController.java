package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.Appointment;
import com.example.DentalPlus_Backend.model.Box;
import com.example.DentalPlus_Backend.model.Dentist;
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

    @PostMapping
    @Transactional
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest request) {

        String validationError = validateAppointmentRequest(request, true);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        Box box = entityManager.find(Box.class, request.getBoxId());
        Dentist dentist = entityManager.find(Dentist.class, request.getDentistId());
        Patient patient = entityManager.find(Patient.class, request.getPatientId());

        if (box == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Box not found");
        }

        if (dentist == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dentist not found");
        }

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        if (!dentist.getClinic().getId().equals(box.getClinic().getId())) {
            return ResponseEntity.badRequest().body("Dentist and box must belong to the same clinic");
        }

        if (hasBoxConflict(box.getId(), request.getStartDateTime(), request.getEndDateTime(), null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Box already occupied in that time range");
        }

        if (hasDentistConflict(dentist.getId(), request.getStartDateTime(), request.getEndDateTime(), null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Dentist already has an appointment in that time range");
        }

        Appointment appointment = new Appointment(
                box,
                dentist,
                patient,
                request.getStartDateTime(),
                request.getEndDateTime(),
                request.getStatus(),
                request.getNotes(),
                request.getActive()
        );

        entityManager.persist(appointment);

        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long dentistId,
            @RequestParam(required = false) Long boxId,
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

        if (dentistId != null) {
            jpql.append(" AND a.dentist.id = :dentistId");
        }

        if (boxId != null) {
            jpql.append(" AND a.box.id = :boxId");
        }

        if (from != null) {
            jpql.append(" AND a.startDateTime >= :from");
        }

        if (to != null) {
            jpql.append(" AND a.endDateTime <= :to");
        }

        jpql.append(" ORDER BY a.startDateTime ASC");

        TypedQuery<Appointment> query = entityManager.createQuery(jpql.toString(), Appointment.class);

        if (patientId != null) {
            query.setParameter("patientId", patientId);
        }

        if (dentistId != null) {
            query.setParameter("dentistId", dentistId);
        }

        if (boxId != null) {
            query.setParameter("boxId", boxId);
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
            @RequestBody AppointmentRequest request) {

        Appointment appointment = entityManager.find(Appointment.class, id);

        if (appointment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
        }

        String validationError = validateAppointmentRequest(request, false);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        Box box = appointment.getBox();
        Dentist dentist = appointment.getDentist();
        Patient patient = appointment.getPatient();
        LocalDateTime startDateTime = appointment.getStartDateTime();
        LocalDateTime endDateTime = appointment.getEndDateTime();
        String status = appointment.getStatus();
        String notes = appointment.getNotes();
        Boolean active = appointment.getActive();

        if (request.getBoxId() != null) {
            box = entityManager.find(Box.class, request.getBoxId());
            if (box == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Box not found");
            }
        }

        if (request.getDentistId() != null) {
            dentist = entityManager.find(Dentist.class, request.getDentistId());
            if (dentist == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dentist not found");
            }
        }

        if (request.getPatientId() != null) {
            patient = entityManager.find(Patient.class, request.getPatientId());
            if (patient == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
            }
        }

        if (request.getStartDateTime() != null) {
            startDateTime = request.getStartDateTime();
        }

        if (request.getEndDateTime() != null) {
            endDateTime = request.getEndDateTime();
        }

        if (!Appointment.isDateRangeValid(startDateTime, endDateTime)) {
            return ResponseEntity.badRequest().body("Invalid date range");
        }

        if (!dentist.getClinic().getId().equals(box.getClinic().getId())) {
            return ResponseEntity.badRequest().body("Dentist and box must belong to the same clinic");
        }

        if (hasBoxConflict(box.getId(), startDateTime, endDateTime, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Box already occupied in that time range");
        }

        if (hasDentistConflict(dentist.getId(), startDateTime, endDateTime, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Dentist already has an appointment in that time range");
        }

        if (request.getStatus() != null) {
            if (!Appointment.isStatusValid(request.getStatus())) {
                return ResponseEntity.badRequest().body("Invalid status");
            }
            status = request.getStatus();
        }

        if (request.isNotesProvided()) {
            if (!Appointment.isNotesValid(request.getNotes())) {
                return ResponseEntity.badRequest().body("Invalid notes");
            }
            notes = request.getNotes();
        }

        if (request.getActive() != null) {
            active = request.getActive();
        }

        appointment.setBox(box);
        appointment.setDentist(dentist);
        appointment.setPatient(patient);
        appointment.setStartDateTime(startDateTime);
        appointment.setEndDateTime(endDateTime);
        appointment.setStatus(status);
        appointment.setNotes(notes);
        appointment.setActive(active);

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

    private String validateAppointmentRequest(AppointmentRequest request, boolean creating) {
        if (request == null) {
            return "Request body is required";
        }

        if (creating) {
            if (request.getBoxId() == null) {
                return "boxId is required";
            }

            if (request.getDentistId() == null) {
                return "dentistId is required";
            }

            if (request.getPatientId() == null) {
                return "patientId is required";
            }

            if (!Appointment.isStartDateTimeValid(request.getStartDateTime())) {
                return "Invalid startDateTime";
            }

            if (!Appointment.isEndDateTimeValid(request.getEndDateTime())) {
                return "Invalid endDateTime";
            }

            if (!Appointment.isDateRangeValid(request.getStartDateTime(), request.getEndDateTime())) {
                return "Invalid date range";
            }

            if (!Appointment.isStatusValid(request.getStatus())) {
                return "Invalid status";
            }
        } else {
            if (request.getStartDateTime() != null && !Appointment.isStartDateTimeValid(request.getStartDateTime())) {
                return "Invalid startDateTime";
            }

            if (request.getEndDateTime() != null && !Appointment.isEndDateTimeValid(request.getEndDateTime())) {
                return "Invalid endDateTime";
            }

            if (request.getStatus() != null && !Appointment.isStatusValid(request.getStatus())) {
                return "Invalid status";
            }
        }

        if (request.isNotesProvided() && !Appointment.isNotesValid(request.getNotes())) {
            return "Invalid notes";
        }

        return null;
    }

    private boolean hasBoxConflict(Long boxId, LocalDateTime startDateTime, LocalDateTime endDateTime, Long excludeAppointmentId) {
        String jpql = """
                FROM Appointment a
                WHERE a.box.id = :boxId
                  AND a.active = true
                  AND (:excludeId IS NULL OR a.id <> :excludeId)
                  AND a.startDateTime < :endDateTime
                  AND a.endDateTime > :startDateTime
                """;

        return !entityManager
                .createQuery(jpql, Appointment.class)
                .setParameter("boxId", boxId)
                .setParameter("excludeId", excludeAppointmentId)
                .setParameter("startDateTime", startDateTime)
                .setParameter("endDateTime", endDateTime)
                .getResultList()
                .isEmpty();
    }

    private boolean hasDentistConflict(Long dentistId, LocalDateTime startDateTime, LocalDateTime endDateTime, Long excludeAppointmentId) {
        String jpql = """
                FROM Appointment a
                WHERE a.dentist.id = :dentistId
                  AND a.active = true
                  AND (:excludeId IS NULL OR a.id <> :excludeId)
                  AND a.startDateTime < :endDateTime
                  AND a.endDateTime > :startDateTime
                """;

        return !entityManager
                .createQuery(jpql, Appointment.class)
                .setParameter("dentistId", dentistId)
                .setParameter("excludeId", excludeAppointmentId)
                .setParameter("startDateTime", startDateTime)
                .setParameter("endDateTime", endDateTime)
                .getResultList()
                .isEmpty();
    }

    public static class AppointmentRequest {
        private Long boxId;
        private Long dentistId;
        private Long patientId;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private String status;
        private String notes;
        private Boolean active;

        private boolean notesProvided;

        public Long getBoxId() {
            return boxId;
        }

        public void setBoxId(Long boxId) {
            this.boxId = boxId;
        }

        public Long getDentistId() {
            return dentistId;
        }

        public void setDentistId(Long dentistId) {
            this.dentistId = dentistId;
        }

        public Long getPatientId() {
            return patientId;
        }

        public void setPatientId(Long patientId) {
            this.patientId = patientId;
        }

        public LocalDateTime getStartDateTime() {
            return startDateTime;
        }

        public void setStartDateTime(LocalDateTime startDateTime) {
            this.startDateTime = startDateTime;
        }

        public LocalDateTime getEndDateTime() {
            return endDateTime;
        }

        public void setEndDateTime(LocalDateTime endDateTime) {
            this.endDateTime = endDateTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
            this.notesProvided = true;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public boolean isNotesProvided() {
            return notesProvided;
        }
    }
}