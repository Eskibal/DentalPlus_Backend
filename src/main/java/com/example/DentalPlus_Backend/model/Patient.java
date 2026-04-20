package com.example.DentalPlus_Backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String nationalId;

    @Column(nullable = false)
    private LocalDate registrationDate;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 20)
    private String phone;

    @Column
    private LocalDate birthDate;

    @Column(length = 20)
    private String gender;

    @Column(length = 150)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 255)
    private String consultationReason;

    public Patient() {
        this.registrationDate = LocalDate.now();
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = normalizeText(nationalId);
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = normalizeText(phone);
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = normalizeText(gender);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = normalizeText(address);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = normalizeText(city);
    }

    public String getConsultationReason() {
        return consultationReason;
    }

    public void setConsultationReason(String consultationReason) {
        this.consultationReason = normalizeText(consultationReason);
    }

    public static boolean isNationalIdValid(String nationalId) {
        return nationalId != null && !nationalId.isBlank() && nationalId.trim().length() <= 20;
    }

    public static boolean isOptionalTextValid(String text, int maxLength) {
        return text == null || text.isBlank() || text.trim().length() <= maxLength;
    }

    public static boolean isBirthDateValid(LocalDate birthDate) {
        return birthDate == null || !birthDate.isAfter(LocalDate.now());
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}