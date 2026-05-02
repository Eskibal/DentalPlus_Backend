package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.CalendarBreakDao;
import com.example.DentalPlus_Backend.model.CalendarBreak;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("hibernate")
public class CalendarBreakDaoImplHibernate implements CalendarBreakDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public CalendarBreak findById(Long id) {
		return entityManager.find(CalendarBreak.class, id);
	}

	@Override
	public List<CalendarBreak> findActiveByCalendarRuleId(Long calendarRuleId) {
		return entityManager.createQuery("""
				FROM CalendarBreak cb
				WHERE cb.calendarRule.id = :calendarRuleId
				  AND cb.active = true
				ORDER BY cb.dayOfWeek ASC, cb.breakStartTime ASC
				""", CalendarBreak.class).setParameter("calendarRuleId", calendarRuleId).getResultList();
	}

	@Override
	public List<CalendarBreak> findActiveByCalendarRuleIdAndDayOfWeek(Long calendarRuleId, String dayOfWeek) {
		return entityManager.createQuery("""
				FROM CalendarBreak cb
				WHERE cb.calendarRule.id = :calendarRuleId
				  AND cb.dayOfWeek = :dayOfWeek
				  AND cb.active = true
				ORDER BY cb.breakStartTime ASC
				""", CalendarBreak.class).setParameter("calendarRuleId", calendarRuleId)
				.setParameter("dayOfWeek", CalendarBreak.normalizeDayOfWeek(dayOfWeek)).getResultList();
	}

	@Override
	public void save(CalendarBreak calendarBreak) {
		entityManager.persist(calendarBreak);
	}

	@Override
	public CalendarBreak update(CalendarBreak calendarBreak) {
		return entityManager.merge(calendarBreak);
	}

	@Override
	public void delete(CalendarBreak calendarBreak) {
		entityManager
				.remove(entityManager.contains(calendarBreak) ? calendarBreak : entityManager.merge(calendarBreak));
	}
}