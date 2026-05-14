package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.AppointmentDao;
import com.example.DentalPlus_Backend.model.Appointment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("hibernate")
public class AppointmentDaoImplHibernate implements AppointmentDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Appointment findById(Long id) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
		Root<Appointment> appointment = cq.from(Appointment.class);

		fetchAppointmentRelations(appointment);

		cq.select(appointment)
				.distinct(true)
				.where(cb.equal(appointment.get("id"), id));

		return entityManager.createQuery(cq).getResultStream().findFirst().orElse(null);
	}

	@Override
	public List<Appointment> findByClinicIdAndDateRange(Long clinicId, LocalDateTime startDateTime,
			LocalDateTime endDateTime) {
		return findByClinicIdAndDateRangeWithFilters(clinicId, startDateTime, endDateTime, null, null, null);
	}

	@Override
	public List<Appointment> findByClinicIdAndDateRangeWithFilters(Long clinicId, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Long patientId, Long dentistId, Long boxId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
		Root<Appointment> appointment = cq.from(Appointment.class);

		fetchAppointmentRelations(appointment);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(appointment.get("dentist").get("clinic").get("id"), clinicId));
		predicates.add(cb.greaterThanOrEqualTo(appointment.get("startDateTime"), startDateTime));
		predicates.add(cb.lessThan(appointment.get("startDateTime"), endDateTime));

		if (patientId != null) {
			predicates.add(cb.equal(appointment.get("patient").get("id"), patientId));
		}

		if (dentistId != null) {
			predicates.add(cb.equal(appointment.get("dentist").get("id"), dentistId));
		}

		if (boxId != null) {
			predicates.add(cb.equal(appointment.get("box").get("id"), boxId));
		}

		cq.select(appointment)
				.distinct(true)
				.where(cb.and(predicates.toArray(new Predicate[0])))
				.orderBy(cb.asc(appointment.get("startDateTime")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Appointment> findActiveByDentistIdAndClinicId(Long dentistId, Long clinicId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
		Root<Appointment> appointment = cq.from(Appointment.class);

		fetchAppointmentRelations(appointment);

		cq.select(appointment)
				.distinct(true)
				.where(cb.and(
						cb.equal(appointment.get("dentist").get("id"), dentistId),
						cb.equal(appointment.get("dentist").get("clinic").get("id"), clinicId),
						cb.isTrue(appointment.get("active")),
						cb.notEqual(appointment.get("status"), "CANCELLED")))
				.orderBy(cb.asc(appointment.get("startDateTime")));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public List<Appointment> findOverlappingAppointments(Long clinicId, LocalDateTime startDateTime,
			LocalDateTime endDateTime) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
		Root<Appointment> appointment = cq.from(Appointment.class);

		fetchAppointmentRelations(appointment);

		cq.select(appointment)
				.distinct(true)
				.where(cb.and(
						cb.equal(appointment.get("dentist").get("clinic").get("id"), clinicId),
						cb.isTrue(appointment.get("active")),
						cb.notEqual(appointment.get("status"), "CANCELLED"),
						cb.lessThan(appointment.get("startDateTime"), endDateTime),
						cb.greaterThan(appointment.get("endDateTime"), startDateTime)));

		return entityManager.createQuery(cq).getResultList();
	}

	@Override
	public boolean existsOverlappingDentistAppointment(Long dentistId, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Long excludedAppointmentId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Appointment> appointment = cq.from(Appointment.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(appointment.get("dentist").get("id"), dentistId));
		predicates.add(cb.isTrue(appointment.get("active")));
		predicates.add(cb.notEqual(appointment.get("status"), "CANCELLED"));
		predicates.add(cb.lessThan(appointment.get("startDateTime"), endDateTime));
		predicates.add(cb.greaterThan(appointment.get("endDateTime"), startDateTime));

		if (excludedAppointmentId != null) {
			predicates.add(cb.notEqual(appointment.get("id"), excludedAppointmentId));
		}

		cq.select(cb.count(appointment))
				.where(cb.and(predicates.toArray(new Predicate[0])));

		Long count = entityManager.createQuery(cq).getSingleResult();

		return count != null && count > 0;
	}

	@Override
	public boolean existsOverlappingBoxAppointment(Long boxId, LocalDateTime startDateTime, LocalDateTime endDateTime,
			Long excludedAppointmentId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Appointment> appointment = cq.from(Appointment.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(appointment.get("box").get("id"), boxId));
		predicates.add(cb.isTrue(appointment.get("active")));
		predicates.add(cb.notEqual(appointment.get("status"), "CANCELLED"));
		predicates.add(cb.lessThan(appointment.get("startDateTime"), endDateTime));
		predicates.add(cb.greaterThan(appointment.get("endDateTime"), startDateTime));

		if (excludedAppointmentId != null) {
			predicates.add(cb.notEqual(appointment.get("id"), excludedAppointmentId));
		}

		cq.select(cb.count(appointment))
				.where(cb.and(predicates.toArray(new Predicate[0])));

		Long count = entityManager.createQuery(cq).getSingleResult();

		return count != null && count > 0;
	}

	@Override
	public boolean existsOverlappingPatientAppointment(Long patientId, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Long excludedAppointmentId) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Appointment> appointment = cq.from(Appointment.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(appointment.get("patient").get("id"), patientId));
		predicates.add(cb.isTrue(appointment.get("active")));
		predicates.add(cb.notEqual(appointment.get("status"), "CANCELLED"));
		predicates.add(cb.lessThan(appointment.get("startDateTime"), endDateTime));
		predicates.add(cb.greaterThan(appointment.get("endDateTime"), startDateTime));

		if (excludedAppointmentId != null) {
			predicates.add(cb.notEqual(appointment.get("id"), excludedAppointmentId));
		}

		cq.select(cb.count(appointment))
				.where(cb.and(predicates.toArray(new Predicate[0])));

		Long count = entityManager.createQuery(cq).getSingleResult();

		return count != null && count > 0;
	}

	@Override
	public void save(Appointment appointment) {
		entityManager.persist(appointment);
	}

	@Override
	public Appointment update(Appointment appointment) {
		return entityManager.merge(appointment);
	}

	@Override
	public void delete(Appointment appointment) {
		entityManager.remove(entityManager.contains(appointment) ? appointment : entityManager.merge(appointment));
	}

	private void fetchAppointmentRelations(Root<Appointment> appointment) {
		appointment.fetch("box", JoinType.LEFT);

		Fetch<Object, Object> dentist = appointment.fetch("dentist", JoinType.LEFT);
		dentist.fetch("person", JoinType.LEFT);
		dentist.fetch("clinic", JoinType.LEFT);

		Fetch<Object, Object> patient = appointment.fetch("patient", JoinType.LEFT);
		patient.fetch("person", JoinType.LEFT);
		patient.fetch("clinic", JoinType.LEFT);
	}
}