package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.ClinicDao;
import com.example.DentalPlus_Backend.model.Clinic;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
		return entityManager.find(Clinic.class, id);
	}

	@Override
	public List<Clinic> findAll() {
		return entityManager.createQuery("FROM Clinic", Clinic.class).getResultList();
	}

	@Override
	public Clinic findFirstActive() {
		return entityManager.createQuery("""
				FROM Clinic c
				WHERE c.active = true
				ORDER BY c.id ASC
				""", Clinic.class).setMaxResults(1).getResultStream().findFirst().orElse(null);
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
}