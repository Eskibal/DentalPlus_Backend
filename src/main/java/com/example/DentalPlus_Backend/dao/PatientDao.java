package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.User;

import java.util.List;

public interface PatientDao {
	Patient findById(Long id);

	Patient findByUserId(Long userId);

	Patient findByPersonId(Long personId);

	User findUserByPersonEmail(String email);

	boolean existsByUserId(Long userId);

	boolean existsByPersonId(Long personId);

	List<Patient> findByClinicId(Long clinicId);

	List<Patient> findActiveByClinicId(Long clinicId);

	List<Patient> findByClinicIdWithSearch(Long clinicId, String search);

	void save(Patient patient);

	Patient update(Patient patient);

	void delete(Patient patient);
}