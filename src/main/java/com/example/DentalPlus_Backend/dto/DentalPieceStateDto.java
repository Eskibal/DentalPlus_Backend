package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.DentalPieceState;

import java.time.LocalDateTime;

public class DentalPieceStateDto {
	private Long id;
	private Long dentalPieceId;
	private String stateType;
	private String notes;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Boolean active;

	public DentalPieceStateDto() {
	}

	public DentalPieceStateDto(DentalPieceState state) {
		this.id = state.getId();
		this.dentalPieceId = state.getDentalPiece() == null ? null : state.getDentalPiece().getId();
		this.stateType = state.getStateType();
		this.notes = state.getNotes();
		this.createdAt = state.getCreatedAt();
		this.updatedAt = state.getUpdatedAt();
		this.active = state.getActive();
	}

	public Long getId() {
		return id;
	}

	public Long getDentalPieceId() {
		return dentalPieceId;
	}

	public String getStateType() {
		return stateType;
	}

	public String getNotes() {
		return notes;
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

	public void setId(Long id) {
		this.id = id;
	}

	public void setDentalPieceId(Long dentalPieceId) {
		this.dentalPieceId = dentalPieceId;
	}

	public void setStateType(String stateType) {
		this.stateType = stateType;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
}