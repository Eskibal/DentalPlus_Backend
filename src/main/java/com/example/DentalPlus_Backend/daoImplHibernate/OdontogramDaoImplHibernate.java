package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.OdontogramDao;
import com.example.DentalPlus_Backend.model.Odontogram;
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
public class OdontogramDaoImplHibernate implements OdontogramDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Odontogram findById(Long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Odontogram> cq = cb.createQuery(Odontogram.class);
		Root<Odontogram> odontogram = cq.from(Odontogram.class);

		fetchOdontogramRelations(odontogram);

		cq.select(odontogram)
				.distinct(true)
				.where(cb.equal(odontogram.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public Odontogram findByPatientId(Long patientId) {
		if (patientId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Odontogram> cq = cb.createQuery(Odontogram.class);
		Root<Odontogram> odontogram = cq.from(Odontogram.class);

		fetchOdontogramRelations(odontogram);

		cq.select(odontogram)
				.distinct(true)
				.where(cb.equal(odontogram.get("patient").get("id"), patientId));

		List<Odontogram> odontograms = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return odontograms.isEmpty() ? null : odontograms.get(0);
	}

	@Override
	public boolean existsByPatientId(Long patientId) {
		if (patientId == null) {
			return false;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Odontogram> odontogram = cq.from(Odontogram.class);

		cq.select(cb.count(odontogram))
				.where(cb.equal(odontogram.get("patient").get("id"), patientId));

		Long count = entityManager.createQuery(cq).getSingleResult();

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

	private void fetchOdontogramRelations(Root<Odontogram> odontogram) {
		Fetch<Object, Object> patient = odontogram.fetch("patient", JoinType.LEFT);
		patient.fetch("person", JoinType.LEFT);
		patient.fetch("clinic", JoinType.LEFT);
	}
}