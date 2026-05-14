package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentalPieceStateDao;
import com.example.DentalPlus_Backend.model.DentalPiece;
import com.example.DentalPlus_Backend.model.DentalPieceState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
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
	public List<DentalPieceState> findByDentalPieceId(Long dentalPieceId) {
		if (dentalPieceId == null) {
			return List.of();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalPieceState> cq = cb.createQuery(DentalPieceState.class);
		Root<DentalPieceState> state = cq.from(DentalPieceState.class);

		Join<DentalPieceState, DentalPiece> dentalPiece = state.join("dentalPiece", JoinType.INNER);

		cq.select(state)
				.where(cb.equal(dentalPiece.get("id"), dentalPieceId))
				.orderBy(cb.desc(state.get("id")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public DentalPieceState findActiveByDentalPieceId(Long dentalPieceId) {
		if (dentalPieceId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalPieceState> cq = cb.createQuery(DentalPieceState.class);
		Root<DentalPieceState> state = cq.from(DentalPieceState.class);

		Join<DentalPieceState, DentalPiece> dentalPiece = state.join("dentalPiece", JoinType.INNER);

		cq.select(state)
				.where(cb.and(
						cb.equal(dentalPiece.get("id"), dentalPieceId),
						cb.isTrue(state.get("active"))
				))
				.orderBy(cb.desc(state.get("id")));

		List<DentalPieceState> states = entityManager.createQuery(cq)
				.setMaxResults(1)
				.getResultList();

		return states.isEmpty() ? null : states.get(0);
	}

	@Override
	public List<DentalPieceState> findActiveByOdontogramId(Long odontogramId) {
		if (odontogramId == null) {
			return List.of();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalPieceState> cq = cb.createQuery(DentalPieceState.class);
		Root<DentalPieceState> state = cq.from(DentalPieceState.class);

		Join<DentalPieceState, DentalPiece> dentalPiece = state.join("dentalPiece", JoinType.INNER);

		cq.select(state)
				.where(cb.and(
						cb.equal(dentalPiece.get("odontogram").get("id"), odontogramId),
						cb.isTrue(state.get("active"))
				))
				.orderBy(
						cb.asc(dentalPiece.get("pieceNumber")),
						cb.desc(state.get("id"))
				);

		return entityManager.createQuery(cq).getResultList();
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
		entityManager.remove(entityManager.contains(dentalPieceState)
				? dentalPieceState
				: entityManager.merge(dentalPieceState));
	}

	@Override
	public void deactivateActiveByDentalPieceId(Long dentalPieceId) {
		// TODO Auto-generated method stub
		
	}
}