package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.DentalSurfaceMark;

import java.time.LocalDateTime;

public class DentalSurfaceMarkDto {
	private Long id;
	private Long dentalSurfaceId;
	private String markType;
	private String markState;
	private String notes;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Boolean active;

	public DentalSurfaceMarkDto() {
	}

	public DentalSurfaceMarkDto(DentalSurfaceMark mark) {
		this.id = mark.getId();
		this.dentalSurfaceId = mark.getDentalSurface() == null ? null : mark.getDentalSurface().getId();
		this.markType = mark.getMarkType();
		this.markState = mark.getMarkState();
		this.notes = mark.getNotes();
		this.createdAt = mark.getCreatedAt();
		this.updatedAt = mark.getUpdatedAt();
		this.active = mark.getActive();
	}

	public Long getId() {
		return id;
	}

	public Long getDentalSurfaceId() {
		return dentalSurfaceId;
	}

	public String getMarkType() {
		return markType;
	}

	public String getMarkState() {
		return markState;
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

	public void setDentalSurfaceId(Long dentalSurfaceId) {
		this.dentalSurfaceId = dentalSurfaceId;
	}

	public void setMarkType(String markType) {
		this.markType = markType;
	}

	public void setMarkState(String markState) {
		this.markState = markState;
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