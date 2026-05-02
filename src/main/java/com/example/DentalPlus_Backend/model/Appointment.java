package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@JsonPropertyOrder({ "id", "box", "dentist", "patient", "startDateTime", "endDateTime", "status", "notes", "active" })
@Entity
@Table(name = "appointment")
public class Appointment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "box_id", nullable = false)
	private Box box;

	@ManyToOne(optional = false)
	@JoinColumn(name = "dentist_id", nullable = false)
	private Dentist dentist;

	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@Column(nullable = false)
	private LocalDateTime startDateTime;

	@Column(nullable = false)
	private LocalDateTime endDateTime;

	@Column(nullable = false, length = 20)
	private String status;

	@Column(length = 500)
	private String notes;

	@Column(nullable = false)
	private Boolean active;

	public Appointment() {
	}

	public Appointment(Box box, Dentist dentist, Patient patient, LocalDateTime startDateTime,
			LocalDateTime endDateTime, String status, String notes, Boolean active) {
		this.box = box;
		this.dentist = dentist;
		this.patient = patient;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		this.status = normalizeStatus(status);
		this.notes = normalizeText(notes);
		this.active = active != null ? active : true;
	}

	public Long getId() {
		return id;
	}

	public Box getBox() {
		return box;
	}

	public void setBox(Box box) {
		this.box = box;
	}

	public Dentist getDentist() {
		return dentist;
	}

	public void setDentist(Dentist dentist) {
		this.dentist = dentist;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(LocalDateTime startDateTime) {
		this.startDateTime = startDateTime;
	}

	public LocalDateTime getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(LocalDateTime endDateTime) {
		this.endDateTime = endDateTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = normalizeStatus(status);
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = normalizeText(notes);
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active != null ? active : true;
	}

	public static boolean isStartDateTimeValid(LocalDateTime startDateTime) {
		return startDateTime != null;
	}

	public static boolean isEndDateTimeValid(LocalDateTime endDateTime) {
		return endDateTime != null;
	}

	public static boolean isDateRangeValid(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		return startDateTime != null && endDateTime != null && endDateTime.isAfter(startDateTime);
	}

	public static boolean isStatusValid(String status) {
		if (status == null || status.isBlank()) {
			return false;
		}

		String normalized = status.trim().toUpperCase();

		return normalized.equals("SCHEDULED") || normalized.equals("COMPLETED") || normalized.equals("CANCELLED");
	}

	public static boolean isNotesValid(String notes) {
		return notes == null || notes.isBlank() || notes.trim().length() <= 500;
	}

	public static String normalizeStatus(String status) {
		return status == null ? null : status.trim().toUpperCase();
	}

	public static String normalizeText(String text) {
		return text == null ? null : text.trim();
	}
}