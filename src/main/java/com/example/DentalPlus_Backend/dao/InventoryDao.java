package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Inventory;

import java.util.List;

public interface InventoryDao {
	Inventory findById(Long id);

	Inventory findByBoxIdAndProductId(Long boxId, Long productId);

	List<Inventory> findByBoxId(Long boxId);

	List<Inventory> findActiveByBoxId(Long boxId);

	List<Inventory> findByClinicId(Long clinicId);

	List<Inventory> findActiveByClinicId(Long clinicId);

	List<Inventory> findLowStockByBoxId(Long boxId);

	List<Inventory> findLowStockByClinicId(Long clinicId);

	void save(Inventory inventory);

	Inventory update(Inventory inventory);

	void delete(Inventory inventory);
}