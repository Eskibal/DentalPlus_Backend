package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.PatientDao;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("hibernate")
public class PatientDaoImplHibernate implements PatientDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Patient findById(Long id) {
		return entityManager.find(Patient.class, id);
	}

	@Override
	public Patient findByUserId(Long userId) {
		if (userId == null) {
			return null;
		}
		List<Patient> patients = entityManager.createQuery("""
				FROM Patient p
				WHERE p.user.id = :userId
				""", Patient.class).setParameter("userId", userId).setMaxResults(1).getResultList();
		return patients.isEmpty() ? null : patients.get(0);
	}

	@Override
	public Patient findByPersonId(Long personId) {
		if (personId == null) {
			return null;
		}

		return entityManager.createQuery("FROM Patient p WHERE p.person.id = :personId", Patient.class)
				.setParameter("personId", personId).getResultStream().findFirst().orElse(null);
	}

	@Override
	public User findUserByPersonEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}

		return entityManager.createQuery("""
				SELECT p.user
				FROM Patient p
				WHERE p.user IS NOT NULL
				  AND LOWER(p.person.email) = :email
				""", User.class).setParameter("email", email.trim().toLowerCase()).getResultStream().findFirst()
				.orElse(null);
	}

	@Override
	public boolean existsByUserId(Long userId) {
		if (userId == null) {
			return false;
		}

		Long count = entityManager.createQuery("SELECT COUNT(p) FROM Patient p WHERE p.user.id = :userId", Long.class)
				.setParameter("userId", userId).getSingleResult();

		return count != null && count > 0;
	}

	@Override
	public boolean existsByPersonId(Long personId) {
		if (personId == null) {
			return false;
		}

		Long count = entityManager
				.createQuery("SELECT COUNT(p) FROM Patient p WHERE p.person.id = :personId", Long.class)
				.setParameter("personId", personId).getSingleResult();

		return count != null && count > 0;
	}

	@Override
	public List<Patient> findByClinicId(Long clinicId) {
		return entityManager.createQuery("FROM Patient p WHERE p.clinic.id = :clinicId", Patient.class)
				.setParameter("clinicId", clinicId).getResultList();
	}

	@Override
	public List<Patient> findActiveByClinicId(Long clinicId) {
		return entityManager.createQuery("""
				FROM Patient p
				WHERE p.clinic.id = :clinicId
				  AND p.active = true
				""", Patient.class).setParameter("clinicId", clinicId).getResultList();
	}

	@Override
	public List<Patient> findByClinicIdWithSearch(Long clinicId, String search) {
		if (search == null || search.isBlank()) {
			return findByClinicId(clinicId);
		}

		String normalizedSearch = "%" + search.trim().toLowerCase() + "%";

		return entityManager.createQuery("""
				FROM Patient p
				WHERE p.clinic.id = :clinicId
				  AND (
				      LOWER(p.person.name) LIKE :search
				      OR LOWER(p.person.firstSurname) LIKE :search
				      OR LOWER(p.person.secondSurname) LIKE :search
				      OR LOWER(p.person.email) LIKE :search
				      OR LOWER(p.person.phoneNumber) LIKE :search
				  )
				""", Patient.class).setParameter("clinicId", clinicId).setParameter("search", normalizedSearch)
				.getResultList();
	}

	@Override
	public void save(Patient patient) {
		entityManager.persist(patient);
	}

	@Override
	public Patient update(Patient patient) {
		return entityManager.merge(patient);
	}

	@Override
	public void delete(Patient patient) {
		entityManager.remove(entityManager.contains(patient) ? patient : entityManager.merge(patient));
	}
}