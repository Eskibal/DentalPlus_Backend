package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.CalendarHolidayDao;
import com.example.DentalPlus_Backend.model.CalendarHoliday;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
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
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CalendarHoliday> cq = cb.createQuery(CalendarHoliday.class);
		Root<CalendarHoliday> calendarHoliday = cq.from(CalendarHoliday.class);

		calendarHoliday.fetch("calendarRule", JoinType.LEFT);

		cq.select(calendarHoliday)
				.distinct(true)
				.where(cb.equal(calendarHoliday.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<CalendarHoliday> findActiveByCalendarRuleId(Long calendarRuleId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CalendarHoliday> cq = cb.createQuery(CalendarHoliday.class);
		Root<CalendarHoliday> calendarHoliday = cq.from(CalendarHoliday.class);

		calendarHoliday.fetch("calendarRule", JoinType.LEFT);

		cq.select(calendarHoliday)
				.distinct(true)
				.where(cb.and(
						cb.equal(calendarHoliday.get("calendarRule").get("id"), calendarRuleId),
						cb.isTrue(calendarHoliday.get("active"))))
				.orderBy(cb.asc(calendarHoliday.get("startDate")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<CalendarHoliday> findActiveByCalendarRuleIdAndDate(Long calendarRuleId, LocalDate date) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CalendarHoliday> cq = cb.createQuery(CalendarHoliday.class);
		Root<CalendarHoliday> calendarHoliday = cq.from(CalendarHoliday.class);

		calendarHoliday.fetch("calendarRule", JoinType.LEFT);

		cq.select(calendarHoliday)
				.distinct(true)
				.where(cb.and(
						cb.equal(calendarHoliday.get("calendarRule").get("id"), calendarRuleId),
						cb.isTrue(calendarHoliday.get("active")),
						cb.lessThanOrEqualTo(calendarHoliday.get("startDate"), date),
						cb.greaterThanOrEqualTo(calendarHoliday.get("endDate"), date)))
				.orderBy(cb.asc(calendarHoliday.get("startDate")));

		return entityManager.createQuery(cq).getResultList();
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