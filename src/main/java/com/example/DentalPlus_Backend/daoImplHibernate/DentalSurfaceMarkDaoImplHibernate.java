package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentalSurfaceMarkDao;
import com.example.DentalPlus_Backend.model.DentalSurfaceMark;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
	public DentalSurfaceMark findActiveByDentalSurfaceId(Long dentalSurfaceId) {
		if (dentalSurfaceId == null) {
			return null;
		}
		List<DentalSurfaceMark> marks = entityManager.createQuery("""
				SELECT dsm
				FROM DentalSurfaceMark dsm
				WHERE dsm.dentalSurface.id = :dentalSurfaceId
				  AND dsm.active = true
				ORDER BY dsm.createdAt DESC
				""", DentalSurfaceMark.class).setParameter("dentalSurfaceId", dentalSurfaceId).setMaxResults(1)
				.getResultList();
		return marks.isEmpty() ? null : marks.get(0);
	}

	@Override
	public List<DentalSurfaceMark> findByDentalSurfaceId(Long dentalSurfaceId) {
		return entityManager.createQuery("""
				FROM DentalSurfaceMark sm
				WHERE sm.dentalSurface.id = :dentalSurfaceId
				ORDER BY sm.createdAt DESC
				""", DentalSurfaceMark.class).setParameter("dentalSurfaceId", dentalSurfaceId).getResultList();
	}

	@Override
	public List<DentalSurfaceMark> findActiveByOdontogramId(Long odontogramId) {
		return entityManager.createQuery("""
				FROM DentalSurfaceMark sm
				WHERE sm.dentalSurface.dentalPiece.odontogram.id = :odontogramId
				  AND sm.active = true
				ORDER BY sm.dentalSurface.dentalPiece.pieceNumber ASC,
				         sm.dentalSurface.surfaceType ASC
				""", DentalSurfaceMark.class).setParameter("odontogramId", odontogramId).getResultList();
	}

	@Override
	public void deactivateActiveByDentalSurfaceId(Long dentalSurfaceId) {
		List<DentalSurfaceMark> activeMarks = entityManager.createQuery("""
				FROM DentalSurfaceMark sm
				WHERE sm.dentalSurface.id = :dentalSurfaceId
				  AND sm.active = true
				""", DentalSurfaceMark.class).setParameter("dentalSurfaceId", dentalSurfaceId).getResultList();

		for (DentalSurfaceMark mark : activeMarks) {
			mark.setActive(false);
			entityManager.merge(mark);
		}
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
		entityManager.remove(
				entityManager.contains(dentalSurfaceMark) ? dentalSurfaceMark : entityManager.merge(dentalSurfaceMark));
	}
}