package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({
    "id",
    "active",
    "notes"
})
@Entity
@Table(name = "calendar_assignment")
public class CalendarAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(nullable = false)
    private Boolean active;

    @Column(length = 500)
    private String notes;

    public CalendarAssignment() {
    }

    public CalendarAssignment(Boolean active, String notes) {
        this.active = active != null ? active : true;
        this.notes = normalizeText(notes);
    }

    public Long getId() {
        return id;
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

    public static boolean isNotesValid(String notes) {
        return notes == null || notes.isBlank() || notes.trim().length() <= 500;
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}