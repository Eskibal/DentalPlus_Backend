package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentalBridgeDao;
import com.example.DentalPlus_Backend.model.DentalBridge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
		return entityManager.find(DentalBridge.class, id);
	}

	@Override
	public List<DentalBridge> findByOdontogramId(Long odontogramId) {
		return entityManager.createQuery("""
				FROM DentalBridge db
				WHERE db.odontogram.id = :odontogramId
				ORDER BY db.createdAt DESC
				""", DentalBridge.class).setParameter("odontogramId", odontogramId).getResultList();
	}

	@Override
	public List<DentalBridge> findActiveByOdontogramId(Long odontogramId) {
		return entityManager.createQuery("""
				FROM DentalBridge db
				WHERE db.odontogram.id = :odontogramId
				  AND db.active = true
				ORDER BY db.createdAt DESC
				""", DentalBridge.class).setParameter("odontogramId", odontogramId).getResultList();
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