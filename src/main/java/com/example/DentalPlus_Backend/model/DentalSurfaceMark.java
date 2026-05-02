package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@JsonPropertyOrder({ "id", "dentalSurface", "markType", "markState", "notes", "createdAt", "updatedAt", "active" })
@Entity
@Table(name = "dental_surface_mark")
public class DentalSurfaceMark {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "dental_surface_id", nullable = false)
	private DentalSurface dentalSurface;

	@Column(nullable = false, length = 40)
	private String markType;

	@Column(nullable = false, length = 20)
	private String markState;

	@Column(length = 500)
	private String notes;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@Column(nullable = false)
	private Boolean active;

	public DentalSurfaceMark() {
	}

	public DentalSurfaceMark(DentalSurface dentalSurface, String markType, String markState, String notes) {
		this.dentalSurface = dentalSurface;
		this.markType = normalizeMarkType(markType);
		this.markState = normalizeMarkState(markState);
		this.notes = normalizeText(notes);
		this.active = true;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
		this.markType = normalizeMarkType(this.markType);
		this.markState = normalizeMarkState(this.markState);
		this.notes = normalizeText(this.notes);

		if (this.active == null) {
			this.active = true;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
		this.markType = normalizeMarkType(this.markType);
		this.markState = normalizeMarkState(this.markState);
		this.notes = normalizeText(this.notes);

		if (this.active == null) {
			this.active = true;
		}
	}

	public Long getId() {
		return id;
	}

	public DentalSurface getDentalSurface() {
		return dentalSurface;
	}

	public void setDentalSurface(DentalSurface dentalSurface) {
		this.dentalSurface = dentalSurface;
	}

	public String getMarkType() {
		return markType;
	}

	public void setMarkType(String markType) {
		this.markType = normalizeMarkType(markType);
	}

	public String getMarkState() {
		return markState;
	}

	public void setMarkState(String markState) {
		this.markState = normalizeMarkState(markState);
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = normalizeText(notes);
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active != null ? active : true;
	}

	public static boolean isMarkTypeValid(String markType) {
		if (markType == null || markType.isBlank()) {
			return false;
		}

		String normalized = markType.trim().toUpperCase();

		return normalized.equals("CARIES") || normalized.equals("FILLING") || normalized.equals("RADIOGRAPH_CARIES")
				|| normalized.equals("FISSURE_SEALANT") || normalized.equals("EXTRACTION") || normalized.equals("CROWN")
				|| normalized.equals("ENDODONTICS") || normalized.equals("BRIDGE")
				|| normalized.equals("NATURAL_ABSENCE");
	}

	public static boolean isMarkStateValid(String markState) {
		if (markState == null || markState.isBlank()) {
			return false;
		}

		String normalized = markState.trim().toUpperCase();

		return normalized.equals("PENDING") || normalized.equals("DONE") || normalized.equals("NATURAL");
	}

	public static boolean isNotesValid(String notes) {
		return notes == null || notes.isBlank() || notes.trim().length() <= 500;
	}

	public static String normalizeMarkType(String markType) {
		return markType == null ? null : markType.trim().toUpperCase();
	}

	public static String normalizeMarkState(String markState) {
		return markState == null ? null : markState.trim().toUpperCase();
	}

	public static String normalizeText(String text) {
		return text == null ? null : text.trim();
	}
}