package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({ "id", "person", "user", "clinic", "calendarRule", "active", "notes" })
@Entity
@Table(name = "receptionist")
public class Receptionist {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@OneToOne(optional = false)
	@JoinColumn(name = "person_id", nullable = false, unique = true)
	private Person person;

	@OneToOne(optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@ManyToOne(optional = false)
	@JoinColumn(name = "clinic_id", nullable = false)
	private Clinic clinic;

	@OneToOne
	@JoinColumn(name = "calendar_rule_id", unique = true)
	private CalendarRule calendarRule;

	@Column(nullable = false)
	private Boolean active;

	@Column(length = 500)
	private String notes;

	public Receptionist() {
	}

	public Receptionist(Person person, User user, Clinic clinic, CalendarRule calendarRule, Boolean active,
			String notes) {
		this.person = person;
		this.user = user;
		this.clinic = clinic;
		this.calendarRule = calendarRule;
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

	public CalendarRule getCalendarRule() {
		return calendarRule;
	}

	public void setCalendarRule(CalendarRule calendarRule) {
		this.calendarRule = calendarRule;
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