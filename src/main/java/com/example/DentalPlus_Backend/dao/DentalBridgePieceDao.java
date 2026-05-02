package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.DentalBridgePiece;

import java.util.List;

public interface DentalBridgePieceDao {
	DentalBridgePiece findById(Long id);

	List<DentalBridgePiece> findByDentalBridgeId(Long dentalBridgeId);

	void save(DentalBridgePiece dentalBridgePiece);

	DentalBridgePiece update(DentalBridgePiece dentalBridgePiece);

	void delete(DentalBridgePiece dentalBridgePiece);

	void deleteByDentalBridgeId(Long dentalBridgeId);
}