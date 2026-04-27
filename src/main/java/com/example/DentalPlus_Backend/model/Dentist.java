package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({
    "id",
    "person",
    "user",
    "clinic",
    "calendarAssignment",
    "speciality",
    "active",
    "notes"
})
@Entity
@Table(name = "dentist")
public class Dentist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @OneToOne
    @JoinColumn(name = "calendar_assignment_id", unique = true)
    private CalendarAssignment calendarAssignment;

    @Column(length = 120)
    private String speciality;

    @Column(nullable = false)
    private Boolean active;

    @Column(length = 500)
    private String notes;

    public Dentist() {
    }

    public Dentist(
            Person person,
            User user,
            Clinic clinic,
            String speciality,
            Boolean active,
            String notes
    ) {
        this.person = person;
        this.user = user;
        this.clinic = clinic;
        this.speciality = normalizeText(speciality);
        this.active = active != null ? active : true;
        this.notes = normalizeText(notes);
    }

    public Long getId() {
        return id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = normalizeText(speciality);
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

    public static boolean isSpecialityValid(String speciality) {
        return speciality == null || speciality.isBlank() || speciality.trim().length() <= 120;
    }

    public static boolean isNotesValid(String notes) {
        return notes == null || notes.isBlank() || notes.trim().length() <= 500;
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}