package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentalBridgeDao;
import com.example.DentalPlus_Backend.model.DentalBridge;
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
public class DentalBridgeDaoImplHibernate implements DentalBridgeDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public DentalBridge findById(Long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalBridge> cq = cb.createQuery(DentalBridge.class);
		Root<DentalBridge> dentalBridge = cq.from(DentalBridge.class);

		dentalBridge.fetch("odontogram", JoinType.LEFT);

		cq.select(dentalBridge)
				.distinct(true)
				.where(cb.equal(dentalBridge.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<DentalBridge> findByOdontogramId(Long odontogramId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalBridge> cq = cb.createQuery(DentalBridge.class);
		Root<DentalBridge> dentalBridge = cq.from(DentalBridge.class);

		dentalBridge.fetch("odontogram", JoinType.LEFT);

		cq.select(dentalBridge)
				.distinct(true)
				.where(cb.equal(dentalBridge.get("odontogram").get("id"), odontogramId))
				.orderBy(cb.desc(dentalBridge.get("createdAt")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<DentalBridge> findActiveByOdontogramId(Long odontogramId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalBridge> cq = cb.createQuery(DentalBridge.class);
		Root<DentalBridge> dentalBridge = cq.from(DentalBridge.class);

		dentalBridge.fetch("odontogram", JoinType.LEFT);

		cq.select(dentalBridge)
				.distinct(true)
				.where(cb.and(
						cb.equal(dentalBridge.get("odontogram").get("id"), odontogramId),
						cb.isTrue(dentalBridge.get("active"))))
				.orderBy(cb.desc(dentalBridge.get("createdAt")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public void save(DentalBridge dentalBridge) {
		entityManager.persist(dentalBridge);
	}

	@Override
	public DentalBridge update(DentalBridge dentalBridge) {
		return entityManager.merge(dentalBridge);
	}

	@Override
	public void delete(DentalBridge dentalBridge) {
		entityManager.remove(entityManager.contains(dentalBridge) ? dentalBridge : entityManager.merge(dentalBridge));
	}
}