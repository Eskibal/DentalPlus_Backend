package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.BoxDao;
import com.example.DentalPlus_Backend.model.Box;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("hibernate")
public class BoxDaoImplHibernate implements BoxDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Box findById(Long id) {
		return entityManager.find(Box.class, id);
	}

	@Override
	public List<Box> findByClinicId(Long clinicId) {
		return entityManager.createQuery("FROM Box b WHERE b.clinic.id = :clinicId", Box.class)
				.setParameter("clinicId", clinicId).getResultList();
	}

	@Override
	public List<Box> findActiveByClinicId(Long clinicId) {
		return entityManager.createQuery("""
				FROM Box b
				WHERE b.clinic.id = :clinicId
				  AND b.active = true
				ORDER BY b.name ASC
				""", Box.class).setParameter("clinicId", clinicId).getResultList();
	}

	@Override
	public void save(Box box) {
		entityManager.persist(box);
	}

	@Override
	public Box update(Box box) {
		return entityManager.merge(box);
	}

	@Override
	public void delete(Box box) {
		entityManager.remove(entityManager.contains(box) ? box : entityManager.merge(box));
	}
}