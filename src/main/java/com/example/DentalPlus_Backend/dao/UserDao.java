package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.User;

import java.util.List;

public interface UserDao {
	User findById(Long id);

	User findByUsername(String username);

	List<User> findAll();

	void save(User user);

	User update(User user);

	void delete(User user);
}