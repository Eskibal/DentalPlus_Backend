package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({ "id", "name", "description", "active", "notes" })
@Entity
@Table(name = "speciality")
public class Speciality {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@Column(nullable = false, unique = true, length = 120)
	private String name;

	@Column(length = 500)
	private String description;

	@Column(nullable = false)
	private Boolean active;

	@Column(length = 500)
	private String notes;

	public Speciality() {
	}

	public Speciality(String name, String description, Boolean active, String notes) {
		this.name = normalizeText(name);
		this.description = normalizeText(description);
		this.active = active != null ? active : true;
		this.notes = normalizeText(notes);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = normalizeText(name);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = normalizeText(description);
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

	public static boolean isDescriptionValid(String description) {
		return description == null || description.isBlank() || description.trim().length() <= 500;
	}

	public static boolean isNotesValid(String notes) {
		return notes == null || notes.isBlank() || notes.trim().length() <= 500;
	}

	public static String normalizeText(String text) {
		if (text == null || text.isBlank()) {
			return null;
		}

		return text.trim();
	}
}