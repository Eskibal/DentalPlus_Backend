package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentalBridgePieceDao;
import com.example.DentalPlus_Backend.model.DentalBridgePiece;
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
public class DentalBridgePieceDaoImplHibernate implements DentalBridgePieceDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public DentalBridgePiece findById(Long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalBridgePiece> cq = cb.createQuery(DentalBridgePiece.class);
		Root<DentalBridgePiece> dentalBridgePiece = cq.from(DentalBridgePiece.class);

		fetchDentalBridgePieceRelations(dentalBridgePiece);

		cq.select(dentalBridgePiece)
				.distinct(true)
				.where(cb.equal(dentalBridgePiece.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<DentalBridgePiece> findByDentalBridgeId(Long dentalBridgeId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalBridgePiece> cq = cb.createQuery(DentalBridgePiece.class);
		Root<DentalBridgePiece> dentalBridgePiece = cq.from(DentalBridgePiece.class);

		fetchDentalBridgePieceRelations(dentalBridgePiece);

		cq.select(dentalBridgePiece)
				.distinct(true)
				.where(cb.equal(dentalBridgePiece.get("dentalBridge").get("id"), dentalBridgeId))
				.orderBy(cb.asc(dentalBridgePiece.get("dentalPiece").get("pieceNumber")));

		return entityManager.createQuery(cq).getResultList();
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

	private void fetchDentalBridgePieceRelations(Root<DentalBridgePiece> dentalBridgePiece) {
		Fetch<Object, Object> dentalBridge = dentalBridgePiece.fetch("dentalBridge", JoinType.LEFT);
		dentalBridge.fetch("odontogram", JoinType.LEFT);

		Fetch<Object, Object> dentalPiece = dentalBridgePiece.fetch("dentalPiece", JoinType.LEFT);
		dentalPiece.fetch("odontogram", JoinType.LEFT);
	}
}