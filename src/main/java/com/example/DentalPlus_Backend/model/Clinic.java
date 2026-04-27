package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({
    "id",
    "organization",
    "name",
    "active",
    "country",
    "city",
    "address",
    "phone",
    "email",
    "timeZone",
    "notes"
})
@Entity
@Table(name = "clinic")
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private Boolean active;

    @Column(length = 80)
    private String country;

    @Column(length = 80)
    private String city;

    @Column(length = 200)
    private String address;

    @Column(length = 40)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(length = 80)
    private String timeZone;

    @Column(length = 500)
    private String notes;

    public Clinic() {
    }

    public Clinic(
            Organization organization,
            String name,
            Boolean active,
            String country,
            String city,
            String address,
            String phone,
            String email,
            String timeZone,
            String notes
    ) {
        this.organization = organization;
        this.name = normalizeText(name);
        this.active = active != null ? active : true;
        this.country = normalizeText(country);
        this.city = normalizeText(city);
        this.address = normalizeText(address);
        this.phone = normalizeText(phone);
        this.email = normalizeText(email);
        this.timeZone = normalizeText(timeZone);
        this.notes = normalizeText(notes);
    }

    public Long getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalizeText(name);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active != null ? active : true;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = normalizeText(country);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = normalizeText(city);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = normalizeText(address);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = normalizeText(phone);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizeText(email);
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = normalizeText(timeZone);
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

    public static boolean isNotesValid(String notes) {
        return notes == null || notes.isBlank() || notes.trim().length() <= 500;
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}