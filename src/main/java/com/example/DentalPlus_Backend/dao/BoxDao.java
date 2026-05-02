package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Box;

import java.util.List;

public interface BoxDao {
	Box findById(Long id);

	List<Box> findByClinicId(Long clinicId);

	List<Box> findActiveByClinicId(Long clinicId);

	void save(Box box);

	Box update(Box box);

	void delete(Box box);
}