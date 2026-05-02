package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.DentalBridge;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DentalBridgeDto {

	private Long id;
	private Long odontogramId;
	private String bridgeState;
	private String notes;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Boolean active;
	private List<DentalBridgePieceDto> pieces;

	public DentalBridgeDto() {
	}

	@JsonCreator(mode = JsonCreator.Mode.DISABLED)
	public DentalBridgeDto(DentalBridge bridge, List<DentalBridgePieceDto> pieces) {
		this.id = bridge.getId();
		this.odontogramId = bridge.getOdontogram() == null ? null : bridge.getOdontogram().getId();
		this.bridgeState = bridge.getBridgeState();
		this.notes = bridge.getNotes();
		this.createdAt = bridge.getCreatedAt();
		this.updatedAt = bridge.getUpdatedAt();
		this.active = bridge.getActive();
		this.pieces = pieces;
	}

	public Long getId() {
		return id;
	}

	public Long getOdontogramId() {
		return odontogramId;
	}

	public String getBridgeState() {
		return bridgeState;
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

	public List<DentalBridgePieceDto> getPieces() {
		return pieces;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setOdontogramId(Long odontogramId) {
		this.odontogramId = odontogramId;
	}

	public void setBridgeState(String bridgeState) {
		this.bridgeState = bridgeState;
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

	public void setPieces(List<DentalBridgePieceDto> pieces) {
		this.pieces = pieces;
	}
}