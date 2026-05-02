package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

import java.time.LocalDate;

@JsonPropertyOrder({ "id", "calendarRule", "name", "startDate", "endDate", "scope", "active", "notes" })
@Entity
@Table(name = "calendar_holiday")
public class CalendarHoliday {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "calendar_rule_id", nullable = false)
	private CalendarRule calendarRule;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false, length = 20)
	private String scope;

	@Column(nullable = false)
	private Boolean active;

	@Column(length = 500)
	private String notes;

	public CalendarHoliday() {
	}

	public CalendarHoliday(CalendarRule calendarRule, String name, LocalDate startDate, LocalDate endDate, String scope,
			Boolean active, String notes) {
		this.calendarRule = calendarRule;
		this.name = normalizeText(name);
		this.startDate = startDate;
		this.endDate = endDate;
		this.scope = normalizeScope(scope);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = normalizeText(name);
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
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

	public static boolean isDateRangeValid(LocalDate startDate, LocalDate endDate) {
		return startDate != null && endDate != null && !endDate.isBefore(startDate);
	}

	public static boolean isScopeValid(String scope) {
		if (scope == null || scope.isBlank()) {
			return false;
		}

		String normalized = scope.trim().toUpperCase();

		return normalized.equals("NATIONAL") || normalized.equals("REGIONAL") || normalized.equals("LOCAL");
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