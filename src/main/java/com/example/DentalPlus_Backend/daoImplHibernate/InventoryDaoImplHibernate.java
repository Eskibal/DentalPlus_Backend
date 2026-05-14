package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.InventoryDao;
import com.example.DentalPlus_Backend.model.Inventory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("hibernate")
public class InventoryDaoImplHibernate implements InventoryDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Inventory findById(Long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Inventory> cq = cb.createQuery(Inventory.class);
		Root<Inventory> inventory = cq.from(Inventory.class);

		fetchInventoryRelations(inventory);

		cq.select(inventory)
				.distinct(true)
				.where(cb.equal(inventory.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public Inventory findByBoxIdAndProductId(Long boxId, Long productId) {
		if (boxId == null || productId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Inventory> cq = cb.createQuery(Inventory.class);
		Root<Inventory> inventory = cq.from(Inventory.class);

		fetchInventoryRelations(inventory);

		cq.select(inventory)
				.distinct(true)
				.where(cb.and(
						cb.equal(inventory.get("box").get("id"), boxId),
						cb.equal(inventory.get("product").get("id"), productId)));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<Inventory> findByBoxId(Long boxId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Inventory> cq = cb.createQuery(Inventory.class);
		Root<Inventory> inventory = cq.from(Inventory.class);

		fetchInventoryRelations(inventory);

		cq.select(inventory)
				.distinct(true)
				.where(cb.equal(inventory.get("box").get("id"), boxId))
				.orderBy(cb.asc(inventory.get("product").get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Inventory> findActiveByBoxId(Long boxId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Inventory> cq = cb.createQuery(Inventory.class);
		Root<Inventory> inventory = cq.from(Inventory.class);

		fetchInventoryRelations(inventory);

		cq.select(inventory)
				.distinct(true)
				.where(cb.and(
						cb.equal(inventory.get("box").get("id"), boxId),
						cb.isTrue(inventory.get("active")),
						cb.isTrue(inventory.get("product").get("active"))))
				.orderBy(cb.asc(inventory.get("product").get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Inventory> findByClinicId(Long clinicId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Inventory> cq = cb.createQuery(Inventory.class);
		Root<Inventory> inventory = cq.from(Inventory.class);

		fetchInventoryRelations(inventory);

		cq.select(inventory)
				.distinct(true)
				.where(cb.equal(inventory.get("box").get("clinic").get("id"), clinicId))
				.orderBy(
						cb.asc(inventory.get("box").get("name")),
						cb.asc(inventory.get("product").get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Inventory> findActiveByClinicId(Long clinicId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Inventory> cq = cb.createQuery(Inventory.class);
		Root<Inventory> inventory = cq.from(Inventory.class);

		fetchInventoryRelations(inventory);

		cq.select(inventory)
				.distinct(true)
				.where(cb.and(
						cb.equal(inventory.get("box").get("clinic").get("id"), clinicId),
						cb.isTrue(inventory.get("active")),
						cb.isTrue(inventory.get("product").get("active")),
						cb.isTrue(inventory.get("box").get("active"))))
				.orderBy(
						cb.asc(inventory.get("box").get("name")),
						cb.asc(inventory.get("product").get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Inventory> findLowStockByBoxId(Long boxId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Inventory> cq = cb.createQuery(Inventory.class);
		Root<Inventory> inventory = cq.from(Inventory.class);

		fetchInventoryRelations(inventory);

		cq.select(inventory)
				.distinct(true)
				.where(cb.and(
						cb.equal(inventory.get("box").get("id"), boxId),
						cb.isTrue(inventory.get("active")),
						cb.isTrue(inventory.get("product").get("active")),
						cb.lessThanOrEqualTo(inventory.get("quantity"), inventory.get("minimumQuantity"))))
				.orderBy(cb.asc(inventory.get("product").get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Inventory> findLowStockByClinicId(Long clinicId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Inventory> cq = cb.createQuery(Inventory.class);
		Root<Inventory> inventory = cq.from(Inventory.class);

		fetchInventoryRelations(inventory);

		cq.select(inventory)
				.distinct(true)
				.where(cb.and(
						cb.equal(inventory.get("box").get("clinic").get("id"), clinicId),
						cb.isTrue(inventory.get("active")),
						cb.isTrue(inventory.get("product").get("active")),
						cb.isTrue(inventory.get("box").get("active")),
						cb.lessThanOrEqualTo(inventory.get("quantity"), inventory.get("minimumQuantity"))))
				.orderBy(
						cb.asc(inventory.get("box").get("name")),
						cb.asc(inventory.get("product").get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public void save(Inventory inventory) {
		entityManager.persist(inventory);
	}

	@Override
	public Inventory update(Inventory inventory) {
		return entityManager.merge(inventory);
	}

	@Override
	public void delete(Inventory inventory) {
		entityManager.remove(entityManager.contains(inventory) ? inventory : entityManager.merge(inventory));
	}

	private void fetchInventoryRelations(Root<Inventory> inventory) {
		Fetch<Object, Object> box = inventory.fetch("box", JoinType.LEFT);
		box.fetch("clinic", JoinType.LEFT);

		inventory.fetch("product", JoinType.LEFT);
	}
}