package com.example.DentalPlus_Backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocument;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, length = 255)
    private String fileUrl;

    @Column(nullable = false)
    private LocalDate captureDate;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    public Document() {
    }

    public Long getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(Long idDocument) {
        this.idDocument = idDocument;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = normalizeText(type);
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = normalizeText(fileUrl);
    }

    public LocalDate getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(LocalDate captureDate) {
        this.captureDate = captureDate;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public static boolean isTypeValid(String type) {
        return type != null && !type.isBlank() && type.trim().length() <= 100;
    }

    public static boolean isFileUrlValid(String fileUrl) {
        return fileUrl != null && !fileUrl.isBlank() && fileUrl.trim().length() <= 255;
    }

    public static boolean isCaptureDateValid(LocalDate captureDate) {
        return captureDate != null && !captureDate.isAfter(LocalDate.now());
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}