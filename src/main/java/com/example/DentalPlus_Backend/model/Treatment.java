package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({
    "id",
    "name",
    "description",
    "estimatedDurationMinutes",
    "active",
    "notes"
})
@Entity
@Table(name = "treatment")
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column
    private Integer estimatedDurationMinutes;

    @Column(nullable = false)
    private Boolean active;

    @Column(length = 500)
    private String notes;

    public Treatment() {
    }

    public Treatment(
            String name,
            String description,
            Integer estimatedDurationMinutes,
            Boolean active,
            String notes
    ) {
        this.name = normalizeText(name);
        this.description = normalizeText(description);
        this.estimatedDurationMinutes = normalizeEstimatedDurationMinutes(estimatedDurationMinutes);
        this.active = active != null ? active : true;
        this.notes = normalizeText(notes);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalizeText(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = normalizeText(description);
    }

    public Integer getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        this.estimatedDurationMinutes = normalizeEstimatedDurationMinutes(estimatedDurationMinutes);
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
                && name.trim().length() <= 120;
    }

    public static boolean isDescriptionValid(String description) {
        return description == null
                || description.isBlank()
                || description.trim().length() <= 500;
    }

    public static boolean isEstimatedDurationMinutesValid(Integer estimatedDurationMinutes) {
        return estimatedDurationMinutes == null || estimatedDurationMinutes >= 0;
    }

    public static boolean isNotesValid(String notes) {
        return notes == null
                || notes.isBlank()
                || notes.trim().length() <= 500;
    }

    public static Integer normalizeEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        return estimatedDurationMinutes != null && estimatedDurationMinutes < 0
                ? 0
                : estimatedDurationMinutes;
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}