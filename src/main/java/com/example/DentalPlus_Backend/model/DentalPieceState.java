package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@JsonPropertyOrder({
    "id",
    "dentalPiece",
    "stateType",
    "notes",
    "createdAt",
    "updatedAt",
    "active"
})
@Entity
@Table(name = "dental_piece_state")
public class DentalPieceState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dental_piece_id", nullable = false)
    private DentalPiece dentalPiece;

    @Column(nullable = false, length = 40)
    private String stateType;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean active;

    public DentalPieceState() {
    }

    public DentalPieceState(DentalPiece dentalPiece, String stateType, String notes) {
        this.dentalPiece = dentalPiece;
        this.stateType = normalizeStateType(stateType);
        this.notes = normalizeText(notes);
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.stateType = normalizeStateType(this.stateType);
        this.notes = normalizeText(this.notes);

        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.stateType = normalizeStateType(this.stateType);
        this.notes = normalizeText(this.notes);

        if (this.active == null) {
            this.active = true;
        }
    }

    public Long getId() {
        return id;
    }

    public DentalPiece getDentalPiece() {
        return dentalPiece;
    }

    public void setDentalPiece(DentalPiece dentalPiece) {
        this.dentalPiece = dentalPiece;
    }

    public String getStateType() {
        return stateType;
    }

    public void setStateType(String stateType) {
        this.stateType = normalizeStateType(stateType);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = normalizeText(notes);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active != null ? active : true;
    }

    public static boolean isStateTypeValid(String stateType) {
        if (stateType == null || stateType.isBlank()) {
            return false;
        }

        String normalized = stateType.trim().toUpperCase();

        return normalized.equals("HEALTHY")
                || normalized.equals("NATURAL_ABSENCE")
                || normalized.equals("EXTRACTION_PENDING")
                || normalized.equals("EXTRACTION_DONE")
                || normalized.equals("CROWN_PENDING")
                || normalized.equals("CROWN_DONE")
                || normalized.equals("ENDODONTICS_PENDING")
                || normalized.equals("ENDODONTICS_DONE")
                || normalized.equals("BRIDGE_PENDING")
                || normalized.equals("BRIDGE_DONE")
                || normalized.equals("UNKNOWN");
    }

    public static boolean isNotesValid(String notes) {
        return notes == null
                || notes.isBlank()
                || notes.trim().length() <= 500;
    }

    public static String normalizeStateType(String stateType) {
        return stateType == null ? null : stateType.trim().toUpperCase();
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}