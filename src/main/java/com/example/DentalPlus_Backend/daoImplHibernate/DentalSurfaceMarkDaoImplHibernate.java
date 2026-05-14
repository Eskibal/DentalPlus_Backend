package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentalSurfaceMarkDao;
import com.example.DentalPlus_Backend.model.DentalPiece;
import com.example.DentalPlus_Backend.model.DentalSurface;
import com.example.DentalPlus_Backend.model.DentalSurfaceMark;
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
public class DentalSurfaceMarkDaoImplHibernate implements DentalSurfaceMarkDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public DentalSurfaceMark findById(Long id) {
		return entityManager.find(DentalSurfaceMark.class, id);
	}

	@Override
	public List<DentalSurfaceMark> findByDentalSurfaceId(Long dentalSurfaceId) {
		if (dentalSurfaceId == null) {
			return List.of();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalSurfaceMark> cq = cb.createQuery(DentalSurfaceMark.class);
		Root<DentalSurfaceMark> mark = cq.from(DentalSurfaceMark.class);

		Join<DentalSurfaceMark, DentalSurface> surface = mark.join("dentalSurface", JoinType.INNER);

		cq.select(mark)
				.where(cb.equal(surface.get("id"), dentalSurfaceId))
				.orderBy(cb.desc(mark.get("id")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public DentalSurfaceMark findActiveByDentalSurfaceId(Long dentalSurfaceId) {
		if (dentalSurfaceId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalSurfaceMark> cq = cb.createQuery(DentalSurfaceMark.class);
		Root<DentalSurfaceMark> mark = cq.from(DentalSurfaceMark.class);

		Join<DentalSurfaceMark, DentalSurface> surface = mark.join("dentalSurface", JoinType.INNER);

		cq.select(mark)
				.where(cb.and(
						cb.equal(surface.get("id"), dentalSurfaceId),
						cb.isTrue(mark.get("active"))
				))
				.orderBy(cb.desc(mark.get("id")));

		List<DentalSurfaceMark> marks = entityManager.createQuery(cq)
				.setMaxResults(1)
				.getResultList();

		return marks.isEmpty() ? null : marks.get(0);
	}

	@Override
	public List<DentalSurfaceMark> findActiveByOdontogramId(Long odontogramId) {
		if (odontogramId == null) {
			return List.of();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<DentalSurfaceMark> cq = cb.createQuery(DentalSurfaceMark.class);
		Root<DentalSurfaceMark> mark = cq.from(DentalSurfaceMark.class);

		Join<DentalSurfaceMark, DentalSurface> surface = mark.join("dentalSurface", JoinType.INNER);
		Join<DentalSurface, DentalPiece> dentalPiece = surface.join("dentalPiece", JoinType.INNER);

		cq.select(mark)
				.where(cb.and(
						cb.equal(dentalPiece.get("odontogram").get("id"), odontogramId),
						cb.isTrue(mark.get("active"))
				))
				.orderBy(
						cb.asc(dentalPiece.get("pieceNumber")),
						cb.asc(surface.get("surfaceType")),
						cb.desc(mark.get("id"))
				);

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public void save(DentalSurfaceMark dentalSurfaceMark) {
		entityManager.persist(dentalSurfaceMark);
	}

	@Override
	public DentalSurfaceMark update(DentalSurfaceMark dentalSurfaceMark) {
		return entityManager.merge(dentalSurfaceMark);
	}

	@Override
	public void delete(DentalSurfaceMark dentalSurfaceMark) {
		entityManager.remove(entityManager.contains(dentalSurfaceMark)
				? dentalSurfaceMark
				: entityManager.merge(dentalSurfaceMark));
	}

	@Override
	public void deactivateActiveByDentalSurfaceId(Long dentalSurfaceId) {
		// TODO Auto-generated method stub
		
	}
}