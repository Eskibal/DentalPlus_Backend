package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.dao.AppointmentDao;
import com.example.DentalPlus_Backend.dao.BoxDao;
import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.dao.PatientDao;
import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.dto.AppointmentDto;
import com.example.DentalPlus_Backend.dto.AvailabilityDto;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.Appointment;
import com.example.DentalPlus_Backend.model.Box;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.Receptionist;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

	private static final int DEFAULT_APPOINTMENT_DURATION_MINUTES = 30;

	private final AppointmentDao appointmentDao;
	private final BoxDao boxDao;
	private final DentistDao dentistDao;
	private final PatientDao patientDao;
	private final ReceptionistDao receptionistDao;
	private final AdminDao adminDao;
	private final CalendarService calendarService;

	public AppointmentService(AppointmentDao appointmentDao, BoxDao boxDao, DentistDao dentistDao,
			PatientDao patientDao, ReceptionistDao receptionistDao, AdminDao adminDao,
			CalendarService calendarService) {
		this.appointmentDao = appointmentDao;
		this.boxDao = boxDao;
		this.dentistDao = dentistDao;
		this.patientDao = patientDao;
		this.receptionistDao = receptionistDao;
		this.adminDao = adminDao;
		this.calendarService = calendarService;
	}

	public List<AppointmentDto> getAppointments(Long callerUserId, LocalDate date, Long patientId, Long dentistId,
			Long boxId) {
		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);

		LocalDate effectiveDate = date != null ? date : LocalDate.now();
		LocalDateTime startOfDay = effectiveDate.atStartOfDay();
		LocalDateTime startOfNextDay = effectiveDate.plusDays(1).atStartOfDay();

		return appointmentDao.findByClinicIdAndDateRangeWithFilters(clinic.getId(), startOfDay, startOfNextDay,
				patientId, dentistId, boxId).stream().map(AppointmentDto::new).toList();
	}

	public AppointmentDto getAppointmentById(Long appointmentId, Long callerUserId) {
		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);
		Appointment appointment = findAppointmentOrThrow(appointmentId);

		validateAppointmentBelongsToClinic(appointment, clinic);

		return new AppointmentDto(appointment);
	}

	@Transactional
	public AppointmentDto createAppointment(AppointmentDto request, Long callerUserId) {
		if (request == null) {
			throw new IllegalArgumentException("Request body is required");
		}

		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);

		Box box = findBoxOrThrow(request.getBoxId());
		Dentist dentist = findDentistOrThrow(request.getDentistId());
		Patient patient = findPatientOrThrow(request.getPatientId());

		validateAppointmentData(request);
		validateAppointmentReferencesBelongToClinic(box, dentist, patient, clinic);

		calendarService.validateAppointmentAvailability(dentist, box, patient, request.getStartDateTime(),
				request.getEndDateTime(), null);

		Appointment appointment = new Appointment(box, dentist, patient, request.getStartDateTime(),
				request.getEndDateTime(), request.getStatus(), request.getNotes(), request.getActive());

		appointmentDao.save(appointment);

		return new AppointmentDto(appointment);
	}

	@Transactional
	public AppointmentDto updateAppointment(Long appointmentId, AppointmentDto request, Long callerUserId) {
		if (request == null) {
			throw new IllegalArgumentException("Request body is required");
		}

		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);
		Appointment appointment = findAppointmentOrThrow(appointmentId);

		validateAppointmentBelongsToClinic(appointment, clinic);

		Box box = request.getBoxId() == null ? appointment.getBox() : findBoxOrThrow(request.getBoxId());

		Dentist dentist = request.getDentistId() == null ? appointment.getDentist()
				: findDentistOrThrow(request.getDentistId());

		Patient patient = request.getPatientId() == null ? appointment.getPatient()
				: findPatientOrThrow(request.getPatientId());

		LocalDateTime startDateTime = request.getStartDateTime() == null ? appointment.getStartDateTime()
				: request.getStartDateTime();

		LocalDateTime endDateTime = request.getEndDateTime() == null ? appointment.getEndDateTime()
				: request.getEndDateTime();

		validateDateRange(startDateTime, endDateTime);
		validateAppointmentReferencesBelongToClinic(box, dentist, patient, clinic);

		calendarService.validateAppointmentAvailability(dentist, box, patient, startDateTime, endDateTime,
				appointmentId);

		appointment.setBox(box);
		appointment.setDentist(dentist);
		appointment.setPatient(patient);
		appointment.setStartDateTime(startDateTime);
		appointment.setEndDateTime(endDateTime);

		if (request.getStatus() != null) {
			if (!Appointment.isStatusValid(request.getStatus())) {
				throw new IllegalArgumentException("Invalid status");
			}
			appointment.setStatus(request.getStatus());
		}

		if (request.getNotes() != null) {
			if (!Appointment.isNotesValid(request.getNotes())) {
				throw new IllegalArgumentException("Invalid notes");
			}
			appointment.setNotes(request.getNotes());
		}

		if (request.getActive() != null) {
			appointment.setActive(request.getActive());
		}

		appointmentDao.update(appointment);

		return new AppointmentDto(appointment);
	}

	@Transactional
	public void deleteAppointment(Long appointmentId, Long callerUserId) {
		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);
		Appointment appointment = findAppointmentOrThrow(appointmentId);

		validateAppointmentBelongsToClinic(appointment, clinic);

		appointmentDao.delete(appointment);
	}

	public AvailabilityDto getAvailability(Long callerUserId, LocalDate date, LocalTime time) {
		if (date == null) {
			throw new IllegalArgumentException("date is required");
		}

		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);

		List<Dentist> activeDentists = dentistDao.findActiveByClinicId(clinic.getId());
		List<Box> activeBoxes = boxDao.findActiveByClinicId(clinic.getId());

		if (time == null) {
			return new AvailabilityDto(activeDentists.stream().map(AvailabilityDto.AvailableDentistDto::new).toList(),
					activeBoxes.stream().map(AvailabilityDto.AvailableBoxDto::new).toList());
		}

		LocalDateTime startDateTime = LocalDateTime.of(date, time);
		LocalDateTime endDateTime = startDateTime.plusMinutes(DEFAULT_APPOINTMENT_DURATION_MINUTES);

		return new AvailabilityDto(
				activeDentists.stream()
						.filter(dentist -> calendarService.isDentistAvailable(dentist, startDateTime, endDateTime,
								null))
						.map(AvailabilityDto.AvailableDentistDto::new).toList(),
				activeBoxes.stream()
						.filter(box -> calendarService.isBoxAvailable(box, startDateTime, endDateTime, null))
						.map(AvailabilityDto.AvailableBoxDto::new).toList());
	}

	private Appointment findAppointmentOrThrow(Long appointmentId) {
		Appointment appointment = appointmentDao.findById(appointmentId);

		if (appointment == null) {
			throw new IllegalArgumentException("Appointment not found");
		}

		return appointment;
	}

	private Box findBoxOrThrow(Long boxId) {
		if (boxId == null) {
			throw new IllegalArgumentException("boxId is required");
		}

		Box box = boxDao.findById(boxId);

		if (box == null) {
			throw new IllegalArgumentException("Box not found");
		}

		return box;
	}

	private Dentist findDentistOrThrow(Long dentistId) {
		if (dentistId == null) {
			throw new IllegalArgumentException("dentistId is required");
		}

		Dentist dentist = dentistDao.findById(dentistId);

		if (dentist == null) {
			throw new IllegalArgumentException("Dentist not found");
		}

		return dentist;
	}

	private Patient findPatientOrThrow(Long patientId) {
		if (patientId == null) {
			throw new IllegalArgumentException("patientId is required");
		}

		Patient patient = patientDao.findById(patientId);

		if (patient == null) {
			throw new IllegalArgumentException("Patient not found");
		}

		return patient;
	}

	private Clinic resolveCallerClinicOrThrow(Long callerUserId) {
		Admin admin = adminDao.findByUserId(callerUserId);
		if (admin != null && admin.getActive() && admin.getClinic() != null) {
			return admin.getClinic();
		}

		Receptionist receptionist = receptionistDao.findByUserId(callerUserId);
		if (receptionist != null && receptionist.getActive() && receptionist.getClinic() != null) {
			return receptionist.getClinic();
		}

		Dentist dentist = dentistDao.findByUserId(callerUserId);
		if (dentist != null && dentist.getActive() && dentist.getClinic() != null) {
			return dentist.getClinic();
		}

		throw new IllegalArgumentException("Caller has no clinic access");
	}

	private void validateAppointmentData(AppointmentDto request) {
		validateDateRange(request.getStartDateTime(), request.getEndDateTime());

		if (!Appointment.isStatusValid(request.getStatus())) {
			throw new IllegalArgumentException("Invalid status");
		}

		if (!Appointment.isNotesValid(request.getNotes())) {
			throw new IllegalArgumentException("Invalid notes");
		}
	}

	private void validateDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		if (!Appointment.isStartDateTimeValid(startDateTime)) {
			throw new IllegalArgumentException("Invalid startDateTime");
		}

		if (!Appointment.isEndDateTimeValid(endDateTime)) {
			throw new IllegalArgumentException("Invalid endDateTime");
		}

		if (!Appointment.isDateRangeValid(startDateTime, endDateTime)) {
			throw new IllegalArgumentException("Invalid date range");
		}
	}

	private void validateAppointmentReferencesBelongToClinic(Box box, Dentist dentist, Patient patient, Clinic clinic) {
		if (box.getClinic() == null || !box.getClinic().getId().equals(clinic.getId())) {
			throw new IllegalArgumentException("Box not found in caller clinic");
		}

		if (dentist.getClinic() == null || !dentist.getClinic().getId().equals(clinic.getId())) {
			throw new IllegalArgumentException("Dentist not found in caller clinic");
		}

		if (patient.getClinic() == null || !patient.getClinic().getId().equals(clinic.getId())) {
			throw new IllegalArgumentException("Patient not found in caller clinic");
		}
	}

	private void validateAppointmentBelongsToClinic(Appointment appointment, Clinic clinic) {
		if (appointment.getDentist() == null || appointment.getDentist().getClinic() == null
				|| !appointment.getDentist().getClinic().getId().equals(clinic.getId())) {
			throw new IllegalArgumentException("Appointment not found in caller clinic");
		}
	}
}