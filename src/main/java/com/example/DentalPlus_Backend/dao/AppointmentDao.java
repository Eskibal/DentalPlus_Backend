package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.Appointment;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentDao {
	Appointment findById(Long id);

	List<Appointment> findByClinicIdAndDateRange(Long clinicId, LocalDateTime startDateTime, LocalDateTime endDateTime);

	List<Appointment> findByClinicIdAndDateRangeWithFilters(Long clinicId, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Long patientId, Long dentistId, Long boxId);

	List<Appointment> findActiveByDentistIdAndClinicId(Long dentistId, Long clinicId);

	List<Appointment> findOverlappingAppointments(Long clinicId, LocalDateTime startDateTime,
			LocalDateTime endDateTime);

	boolean existsOverlappingDentistAppointment(Long dentistId, LocalDateTime startDateTime, LocalDateTime endDateTime,
			Long excludedAppointmentId);

	boolean existsOverlappingBoxAppointment(Long boxId, LocalDateTime startDateTime, LocalDateTime endDateTime,
			Long excludedAppointmentId);

	boolean existsOverlappingPatientAppointment(Long patientId, LocalDateTime startDateTime, LocalDateTime endDateTime,
			Long excludedAppointmentId);

	void save(Appointment appointment);

	Appointment update(Appointment appointment);

	void delete(Appointment appointment);
}