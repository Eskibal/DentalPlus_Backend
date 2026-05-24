package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({ "id", "speciality", "treatment", "active", "notes" })
@Entity
@Table(
	name = "speciality_treatment",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "speciality_id", "treatment_id" })
	}
)
public class SpecialityTreatment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "speciality_id", nullable = false)
	private Speciality speciality;

	@ManyToOne(optional = false)
	@JoinColumn(name = "treatment_id", nullable = false)
	private Treatment treatment;

	@Column(nullable = false)
	private Boolean active;

	@Column(length = 500)
	private String notes;

	public SpecialityTreatment() {
	}

	public SpecialityTreatment(Speciality speciality, Treatment treatment, Boolean active, String notes) {
		this.speciality = speciality;
		this.treatment = treatment;
		this.active = active != null ? active : true;
		this.notes = normalizeText(notes);
	}

	public Long getId() {
		return id;
	}

	public Speciality getSpeciality() {
		return speciality;
	}

	public void setSpeciality(Speciality speciality) {
		this.speciality = speciality;
	}

	public Treatment getTreatment() {
		return treatment;
	}

	public void setTreatment(Treatment treatment) {
		this.treatment = treatment;
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
		if (text == null || text.isBlank()) {
			return null;
		}

		return text.trim();
	}
}