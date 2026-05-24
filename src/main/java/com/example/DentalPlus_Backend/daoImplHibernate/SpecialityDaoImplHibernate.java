package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.SpecialityDao;
import com.example.DentalPlus_Backend.model.Speciality;
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
public class SpecialityDaoImplHibernate implements SpecialityDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Speciality findById(Long id) {
		if (id == null) {
			return null;
		}

		return entityManager.find(Speciality.class, id);
	}

	@Override
	public Speciality findActiveById(Long id) {
		if (id == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Speciality> cq = cb.createQuery(Speciality.class);
		Root<Speciality> speciality = cq.from(Speciality.class);

		cq.select(speciality)
				.where(cb.and(cb.equal(speciality.get("id"), id), cb.isTrue(speciality.get("active"))));

		List<Speciality> result = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return result.isEmpty() ? null : result.get(0);
	}

	@Override
	public Speciality findByName(String name) {
		if (name == null || name.isBlank()) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Speciality> cq = cb.createQuery(Speciality.class);
		Root<Speciality> speciality = cq.from(Speciality.class);

		cq.select(speciality)
				.where(cb.equal(cb.lower(speciality.get("name")), name.trim().toLowerCase()));

		List<Speciality> result = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return result.isEmpty() ? null : result.get(0);
	}

	@Override
	public List<Speciality> findAll() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Speciality> cq = cb.createQuery(Speciality.class);
		Root<Speciality> speciality = cq.from(Speciality.class);

		cq.select(speciality).orderBy(cb.asc(speciality.get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Speciality> findActive() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Speciality> cq = cb.createQuery(Speciality.class);
		Root<Speciality> speciality = cq.from(Speciality.class);

		cq.select(speciality)
				.where(cb.isTrue(speciality.get("active")))
				.orderBy(cb.asc(speciality.get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public void save(Speciality speciality) {
		entityManager.persist(speciality);
	}

	@Override
	public Speciality update(Speciality speciality) {
		return entityManager.merge(speciality);
	}

	@Override
	public void delete(Speciality speciality) {
		entityManager.remove(entityManager.contains(speciality) ? speciality : entityManager.merge(speciality));
	}
}