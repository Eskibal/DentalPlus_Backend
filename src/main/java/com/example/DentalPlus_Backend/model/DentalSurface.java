package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({
    "id",
    "dentalPiece",
    "surfaceType",
    "notes"
})
@Entity
@Table(
    name = "dental_surface",
    uniqueConstraints = @UniqueConstraint(columnNames = {"dental_piece_id", "surface_type"})
)
public class DentalSurface {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dental_piece_id", nullable = false)
    private DentalPiece dentalPiece;

    @Column(nullable = false, length = 20)
    private String surfaceType;

    @Column(length = 500)
    private String notes;

    public DentalSurface() {
    }

    public DentalSurface(DentalPiece dentalPiece, String surfaceType, String notes) {
        this.dentalPiece = dentalPiece;
        this.surfaceType = normalizeSurfaceType(surfaceType);
        this.notes = normalizeText(notes);
    }

    @PrePersist
    @PreUpdate
    protected void normalizeBeforeSave() {
        this.surfaceType = normalizeSurfaceType(this.surfaceType);
        this.notes = normalizeText(this.notes);
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

    public String getSurfaceType() {
        return surfaceType;
    }

    public void setSurfaceType(String surfaceType) {
        this.surfaceType = normalizeSurfaceType(surfaceType);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = normalizeText(notes);
    }

    public static boolean isSurfaceTypeValid(String surfaceType) {
        if (surfaceType == null || surfaceType.isBlank()) {
            return false;
        }

        String normalized = surfaceType.trim().toUpperCase();

        return normalized.equals("MESIAL")
                || normalized.equals("DISTAL")
                || normalized.equals("VESTIBULAR")
                || normalized.equals("LINGUAL")
                || normalized.equals("OCCLUSAL");
    }

    public static boolean isSurfaceTypeValidForPieceKind(String surfaceType, String pieceKind) {
        if (!isSurfaceTypeValid(surfaceType) || pieceKind == null || pieceKind.isBlank()) {
            return false;
        }

        String normalizedSurface = surfaceType.trim().toUpperCase();
        String normalizedPieceKind = pieceKind.trim().toUpperCase();

        if (normalizedPieceKind.equals("FRONT")) {
            return normalizedSurface.equals("MESIAL")
                    || normalizedSurface.equals("DISTAL")
                    || normalizedSurface.equals("VESTIBULAR")
                    || normalizedSurface.equals("LINGUAL");
        }

        if (normalizedPieceKind.equals("BACK")) {
            return normalizedSurface.equals("MESIAL")
                    || normalizedSurface.equals("DISTAL")
                    || normalizedSurface.equals("VESTIBULAR")
                    || normalizedSurface.equals("LINGUAL")
                    || normalizedSurface.equals("OCCLUSAL");
        }

        return false;
    }

    public static boolean isNotesValid(String notes) {
        return notes == null
                || notes.isBlank()
                || notes.trim().length() <= 500;
    }

    public static String normalizeSurfaceType(String surfaceType) {
        return surfaceType == null ? null : surfaceType.trim().toUpperCase();
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }

    public static List<String> getSurfaceTypesForPieceKind(String pieceKind) {
        List<String> surfaceTypes = new ArrayList<>();

        if (pieceKind == null || pieceKind.isBlank()) {
            return surfaceTypes;
        }

        String normalized = pieceKind.trim().toUpperCase();

        surfaceTypes.add("MESIAL");
        surfaceTypes.add("DISTAL");
        surfaceTypes.add("VESTIBULAR");
        surfaceTypes.add("LINGUAL");

        if (normalized.equals("BACK")) {
            surfaceTypes.add("OCCLUSAL");
        }

        return surfaceTypes;
    }
}