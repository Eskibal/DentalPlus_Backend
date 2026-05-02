package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Document;

import java.util.List;

public interface DocumentDao {
	Document findById(Long id);

	List<Document> findByPatientId(Long patientId);

	List<Document> findActiveByPatientId(Long patientId);

	void save(Document document);

	Document update(Document document);

	void delete(Document document);
}