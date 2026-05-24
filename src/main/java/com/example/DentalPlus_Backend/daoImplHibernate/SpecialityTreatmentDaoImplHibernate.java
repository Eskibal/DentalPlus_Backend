package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.SpecialityTreatmentDao;
import com.example.DentalPlus_Backend.model.SpecialityTreatment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("hibernate")
public class SpecialityTreatmentDaoImplHibernate implements SpecialityTreatmentDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public SpecialityTreatment findById(Long id) {
		if (id == null) {
			return null;
		}

		return entityManager.find(SpecialityTreatment.class, id);
	}

	@Override
	public SpecialityTreatment findBySpecialityIdAndTreatmentId(Long specialityId, Long treatmentId) {
		if (specialityId == null || treatmentId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<SpecialityTreatment> cq = cb.createQuery(SpecialityTreatment.class);
		Root<SpecialityTreatment> specialityTreatment = cq.from(SpecialityTreatment.class);

		cq.select(specialityTreatment)
				.where(cb.and(
						cb.equal(specialityTreatment.get("speciality").get("id"), specialityId),
						cb.equal(specialityTreatment.get("treatment").get("id"), treatmentId)
				));

		List<SpecialityTreatment> result = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return result.isEmpty() ? null : result.get(0);
	}

	@Override
	public List<SpecialityTreatment> findActiveBySpecialityId(Long specialityId) {
		if (specialityId == null) {
			return List.of();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<SpecialityTreatment> cq = cb.createQuery(SpecialityTreatment.class);
		Root<SpecialityTreatment> specialityTreatment = cq.from(SpecialityTreatment.class);

		cq.select(specialityTreatment)
				.where(cb.and(
						cb.equal(specialityTreatment.get("speciality").get("id"), specialityId),
						cb.isTrue(specialityTreatment.get("active")),
						cb.isTrue(specialityTreatment.get("speciality").get("active")),
						cb.isTrue(specialityTreatment.get("treatment").get("active"))
				))
				.orderBy(cb.asc(specialityTreatment.get("treatment").get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<SpecialityTreatment> findActiveByTreatmentId(Long treatmentId) {
		if (treatmentId == null) {
			return List.of();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<SpecialityTreatment> cq = cb.createQuery(SpecialityTreatment.class);
		Root<SpecialityTreatment> specialityTreatment = cq.from(SpecialityTreatment.class);

		cq.select(specialityTreatment)
				.where(cb.and(
						cb.equal(specialityTreatment.get("treatment").get("id"), treatmentId),
						cb.isTrue(specialityTreatment.get("active")),
						cb.isTrue(specialityTreatment.get("speciality").get("active")),
						cb.isTrue(specialityTreatment.get("treatment").get("active"))
				))
				.orderBy(cb.asc(specialityTreatment.get("speciality").get("name")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public void save(SpecialityTreatment specialityTreatment) {
		entityManager.persist(specialityTreatment);
	}

	@Override
	public SpecialityTreatment update(SpecialityTreatment specialityTreatment) {
		return entityManager.merge(specialityTreatment);
	}

	@Override
	public void delete(SpecialityTreatment specialityTreatment) {
		entityManager.remove(entityManager.contains(specialityTreatment) ? specialityTreatment
				: entityManager.merge(specialityTreatment));
	}
}