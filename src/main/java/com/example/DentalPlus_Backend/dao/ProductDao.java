package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Product;

import java.util.List;

public interface ProductDao {
	Product findById(Long id);

	Product findByName(String name);

	List<Product> findAll();

	List<Product> findActive();

	void save(Product product);

	Product update(Product product);

	void delete(Product product);
}