package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.SpecialityTreatment;
import com.example.DentalPlus_Backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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
		if (id == null) {
			return null;
		}

		return entityManager.find(Dentist.class, id);
	}

	@Override
	public Dentist findByUserId(Long userId) {
		if (userId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Dentist> cq = cb.createQuery(Dentist.class);
		Root<Dentist> dentist = cq.from(Dentist.class);

		cq.select(dentist).where(cb.equal(dentist.get("user").get("id"), userId));

		List<Dentist> dentists = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return dentists.isEmpty() ? null : dentists.get(0);
	}

	@Override
	public Dentist findByPersonId(Long personId) {
		if (personId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Dentist> cq = cb.createQuery(Dentist.class);
		Root<Dentist> dentist = cq.from(Dentist.class);

		cq.select(dentist).where(cb.equal(dentist.get("person").get("id"), personId));

		List<Dentist> dentists = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return dentists.isEmpty() ? null : dentists.get(0);
	}

	@Override
	public User findUserByPersonEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<Dentist> dentist = cq.from(Dentist.class);

		cq.select(dentist.get("user"))
				.where(cb.equal(cb.lower(dentist.get("person").get("email")), email.trim().toLowerCase()));

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
		Root<Dentist> dentist = cq.from(Dentist.class);

		cq.select(cb.count(dentist)).where(cb.equal(dentist.get("user").get("id"), userId));

		Long count = entityManager.createQuery(cq).getSingleResult();

		return count != null && count > 0;
	}

	@Override
	public List<Dentist> findByClinicId(Long clinicId) {
		if (clinicId == null) {
			return List.of();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Dentist> cq = cb.createQuery(Dentist.class);
		Root<Dentist> dentist = cq.from(Dentist.class);

		cq.select(dentist).where(cb.equal(dentist.get("clinic").get("id"), clinicId));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Dentist> findActiveByClinicId(Long clinicId) {
		if (clinicId == null) {
			return List.of();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Dentist> cq = cb.createQuery(Dentist.class);
		Root<Dentist> dentist = cq.from(Dentist.class);

		cq.select(dentist)
				.where(cb.and(
						cb.equal(dentist.get("clinic").get("id"), clinicId),
						cb.isTrue(dentist.get("active"))
				))
				.orderBy(cb.asc(dentist.get("id")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Dentist> findActiveByClinicIdAndTreatmentId(Long clinicId, Long treatmentId) {
		if (clinicId == null || treatmentId == null) {
			return List.of();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Dentist> cq = cb.createQuery(Dentist.class);

		Root<Dentist> dentist = cq.from(Dentist.class);
		Root<SpecialityTreatment> specialityTreatment = cq.from(SpecialityTreatment.class);

		cq.select(dentist)
				.distinct(true)
				.where(cb.and(
						cb.equal(dentist.get("clinic").get("id"), clinicId),
						cb.isTrue(dentist.get("active")),
						cb.isNotNull(dentist.get("speciality")),
						cb.equal(specialityTreatment.get("speciality"), dentist.get("speciality")),
						cb.equal(specialityTreatment.get("treatment").get("id"), treatmentId),
						cb.isTrue(specialityTreatment.get("active")),
						cb.isTrue(specialityTreatment.get("speciality").get("active")),
						cb.isTrue(specialityTreatment.get("treatment").get("active"))
				))
				.orderBy(cb.asc(dentist.get("id")));

		return entityManager.createQuery(cq).getResultList();
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