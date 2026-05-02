package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.DentalPiece;

import java.util.List;

public class DentalPieceDto {
	private Long id;
	private Long odontogramId;
	private Integer pieceNumber;
	private String pieceKind;
	private DentalPieceStateDto activeState;
	private List<DentalSurfaceDto> surfaces;

	public DentalPieceDto() {
	}

	public DentalPieceDto(DentalPiece dentalPiece, DentalPieceStateDto activeState, List<DentalSurfaceDto> surfaces) {
		this.id = dentalPiece.getId();
		this.odontogramId = dentalPiece.getOdontogram() == null ? null : dentalPiece.getOdontogram().getId();
		this.pieceNumber = dentalPiece.getPieceNumber();
		this.pieceKind = dentalPiece.getPieceKind();
		this.activeState = activeState;
		this.surfaces = surfaces;
	}

	public Long getId() {
		return id;
	}

	public Long getOdontogramId() {
		return odontogramId;
	}

	public Integer getPieceNumber() {
		return pieceNumber;
	}

	public String getPieceKind() {
		return pieceKind;
	}

	public DentalPieceStateDto getActiveState() {
		return activeState;
	}

	public List<DentalSurfaceDto> getSurfaces() {
		return surfaces;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setOdontogramId(Long odontogramId) {
		this.odontogramId = odontogramId;
	}

	public void setPieceNumber(Integer pieceNumber) {
		this.pieceNumber = pieceNumber;
	}

	public void setPieceKind(String pieceKind) {
		this.pieceKind = pieceKind;
	}

	public void setActiveState(DentalPieceStateDto activeState) {
		this.activeState = activeState;
	}

	public void setSurfaces(List<DentalSurfaceDto> surfaces) {
		this.surfaces = surfaces;
	}
}