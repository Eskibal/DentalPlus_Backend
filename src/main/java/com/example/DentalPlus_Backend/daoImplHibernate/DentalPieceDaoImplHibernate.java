package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentalPieceDao;
import com.example.DentalPlus_Backend.model.DentalPiece;
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
public class DentalPieceDaoImplHibernate implements DentalPieceDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public DentalPiece findById(Long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalPiece> cq = cb.createQuery(DentalPiece.class);
		Root<DentalPiece> dentalPiece = cq.from(DentalPiece.class);

		dentalPiece.fetch("odontogram", JoinType.LEFT);

		cq.select(dentalPiece)
				.distinct(true)
				.where(cb.equal(dentalPiece.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public DentalPiece findByOdontogramIdAndPieceNumber(Long odontogramId, Integer pieceNumber) {
		if (odontogramId == null || pieceNumber == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalPiece> cq = cb.createQuery(DentalPiece.class);
		Root<DentalPiece> dentalPiece = cq.from(DentalPiece.class);

		dentalPiece.fetch("odontogram", JoinType.LEFT);

		cq.select(dentalPiece)
				.distinct(true)
				.where(cb.and(
						cb.equal(dentalPiece.get("odontogram").get("id"), odontogramId),
						cb.equal(dentalPiece.get("pieceNumber"), pieceNumber)));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<DentalPiece> findByOdontogramId(Long odontogramId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalPiece> cq = cb.createQuery(DentalPiece.class);
		Root<DentalPiece> dentalPiece = cq.from(DentalPiece.class);

		dentalPiece.fetch("odontogram", JoinType.LEFT);

		cq.select(dentalPiece)
				.distinct(true)
				.where(cb.equal(dentalPiece.get("odontogram").get("id"), odontogramId))
				.orderBy(cb.asc(dentalPiece.get("pieceNumber")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public void save(DentalPiece dentalPiece) {
		entityManager.persist(dentalPiece);
	}

	@Override
	public DentalPiece update(DentalPiece dentalPiece) {
		return entityManager.merge(dentalPiece);
	}

	@Override
	public void delete(DentalPiece dentalPiece) {
		entityManager.remove(entityManager.contains(dentalPiece) ? dentalPiece : entityManager.merge(dentalPiece));
	}
}