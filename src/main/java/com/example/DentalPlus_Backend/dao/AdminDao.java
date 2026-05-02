package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.User;

import java.util.List;

public interface AdminDao {
	Admin findById(Long id);

	Admin findByUserId(Long userId);

	Admin findByPersonId(Long personId);

	User findUserByPersonEmail(String email);

	boolean existsByUserId(Long userId);

	List<Admin> findByClinicId(Long clinicId);

	List<Admin> findActiveByClinicId(Long clinicId);

	void save(Admin admin);

	Admin update(Admin admin);

	void delete(Admin admin);
}