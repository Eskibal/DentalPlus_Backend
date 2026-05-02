package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.model.Receptionist;
import com.example.DentalPlus_Backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@Profile("hibernate")
public class ReceptionistDaoImplHibernate implements ReceptionistDao {
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Receptionist findById(Long id) {
		return entityManager.find(Receptionist.class, id);
	}

	@Override
	public Receptionist findByUserId(Long userId) {
		if (userId == null) {
			return null;
		}
		List<Receptionist> receptionists = entityManager
				.createQuery("FROM Receptionist r WHERE r.user.id = :userId", Receptionist.class)
				.setParameter("userId", userId).setMaxResults(1).getResultList();
		return receptionists.isEmpty() ? null : receptionists.get(0);
	}

	@Override
	public Receptionist findByPersonId(Long personId) {
		if (personId == null) {
			return null;
		}
		List<Receptionist> receptionists = entityManager
				.createQuery("FROM Receptionist r WHERE r.person.id = :personId", Receptionist.class)
				.setParameter("personId", personId).setMaxResults(1).getResultList();
		return receptionists.isEmpty() ? null : receptionists.get(0);
	}

	@Override
	public User findUserByPersonEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		List<User> users = entityManager.createQuery("""
				SELECT r.user
				FROM Receptionist r
				WHERE LOWER(r.person.email) = :email
				""", User.class).setParameter("email", email.trim().toLowerCase()).setMaxResults(1).getResultList();
		return users.isEmpty() ? null : users.get(0);
	}

	@Override
	public boolean existsByUserId(Long userId) {
		if (userId == null) {
			return false;
		}
		Long count = entityManager
				.createQuery("SELECT COUNT(r) FROM Receptionist r WHERE r.user.id = :userId", Long.class)
				.setParameter("userId", userId).getSingleResult();
		return count != null && count > 0;
	}

	@Override
	public List<Receptionist> findByClinicId(Long clinicId) {
		return entityManager.createQuery("""
				FROM Receptionist r
				WHERE r.clinic.id = :clinicId
				ORDER BY r.person.name ASC, r.person.firstSurname ASC
				""", Receptionist.class).setParameter("clinicId", clinicId).getResultList();
	}

	@Override
	public List<Receptionist> findActiveByClinicId(Long clinicId) {
		return entityManager.createQuery("""
				FROM Receptionist r
				WHERE r.clinic.id = :clinicId
				  AND r.active = true
				ORDER BY r.person.name ASC, r.person.firstSurname ASC
				""", Receptionist.class).setParameter("clinicId", clinicId).getResultList();
	}

	@Override
	public void save(Receptionist receptionist) {
		entityManager.persist(receptionist);
	}

	@Override
	public Receptionist update(Receptionist receptionist) {
		return entityManager.merge(receptionist);
	}

	@Override
	public void delete(Receptionist receptionist) {
		entityManager.remove(entityManager.contains(receptionist) ? receptionist : entityManager.merge(receptionist));
	}
}