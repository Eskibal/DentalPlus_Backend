package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.CalendarExceptionDao;
import com.example.DentalPlus_Backend.model.CalendarException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@Profile("hibernate")
public class CalendarExceptionDaoImplHibernate implements CalendarExceptionDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public CalendarException findById(Long id) {
		return entityManager.find(CalendarException.class, id);
	}

	@Override
	public List<CalendarException> findActiveByCalendarRuleId(Long calendarRuleId) {
		return entityManager.createQuery("""
				FROM CalendarException ce
				WHERE ce.calendarRule.id = :calendarRuleId
				  AND ce.active = true
				ORDER BY ce.date ASC, ce.startTime ASC
				""", CalendarException.class).setParameter("calendarRuleId", calendarRuleId).getResultList();
	}

	@Override
	public List<CalendarException> findActiveByCalendarRuleIdAndDate(Long calendarRuleId, LocalDate date) {
		return entityManager.createQuery("""
				FROM CalendarException ce
				WHERE ce.calendarRule.id = :calendarRuleId
				  AND ce.date = :date
				  AND ce.active = true
				ORDER BY ce.startTime ASC
				""", CalendarException.class).setParameter("calendarRuleId", calendarRuleId).setParameter("date", date)
				.getResultList();
	}

	@Override
	public void save(CalendarException calendarException) {
		entityManager.persist(calendarException);
	}

	@Override
	public CalendarException update(CalendarException calendarException) {
		return entityManager.merge(calendarException);
	}

	@Override
	public void delete(CalendarException calendarException) {
		entityManager.remove(
				entityManager.contains(calendarException) ? calendarException : entityManager.merge(calendarException));
	}
}