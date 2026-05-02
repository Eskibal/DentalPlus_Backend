package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

@JsonPropertyOrder({ "id", "dentalBridge", "dentalPiece", "pieceRole" })
@Entity
@Table(name = "dental_bridge_piece", uniqueConstraints = @UniqueConstraint(columnNames = { "dental_bridge_id",
		"dental_piece_id" }))
public class DentalBridgePiece {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "dental_bridge_id", nullable = false)
	private DentalBridge dentalBridge;

	@ManyToOne(optional = false)
	@JoinColumn(name = "dental_piece_id", nullable = false)
	private DentalPiece dentalPiece;

	@Column(nullable = false, length = 20)
	private String pieceRole;

	public DentalBridgePiece() {
	}

	public DentalBridgePiece(DentalBridge dentalBridge, DentalPiece dentalPiece, String pieceRole) {
		this.dentalBridge = dentalBridge;
		this.dentalPiece = dentalPiece;
		this.pieceRole = normalizePieceRole(pieceRole);
	}

	@PrePersist
	@PreUpdate
	protected void normalizeBeforeSave() {
		this.pieceRole = normalizePieceRole(this.pieceRole);
	}

	public Long getId() {
		return id;
	}

	public DentalBridge getDentalBridge() {
		return dentalBridge;
	}

	public void setDentalBridge(DentalBridge dentalBridge) {
		this.dentalBridge = dentalBridge;
	}

	public DentalPiece getDentalPiece() {
		return dentalPiece;
	}

	public void setDentalPiece(DentalPiece dentalPiece) {
		this.dentalPiece = dentalPiece;
	}

	public String getPieceRole() {
		return pieceRole;
	}

	public void setPieceRole(String pieceRole) {
		this.pieceRole = normalizePieceRole(pieceRole);
	}

	public static boolean isPieceRoleValid(String pieceRole) {
		if (pieceRole == null || pieceRole.isBlank()) {
			return false;
		}

		String normalized = pieceRole.trim().toUpperCase();

		return normalized.equals("ABUTMENT") || normalized.equals("PONTIC");
	}

	public static String normalizePieceRole(String pieceRole) {
		return pieceRole == null ? null : pieceRole.trim().toUpperCase();
	}
}