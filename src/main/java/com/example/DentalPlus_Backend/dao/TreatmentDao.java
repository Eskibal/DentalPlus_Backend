package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Treatment;

import java.util.List;

public interface TreatmentDao {

	Treatment save(Treatment treatment);

	Treatment update(Treatment treatment);

	void delete(Treatment treatment);

	Treatment findById(Long id);

	Treatment findActiveById(Long id);

	List<Treatment> findAll();

	List<Treatment> findActive();

	Treatment findByName(String name);
}