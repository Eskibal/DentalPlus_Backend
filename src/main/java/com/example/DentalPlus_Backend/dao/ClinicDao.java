package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Clinic;

import java.util.List;

public interface ClinicDao {
	Clinic findById(Long id);

	List<Clinic> findAll();

	Clinic findFirstActive();

	void save(Clinic clinic);

	Clinic update(Clinic clinic);

	void delete(Clinic clinic);
}