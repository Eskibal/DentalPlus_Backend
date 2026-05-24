package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.SpecialityTreatment;

import java.util.List;

public interface SpecialityTreatmentDao {

	SpecialityTreatment findById(Long id);

	SpecialityTreatment findBySpecialityIdAndTreatmentId(Long specialityId, Long treatmentId);

	List<SpecialityTreatment> findActiveBySpecialityId(Long specialityId);

	List<SpecialityTreatment> findActiveByTreatmentId(Long treatmentId);

	void save(SpecialityTreatment specialityTreatment);

	SpecialityTreatment update(SpecialityTreatment specialityTreatment);

	void delete(SpecialityTreatment specialityTreatment);
}