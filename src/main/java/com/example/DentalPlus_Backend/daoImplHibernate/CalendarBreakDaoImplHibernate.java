package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.CalendarBreakDao;
import com.example.DentalPlus_Backend.model.CalendarBreak;
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
public class CalendarBreakDaoImplHibernate implements CalendarBreakDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public CalendarBreak findById(Long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CalendarBreak> cq = cb.createQuery(CalendarBreak.class);
		Root<CalendarBreak> calendarBreak = cq.from(CalendarBreak.class);

		calendarBreak.fetch("calendarRule", JoinType.LEFT);

		cq.select(calendarBreak)
				.distinct(true)
				.where(cb.equal(calendarBreak.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<CalendarBreak> findActiveByCalendarRuleId(Long calendarRuleId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CalendarBreak> cq = cb.createQuery(CalendarBreak.class);
		Root<CalendarBreak> calendarBreak = cq.from(CalendarBreak.class);

		calendarBreak.fetch("calendarRule", JoinType.LEFT);

		cq.select(calendarBreak)
				.distinct(true)
				.where(cb.and(
						cb.equal(calendarBreak.get("calendarRule").get("id"), calendarRuleId),
						cb.isTrue(calendarBreak.get("active"))))
				.orderBy(
						cb.asc(calendarBreak.get("dayOfWeek")),
						cb.asc(calendarBreak.get("breakStartTime")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<CalendarBreak> findActiveByCalendarRuleIdAndDayOfWeek(Long calendarRuleId, String dayOfWeek) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CalendarBreak> cq = cb.createQuery(CalendarBreak.class);
		Root<CalendarBreak> calendarBreak = cq.from(CalendarBreak.class);

		calendarBreak.fetch("calendarRule", JoinType.LEFT);

		cq.select(calendarBreak)
				.distinct(true)
				.where(cb.and(
						cb.equal(calendarBreak.get("calendarRule").get("id"), calendarRuleId),
						cb.equal(calendarBreak.get("dayOfWeek"), CalendarBreak.normalizeDayOfWeek(dayOfWeek)),
						cb.isTrue(calendarBreak.get("active"))))
				.orderBy(cb.asc(calendarBreak.get("breakStartTime")));

		return entityManager.createQuery(cq).getResultList();
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