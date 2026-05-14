package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.BoxDao;
import com.example.DentalPlus_Backend.model.Box;
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
public class BoxDaoImplHibernate implements BoxDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Box findById(Long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Box> cq = cb.createQuery(Box.class);
		Root<Box> box = cq.from(Box.class);

		box.fetch("clinic", JoinType.LEFT);

		cq.select(box)
				.distinct(true)
				.where(cb.equal(box.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<Box> findByClinicId(Long clinicId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Box> cq = cb.createQuery(Box.class);
		Root<Box> box = cq.from(Box.class);

		box.fetch("clinic", JoinType.LEFT);

		cq.select(box)
				.distinct(true)
				.where(cb.equal(box.get("clinic").get("id"), clinicId));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Box> findActiveByClinicId(Long clinicId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Box> cq = cb.createQuery(Box.class);
		Root<Box> box = cq.from(Box.class);

		box.fetch("clinic", JoinType.LEFT);

		cq.select(box)
				.distinct(true)
				.where(cb.and(
						cb.equal(box.get("clinic").get("id"), clinicId),
						cb.isTrue(box.get("active"))))
				.orderBy(cb.asc(box.get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public void save(Box box) {
		entityManager.persist(box);
	}

	@Override
	public Box update(Box box) {
		return entityManager.merge(box);
	}

	@Override
	public void delete(Box box) {
		entityManager.remove(entityManager.contains(box) ? box : entityManager.merge(box));
	}
}