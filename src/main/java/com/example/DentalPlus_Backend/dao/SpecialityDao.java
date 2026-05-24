package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Speciality;

import java.util.List;

public interface SpecialityDao {

	Speciality findById(Long id);

	Speciality findActiveById(Long id);

	Speciality findByName(String name);

	List<Speciality> findAll();

	List<Speciality> findActive();

	void save(Speciality speciality);

	Speciality update(Speciality speciality);

	void delete(Speciality speciality);
}