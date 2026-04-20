package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@JsonPropertyOrder({
    "id",
    "patient",
    "viewMode",
    "createdAt",
    "updatedAt"
})
@Entity
@Table(name = "odontogram")
public class Odontogram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    @Column(nullable = false, length = 20)
    private String viewMode;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Odontogram() {
    }

    public Odontogram(Patient patient) {
        this.patient = patient;
        this.viewMode = "MIXED";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.viewMode = normalizeViewMode(this.viewMode);

        if (this.viewMode == null) {
            this.viewMode = "MIXED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.viewMode = normalizeViewMode(this.viewMode);

        if (this.viewMode == null) {
            this.viewMode = "MIXED";
        }
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

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = normalizeViewMode(viewMode);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static boolean isViewModeValid(String viewMode) {
        if (viewMode == null || viewMode.isBlank()) {
            return false;
        }

        String normalized = viewMode.trim().toUpperCase();

        return normalized.equals("TEMPORARY")
                || normalized.equals("PERMANENT")
                || normalized.equals("MIXED");
    }

    public static String normalizeViewMode(String viewMode) {
        return viewMode == null ? null : viewMode.trim().toUpperCase();
    }
}