package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.DentalPieceDao;
import com.example.DentalPlus_Backend.model.DentalPiece;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
		return entityManager.find(DentalPiece.class, id);
	}

	@Override
	public DentalPiece findByOdontogramIdAndPieceNumber(Long odontogramId, Integer pieceNumber) {
		if (odontogramId == null || pieceNumber == null) {
			return null;
		}

		return entityManager.createQuery("""
				FROM DentalPiece dp
				WHERE dp.odontogram.id = :odontogramId
				  AND dp.pieceNumber = :pieceNumber
				""", DentalPiece.class).setParameter("odontogramId", odontogramId)
				.setParameter("pieceNumber", pieceNumber).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<DentalPiece> findByOdontogramId(Long odontogramId) {
		return entityManager.createQuery("""
				FROM DentalPiece dp
				WHERE dp.odontogram.id = :odontogramId
				ORDER BY dp.pieceNumber ASC
				""", DentalPiece.class).setParameter("odontogramId", odontogramId).getResultList();
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