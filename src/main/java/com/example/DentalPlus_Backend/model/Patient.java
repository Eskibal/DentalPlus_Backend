package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

import java.time.LocalDate;

@JsonPropertyOrder({ "id", "person", "user", "clinic", "registrationDate", "active", "medicalAlert", "notes" })
@Entity
@Table(name = "patient")
public class Patient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@OneToOne(optional = false)
	@JoinColumn(name = "person_id", nullable = false, unique = true)
	private Person person;

	@OneToOne
	@JoinColumn(name = "user_id", unique = true)
	private User user;

	@ManyToOne(optional = false)
	@JoinColumn(name = "clinic_id", nullable = false)
	private Clinic clinic;

	@Column(nullable = false)
	private LocalDate registrationDate;

	@Column(nullable = false)
	private Boolean active;

	@Column(name = "medical_alert", length = 80)
	private String medicalAlert;

	@Column(length = 500)
	private String notes;

	public Patient() {
	}

	public Patient(Person person, User user, Clinic clinic, Boolean active, String medicalAlert, String notes) {
		this.person = person;
		this.user = user;
		this.clinic = clinic;
		this.registrationDate = LocalDate.now();
		this.active = active != null ? active : true;
		this.medicalAlert = normalizeMedicalAlert(medicalAlert);
		this.notes = normalizeText(notes);
	}

	public Long getId() {
		return id;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Clinic getClinic() {
		return clinic;
	}

	public void setClinic(Clinic clinic) {
		this.clinic = clinic;
	}

	public LocalDate getRegistrationDate() {
		return registrationDate;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active != null ? active : true;
	}
	
	public String getMedicalAlert() {
		return medicalAlert;
	}

	public void setMedicalAlert(String medicalAlert) {
		this.medicalAlert = normalizeMedicalAlert(medicalAlert);
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = normalizeText(notes);
	}

	public static boolean isNotesValid(String notes) {
		return notes == null || notes.isBlank() || notes.trim().length() <= 500;
	}

	public static String normalizeText(String text) {
		return text == null ? null : text.trim();
	}
	
	public static boolean isMedicalAlertValid(String medicalAlert) {
		if (medicalAlert == null || medicalAlert.isBlank()) {
			return true;
		}

		String normalized = normalizeMedicalAlert(medicalAlert);

		if (normalized == null) {
			return true;
		}

		String[] alerts = normalized.split("\\|");

		for (String alert : alerts) {
			if (!isSingleMedicalAlertValid(alert)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isSingleMedicalAlertValid(String medicalAlert) {
		if (medicalAlert == null || medicalAlert.isBlank()) {
			return false;
		}

		return medicalAlert.equals("ALLERGY:PENICILLIN")
				|| medicalAlert.equals("ALLERGY:LATEX")
				|| medicalAlert.equals("ALLERGY:ANESTHETIC")
				|| medicalAlert.equals("ADVERSE_REACTION:ANESTHETIC")
				|| medicalAlert.equals("INFECTION_RISK:HIV")
				|| medicalAlert.equals("INFECTION_RISK:HEPATITIS_B")
				|| medicalAlert.equals("INFECTION_RISK:HEPATITIS_C")
				|| medicalAlert.equals("BLEEDING_RISK:ANTICOAGULANTS")
				|| medicalAlert.equals("CARDIAC_RISK:ANTIBIOTIC_PROPHYLAXIS")
				|| medicalAlert.equals("OTHER:REVIEW_NOTES");
	}

	public static String normalizeMedicalAlert(String medicalAlert) {
		if (medicalAlert == null || medicalAlert.isBlank()) {
			return null;
		}

		String[] alerts = medicalAlert.trim().toUpperCase().split("\\|");
		StringBuilder normalized = new StringBuilder();

		for (String alert : alerts) {
			String trimmedAlert = alert.trim();

			if (trimmedAlert.isBlank()) {
				continue;
			}

			if (!normalized.isEmpty()) {
				normalized.append("|");
			}

			normalized.append(trimmedAlert);
		}

		return normalized.isEmpty() ? null : normalized.toString();
	}
}