package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@Profile("hibernate")
public class AdminDaoImplHibernate implements AdminDao {
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Admin findById(Long id) {
		return entityManager.find(Admin.class, id);
	}

	@Override
	public Admin findByUserId(Long userId) {
		if (userId == null) {
			return null;
		}
		List<Admin> admins = entityManager.createQuery("""
				FROM Admin a
				WHERE a.user.id = :userId
				""", Admin.class).setParameter("userId", userId).setMaxResults(1).getResultList();
		return admins.isEmpty() ? null : admins.get(0);
	}

	@Override
	public Admin findByPersonId(Long personId) {
		if (personId == null) {
			return null;
		}
		List<Admin> admins = entityManager.createQuery("""
				FROM Admin a
				WHERE a.person.id = :personId
				""", Admin.class).setParameter("personId", personId).setMaxResults(1).getResultList();
		return admins.isEmpty() ? null : admins.get(0);
	}

	@Override
	public User findUserByPersonEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		List<User> users = entityManager.createQuery("""
				SELECT a.user
				FROM Admin a
				WHERE LOWER(a.person.email) = :email
				""", User.class).setParameter("email", email.trim().toLowerCase()).setMaxResults(1).getResultList();
		return users.isEmpty() ? null : users.get(0);
	}

	@Override
	public boolean existsByUserId(Long userId) {
		if (userId == null) {
			return false;
		}
		Long count = entityManager.createQuery("SELECT COUNT(a) FROM Admin a WHERE a.user.id = :userId", Long.class)
				.setParameter("userId", userId).getSingleResult();
		return count != null && count > 0;
	}

	@Override
	public List<Admin> findByClinicId(Long clinicId) {
		return entityManager.createQuery("""
				FROM Admin a
				WHERE a.clinic.id = :clinicId
				ORDER BY a.person.name ASC, a.person.firstSurname ASC
				""", Admin.class).setParameter("clinicId", clinicId).getResultList();
	}

	@Override
	public List<Admin> findActiveByClinicId(Long clinicId) {
		return entityManager.createQuery("""
				FROM Admin a
				WHERE a.clinic.id = :clinicId
				  AND a.active = true
				ORDER BY a.person.name ASC, a.person.firstSurname ASC
				""", Admin.class).setParameter("clinicId", clinicId).getResultList();
	}

	@Override
	public void save(Admin admin) {
		entityManager.persist(admin);
	}

	@Override
	public Admin update(Admin admin) {
		return entityManager.merge(admin);
	}

	@Override
	public void delete(Admin admin) {
		entityManager.remove(entityManager.contains(admin) ? admin : entityManager.merge(admin));
	}
}