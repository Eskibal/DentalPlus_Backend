package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
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
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Admin> cq = cb.createQuery(Admin.class);
		Root<Admin> admin = cq.from(Admin.class);

		fetchAdminRelations(admin);

		cq.select(admin).distinct(true).where(cb.equal(admin.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public Admin findByUserId(Long userId) {
		if (userId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Admin> cq = cb.createQuery(Admin.class);
		Root<Admin> admin = cq.from(Admin.class);

		fetchAdminRelations(admin);

		cq.select(admin).distinct(true).where(cb.equal(admin.get("user").get("id"), userId));

		List<Admin> admins = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return admins.isEmpty() ? null : admins.get(0);
	}

	@Override
	public Admin findByPersonId(Long personId) {
		if (personId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Admin> cq = cb.createQuery(Admin.class);
		Root<Admin> admin = cq.from(Admin.class);

		fetchAdminRelations(admin);

		cq.select(admin).distinct(true).where(cb.equal(admin.get("person").get("id"), personId));

		List<Admin> admins = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return admins.isEmpty() ? null : admins.get(0);
	}

	@Override
	public User findUserByPersonEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<Admin> admin = cq.from(Admin.class);

		cq.select(admin.get("user"))
				.where(cb.equal(cb.lower(admin.get("person").get("email")), email.trim().toLowerCase()));

		List<User> users = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return users.isEmpty() ? null : users.get(0);
	}

	@Override
	public boolean existsByUserId(Long userId) {
		if (userId == null) {
			return false;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Admin> admin = cq.from(Admin.class);

		cq.select(cb.count(admin)).where(cb.equal(admin.get("user").get("id"), userId));

		Long count = entityManager.createQuery(cq).getSingleResult();

		return count != null && count > 0;
	}

	@Override
	public List<Admin> findByClinicId(Long clinicId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Admin> cq = cb.createQuery(Admin.class);
		Root<Admin> admin = cq.from(Admin.class);

		fetchAdminRelations(admin);

		cq.select(admin)
				.distinct(true)
				.where(cb.equal(admin.get("clinic").get("id"), clinicId))
				.orderBy(cb.asc(admin.get("person").get("name")),
						cb.asc(admin.get("person").get("firstSurname")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Admin> findActiveByClinicId(Long clinicId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Admin> cq = cb.createQuery(Admin.class);
		Root<Admin> admin = cq.from(Admin.class);

		fetchAdminRelations(admin);

		cq.select(admin)
				.distinct(true)
				.where(cb.and(cb.equal(admin.get("clinic").get("id"), clinicId),
						cb.isTrue(admin.get("active"))))
				.orderBy(cb.asc(admin.get("person").get("name")),
						cb.asc(admin.get("person").get("firstSurname")));

		return entityManager.createQuery(cq).getResultList();
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

	private void fetchAdminRelations(Root<Admin> admin) {
		admin.fetch("person", JoinType.LEFT);
		admin.fetch("user", JoinType.LEFT);
		admin.fetch("clinic", JoinType.LEFT);
	}
}