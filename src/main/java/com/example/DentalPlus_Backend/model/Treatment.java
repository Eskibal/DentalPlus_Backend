package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({ "id", "name", "description", "estimatedDurationMinutes", "beforeMarginMinutes",
		"afterMarginMinutes", "active", "notes" })
@Entity
@Table(name = "treatment")
public class Treatment {

	public static final int DEFAULT_ESTIMATED_DURATION_MINUTES = 30;
	public static final int DEFAULT_BEFORE_MARGIN_MINUTES = 0;
	public static final int DEFAULT_AFTER_MARGIN_MINUTES = 5;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(length = 500)
	private String description;

	@Column
	private Integer estimatedDurationMinutes;

	@Column
	private Integer beforeMarginMinutes;

	@Column
	private Integer afterMarginMinutes;

	@Column(nullable = false)
	private Boolean active;

	@Column(length = 500)
	private String notes;

	public Treatment() {
	}

	public Treatment(String name, String description, Integer estimatedDurationMinutes, Integer beforeMarginMinutes,
			Integer afterMarginMinutes, Boolean active, String notes) {
		this.name = normalizeText(name);
		this.description = normalizeText(description);
		this.estimatedDurationMinutes = normalizeEstimatedDurationMinutes(estimatedDurationMinutes);
		this.beforeMarginMinutes = normalizeBeforeMarginMinutes(beforeMarginMinutes);
		this.afterMarginMinutes = normalizeAfterMarginMinutes(afterMarginMinutes);
		this.active = active != null ? active : true;
		this.notes = normalizeText(notes);
	}

	public Treatment(String name, String description, Integer estimatedDurationMinutes, Boolean active, String notes) {
		this(name, description, estimatedDurationMinutes, DEFAULT_BEFORE_MARGIN_MINUTES, DEFAULT_AFTER_MARGIN_MINUTES,
				active, notes);
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

	public Integer getEstimatedDurationMinutes() {
		return estimatedDurationMinutes;
	}

	public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
		this.estimatedDurationMinutes = normalizeEstimatedDurationMinutes(estimatedDurationMinutes);
	}

	public Integer getBeforeMarginMinutes() {
		return beforeMarginMinutes;
	}

	public void setBeforeMarginMinutes(Integer beforeMarginMinutes) {
		this.beforeMarginMinutes = normalizeBeforeMarginMinutes(beforeMarginMinutes);
	}

	public Integer getAfterMarginMinutes() {
		return afterMarginMinutes;
	}

	public void setAfterMarginMinutes(Integer afterMarginMinutes) {
		this.afterMarginMinutes = normalizeAfterMarginMinutes(afterMarginMinutes);
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

	public int resolveEstimatedDurationMinutes() {
		return estimatedDurationMinutes == null ? DEFAULT_ESTIMATED_DURATION_MINUTES : estimatedDurationMinutes;
	}

	public int resolveBeforeMarginMinutes() {
		return beforeMarginMinutes == null ? DEFAULT_BEFORE_MARGIN_MINUTES : beforeMarginMinutes;
	}

	public int resolveAfterMarginMinutes() {
		return afterMarginMinutes == null ? DEFAULT_AFTER_MARGIN_MINUTES : afterMarginMinutes;
	}

	public static boolean isNameValid(String name) {
		return name != null && !name.isBlank() && name.trim().length() <= 120;
	}

	public static boolean isDescriptionValid(String description) {
		return description == null || description.isBlank() || description.trim().length() <= 500;
	}

	public static boolean isEstimatedDurationMinutesValid(Integer estimatedDurationMinutes) {
		return estimatedDurationMinutes == null || estimatedDurationMinutes >= 0;
	}

	public static boolean isBeforeMarginMinutesValid(Integer beforeMarginMinutes) {
		return beforeMarginMinutes == null || beforeMarginMinutes >= 0;
	}

	public static boolean isAfterMarginMinutesValid(Integer afterMarginMinutes) {
		return afterMarginMinutes == null || afterMarginMinutes >= 0;
	}

	public static boolean isNotesValid(String notes) {
		return notes == null || notes.isBlank() || notes.trim().length() <= 500;
	}

	public static Integer normalizeEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
		if (estimatedDurationMinutes == null) {
			return DEFAULT_ESTIMATED_DURATION_MINUTES;
		}

		return estimatedDurationMinutes < 0 ? DEFAULT_ESTIMATED_DURATION_MINUTES : estimatedDurationMinutes;
	}

	public static Integer normalizeBeforeMarginMinutes(Integer beforeMarginMinutes) {
		if (beforeMarginMinutes == null) {
			return DEFAULT_BEFORE_MARGIN_MINUTES;
		}

		return beforeMarginMinutes < 0 ? DEFAULT_BEFORE_MARGIN_MINUTES : beforeMarginMinutes;
	}

	public static Integer normalizeAfterMarginMinutes(Integer afterMarginMinutes) {
		if (afterMarginMinutes == null) {
			return DEFAULT_AFTER_MARGIN_MINUTES;
		}

		return afterMarginMinutes < 0 ? DEFAULT_AFTER_MARGIN_MINUTES : afterMarginMinutes;
	}

	public static String normalizeText(String text) {
		return text == null ? null : text.trim();
	}
}