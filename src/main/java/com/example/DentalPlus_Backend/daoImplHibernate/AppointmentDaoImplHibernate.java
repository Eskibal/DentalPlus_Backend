package com.example.DentalPlus_Backend.daoImplHibernate;

import com.example.DentalPlus_Backend.dao.AppointmentDao;
import com.example.DentalPlus_Backend.model.Appointment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Profile("hibernate")
public class AppointmentDaoImplHibernate implements AppointmentDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Appointment findById(Long id) {
		return entityManager.find(Appointment.class, id);
	}

	@Override
	public List<Appointment> findByClinicIdAndDateRange(Long clinicId, LocalDateTime startDateTime,
			LocalDateTime endDateTime) {
		return findByClinicIdAndDateRangeWithFilters(clinicId, startDateTime, endDateTime, null, null, null);
	}

	@Override
	public List<Appointment> findByClinicIdAndDateRangeWithFilters(Long clinicId, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Long patientId, Long dentistId, Long boxId) {
		StringBuilder jpql = new StringBuilder("""
				FROM Appointment a
				WHERE a.dentist.clinic.id = :clinicId
				  AND a.startDateTime >= :startDateTime
				  AND a.startDateTime < :endDateTime
				""");

		if (patientId != null) {
			jpql.append(" AND a.patient.id = :patientId");
		}

		if (dentistId != null) {
			jpql.append(" AND a.dentist.id = :dentistId");
		}

		if (boxId != null) {
			jpql.append(" AND a.box.id = :boxId");
		}

		jpql.append(" ORDER BY a.startDateTime ASC");

		TypedQuery<Appointment> query = entityManager.createQuery(jpql.toString(), Appointment.class);

		query.setParameter("clinicId", clinicId);
		query.setParameter("startDateTime", startDateTime);
		query.setParameter("endDateTime", endDateTime);

		if (patientId != null) {
			query.setParameter("patientId", patientId);
		}

		if (dentistId != null) {
			query.setParameter("dentistId", dentistId);
		}

		if (boxId != null) {
			query.setParameter("boxId", boxId);
		}

		return query.getResultList();
	}

	@Override
	public List<Appointment> findActiveByDentistIdAndClinicId(Long dentistId, Long clinicId) {
		return entityManager.createQuery("""
				FROM Appointment a
				WHERE a.dentist.id = :dentistId
				  AND a.dentist.clinic.id = :clinicId
				  AND a.active = true
				  AND a.status <> 'CANCELLED'
				ORDER BY a.startDateTime ASC
				""", Appointment.class).setParameter("dentistId", dentistId).setParameter("clinicId", clinicId)
				.getResultList();
	}

	@Override
	public List<Appointment> findOverlappingAppointments(Long clinicId, LocalDateTime startDateTime,
			LocalDateTime endDateTime) {
		return entityManager.createQuery("""
				FROM Appointment a
				WHERE a.dentist.clinic.id = :clinicId
				  AND a.active = true
				  AND a.status <> 'CANCELLED'
				  AND a.startDateTime < :endDateTime
				  AND a.endDateTime > :startDateTime
				""", Appointment.class).setParameter("clinicId", clinicId).setParameter("startDateTime", startDateTime)
				.setParameter("endDateTime", endDateTime).getResultList();
	}

	@Override
	public boolean existsOverlappingDentistAppointment(Long dentistId, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Long excludedAppointmentId) {
		Long count = entityManager.createQuery("""
				SELECT COUNT(a)
				FROM Appointment a
				WHERE a.dentist.id = :dentistId
				  AND a.active = true
				  AND a.status <> 'CANCELLED'
				  AND a.startDateTime < :endDateTime
				  AND a.endDateTime > :startDateTime
				  AND (:excludedAppointmentId IS NULL OR a.id <> :excludedAppointmentId)
				""", Long.class).setParameter("dentistId", dentistId).setParameter("startDateTime", startDateTime)
				.setParameter("endDateTime", endDateTime).setParameter("excludedAppointmentId", excludedAppointmentId)
				.getSingleResult();

		return count != null && count > 0;
	}

	@Override
	public boolean existsOverlappingBoxAppointment(Long boxId, LocalDateTime startDateTime, LocalDateTime endDateTime,
			Long excludedAppointmentId) {
		Long count = entityManager.createQuery("""
				SELECT COUNT(a)
				FROM Appointment a
				WHERE a.box.id = :boxId
				  AND a.active = true
				  AND a.status <> 'CANCELLED'
				  AND a.startDateTime < :endDateTime
				  AND a.endDateTime > :startDateTime
				  AND (:excludedAppointmentId IS NULL OR a.id <> :excludedAppointmentId)
				""", Long.class).setParameter("boxId", boxId).setParameter("startDateTime", startDateTime)
				.setParameter("endDateTime", endDateTime).setParameter("excludedAppointmentId", excludedAppointmentId)
				.getSingleResult();

		return count != null && count > 0;
	}

	@Override
	public boolean existsOverlappingPatientAppointment(Long patientId, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Long excludedAppointmentId) {
		Long count = entityManager.createQuery("""
				SELECT COUNT(a)
				FROM Appointment a
				WHERE a.patient.id = :patientId
				  AND a.active = true
				  AND a.status <> 'CANCELLED'
				  AND a.startDateTime < :endDateTime
				  AND a.endDateTime > :startDateTime
				  AND (:excludedAppointmentId IS NULL OR a.id <> :excludedAppointmentId)
				""", Long.class).setParameter("patientId", patientId).setParameter("startDateTime", startDateTime)
				.setParameter("endDateTime", endDateTime).setParameter("excludedAppointmentId", excludedAppointmentId)
				.getSingleResult();

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
}