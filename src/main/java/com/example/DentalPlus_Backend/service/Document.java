package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.model.Patient;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({
    "id",
    "patient",
    "name",
    "storagePath",
    "mimeType",
    "documentType",
    "active",
    "notes"
})
@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String storagePath;

    @Column(nullable = false, length = 100)
    private String mimeType;

    @Column(nullable = false, length = 30)
    private String documentType;

    @Column(nullable = false)
    private Boolean active;

    @Column(length = 500)
    private String notes;

    public Document() {
    }

    public Document(
            Patient patient,
            String name,
            String storagePath,
            String mimeType,
            String documentType,
            Boolean active,
            String notes
    ) {
        this.patient = patient;
        this.name = normalizeText(name);
        this.storagePath = normalizeText(storagePath);
        this.mimeType = normalizeText(mimeType);
        this.documentType = normalizeDocumentType(documentType);
        this.active = active != null ? active : true;
        this.notes = normalizeText(notes);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalizeText(name);
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = normalizeText(storagePath);
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = normalizeText(mimeType);
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = normalizeDocumentType(documentType);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active != null ? active : true;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = normalizeText(notes);
    }

    public static boolean isNameValid(String name) {
        return name != null
                && !name.isBlank()
                && name.trim().length() <= 150;
    }

    public static boolean isStoragePathValid(String storagePath) {
        return storagePath != null
                && !storagePath.isBlank()
                && storagePath.trim().length() <= 255;
    }

    public static boolean isMimeTypeValid(String mimeType) {
        return mimeType != null
                && !mimeType.isBlank()
                && mimeType.trim().length() <= 100;
    }

    public static boolean isDocumentTypeValid(String documentType) {
        if (documentType == null || documentType.isBlank()) {
            return false;
        }

        String normalized = documentType.trim().toUpperCase();

        return normalized.equals("CONSENT")
                || normalized.equals("XRAY")
                || normalized.equals("REPORT")
                || normalized.equals("PRESCRIPTION")
                || normalized.equals("OTHER");
    }

    public static boolean isNotesValid(String notes) {
        return notes == null
                || notes.isBlank()
                || notes.trim().length() <= 500;
    }

    public static String normalizeDocumentType(String documentType) {
        return documentType == null ? null : documentType.trim().toUpperCase();
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}