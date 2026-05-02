package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.OdontogramDao;
import com.example.DentalPlus_Backend.model.Odontogram;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("hibernate")
public class OdontogramDaoImplHibernate implements OdontogramDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Odontogram findById(Long id) {
		return entityManager.find(Odontogram.class, id);
	}

	@Override
	public Odontogram findByPatientId(Long patientId) {
		if (patientId == null) {
			return null;
		}
		List<Odontogram> odontograms = entityManager.createQuery("""
				SELECT o
				FROM Odontogram o
				WHERE o.patient.id = :patientId
				""", Odontogram.class).setParameter("patientId", patientId).setMaxResults(1).getResultList();
		return odontograms.isEmpty() ? null : odontograms.get(0);
	}

	@Override
	public boolean existsByPatientId(Long patientId) {
		if (patientId == null) {
			return false;
		}

		Long count = entityManager.createQuery("""
				SELECT COUNT(o)
				FROM Odontogram o
				WHERE o.patient.id = :patientId
				""", Long.class).setParameter("patientId", patientId).getSingleResult();

		return count != null && count > 0;
	}

	@Override
	public void save(Odontogram odontogram) {
		entityManager.persist(odontogram);
	}

	@Override
	public Odontogram update(Odontogram odontogram) {
		return entityManager.merge(odontogram);
	}

	@Override
	public void delete(Odontogram odontogram) {
		entityManager.remove(entityManager.contains(odontogram) ? odontogram : entityManager.merge(odontogram));
	}
}