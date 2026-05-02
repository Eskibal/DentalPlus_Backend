package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.Appointment;

import java.time.LocalDateTime;

public class AppointmentDto {
	private Long id;
	private Long boxId;
	private String boxName;
	private Long dentistId;
	private String dentistName;
	private Long patientId;
	private String patientName;
	private LocalDateTime startDateTime;
	private LocalDateTime endDateTime;
	private String status;
	private String notes;
	private Boolean active;

	public AppointmentDto() {
	}

	public AppointmentDto(Appointment appointment) {
		this.id = appointment.getId();
		this.boxId = appointment.getBox() == null ? null : appointment.getBox().getId();
		this.boxName = appointment.getBox() == null ? null : appointment.getBox().getName();
		this.dentistId = appointment.getDentist() == null ? null : appointment.getDentist().getId();
		this.dentistName = appointment.getDentist() == null || appointment.getDentist().getPerson() == null ? null
				: buildPersonFullName(appointment.getDentist().getPerson().getName(),
						appointment.getDentist().getPerson().getFirstSurname(),
						appointment.getDentist().getPerson().getSecondSurname());
		this.patientId = appointment.getPatient() == null ? null : appointment.getPatient().getId();
		this.patientName = appointment.getPatient() == null || appointment.getPatient().getPerson() == null ? null
				: buildPersonFullName(appointment.getPatient().getPerson().getName(),
						appointment.getPatient().getPerson().getFirstSurname(),
						appointment.getPatient().getPerson().getSecondSurname());
		this.startDateTime = appointment.getStartDateTime();
		this.endDateTime = appointment.getEndDateTime();
		this.status = appointment.getStatus();
		this.notes = appointment.getNotes();
		this.active = appointment.getActive();
	}

	private String buildPersonFullName(String name, String firstSurname, String secondSurname) {
		StringBuilder fullName = new StringBuilder();

		if (name != null && !name.isBlank()) {
			fullName.append(name.trim());
		}

		if (firstSurname != null && !firstSurname.isBlank()) {
			if (!fullName.isEmpty()) {
				fullName.append(" ");
			}
			fullName.append(firstSurname.trim());
		}

		if (secondSurname != null && !secondSurname.isBlank()) {
			if (!fullName.isEmpty()) {
				fullName.append(" ");
			}
			fullName.append(secondSurname.trim());
		}

		return fullName.toString();
	}

	public Long getId() {
		return id;
	}

	public Long getBoxId() {
		return boxId;
	}

	public String getBoxName() {
		return boxName;
	}

	public Long getDentistId() {
		return dentistId;
	}

	public String getDentistName() {
		return dentistName;
	}

	public Long getPatientId() {
		return patientId;
	}

	public String getPatientName() {
		return patientName;
	}

	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}

	public LocalDateTime getEndDateTime() {
		return endDateTime;
	}

	public String getStatus() {
		return status;
	}

	public String getNotes() {
		return notes;
	}

	public Boolean getActive() {
		return active;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setBoxId(Long boxId) {
		this.boxId = boxId;
	}

	public void setBoxName(String boxName) {
		this.boxName = boxName;
	}

	public void setDentistId(Long dentistId) {
		this.dentistId = dentistId;
	}

	public void setDentistName(String dentistName) {
		this.dentistName = dentistName;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public void setStartDateTime(LocalDateTime startDateTime) {
		this.startDateTime = startDateTime;
	}

	public void setEndDateTime(LocalDateTime endDateTime) {
		this.endDateTime = endDateTime;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
}