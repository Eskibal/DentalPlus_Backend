package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.Odontogram;

import java.time.LocalDateTime;
import java.util.List;

public class OdontogramDto {
	private Long id;
	private Long patientId;
	private String viewMode;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<DentalPieceDto> pieces;
	private List<DentalBridgeDto> bridges;

	public OdontogramDto() {
	}

	public OdontogramDto(Odontogram odontogram, List<DentalPieceDto> pieces, List<DentalBridgeDto> bridges) {
		this.id = odontogram.getId();
		this.patientId = odontogram.getPatient() == null ? null : odontogram.getPatient().getId();
		this.viewMode = odontogram.getViewMode();
		this.createdAt = odontogram.getCreatedAt();
		this.updatedAt = odontogram.getUpdatedAt();
		this.pieces = pieces;
		this.bridges = bridges;
	}

	public Long getId() {
		return id;
	}

	public Long getPatientId() {
		return patientId;
	}

	public String getViewMode() {
		return viewMode;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public List<DentalPieceDto> getPieces() {
		return pieces;
	}

	public List<DentalBridgeDto> getBridges() {
		return bridges;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public void setViewMode(String viewMode) {
		this.viewMode = viewMode;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setPieces(List<DentalPieceDto> pieces) {
		this.pieces = pieces;
	}

	public void setBridges(List<DentalBridgeDto> bridges) {
		this.bridges = bridges;
	}
}