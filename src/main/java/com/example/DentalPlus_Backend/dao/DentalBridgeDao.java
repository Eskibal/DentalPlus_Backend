package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.DentalBridge;

import java.util.List;

public interface DentalBridgeDao {
	DentalBridge findById(Long id);

	List<DentalBridge> findByOdontogramId(Long odontogramId);

	List<DentalBridge> findActiveByOdontogramId(Long odontogramId);

	void save(DentalBridge dentalBridge);

	DentalBridge update(DentalBridge dentalBridge);

	void delete(DentalBridge dentalBridge);
}