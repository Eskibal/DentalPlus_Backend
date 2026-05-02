package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentalSurfaceDao;
import com.example.DentalPlus_Backend.model.DentalSurface;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
	public DentalSurface findByDentalPieceIdAndSurfaceType(Long dentalPieceId, String surfaceType) {
		if (dentalPieceId == null || surfaceType == null || surfaceType.isBlank()) {
			return null;
		}

		return entityManager.createQuery("""
				FROM DentalSurface ds
				WHERE ds.dentalPiece.id = :dentalPieceId
				  AND ds.surfaceType = :surfaceType
				""", DentalSurface.class).setParameter("dentalPieceId", dentalPieceId)
				.setParameter("surfaceType", surfaceType.trim().toUpperCase()).getResultStream().findFirst()
				.orElse(null);
	}

	@Override
	public List<DentalSurface> findByDentalPieceId(Long dentalPieceId) {
		return entityManager.createQuery("""
				FROM DentalSurface ds
				WHERE ds.dentalPiece.id = :dentalPieceId
				ORDER BY ds.surfaceType ASC
				""", DentalSurface.class).setParameter("dentalPieceId", dentalPieceId).getResultList();
	}

	@Override
	public List<DentalSurface> findByOdontogramId(Long odontogramId) {
		return entityManager.createQuery("""
				FROM DentalSurface ds
				WHERE ds.dentalPiece.odontogram.id = :odontogramId
				ORDER BY ds.dentalPiece.pieceNumber ASC, ds.surfaceType ASC
				""", DentalSurface.class).setParameter("odontogramId", odontogramId).getResultList();
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
		entityManager
				.remove(entityManager.contains(dentalSurface) ? dentalSurface : entityManager.merge(dentalSurface));
	}
}