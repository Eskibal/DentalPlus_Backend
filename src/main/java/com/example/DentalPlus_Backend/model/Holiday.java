package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import java.time.LocalDate;

@JsonPropertyOrder({
    "id",
    "clinic",
    "name",
    "date",
    "scope",
    "active",
    "notes"
})
@Entity
@Table(name = "holiday")
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 20)
    private String scope;

    @Column(nullable = false)
    private Boolean active;

    @Column(length = 500)
    private String notes;

    public Holiday() {
    }

    public Holiday(Clinic clinic, String name, LocalDate date, String scope, Boolean active, String notes) {
        this.clinic = clinic;
        this.name = normalizeText(name);
        this.date = date;
        this.scope = normalizeScope(scope);
        this.active = active != null ? active : true;
        this.notes = normalizeText(notes);
    }

    public Long getId() {
        return id;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalizeText(name);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = normalizeScope(scope);
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
        return name != null && !name.isBlank() && name.trim().length() <= 120;
    }

    public static boolean isScopeValid(String scope) {
        if (scope == null || scope.isBlank()) {
            return false;
        }

        String normalized = scope.trim().toUpperCase();

        return normalized.equals("NATIONAL")
                || normalized.equals("REGIONAL")
                || normalized.equals("LOCAL");
    }

    public static boolean isNotesValid(String notes) {
        return notes == null || notes.isBlank() || notes.trim().length() <= 500;
    }

    public static String normalizeScope(String scope) {
        return scope == null ? null : scope.trim().toUpperCase();
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}