package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.ClinicDao;
import com.example.DentalPlus_Backend.model.Clinic;
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
public class ClinicDaoImplHibernate implements ClinicDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Clinic findById(Long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Clinic> cq = cb.createQuery(Clinic.class);
		Root<Clinic> clinic = cq.from(Clinic.class);

		fetchClinicRelations(clinic);

		cq.select(clinic)
				.distinct(true)
				.where(cb.equal(clinic.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<Clinic> findAll() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Clinic> cq = cb.createQuery(Clinic.class);
		Root<Clinic> clinic = cq.from(Clinic.class);

		fetchClinicRelations(clinic);

		cq.select(clinic).distinct(true);

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public Clinic findFirstActive() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Clinic> cq = cb.createQuery(Clinic.class);
		Root<Clinic> clinic = cq.from(Clinic.class);

		fetchClinicRelations(clinic);

		cq.select(clinic)
				.distinct(true)
				.where(cb.isTrue(clinic.get("active")))
				.orderBy(cb.asc(clinic.get("id")));

		return entityManager.createQuery(cq).setMaxResults(1).getResultStream().findFirst().orElse(null);
	}

	@Override
	public void save(Clinic clinic) {
		entityManager.persist(clinic);
	}

	@Override
	public Clinic update(Clinic clinic) {
		return entityManager.merge(clinic);
	}

	@Override
	public void delete(Clinic clinic) {
		entityManager.remove(entityManager.contains(clinic) ? clinic : entityManager.merge(clinic));
	}

	private void fetchClinicRelations(Root<Clinic> clinic) {
		clinic.fetch("organization", JoinType.LEFT);
		clinic.fetch("calendarRule", JoinType.LEFT);
	}
}