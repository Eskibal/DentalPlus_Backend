package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.DentalSurface;

import java.util.List;

public interface DentalSurfaceDao {
	DentalSurface findById(Long id);

	DentalSurface findByDentalPieceIdAndSurfaceType(Long dentalPieceId, String surfaceType);

	List<DentalSurface> findByDentalPieceId(Long dentalPieceId);

	List<DentalSurface> findByOdontogramId(Long odontogramId);

	void save(DentalSurface dentalSurface);

	DentalSurface update(DentalSurface dentalSurface);

	void delete(DentalSurface dentalSurface);
}