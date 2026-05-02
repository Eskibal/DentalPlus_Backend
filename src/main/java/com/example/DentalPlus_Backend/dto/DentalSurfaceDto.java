package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.DentalSurface;

public class DentalSurfaceDto {
	private Long id;
	private Long dentalPieceId;
	private String surfaceType;
	private String notes;
	private DentalSurfaceMarkDto activeMark;

	public DentalSurfaceDto() {
	}

	public DentalSurfaceDto(DentalSurface surface, DentalSurfaceMarkDto activeMark) {
		this.id = surface.getId();
		this.dentalPieceId = surface.getDentalPiece() == null ? null : surface.getDentalPiece().getId();
		this.surfaceType = surface.getSurfaceType();
		this.notes = surface.getNotes();
		this.activeMark = activeMark;
	}

	public Long getId() {
		return id;
	}

	public Long getDentalPieceId() {
		return dentalPieceId;
	}

	public String getSurfaceType() {
		return surfaceType;
	}

	public String getNotes() {
		return notes;
	}

	public DentalSurfaceMarkDto getActiveMark() {
		return activeMark;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setDentalPieceId(Long dentalPieceId) {
		this.dentalPieceId = dentalPieceId;
	}

	public void setSurfaceType(String surfaceType) {
		this.surfaceType = surfaceType;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setActiveMark(DentalSurfaceMarkDto activeMark) {
		this.activeMark = activeMark;
	}
}