package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Odontogram;

public interface OdontogramDao {
	Odontogram findById(Long id);

	Odontogram findByPatientId(Long patientId);

	boolean existsByPatientId(Long patientId);

	void save(Odontogram odontogram);

	Odontogram update(Odontogram odontogram);

	void delete(Odontogram odontogram);
}