package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

import java.time.LocalTime;

@JsonPropertyOrder({ "id", "calendarRule", "dayOfWeek", "breakStartTime", "breakEndTime", "active", "notes" })
@Entity
@Table(name = "calendar_break")
public class CalendarBreak {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "calendar_rule_id", nullable = false)
	private CalendarRule calendarRule;

	@Column(nullable = false, length = 20)
	private String dayOfWeek;

	@Column(nullable = false)
	private LocalTime breakStartTime;

	@Column(nullable = false)
	private LocalTime breakEndTime;

	@Column(nullable = false)
	private Boolean active;

	@Column(length = 500)
	private String notes;

	public CalendarBreak() {
	}

	public CalendarBreak(CalendarRule calendarRule, String dayOfWeek, LocalTime breakStartTime, LocalTime breakEndTime,
			Boolean active, String notes) {
		this.calendarRule = calendarRule;
		this.dayOfWeek = normalizeDayOfWeek(dayOfWeek);
		this.breakStartTime = breakStartTime;
		this.breakEndTime = breakEndTime;
		this.active = active != null ? active : true;
		this.notes = normalizeText(notes);
	}

	public Long getId() {
		return id;
	}

	public CalendarRule getCalendarRule() {
		return calendarRule;
	}

	public void setCalendarRule(CalendarRule calendarRule) {
		this.calendarRule = calendarRule;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = normalizeDayOfWeek(dayOfWeek);
	}

	public LocalTime getBreakStartTime() {
		return breakStartTime;
	}

	public void setBreakStartTime(LocalTime breakStartTime) {
		this.breakStartTime = breakStartTime;
	}

	public LocalTime getBreakEndTime() {
		return breakEndTime;
	}

	public void setBreakEndTime(LocalTime breakEndTime) {
		this.breakEndTime = breakEndTime;
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

	public static boolean isDayOfWeekValid(String dayOfWeek) {
		if (dayOfWeek == null || dayOfWeek.isBlank()) {
			return false;
		}

		String normalized = dayOfWeek.trim().toUpperCase();

		return normalized.equals("MONDAY") || normalized.equals("TUESDAY") || normalized.equals("WEDNESDAY")
				|| normalized.equals("THURSDAY") || normalized.equals("FRIDAY") || normalized.equals("SATURDAY")
				|| normalized.equals("SUNDAY");
	}

	public static boolean isBreakRangeValid(LocalTime breakStartTime, LocalTime breakEndTime) {
		return breakStartTime != null && breakEndTime != null && breakEndTime.isAfter(breakStartTime);
	}

	public static boolean isNotesValid(String notes) {
		return notes == null || notes.isBlank() || notes.trim().length() <= 500;
	}

	public static String normalizeDayOfWeek(String dayOfWeek) {
		return dayOfWeek == null ? null : dayOfWeek.trim().toUpperCase();
	}

	public static String normalizeText(String text) {
		return text == null ? null : text.trim();
	}
}