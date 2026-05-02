package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.DentalPieceState;

import java.util.List;

public interface DentalPieceStateDao {
	DentalPieceState findById(Long id);

	DentalPieceState findActiveByDentalPieceId(Long dentalPieceId);

	List<DentalPieceState> findByDentalPieceId(Long dentalPieceId);

	List<DentalPieceState> findActiveByOdontogramId(Long odontogramId);

	void deactivateActiveByDentalPieceId(Long dentalPieceId);

	void save(DentalPieceState dentalPieceState);

	DentalPieceState update(DentalPieceState dentalPieceState);

	void delete(DentalPieceState dentalPieceState);
}