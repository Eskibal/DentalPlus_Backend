package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.CalendarRuleDao;
import com.example.DentalPlus_Backend.model.CalendarRule;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Receptionist;
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
public class CalendarRuleDaoImplHibernate implements CalendarRuleDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public CalendarRule findById(Long id) {
		return entityManager.find(CalendarRule.class, id);
	}

	@Override
	public CalendarRule findByClinicId(Long clinicId) {
		if (clinicId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CalendarRule> cq = cb.createQuery(CalendarRule.class);
		Root<Clinic> clinic = cq.from(Clinic.class);

		cq.select(clinic.get("calendarRule"))
				.where(cb.and(
						cb.equal(clinic.get("id"), clinicId),
						cb.isNotNull(clinic.get("calendarRule"))));

		List<CalendarRule> calendarRules = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return calendarRules.isEmpty() ? null : calendarRules.get(0);
	}

	@Override
	public CalendarRule findByDentistId(Long dentistId) {
		if (dentistId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CalendarRule> cq = cb.createQuery(CalendarRule.class);
		Root<Dentist> dentist = cq.from(Dentist.class);

		cq.select(dentist.get("calendarRule"))
				.where(cb.and(
						cb.equal(dentist.get("id"), dentistId),
						cb.isNotNull(dentist.get("calendarRule"))));

		List<CalendarRule> calendarRules = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return calendarRules.isEmpty() ? null : calendarRules.get(0);
	}

	@Override
	public CalendarRule findByReceptionistId(Long receptionistId) {
		if (receptionistId == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CalendarRule> cq = cb.createQuery(CalendarRule.class);
		Root<Receptionist> receptionist = cq.from(Receptionist.class);

		cq.select(receptionist.get("calendarRule"))
				.where(cb.and(
						cb.equal(receptionist.get("id"), receptionistId),
						cb.isNotNull(receptionist.get("calendarRule"))));

		List<CalendarRule> calendarRules = entityManager.createQuery(cq).setMaxResults(1).getResultList();

		return calendarRules.isEmpty() ? null : calendarRules.get(0);
	}

	@Override
	public void save(CalendarRule calendarRule) {
		entityManager.persist(calendarRule);
	}

	@Override
	public CalendarRule update(CalendarRule calendarRule) {
		return entityManager.merge(calendarRule);
	}

	@Override
	public void delete(CalendarRule calendarRule) {
		entityManager.remove(entityManager.contains(calendarRule) ? calendarRule : entityManager.merge(calendarRule));
	}
}