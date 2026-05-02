package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

import java.time.LocalDate;

@JsonPropertyOrder({ "id", "person", "user", "clinic", "registrationDate", "active", "notes" })
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

	@Column(length = 500)
	private String notes;

	public Patient() {
	}

	public Patient(Person person, User user, Clinic clinic, Boolean active, String notes) {
		this.person = person;
		this.user = user;
		this.clinic = clinic;
		this.registrationDate = LocalDate.now();
		this.active = active != null ? active : true;
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
}