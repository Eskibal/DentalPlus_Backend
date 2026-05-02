package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.DentalSurfaceMark;

import java.util.List;

public interface DentalSurfaceMarkDao {
	DentalSurfaceMark findById(Long id);

	DentalSurfaceMark findActiveByDentalSurfaceId(Long dentalSurfaceId);

	List<DentalSurfaceMark> findByDentalSurfaceId(Long dentalSurfaceId);

	List<DentalSurfaceMark> findActiveByOdontogramId(Long odontogramId);

	void deactivateActiveByDentalSurfaceId(Long dentalSurfaceId);

	void save(DentalSurfaceMark dentalSurfaceMark);

	DentalSurfaceMark update(DentalSurfaceMark dentalSurfaceMark);

	void delete(DentalSurfaceMark dentalSurfaceMark);
}