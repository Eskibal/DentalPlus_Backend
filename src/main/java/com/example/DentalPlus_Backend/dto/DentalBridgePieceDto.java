package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.DentalBridgePiece;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DentalBridgePieceDto {

	private Long id;
	private Long dentalBridgeId;
	private Long dentalPieceId;
	private Integer pieceNumber;
	private String pieceRole;

	public DentalBridgePieceDto() {
	}

	@JsonCreator(mode = JsonCreator.Mode.DISABLED)
	public DentalBridgePieceDto(DentalBridgePiece bridgePiece) {
		this.id = bridgePiece.getId();
		this.dentalBridgeId = bridgePiece.getDentalBridge() == null ? null : bridgePiece.getDentalBridge().getId();
		this.dentalPieceId = bridgePiece.getDentalPiece() == null ? null : bridgePiece.getDentalPiece().getId();
		this.pieceNumber = bridgePiece.getDentalPiece() == null ? null : bridgePiece.getDentalPiece().getPieceNumber();
		this.pieceRole = bridgePiece.getPieceRole();
	}

	public Long getId() {
		return id;
	}

	public Long getDentalBridgeId() {
		return dentalBridgeId;
	}

	public Long getDentalPieceId() {
		return dentalPieceId;
	}

	public Integer getPieceNumber() {
		return pieceNumber;
	}

	public String getPieceRole() {
		return pieceRole;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setDentalBridgeId(Long dentalBridgeId) {
		this.dentalBridgeId = dentalBridgeId;
	}

	public void setDentalPieceId(Long dentalPieceId) {
		this.dentalPieceId = dentalPieceId;
	}

	public void setPieceNumber(Integer pieceNumber) {
		this.pieceNumber = pieceNumber;
	}

	public void setPieceRole(String pieceRole) {
		this.pieceRole = pieceRole;
	}
}