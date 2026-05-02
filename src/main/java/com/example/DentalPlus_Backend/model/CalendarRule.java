package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

import java.time.LocalTime;

@JsonPropertyOrder({ "id", "mondayStartTime", "mondayEndTime", "tuesdayStartTime", "tuesdayEndTime",
		"wednesdayStartTime", "wednesdayEndTime", "thursdayStartTime", "thursdayEndTime", "fridayStartTime",
		"fridayEndTime", "saturdayStartTime", "saturdayEndTime", "sundayStartTime", "sundayEndTime", "active",
		"notes" })
@Entity
@Table(name = "calendar_rule")
public class CalendarRule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@Column
	private LocalTime mondayStartTime;

	@Column
	private LocalTime mondayEndTime;

	@Column
	private LocalTime tuesdayStartTime;

	@Column
	private LocalTime tuesdayEndTime;

	@Column
	private LocalTime wednesdayStartTime;

	@Column
	private LocalTime wednesdayEndTime;

	@Column
	private LocalTime thursdayStartTime;

	@Column
	private LocalTime thursdayEndTime;

	@Column
	private LocalTime fridayStartTime;

	@Column
	private LocalTime fridayEndTime;

	@Column
	private LocalTime saturdayStartTime;

	@Column
	private LocalTime saturdayEndTime;

	@Column
	private LocalTime sundayStartTime;

	@Column
	private LocalTime sundayEndTime;

	@Column(nullable = false)
	private Boolean active;

	@Column(length = 500)
	private String notes;

	public CalendarRule() {
	}

	public CalendarRule(LocalTime mondayStartTime, LocalTime mondayEndTime, LocalTime tuesdayStartTime,
			LocalTime tuesdayEndTime, LocalTime wednesdayStartTime, LocalTime wednesdayEndTime,
			LocalTime thursdayStartTime, LocalTime thursdayEndTime, LocalTime fridayStartTime, LocalTime fridayEndTime,
			LocalTime saturdayStartTime, LocalTime saturdayEndTime, LocalTime sundayStartTime, LocalTime sundayEndTime,
			Boolean active, String notes) {
		this.mondayStartTime = mondayStartTime;
		this.mondayEndTime = mondayEndTime;
		this.tuesdayStartTime = tuesdayStartTime;
		this.tuesdayEndTime = tuesdayEndTime;
		this.wednesdayStartTime = wednesdayStartTime;
		this.wednesdayEndTime = wednesdayEndTime;
		this.thursdayStartTime = thursdayStartTime;
		this.thursdayEndTime = thursdayEndTime;
		this.fridayStartTime = fridayStartTime;
		this.fridayEndTime = fridayEndTime;
		this.saturdayStartTime = saturdayStartTime;
		this.saturdayEndTime = saturdayEndTime;
		this.sundayStartTime = sundayStartTime;
		this.sundayEndTime = sundayEndTime;
		this.active = active != null ? active : true;
		this.notes = normalizeText(notes);
	}

	public Long getId() {
		return id;
	}

	public LocalTime getMondayStartTime() {
		return mondayStartTime;
	}

	public void setMondayStartTime(LocalTime mondayStartTime) {
		this.mondayStartTime = mondayStartTime;
	}

	public LocalTime getMondayEndTime() {
		return mondayEndTime;
	}

	public void setMondayEndTime(LocalTime mondayEndTime) {
		this.mondayEndTime = mondayEndTime;
	}

	public LocalTime getTuesdayStartTime() {
		return tuesdayStartTime;
	}

	public void setTuesdayStartTime(LocalTime tuesdayStartTime) {
		this.tuesdayStartTime = tuesdayStartTime;
	}

	public LocalTime getTuesdayEndTime() {
		return tuesdayEndTime;
	}

	public void setTuesdayEndTime(LocalTime tuesdayEndTime) {
		this.tuesdayEndTime = tuesdayEndTime;
	}

	public LocalTime getWednesdayStartTime() {
		return wednesdayStartTime;
	}

	public void setWednesdayStartTime(LocalTime wednesdayStartTime) {
		this.wednesdayStartTime = wednesdayStartTime;
	}

	public LocalTime getWednesdayEndTime() {
		return wednesdayEndTime;
	}

	public void setWednesdayEndTime(LocalTime wednesdayEndTime) {
		this.wednesdayEndTime = wednesdayEndTime;
	}

	public LocalTime getThursdayStartTime() {
		return thursdayStartTime;
	}

	public void setThursdayStartTime(LocalTime thursdayStartTime) {
		this.thursdayStartTime = thursdayStartTime;
	}

	public LocalTime getThursdayEndTime() {
		return thursdayEndTime;
	}

	public void setThursdayEndTime(LocalTime thursdayEndTime) {
		this.thursdayEndTime = thursdayEndTime;
	}

	public LocalTime getFridayStartTime() {
		return fridayStartTime;
	}

	public void setFridayStartTime(LocalTime fridayStartTime) {
		this.fridayStartTime = fridayStartTime;
	}

	public LocalTime getFridayEndTime() {
		return fridayEndTime;
	}

	public void setFridayEndTime(LocalTime fridayEndTime) {
		this.fridayEndTime = fridayEndTime;
	}

	public LocalTime getSaturdayStartTime() {
		return saturdayStartTime;
	}

	public void setSaturdayStartTime(LocalTime saturdayStartTime) {
		this.saturdayStartTime = saturdayStartTime;
	}

	public LocalTime getSaturdayEndTime() {
		return saturdayEndTime;
	}

	public void setSaturdayEndTime(LocalTime saturdayEndTime) {
		this.saturdayEndTime = saturdayEndTime;
	}

	public LocalTime getSundayStartTime() {
		return sundayStartTime;
	}

	public void setSundayStartTime(LocalTime sundayStartTime) {
		this.sundayStartTime = sundayStartTime;
	}

	public LocalTime getSundayEndTime() {
		return sundayEndTime;
	}

	public void setSundayEndTime(LocalTime sundayEndTime) {
		this.sundayEndTime = sundayEndTime;
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

	public static boolean isDayRangeValid(LocalTime startTime, LocalTime endTime) {
		if (startTime == null && endTime == null) {
			return true;
		}

		if (startTime == null || endTime == null) {
			return false;
		}

		return endTime.isAfter(startTime);
	}

	public static boolean isNotesValid(String notes) {
		return notes == null || notes.isBlank() || notes.trim().length() <= 500;
	}

	public static boolean isWeeklyScheduleValid(CalendarRule calendarRule) {
		if (calendarRule == null) {
			return false;
		}

		return isDayRangeValid(calendarRule.getMondayStartTime(), calendarRule.getMondayEndTime())
				&& isDayRangeValid(calendarRule.getTuesdayStartTime(), calendarRule.getTuesdayEndTime())
				&& isDayRangeValid(calendarRule.getWednesdayStartTime(), calendarRule.getWednesdayEndTime())
				&& isDayRangeValid(calendarRule.getThursdayStartTime(), calendarRule.getThursdayEndTime())
				&& isDayRangeValid(calendarRule.getFridayStartTime(), calendarRule.getFridayEndTime())
				&& isDayRangeValid(calendarRule.getSaturdayStartTime(), calendarRule.getSaturdayEndTime())
				&& isDayRangeValid(calendarRule.getSundayStartTime(), calendarRule.getSundayEndTime());
	}

	public static String normalizeText(String text) {
		return text == null ? null : text.trim();
	}
}