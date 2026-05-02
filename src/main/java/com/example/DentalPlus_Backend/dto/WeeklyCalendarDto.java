package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.CalendarRule;

import java.time.LocalTime;

public class WeeklyCalendarDto {

	private Long id;
	private LocalTime mondayStartTime;
	private LocalTime mondayEndTime;
	private LocalTime tuesdayStartTime;
	private LocalTime tuesdayEndTime;
	private LocalTime wednesdayStartTime;
	private LocalTime wednesdayEndTime;
	private LocalTime thursdayStartTime;
	private LocalTime thursdayEndTime;
	private LocalTime fridayStartTime;
	private LocalTime fridayEndTime;
	private LocalTime saturdayStartTime;
	private LocalTime saturdayEndTime;
	private LocalTime sundayStartTime;
	private LocalTime sundayEndTime;
	private Boolean active;
	private String notes;

	public WeeklyCalendarDto() {
	}

	public WeeklyCalendarDto(CalendarRule calendarRule) {
		this.id = calendarRule.getId();
		this.mondayStartTime = calendarRule.getMondayStartTime();
		this.mondayEndTime = calendarRule.getMondayEndTime();
		this.tuesdayStartTime = calendarRule.getTuesdayStartTime();
		this.tuesdayEndTime = calendarRule.getTuesdayEndTime();
		this.wednesdayStartTime = calendarRule.getWednesdayStartTime();
		this.wednesdayEndTime = calendarRule.getWednesdayEndTime();
		this.thursdayStartTime = calendarRule.getThursdayStartTime();
		this.thursdayEndTime = calendarRule.getThursdayEndTime();
		this.fridayStartTime = calendarRule.getFridayStartTime();
		this.fridayEndTime = calendarRule.getFridayEndTime();
		this.saturdayStartTime = calendarRule.getSaturdayStartTime();
		this.saturdayEndTime = calendarRule.getSaturdayEndTime();
		this.sundayStartTime = calendarRule.getSundayStartTime();
		this.sundayEndTime = calendarRule.getSundayEndTime();
		this.active = calendarRule.getActive();
		this.notes = calendarRule.getNotes();
	}

	public Long getId() {
		return id;
	}

	public LocalTime getMondayStartTime() {
		return mondayStartTime;
	}

	public LocalTime getMondayEndTime() {
		return mondayEndTime;
	}

	public LocalTime getTuesdayStartTime() {
		return tuesdayStartTime;
	}

	public LocalTime getTuesdayEndTime() {
		return tuesdayEndTime;
	}

	public LocalTime getWednesdayStartTime() {
		return wednesdayStartTime;
	}

	public LocalTime getWednesdayEndTime() {
		return wednesdayEndTime;
	}

	public LocalTime getThursdayStartTime() {
		return thursdayStartTime;
	}

	public LocalTime getThursdayEndTime() {
		return thursdayEndTime;
	}

	public LocalTime getFridayStartTime() {
		return fridayStartTime;
	}

	public LocalTime getFridayEndTime() {
		return fridayEndTime;
	}

	public LocalTime getSaturdayStartTime() {
		return saturdayStartTime;
	}

	public LocalTime getSaturdayEndTime() {
		return saturdayEndTime;
	}

	public LocalTime getSundayStartTime() {
		return sundayStartTime;
	}

	public LocalTime getSundayEndTime() {
		return sundayEndTime;
	}

	public Boolean getActive() {
		return active;
	}

	public String getNotes() {
		return notes;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setMondayStartTime(LocalTime mondayStartTime) {
		this.mondayStartTime = mondayStartTime;
	}

	public void setMondayEndTime(LocalTime mondayEndTime) {
		this.mondayEndTime = mondayEndTime;
	}

	public void setTuesdayStartTime(LocalTime tuesdayStartTime) {
		this.tuesdayStartTime = tuesdayStartTime;
	}

	public void setTuesdayEndTime(LocalTime tuesdayEndTime) {
		this.tuesdayEndTime = tuesdayEndTime;
	}

	public void setWednesdayStartTime(LocalTime wednesdayStartTime) {
		this.wednesdayStartTime = wednesdayStartTime;
	}

	public void setWednesdayEndTime(LocalTime wednesdayEndTime) {
		this.wednesdayEndTime = wednesdayEndTime;
	}

	public void setThursdayStartTime(LocalTime thursdayStartTime) {
		this.thursdayStartTime = thursdayStartTime;
	}

	public void setThursdayEndTime(LocalTime thursdayEndTime) {
		this.thursdayEndTime = thursdayEndTime;
	}

	public void setFridayStartTime(LocalTime fridayStartTime) {
		this.fridayStartTime = fridayStartTime;
	}

	public void setFridayEndTime(LocalTime fridayEndTime) {
		this.fridayEndTime = fridayEndTime;
	}

	public void setSaturdayStartTime(LocalTime saturdayStartTime) {
		this.saturdayStartTime = saturdayStartTime;
	}

	public void setSaturdayEndTime(LocalTime saturdayEndTime) {
		this.saturdayEndTime = saturdayEndTime;
	}

	public void setSundayStartTime(LocalTime sundayStartTime) {
		this.sundayStartTime = sundayStartTime;
	}

	public void setSundayEndTime(LocalTime sundayEndTime) {
		this.sundayEndTime = sundayEndTime;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}