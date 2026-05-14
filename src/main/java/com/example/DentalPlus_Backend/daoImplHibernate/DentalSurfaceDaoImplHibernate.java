package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentalSurfaceDao;
import com.example.DentalPlus_Backend.model.DentalPiece;
import com.example.DentalPlus_Backend.model.DentalSurface;
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
public class DentalSurfaceDaoImplHibernate implements DentalSurfaceDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public DentalSurface findById(Long id) {
		return entityManager.find(DentalSurface.class, id);
	}

	@Override
	public List<DentalSurface> findByDentalPieceId(Long dentalPieceId) {
		if (dentalPieceId == null) {
			return List.of();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalSurface> cq = cb.createQuery(DentalSurface.class);
		Root<DentalSurface> surface = cq.from(DentalSurface.class);

		Join<DentalSurface, DentalPiece> dentalPiece = surface.join("dentalPiece", JoinType.INNER);

		cq.select(surface)
				.where(cb.equal(dentalPiece.get("id"), dentalPieceId))
				.orderBy(cb.asc(surface.get("surfaceType")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public DentalSurface findByDentalPieceIdAndSurfaceType(Long dentalPieceId, String surfaceType) {
		if (dentalPieceId == null || surfaceType == null || surfaceType.isBlank()) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalSurface> cq = cb.createQuery(DentalSurface.class);
		Root<DentalSurface> surface = cq.from(DentalSurface.class);

		Join<DentalSurface, DentalPiece> dentalPiece = surface.join("dentalPiece", JoinType.INNER);

		cq.select(surface)
				.where(cb.and(
						cb.equal(dentalPiece.get("id"), dentalPieceId),
						cb.equal(surface.get("surfaceType"), surfaceType.trim())
				));

		List<DentalSurface> surfaces = entityManager.createQuery(cq)
				.setMaxResults(1)
				.getResultList();

		return surfaces.isEmpty() ? null : surfaces.get(0);
	}

	@Override
	public List<DentalSurface> findByOdontogramId(Long odontogramId) {
		if (odontogramId == null) {
			return List.of();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalSurface> cq = cb.createQuery(DentalSurface.class);
		Root<DentalSurface> surface = cq.from(DentalSurface.class);

		Join<DentalSurface, DentalPiece> dentalPiece = surface.join("dentalPiece", JoinType.INNER);

		cq.select(surface)
				.where(cb.equal(dentalPiece.get("odontogram").get("id"), odontogramId))
				.orderBy(
						cb.asc(dentalPiece.get("pieceNumber")),
						cb.asc(surface.get("surfaceType"))
				);

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public void save(DentalSurface dentalSurface) {
		entityManager.persist(dentalSurface);
	}

	@Override
	public DentalSurface update(DentalSurface dentalSurface) {
		return entityManager.merge(dentalSurface);
	}

	@Override
	public void delete(DentalSurface dentalSurface) {
		entityManager.remove(entityManager.contains(dentalSurface) ? dentalSurface : entityManager.merge(dentalSurface));
	}
}