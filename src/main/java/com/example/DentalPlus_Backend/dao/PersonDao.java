package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Person;

import java.util.List;

public interface PersonDao {
	Person findById(Long id);

	Person findByEmail(String email);

	List<Person> findAll();

	void save(Person person);

	Person update(Person person);

	void delete(Person person);
}