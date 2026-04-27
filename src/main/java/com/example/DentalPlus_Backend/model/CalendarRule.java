package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import java.time.LocalTime;

@JsonPropertyOrder({
    "id",
    "clinic",
    "dayOfWeek",
    "startTime",
    "endTime",
    "ruleType",
    "active"
})
@Entity
@Table(name = "calendar_rule")
public class CalendarRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(nullable = false, length = 20)
    private String dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 20)
    private String ruleType;

    @Column(nullable = false)
    private Boolean active;

    public CalendarRule() {
    }

    public CalendarRule(
            Clinic clinic,
            String dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            String ruleType,
            Boolean active
    ) {
        this.clinic = clinic;
        this.dayOfWeek = normalizeDayOfWeek(dayOfWeek);
        this.startTime = startTime;
        this.endTime = endTime;
        this.ruleType = normalizeRuleType(ruleType);
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

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = normalizeDayOfWeek(dayOfWeek);
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

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = normalizeRuleType(ruleType);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active != null ? active : true;
    }

    public static boolean isDayOfWeekValid(String dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek.isBlank()) {
            return false;
        }

        String normalized = dayOfWeek.trim().toUpperCase();

        return normalized.equals("MONDAY")
                || normalized.equals("TUESDAY")
                || normalized.equals("WEDNESDAY")
                || normalized.equals("THURSDAY")
                || normalized.equals("FRIDAY")
                || normalized.equals("SATURDAY")
                || normalized.equals("SUNDAY");
    }

    public static boolean isRuleTypeValid(String ruleType) {
        if (ruleType == null || ruleType.isBlank()) {
            return false;
        }

        String normalized = ruleType.trim().toUpperCase();

        return normalized.equals("WORKING")
                || normalized.equals("BREAK");
    }

    public static String normalizeDayOfWeek(String dayOfWeek) {
        return dayOfWeek == null ? null : dayOfWeek.trim().toUpperCase();
    }

    public static String normalizeRuleType(String ruleType) {
        return ruleType == null ? null : ruleType.trim().toUpperCase();
    }
}