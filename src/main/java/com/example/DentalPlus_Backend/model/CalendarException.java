package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@JsonPropertyOrder({
    "id",
    "clinic",
    "calendarAssignment",
    "date",
    "startTime",
    "endTime",
    "exceptionType",
    "notes",
    "active"
})
@Entity
@Table(name = "calendar_exception")
public class CalendarException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @ManyToOne
    @JoinColumn(name = "calendar_assignment_id")
    private CalendarAssignment calendarAssignment;

    @Column(nullable = false)
    private LocalDate date;

    @Column
    private LocalTime startTime;

    @Column
    private LocalTime endTime;

    @Column(nullable = false, length = 30)
    private String exceptionType;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private Boolean active;

    public CalendarException() {
    }

    public CalendarException(
            Clinic clinic,
            CalendarAssignment calendarAssignment,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String exceptionType,
            String notes,
            Boolean active
    ) {
        this.clinic = clinic;
        this.calendarAssignment = calendarAssignment;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.exceptionType = normalizeExceptionType(exceptionType);
        this.notes = normalizeText(notes);
        this.active = active != null ? active : true;
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

    public CalendarAssignment getCalendarAssignment() {
        return calendarAssignment;
    }

    public void setCalendarAssignment(CalendarAssignment calendarAssignment) {
        this.calendarAssignment = calendarAssignment;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = normalizeExceptionType(exceptionType);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = normalizeText(notes);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active != null ? active : true;
    }

    public static boolean isExceptionTypeValid(String exceptionType) {
        if (exceptionType == null || exceptionType.isBlank()) {
            return false;
        }

        String normalized = exceptionType.trim().toUpperCase();

        return normalized.equals("UNAVAILABLE")
                || normalized.equals("AVAILABLE")
                || normalized.equals("SPECIAL_HOURS");
    }

    public static boolean isNotesValid(String notes) {
        return notes == null || notes.isBlank() || notes.trim().length() <= 500;
    }

    public static String normalizeExceptionType(String exceptionType) {
        return exceptionType == null ? null : exceptionType.trim().toUpperCase();
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}