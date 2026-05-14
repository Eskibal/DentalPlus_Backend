package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.model.Receptionist;
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
public class ReceptionistDaoImplHibernate implements ReceptionistDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Receptionist findById(Long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Receptionist> cq = cb.createQuery(Receptionist.class);
		Root<Receptionist> receptionist = cq.from(Receptionist.class);

		fetchReceptionistRelations(receptionist);

		cq.select(receptionist)
				.distinct(true)
				.where(cb.equal(receptionist.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public Receptionist findByUserId(Long userId) {
		if (userId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Receptionist> cq = cb.createQuery(Receptionist.class);
		Root<Receptionist> receptionist = cq.from(Receptionist.class);

		fetchReceptionistRelations(receptionist);

		cq.select(receptionist)
				.distinct(true)
				.where(cb.equal(receptionist.get("user").get("id"), userId));

		List<Receptionist> receptionists = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return receptionists.isEmpty() ? null : receptionists.get(0);
	}

	@Override
	public Receptionist findByPersonId(Long personId) {
		if (personId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Receptionist> cq = cb.createQuery(Receptionist.class);
		Root<Receptionist> receptionist = cq.from(Receptionist.class);

		fetchReceptionistRelations(receptionist);

		cq.select(receptionist)
				.distinct(true)
				.where(cb.equal(receptionist.get("person").get("id"), personId));

		List<Receptionist> receptionists = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return receptionists.isEmpty() ? null : receptionists.get(0);
	}

	@Override
	public User findUserByPersonEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<Receptionist> receptionist = cq.from(Receptionist.class);

		cq.select(receptionist.get("user"))
				.where(cb.equal(cb.lower(receptionist.get("person").get("email")), email.trim().toLowerCase()));

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
		Root<Receptionist> receptionist = cq.from(Receptionist.class);

		cq.select(cb.count(receptionist))
				.where(cb.equal(receptionist.get("user").get("id"), userId));

		Long count = entityManager.createQuery(cq).getSingleResult();

		return count != null && count > 0;
	}

	@Override
	public List<Receptionist> findByClinicId(Long clinicId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Receptionist> cq = cb.createQuery(Receptionist.class);
		Root<Receptionist> receptionist = cq.from(Receptionist.class);

		fetchReceptionistRelations(receptionist);

		cq.select(receptionist)
				.distinct(true)
				.where(cb.equal(receptionist.get("clinic").get("id"), clinicId))
				.orderBy(
						cb.asc(receptionist.get("person").get("name")),
						cb.asc(receptionist.get("person").get("firstSurname")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Receptionist> findActiveByClinicId(Long clinicId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Receptionist> cq = cb.createQuery(Receptionist.class);
		Root<Receptionist> receptionist = cq.from(Receptionist.class);

		fetchReceptionistRelations(receptionist);

		cq.select(receptionist)
				.distinct(true)
				.where(cb.and(
						cb.equal(receptionist.get("clinic").get("id"), clinicId),
						cb.isTrue(receptionist.get("active"))))
				.orderBy(
						cb.asc(receptionist.get("person").get("name")),
						cb.asc(receptionist.get("person").get("firstSurname")));

		return entityManager.createQuery(cq).getResultList();
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

	private void fetchReceptionistRelations(Root<Receptionist> receptionist) {
		receptionist.fetch("person", JoinType.LEFT);
		receptionist.fetch("user", JoinType.LEFT);
		receptionist.fetch("clinic", JoinType.LEFT);
		receptionist.fetch("calendarRule", JoinType.LEFT);
	}
}