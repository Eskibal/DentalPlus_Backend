package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.TreatmentDao;
import com.example.DentalPlus_Backend.model.Treatment;
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
public class TreatmentDaoImplHibernate implements TreatmentDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Treatment save(Treatment treatment) {
		entityManager.persist(treatment);
		return treatment;
	}

	@Override
	public Treatment update(Treatment treatment) {
		return entityManager.merge(treatment);
	}

	@Override
	public void delete(Treatment treatment) {
		entityManager.remove(entityManager.contains(treatment) ? treatment : entityManager.merge(treatment));
	}

	@Override
	public Treatment findById(Long id) {
		if (id == null) {
			return null;
		}

		return entityManager.find(Treatment.class, id);
	}

	@Override
	public Treatment findActiveById(Long id) {
		if (id == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Treatment> cq = cb.createQuery(Treatment.class);
		Root<Treatment> treatment = cq.from(Treatment.class);

		cq.select(treatment)
				.where(cb.and(cb.equal(treatment.get("id"), id), cb.isTrue(treatment.get("active"))));

		List<Treatment> result = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return result.isEmpty() ? null : result.get(0);
	}

	@Override
	public List<Treatment> findAll() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Treatment> cq = cb.createQuery(Treatment.class);
		Root<Treatment> treatment = cq.from(Treatment.class);

		cq.select(treatment).orderBy(cb.asc(treatment.get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Treatment> findActive() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Treatment> cq = cb.createQuery(Treatment.class);
		Root<Treatment> treatment = cq.from(Treatment.class);

		cq.select(treatment)
				.where(cb.isTrue(treatment.get("active")))
				.orderBy(cb.asc(treatment.get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public Treatment findByName(String name) {
		if (name == null || name.isBlank()) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Treatment> cq = cb.createQuery(Treatment.class);
		Root<Treatment> treatment = cq.from(Treatment.class);

		cq.select(treatment)
				.where(cb.equal(cb.lower(treatment.get("name")), name.trim().toLowerCase()));

		List<Treatment> result = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return result.isEmpty() ? null : result.get(0);
	}
}