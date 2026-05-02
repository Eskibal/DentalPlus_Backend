package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.DentalPiece;

import java.util.List;

public interface DentalPieceDao {
	DentalPiece findById(Long id);

	DentalPiece findByOdontogramIdAndPieceNumber(Long odontogramId, Integer pieceNumber);

	List<DentalPiece> findByOdontogramId(Long odontogramId);

	void save(DentalPiece dentalPiece);

	DentalPiece update(DentalPiece dentalPiece);

	void delete(DentalPiece dentalPiece);
}