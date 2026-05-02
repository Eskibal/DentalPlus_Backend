package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.CalendarRuleDao;
import com.example.DentalPlus_Backend.model.CalendarRule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

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

		List<CalendarRule> calendarRules = entityManager.createQuery("""
				SELECT c.calendarRule
				FROM Clinic c
				WHERE c.id = :clinicId
				  AND c.calendarRule IS NOT NULL
				""", CalendarRule.class).setParameter("clinicId", clinicId).setMaxResults(1).getResultList();

		return calendarRules.isEmpty() ? null : calendarRules.get(0);
	}

	@Override
	public CalendarRule findByDentistId(Long dentistId) {
		if (dentistId == null) {
			return null;
		}

		List<CalendarRule> calendarRules = entityManager.createQuery("""
				SELECT d.calendarRule
				FROM Dentist d
				WHERE d.id = :dentistId
				  AND d.calendarRule IS NOT NULL
				""", CalendarRule.class).setParameter("dentistId", dentistId).setMaxResults(1).getResultList();

		return calendarRules.isEmpty() ? null : calendarRules.get(0);
	}

	@Override
	public CalendarRule findByReceptionistId(Long receptionistId) {
		if (receptionistId == null) {
			return null;
		}

		List<CalendarRule> calendarRules = entityManager.createQuery("""
				SELECT r.calendarRule
				FROM Receptionist r
				WHERE r.id = :receptionistId
				  AND r.calendarRule IS NOT NULL
				""", CalendarRule.class).setParameter("receptionistId", receptionistId).setMaxResults(1)
				.getResultList();

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