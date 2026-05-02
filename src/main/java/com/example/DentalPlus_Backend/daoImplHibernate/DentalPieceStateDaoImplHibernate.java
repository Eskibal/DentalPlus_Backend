package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentalPieceStateDao;
import com.example.DentalPlus_Backend.model.DentalPieceState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("hibernate")
public class DentalPieceStateDaoImplHibernate implements DentalPieceStateDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public DentalPieceState findById(Long id) {
		return entityManager.find(DentalPieceState.class, id);
	}

	@Override
	public DentalPieceState findActiveByDentalPieceId(Long dentalPieceId) {
		if (dentalPieceId == null) {
			return null;
		}

		List<DentalPieceState> states = entityManager.createQuery("""
				SELECT dps
				FROM DentalPieceState dps
				WHERE dps.dentalPiece.id = :dentalPieceId
				  AND dps.active = true
				ORDER BY dps.createdAt DESC
				""", DentalPieceState.class).setParameter("dentalPieceId", dentalPieceId).setMaxResults(1)
				.getResultList();

		return states.isEmpty() ? null : states.get(0);
	}

	@Override
	public List<DentalPieceState> findByDentalPieceId(Long dentalPieceId) {
		return entityManager.createQuery("""
				FROM DentalPieceState dps
				WHERE dps.dentalPiece.id = :dentalPieceId
				ORDER BY dps.createdAt DESC
				""", DentalPieceState.class).setParameter("dentalPieceId", dentalPieceId).getResultList();
	}

	@Override
	public List<DentalPieceState> findActiveByOdontogramId(Long odontogramId) {
		return entityManager.createQuery("""
				FROM DentalPieceState dps
				WHERE dps.dentalPiece.odontogram.id = :odontogramId
				  AND dps.active = true
				ORDER BY dps.dentalPiece.pieceNumber ASC
				""", DentalPieceState.class).setParameter("odontogramId", odontogramId).getResultList();
	}

	@Override
	public void deactivateActiveByDentalPieceId(Long dentalPieceId) {
		List<DentalPieceState> activeStates = entityManager.createQuery("""
				FROM DentalPieceState dps
				WHERE dps.dentalPiece.id = :dentalPieceId
				  AND dps.active = true
				""", DentalPieceState.class).setParameter("dentalPieceId", dentalPieceId).getResultList();

		for (DentalPieceState state : activeStates) {
			state.setActive(false);
			entityManager.merge(state);
		}
	}

	@Override
	public void save(DentalPieceState dentalPieceState) {
		entityManager.persist(dentalPieceState);
	}

	@Override
	public DentalPieceState update(DentalPieceState dentalPieceState) {
		return entityManager.merge(dentalPieceState);
	}

	@Override
	public void delete(DentalPieceState dentalPieceState) {
		entityManager.remove(
				entityManager.contains(dentalPieceState) ? dentalPieceState : entityManager.merge(dentalPieceState));
	}
}