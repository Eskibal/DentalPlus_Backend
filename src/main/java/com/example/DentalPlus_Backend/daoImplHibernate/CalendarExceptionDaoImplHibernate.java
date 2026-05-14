package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.CalendarExceptionDao;
import com.example.DentalPlus_Backend.model.CalendarException;
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
public class CalendarExceptionDaoImplHibernate implements CalendarExceptionDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public CalendarException findById(Long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CalendarException> cq = cb.createQuery(CalendarException.class);
		Root<CalendarException> calendarException = cq.from(CalendarException.class);

		calendarException.fetch("calendarRule", JoinType.LEFT);

		cq.select(calendarException)
				.distinct(true)
				.where(cb.equal(calendarException.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<CalendarException> findActiveByCalendarRuleId(Long calendarRuleId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CalendarException> cq = cb.createQuery(CalendarException.class);
		Root<CalendarException> calendarException = cq.from(CalendarException.class);

		calendarException.fetch("calendarRule", JoinType.LEFT);

		cq.select(calendarException)
				.distinct(true)
				.where(cb.and(
						cb.equal(calendarException.get("calendarRule").get("id"), calendarRuleId),
						cb.isTrue(calendarException.get("active"))))
				.orderBy(
						cb.asc(calendarException.get("date")),
						cb.asc(calendarException.get("startTime")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<CalendarException> findActiveByCalendarRuleIdAndDate(Long calendarRuleId, LocalDate date) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<CalendarException> cq = cb.createQuery(CalendarException.class);
		Root<CalendarException> calendarException = cq.from(CalendarException.class);

		calendarException.fetch("calendarRule", JoinType.LEFT);

		cq.select(calendarException)
				.distinct(true)
				.where(cb.and(
						cb.equal(calendarException.get("calendarRule").get("id"), calendarRuleId),
						cb.equal(calendarException.get("date"), date),
						cb.isTrue(calendarException.get("active"))))
				.orderBy(cb.asc(calendarException.get("startTime")));

		return entityManager.createQuery(cq).getResultList();
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