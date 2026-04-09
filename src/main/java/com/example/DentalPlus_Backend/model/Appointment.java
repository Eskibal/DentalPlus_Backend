package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@JsonPropertyOrder({
    "id",
    "patient",
    "scheduledAt",
    "reason",
    "notes"
})
@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(length = 500)
    private String notes;

    public Appointment() {}

    public Appointment(Patient patient, LocalDateTime scheduledAt, String reason) {
        this.patient = patient;
        this.scheduledAt = scheduledAt;
        this.reason = normalizeText(reason);
    }

    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = normalizeText(reason);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = normalizeText(notes);
    }

    public static boolean isScheduledAtValid(LocalDateTime scheduledAt) {
        return scheduledAt != null;
    }

    public static boolean isReasonValid(String reason) {
        return reason != null
                && !reason.isBlank()
                && reason.trim().length() <= 255;
    }

    public static boolean isNotesValid(String notes) {
        return notes == null
                || notes.isBlank()
                || notes.trim().length() <= 500;
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}