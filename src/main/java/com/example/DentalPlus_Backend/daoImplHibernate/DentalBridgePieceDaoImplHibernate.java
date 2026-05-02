package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentalBridgePieceDao;
import com.example.DentalPlus_Backend.model.DentalBridgePiece;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("hibernate")
public class DentalBridgePieceDaoImplHibernate implements DentalBridgePieceDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public DentalBridgePiece findById(Long id) {
		return entityManager.find(DentalBridgePiece.class, id);
	}

	@Override
	public List<DentalBridgePiece> findByDentalBridgeId(Long dentalBridgeId) {
		return entityManager.createQuery("""
				FROM DentalBridgePiece dbp
				WHERE dbp.dentalBridge.id = :dentalBridgeId
				ORDER BY dbp.dentalPiece.pieceNumber ASC
				""", DentalBridgePiece.class).setParameter("dentalBridgeId", dentalBridgeId).getResultList();
	}

	@Override
	public void save(DentalBridgePiece dentalBridgePiece) {
		entityManager.persist(dentalBridgePiece);
	}

	@Override
	public DentalBridgePiece update(DentalBridgePiece dentalBridgePiece) {
		return entityManager.merge(dentalBridgePiece);
	}

	@Override
	public void delete(DentalBridgePiece dentalBridgePiece) {
		entityManager.remove(
				entityManager.contains(dentalBridgePiece) ? dentalBridgePiece : entityManager.merge(dentalBridgePiece));
	}

	@Override
	public void deleteByDentalBridgeId(Long dentalBridgeId) {
		List<DentalBridgePiece> bridgePieces = findByDentalBridgeId(dentalBridgeId);

		for (DentalBridgePiece bridgePiece : bridgePieces) {
			delete(bridgePiece);
		}
	}
}