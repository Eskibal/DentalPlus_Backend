package com.example.DentalPlus_Backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Dentist")
public class Dentist {

    private static final int MAX_TEXT_LENGTH = 150;
    private static final int MAX_WEEKDAY_LENGTH = 20;

    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = MAX_TEXT_LENGTH)
    private String speciality;

    @Column(nullable = false, length = MAX_WEEKDAY_LENGTH)
    private String visitWeekday;

    @Column(nullable = false, length = MAX_TEXT_LENGTH)
    private String city;

    @Column(nullable = false, length = MAX_TEXT_LENGTH)
    private String direction;

    public Dentist() {
    }

    public Dentist(User user, String speciality, String visitWeekday, String city, String direction) {
        this.user = user;
        this.speciality = normalizeText(speciality);
        this.visitWeekday = normalizeText(visitWeekday);
        this.city = normalizeText(city);
        this.direction = normalizeText(direction);
    }

    public Long getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = normalizeText(speciality);
    }

    public String getVisitWeekday() {
        return visitWeekday;
    }

    public void setVisitWeekday(String visitWeekday) {
        this.visitWeekday = normalizeText(visitWeekday);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = normalizeText(city);
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = normalizeText(direction);
    }

    public static boolean isTextValid(String text) {
        return text != null && !text.isBlank() && text.trim().length() <= MAX_TEXT_LENGTH;
    }

    public static boolean isWeekdayValid(String text) {
        return text != null && !text.isBlank() && text.trim().length() <= MAX_WEEKDAY_LENGTH;
    }

    public static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }
}