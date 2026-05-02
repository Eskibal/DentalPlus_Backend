package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Receptionist;
import com.example.DentalPlus_Backend.model.User;

import java.util.List;

public interface ReceptionistDao {
	Receptionist findById(Long id);

	Receptionist findByUserId(Long userId);

	Receptionist findByPersonId(Long personId);

	User findUserByPersonEmail(String email);

	boolean existsByUserId(Long userId);

	List<Receptionist> findByClinicId(Long clinicId);

	List<Receptionist> findActiveByClinicId(Long clinicId);

	void save(Receptionist receptionist);

	Receptionist update(Receptionist receptionist);

	void delete(Receptionist receptionist);
}