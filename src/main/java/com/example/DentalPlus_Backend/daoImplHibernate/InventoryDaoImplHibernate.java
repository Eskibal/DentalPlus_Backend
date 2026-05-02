package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.InventoryDao;
import com.example.DentalPlus_Backend.model.Inventory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
		return entityManager.find(Inventory.class, id);
	}

	@Override
	public Inventory findByBoxIdAndProductId(Long boxId, Long productId) {
		if (boxId == null || productId == null) {
			return null;
		}

		return entityManager.createQuery("""
				FROM Inventory i
				WHERE i.box.id = :boxId
				  AND i.product.id = :productId
				""", Inventory.class).setParameter("boxId", boxId).setParameter("productId", productId)
				.getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<Inventory> findByBoxId(Long boxId) {
		return entityManager.createQuery("""
				FROM Inventory i
				WHERE i.box.id = :boxId
				ORDER BY i.product.name ASC
				""", Inventory.class).setParameter("boxId", boxId).getResultList();
	}

	@Override
	public List<Inventory> findActiveByBoxId(Long boxId) {
		return entityManager.createQuery("""
				FROM Inventory i
				WHERE i.box.id = :boxId
				  AND i.active = true
				  AND i.product.active = true
				ORDER BY i.product.name ASC
				""", Inventory.class).setParameter("boxId", boxId).getResultList();
	}

	@Override
	public List<Inventory> findByClinicId(Long clinicId) {
		return entityManager.createQuery("""
				FROM Inventory i
				WHERE i.box.clinic.id = :clinicId
				ORDER BY i.box.name ASC, i.product.name ASC
				""", Inventory.class).setParameter("clinicId", clinicId).getResultList();
	}

	@Override
	public List<Inventory> findActiveByClinicId(Long clinicId) {
		return entityManager.createQuery("""
				FROM Inventory i
				WHERE i.box.clinic.id = :clinicId
				  AND i.active = true
				  AND i.product.active = true
				  AND i.box.active = true
				ORDER BY i.box.name ASC, i.product.name ASC
				""", Inventory.class).setParameter("clinicId", clinicId).getResultList();
	}

	@Override
	public List<Inventory> findLowStockByBoxId(Long boxId) {
		return entityManager.createQuery("""
				FROM Inventory i
				WHERE i.box.id = :boxId
				  AND i.active = true
				  AND i.product.active = true
				  AND i.quantity <= i.minimumQuantity
				ORDER BY i.product.name ASC
				""", Inventory.class).setParameter("boxId", boxId).getResultList();
	}

	@Override
	public List<Inventory> findLowStockByClinicId(Long clinicId) {
		return entityManager.createQuery("""
				FROM Inventory i
				WHERE i.box.clinic.id = :clinicId
				  AND i.active = true
				  AND i.product.active = true
				  AND i.box.active = true
				  AND i.quantity <= i.minimumQuantity
				ORDER BY i.box.name ASC, i.product.name ASC
				""", Inventory.class).setParameter("clinicId", clinicId).getResultList();
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
}