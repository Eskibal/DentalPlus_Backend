package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@JsonPropertyOrder({ "id", "odontogram", "bridgeState", "notes", "createdAt", "updatedAt", "active" })
@Entity
@Table(name = "dental_bridge")
public class DentalBridge {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "odontogram_id", nullable = false)
	private Odontogram odontogram;

	@Column(nullable = false, length = 20)
	private String bridgeState;

	@Column(length = 500)
	private String notes;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@Column(nullable = false)
	private Boolean active;

	public DentalBridge() {
	}

	public DentalBridge(Odontogram odontogram, String bridgeState, String notes) {
		this.odontogram = odontogram;
		this.bridgeState = normalizeBridgeState(bridgeState);
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
		this.bridgeState = normalizeBridgeState(this.bridgeState);
		this.notes = normalizeText(this.notes);

		if (this.active == null) {
			this.active = true;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
		this.bridgeState = normalizeBridgeState(this.bridgeState);
		this.notes = normalizeText(this.notes);

		if (this.active == null) {
			this.active = true;
		}
	}

	public Long getId() {
		return id;
	}

	public Odontogram getOdontogram() {
		return odontogram;
	}

	public void setOdontogram(Odontogram odontogram) {
		this.odontogram = odontogram;
	}

	public String getBridgeState() {
		return bridgeState;
	}

	public void setBridgeState(String bridgeState) {
		this.bridgeState = normalizeBridgeState(bridgeState);
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

	public static boolean isBridgeStateValid(String bridgeState) {
		if (bridgeState == null || bridgeState.isBlank()) {
			return false;
		}

		String normalized = bridgeState.trim().toUpperCase();

		return normalized.equals("PENDING") || normalized.equals("DONE");
	}

	public static boolean isNotesValid(String notes) {
		return notes == null || notes.isBlank() || notes.trim().length() <= 500;
	}

	public static String normalizeBridgeState(String bridgeState) {
		return bridgeState == null ? null : bridgeState.trim().toUpperCase();
	}

	public static String normalizeText(String text) {
		return text == null ? null : text.trim();
	}
}