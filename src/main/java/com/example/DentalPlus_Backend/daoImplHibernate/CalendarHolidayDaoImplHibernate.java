package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.CalendarHolidayDao;
import com.example.DentalPlus_Backend.model.CalendarHoliday;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@Profile("hibernate")
public class CalendarHolidayDaoImplHibernate implements CalendarHolidayDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public CalendarHoliday findById(Long id) {
		return entityManager.find(CalendarHoliday.class, id);
	}

	@Override
	public List<CalendarHoliday> findActiveByCalendarRuleId(Long calendarRuleId) {
		return entityManager.createQuery("""
				FROM CalendarHoliday ch
				WHERE ch.calendarRule.id = :calendarRuleId
				  AND ch.active = true
				ORDER BY ch.startDate ASC
				""", CalendarHoliday.class).setParameter("calendarRuleId", calendarRuleId).getResultList();
	}

	@Override
	public List<CalendarHoliday> findActiveByCalendarRuleIdAndDate(Long calendarRuleId, LocalDate date) {
		return entityManager.createQuery("""
				FROM CalendarHoliday ch
				WHERE ch.calendarRule.id = :calendarRuleId
				  AND ch.active = true
				  AND ch.startDate <= :date
				  AND ch.endDate >= :date
				ORDER BY ch.startDate ASC
				""", CalendarHoliday.class).setParameter("calendarRuleId", calendarRuleId).setParameter("date", date)
				.getResultList();
	}

	@Override
	public void save(CalendarHoliday calendarHoliday) {
		entityManager.persist(calendarHoliday);
	}

	@Override
	public CalendarHoliday update(CalendarHoliday calendarHoliday) {
		return entityManager.merge(calendarHoliday);
	}

	@Override
	public void delete(CalendarHoliday calendarHoliday) {
		entityManager.remove(
				entityManager.contains(calendarHoliday) ? calendarHoliday : entityManager.merge(calendarHoliday));
	}
}