package com.example.DentalPlus_Backend.dto;

import java.time.LocalTime;

public class DayScheduleDto {

	private LocalTime startTime;
	private LocalTime endTime;
	private Boolean working;

	public DayScheduleDto() {
	}

	public DayScheduleDto(LocalTime startTime, LocalTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.working = startTime != null && endTime != null;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public Boolean getWorking() {
		return working;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
		this.working = this.startTime != null && this.endTime != null;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
		this.working = this.startTime != null && this.endTime != null;
	}

	public void setWorking(Boolean working) {
		this.working = working;
	}
}