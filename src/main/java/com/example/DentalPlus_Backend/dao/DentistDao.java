package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.User;

import java.util.List;

public interface DentistDao {
	Dentist findById(Long id);

	Dentist findByUserId(Long userId);

	Dentist findByPersonId(Long personId);

	User findUserByPersonEmail(String email);

	boolean existsByUserId(Long userId);

	List<Dentist> findByClinicId(Long clinicId);

	List<Dentist> findActiveByClinicId(Long clinicId);

	void save(Dentist dentist);

	Dentist update(Dentist dentist);

	void delete(Dentist dentist);
}