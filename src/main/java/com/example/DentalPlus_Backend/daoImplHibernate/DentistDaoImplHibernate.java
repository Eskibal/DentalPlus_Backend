package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@Profile("hibernate")
public class DentistDaoImplHibernate implements DentistDao {
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Dentist findById(Long id) {
		return entityManager.find(Dentist.class, id);
	}

	@Override
	public Dentist findByUserId(Long userId) {
		if (userId == null) {
			return null;
		}
		List<Dentist> dentists = entityManager.createQuery("""
				FROM Dentist d
				WHERE d.user.id = :userId
				""", Dentist.class).setParameter("userId", userId).setMaxResults(1).getResultList();
		return dentists.isEmpty() ? null : dentists.get(0);
	}

	@Override
	public Dentist findByPersonId(Long personId) {
		if (personId == null) {
			return null;
		}
		List<Dentist> dentists = entityManager.createQuery("""
				FROM Dentist d
				WHERE d.person.id = :personId
				""", Dentist.class).setParameter("personId", personId).setMaxResults(1).getResultList();
		return dentists.isEmpty() ? null : dentists.get(0);
	}

	@Override
	public User findUserByPersonEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		List<User> users = entityManager.createQuery("""
				SELECT d.user
				FROM Dentist d
				WHERE LOWER(d.person.email) = :email
				""", User.class).setParameter("email", email.trim().toLowerCase()).setMaxResults(1).getResultList();
		return users.isEmpty() ? null : users.get(0);
	}

	@Override
	public boolean existsByUserId(Long userId) {
		if (userId == null) {
			return false;
		}
		Long count = entityManager.createQuery("SELECT COUNT(d) FROM Dentist d WHERE d.user.id = :userId", Long.class)
				.setParameter("userId", userId).getSingleResult();
		return count != null && count > 0;
	}

	@Override
	public List<Dentist> findByClinicId(Long clinicId) {
		return entityManager.createQuery("FROM Dentist d WHERE d.clinic.id = :clinicId", Dentist.class)
				.setParameter("clinicId", clinicId).getResultList();
	}

	@Override
	public List<Dentist> findActiveByClinicId(Long clinicId) {
		return entityManager.createQuery("""
				FROM Dentist d
				WHERE d.clinic.id = :clinicId
				  AND d.active = true
				""", Dentist.class).setParameter("clinicId", clinicId).getResultList();
	}

	@Override
	public void save(Dentist dentist) {
		entityManager.persist(dentist);
	}

	@Override
	public Dentist update(Dentist dentist) {
		return entityManager.merge(dentist);
	}

	@Override
	public void delete(Dentist dentist) {
		entityManager.remove(entityManager.contains(dentist) ? dentist : entityManager.merge(dentist));
	}
}