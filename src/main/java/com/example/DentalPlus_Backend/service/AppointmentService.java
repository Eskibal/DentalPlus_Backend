package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.dao.AppointmentDao;
import com.example.DentalPlus_Backend.dao.BoxDao;
import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.dao.PatientDao;
import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.dao.TreatmentDao;
import com.example.DentalPlus_Backend.dto.AppointmentDto;
import com.example.DentalPlus_Backend.dto.AvailabilityDto;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.Appointment;
import com.example.DentalPlus_Backend.model.Box;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.Receptionist;
import com.example.DentalPlus_Backend.model.Treatment;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AppointmentService {

	private static final int DEFAULT_APPOINTMENT_DURATION_MINUTES = Treatment.DEFAULT_ESTIMATED_DURATION_MINUTES;
	private static final int DEFAULT_BEFORE_MARGIN_MINUTES = Treatment.DEFAULT_BEFORE_MARGIN_MINUTES;
	private static final int DEFAULT_AFTER_MARGIN_MINUTES = Treatment.DEFAULT_AFTER_MARGIN_MINUTES;
	private static final int AVAILABILITY_STEP_MINUTES = 5;

	private static final String SUGGESTION_REQUESTED_TIME = "REQUESTED_TIME";
	private static final String SUGGESTION_AFTER_PATIENT_APPOINTMENT = "AFTER_PATIENT_APPOINTMENT";
	private static final String SUGGESTION_BEFORE_PATIENT_APPOINTMENT = "BEFORE_PATIENT_APPOINTMENT";
	private static final String SUGGESTION_END_OF_DAY_MEDICAL_ALERT = "END_OF_DAY_MEDICAL_ALERT";
	private static final String SUGGESTION_START_OF_DAY = "START_OF_DAY";
	private static final String SUGGESTION_NONE = "NONE";

	private final AppointmentDao appointmentDao;
	private final BoxDao boxDao;
	private final DentistDao dentistDao;
	private final PatientDao patientDao;
	private final ReceptionistDao receptionistDao;
	private final AdminDao adminDao;
	private final TreatmentDao treatmentDao;
	private final CalendarService calendarService;

	public AppointmentService(AppointmentDao appointmentDao, BoxDao boxDao, DentistDao dentistDao,
			PatientDao patientDao, ReceptionistDao receptionistDao, AdminDao adminDao, TreatmentDao treatmentDao,
			CalendarService calendarService) {
		this.appointmentDao = appointmentDao;
		this.boxDao = boxDao;
		this.dentistDao = dentistDao;
		this.patientDao = patientDao;
		this.receptionistDao = receptionistDao;
		this.adminDao = adminDao;
		this.treatmentDao = treatmentDao;
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
		Treatment treatment = findTreatmentOrThrow(request.getTreatmentId());
		Dentist dentist = findDentistOrThrow(request.getDentistId());
		Patient patient = findPatientOrThrow(request.getPatientId());

		validateAppointmentData(request);
		validateAppointmentReferencesBelongToClinic(box, dentist, patient, clinic);
		validateDentistCanPerformTreatment(dentist, treatment, clinic);

		LocalDateTime bufferedStartDateTime = request.getStartDateTime()
				.minusMinutes(resolveBeforeMarginMinutes(treatment));
		LocalDateTime bufferedEndDateTime = request.getEndDateTime().plusMinutes(resolveAfterMarginMinutes(treatment));

		calendarService.validateAppointmentAvailability(dentist, box, patient, bufferedStartDateTime,
				bufferedEndDateTime, null);

		Appointment appointment = new Appointment(box, treatment, dentist, patient, request.getStartDateTime(),
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

		Treatment treatment = request.getTreatmentId() == null ? appointment.getTreatment()
				: findTreatmentOrThrow(request.getTreatmentId());

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

		if (treatment == null) {
			throw new IllegalArgumentException("treatmentId is required");
		}

		validateDentistCanPerformTreatment(dentist, treatment, clinic);

		LocalDateTime bufferedStartDateTime = startDateTime.minusMinutes(resolveBeforeMarginMinutes(treatment));
		LocalDateTime bufferedEndDateTime = endDateTime.plusMinutes(resolveAfterMarginMinutes(treatment));

		calendarService.validateAppointmentAvailability(dentist, box, patient, bufferedStartDateTime,
				bufferedEndDateTime, appointmentId);

		appointment.setBox(box);
		appointment.setTreatment(treatment);
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

	public AvailabilityDto getAvailability(Long callerUserId, AvailabilityDto request) {
		if (request == null) {
			throw new IllegalArgumentException("Request body is required");
		}

		if (request.getDate() == null) {
			throw new IllegalArgumentException("date is required");
		}

		if (request.hasIncompleteTimeRange()) {
			throw new IllegalArgumentException("startTime and endTime must be sent together");
		}

		if (request.hasRequestedTimeRange() && !request.getEndTime().isAfter(request.getStartTime())) {
			throw new IllegalArgumentException("Invalid time range");
		}

		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);
		Treatment treatment = request.getTreatmentId() == null ? null : findTreatmentOrThrow(request.getTreatmentId());
		Patient patient = request.getPatientId() == null ? null : findPatientOrThrow(request.getPatientId());

		if (patient != null && (patient.getClinic() == null || !patient.getClinic().getId().equals(clinic.getId()))) {
			throw new IllegalArgumentException("Patient not found in caller clinic");
		}

		int durationMinutes = resolveRequestedDurationMinutes(request, treatment);
		int beforeMarginMinutes = resolveBeforeMarginMinutes(treatment);
		int afterMarginMinutes = resolveAfterMarginMinutes(treatment);

		List<Dentist> activeDentists = request.getTreatmentId() == null
				? dentistDao.findActiveByClinicId(clinic.getId())
				: dentistDao.findActiveByClinicIdAndTreatmentId(clinic.getId(), request.getTreatmentId());

		List<Box> activeBoxes = boxDao.findActiveByClinicId(clinic.getId());

		List<AvailabilityDto.AvailableDentistDto> availableDentists = activeDentists.stream()
				.map(dentist -> new AvailabilityDto.AvailableDentistDto(dentist,
						buildDentistAvailableRanges(dentist, request.getDate())))
				.filter(dto -> !dto.getAvailableRanges().isEmpty())
				.filter(dto -> request.hasRequestedTimeRange()
						? rangeListCanFitRequestedTime(dto.getAvailableRanges(), request.getStartTime(),
								request.getEndTime(), beforeMarginMinutes, afterMarginMinutes)
						: rangeListCanFitDuration(dto.getAvailableRanges(), durationMinutes, beforeMarginMinutes,
								afterMarginMinutes))
				.toList();

		List<AvailabilityDto.AvailableBoxDto> availableBoxes = activeBoxes.stream()
				.map(box -> new AvailabilityDto.AvailableBoxDto(box, buildBoxAvailableRanges(box, request.getDate())))
				.filter(dto -> !dto.getAvailableRanges().isEmpty())
				.filter(dto -> request.hasRequestedTimeRange()
						? rangeListCanFitRequestedTime(dto.getAvailableRanges(), request.getStartTime(),
								request.getEndTime(), beforeMarginMinutes, afterMarginMinutes)
						: rangeListCanFitDuration(dto.getAvailableRanges(), durationMinutes, beforeMarginMinutes,
								afterMarginMinutes))
				.toList();

		AvailabilityDto.AvailabilitySuggestionDto suggestion = buildSuggestion(request, patient, activeDentists,
				activeBoxes, treatment, durationMinutes, beforeMarginMinutes, afterMarginMinutes);

		return new AvailabilityDto(availableDentists, availableBoxes, suggestion);
	}

	private AvailabilityDto.AvailabilitySuggestionDto buildSuggestion(AvailabilityDto request, Patient patient,
			List<Dentist> dentists, List<Box> boxes, Treatment treatment, int durationMinutes, int beforeMarginMinutes,
			int afterMarginMinutes) {
		if (dentists == null || dentists.isEmpty() || boxes == null || boxes.isEmpty()) {
			return new AvailabilityDto.AvailabilitySuggestionDto(null, null, SUGGESTION_NONE);
		}

		if (request.hasRequestedTimeRange()) {
			if (anyDentistAndBoxCanFit(dentists, boxes, request.getDate(), request.getStartTime(), request.getEndTime(),
					beforeMarginMinutes, afterMarginMinutes)) {
				return new AvailabilityDto.AvailabilitySuggestionDto(request.getStartTime(), request.getEndTime(),
						SUGGESTION_REQUESTED_TIME);
			}
		}

		if (patient != null) {
			AvailabilityDto.AvailabilitySuggestionDto patientAppointmentSuggestion = buildPatientAppointmentSuggestion(
					request, patient, dentists, boxes, durationMinutes, beforeMarginMinutes, afterMarginMinutes);

			if (patientAppointmentSuggestion != null) {
				return patientAppointmentSuggestion;
			}

			if (hasMedicalAlert(patient)) {
				AvailabilityDto.AvailabilitySuggestionDto endOfDaySuggestion = findLatestAvailableSuggestion(
						request.getDate(), dentists, boxes, durationMinutes, beforeMarginMinutes, afterMarginMinutes,
						SUGGESTION_END_OF_DAY_MEDICAL_ALERT);

				if (endOfDaySuggestion != null) {
					return endOfDaySuggestion;
				}
			}
		}

		AvailabilityDto.AvailabilitySuggestionDto startOfDaySuggestion = findEarliestAvailableSuggestion(
				request.getDate(), dentists, boxes, durationMinutes, beforeMarginMinutes, afterMarginMinutes,
				SUGGESTION_START_OF_DAY);

		return startOfDaySuggestion != null ? startOfDaySuggestion
				: new AvailabilityDto.AvailabilitySuggestionDto(null, null, SUGGESTION_NONE);
	}

	private AvailabilityDto.AvailabilitySuggestionDto buildPatientAppointmentSuggestion(AvailabilityDto request,
			Patient patient, List<Dentist> dentists, List<Box> boxes, int durationMinutes, int beforeMarginMinutes,
			int afterMarginMinutes) {
		LocalDateTime startOfDay = request.getDate().atStartOfDay();
		LocalDateTime startOfNextDay = request.getDate().plusDays(1).atStartOfDay();

		List<Appointment> patientAppointments = appointmentDao.findActiveByPatientIdAndDateRange(patient.getId(),
				startOfDay, startOfNextDay);

		if (patientAppointments.isEmpty()) {
			return null;
		}

		List<Appointment> orderedAppointments = patientAppointments.stream()
				.sorted(Comparator.comparing(Appointment::getStartDateTime)).toList();

		for (Appointment patientAppointment : orderedAppointments) {
			LocalTime suggestedStart = patientAppointment.getEndDateTime().toLocalTime()
					.plusMinutes(afterMarginMinutes);
			LocalTime suggestedEnd = suggestedStart.plusMinutes(durationMinutes);

			if (anyDentistAndBoxCanFit(dentists, boxes, request.getDate(), suggestedStart, suggestedEnd,
					beforeMarginMinutes, afterMarginMinutes)) {
				return new AvailabilityDto.AvailabilitySuggestionDto(suggestedStart, suggestedEnd,
						SUGGESTION_AFTER_PATIENT_APPOINTMENT);
			}
		}

		for (Appointment patientAppointment : orderedAppointments) {
			LocalTime suggestedEnd = patientAppointment.getStartDateTime().toLocalTime()
					.minusMinutes(beforeMarginMinutes);
			LocalTime suggestedStart = suggestedEnd.minusMinutes(durationMinutes);

			if (!suggestedStart.isBefore(LocalTime.MIN) && anyDentistAndBoxCanFit(dentists, boxes, request.getDate(),
					suggestedStart, suggestedEnd, beforeMarginMinutes, afterMarginMinutes)) {
				return new AvailabilityDto.AvailabilitySuggestionDto(suggestedStart, suggestedEnd,
						SUGGESTION_BEFORE_PATIENT_APPOINTMENT);
			}
		}

		return null;
	}

	private AvailabilityDto.AvailabilitySuggestionDto findEarliestAvailableSuggestion(LocalDate date,
			List<Dentist> dentists, List<Box> boxes, int durationMinutes, int beforeMarginMinutes,
			int afterMarginMinutes, String type) {
		LocalTime start = LocalTime.MIN;
		LocalTime latestStart = LocalTime.MAX.minusMinutes(durationMinutes);

		while (!start.isAfter(latestStart)) {
			LocalTime end = start.plusMinutes(durationMinutes);

			if (anyDentistAndBoxCanFit(dentists, boxes, date, start, end, beforeMarginMinutes, afterMarginMinutes)) {
				return new AvailabilityDto.AvailabilitySuggestionDto(start, end, type);
			}

			start = start.plusMinutes(AVAILABILITY_STEP_MINUTES);
		}

		return null;
	}

	private AvailabilityDto.AvailabilitySuggestionDto findLatestAvailableSuggestion(LocalDate date,
			List<Dentist> dentists, List<Box> boxes, int durationMinutes, int beforeMarginMinutes,
			int afterMarginMinutes, String type) {
		LocalTime start = LocalTime.MAX.minusMinutes(durationMinutes);
		LocalTime earliestStart = LocalTime.MIN;

		while (!start.isBefore(earliestStart)) {
			LocalTime end = start.plusMinutes(durationMinutes);

			if (anyDentistAndBoxCanFit(dentists, boxes, date, start, end, beforeMarginMinutes, afterMarginMinutes)) {
				return new AvailabilityDto.AvailabilitySuggestionDto(start, end, type);
			}

			start = start.minusMinutes(AVAILABILITY_STEP_MINUTES);
		}

		return null;
	}

	private boolean anyDentistAndBoxCanFit(List<Dentist> dentists, List<Box> boxes, LocalDate date, LocalTime startTime,
			LocalTime endTime, int beforeMarginMinutes, int afterMarginMinutes) {
		if (dentists == null || dentists.isEmpty() || boxes == null || boxes.isEmpty() || date == null
				|| startTime == null || endTime == null || !endTime.isAfter(startTime)) {
			return false;
		}

		for (Dentist dentist : dentists) {
			if (!isDentistAvailableWithMargins(dentist, date, startTime, endTime, beforeMarginMinutes,
					afterMarginMinutes)) {
				continue;
			}

			for (Box box : boxes) {
				if (isBoxAvailableWithMargins(box, date, startTime, endTime, beforeMarginMinutes,
						afterMarginMinutes)) {
					return true;
				}
			}
		}

		return false;
	}

	private List<AvailabilityDto.AvailableTimeRangeDto> buildDentistAvailableRanges(Dentist dentist, LocalDate date) {
		List<AvailabilityDto.AvailableTimeRangeDto> ranges = new ArrayList<>();

		LocalTime currentRangeStart = null;
		LocalTime previousAvailableEnd = null;

		LocalTime cursor = LocalTime.MIN;

		while (cursor.isBefore(LocalTime.MAX.minusMinutes(AVAILABILITY_STEP_MINUTES))) {
			LocalTime slotEnd = cursor.plusMinutes(AVAILABILITY_STEP_MINUTES);

			boolean available = calendarService.isDentistAvailable(dentist, LocalDateTime.of(date, cursor),
					LocalDateTime.of(date, slotEnd), null);

			if (available) {
				if (currentRangeStart == null) {
					currentRangeStart = cursor;
				}
				previousAvailableEnd = slotEnd;
			} else if (currentRangeStart != null) {
				ranges.add(new AvailabilityDto.AvailableTimeRangeDto(currentRangeStart, previousAvailableEnd));
				currentRangeStart = null;
				previousAvailableEnd = null;
			}

			cursor = slotEnd;
		}

		if (currentRangeStart != null && previousAvailableEnd != null) {
			ranges.add(new AvailabilityDto.AvailableTimeRangeDto(currentRangeStart, previousAvailableEnd));
		}

		return ranges;
	}

	private List<AvailabilityDto.AvailableTimeRangeDto> buildBoxAvailableRanges(Box box, LocalDate date) {
		List<AvailabilityDto.AvailableTimeRangeDto> ranges = new ArrayList<>();

		LocalTime currentRangeStart = null;
		LocalTime previousAvailableEnd = null;

		LocalTime cursor = LocalTime.MIN;

		while (cursor.isBefore(LocalTime.MAX.minusMinutes(AVAILABILITY_STEP_MINUTES))) {
			LocalTime slotEnd = cursor.plusMinutes(AVAILABILITY_STEP_MINUTES);

			boolean available = calendarService.isBoxAvailable(box, LocalDateTime.of(date, cursor),
					LocalDateTime.of(date, slotEnd), null);

			if (available) {
				if (currentRangeStart == null) {
					currentRangeStart = cursor;
				}
				previousAvailableEnd = slotEnd;
			} else if (currentRangeStart != null) {
				ranges.add(new AvailabilityDto.AvailableTimeRangeDto(currentRangeStart, previousAvailableEnd));
				currentRangeStart = null;
				previousAvailableEnd = null;
			}

			cursor = slotEnd;
		}

		if (currentRangeStart != null && previousAvailableEnd != null) {
			ranges.add(new AvailabilityDto.AvailableTimeRangeDto(currentRangeStart, previousAvailableEnd));
		}

		return ranges;
	}

	private boolean rangeListCanFitRequestedTime(List<AvailabilityDto.AvailableTimeRangeDto> ranges,
			LocalTime requestedStartTime, LocalTime requestedEndTime, int beforeMarginMinutes, int afterMarginMinutes) {
		if (ranges == null || ranges.isEmpty() || requestedStartTime == null || requestedEndTime == null
				|| !requestedEndTime.isAfter(requestedStartTime)) {
			return false;
		}

		LocalTime bufferedStartTime = safeMinusMinutes(requestedStartTime, beforeMarginMinutes);
		LocalTime bufferedEndTime = safePlusMinutes(requestedEndTime, afterMarginMinutes);

		return ranges.stream().anyMatch(range -> !bufferedStartTime.isBefore(range.getStartTime())
				&& !bufferedEndTime.isAfter(range.getEndTime()));
	}

	private boolean rangeListCanFitDuration(List<AvailabilityDto.AvailableTimeRangeDto> ranges, int durationMinutes,
			int beforeMarginMinutes, int afterMarginMinutes) {
		if (ranges == null || ranges.isEmpty()) {
			return false;
		}

		int requiredMinutes = durationMinutes + beforeMarginMinutes + afterMarginMinutes;

		return ranges.stream().anyMatch(range -> Duration.between(range.getStartTime(), range.getEndTime()).toMinutes()
				>= requiredMinutes);
	}

	private boolean isDentistAvailableWithMargins(Dentist dentist, LocalDate date, LocalTime startTime,
			LocalTime endTime, int beforeMarginMinutes, int afterMarginMinutes) {
		LocalDateTime bufferedStart = LocalDateTime.of(date, safeMinusMinutes(startTime, beforeMarginMinutes));
		LocalDateTime bufferedEnd = LocalDateTime.of(date, safePlusMinutes(endTime, afterMarginMinutes));

		return calendarService.isDentistAvailable(dentist, bufferedStart, bufferedEnd, null);
	}

	private boolean isBoxAvailableWithMargins(Box box, LocalDate date, LocalTime startTime, LocalTime endTime,
			int beforeMarginMinutes, int afterMarginMinutes) {
		LocalDateTime bufferedStart = LocalDateTime.of(date, safeMinusMinutes(startTime, beforeMarginMinutes));
		LocalDateTime bufferedEnd = LocalDateTime.of(date, safePlusMinutes(endTime, afterMarginMinutes));

		return calendarService.isBoxAvailable(box, bufferedStart, bufferedEnd, null);
	}

	private LocalTime safeMinusMinutes(LocalTime time, int minutes) {
		if (time == null) {
			return null;
		}

		if (minutes <= 0) {
			return time;
		}

		if (time.toSecondOfDay() < minutes * 60) {
			return LocalTime.MIN;
		}

		return time.minusMinutes(minutes);
	}

	private LocalTime safePlusMinutes(LocalTime time, int minutes) {
		if (time == null) {
			return null;
		}

		if (minutes <= 0) {
			return time;
		}

		int maxSecond = LocalTime.MAX.toSecondOfDay();
		int targetSecond = time.toSecondOfDay() + minutes * 60;

		if (targetSecond >= maxSecond) {
			return LocalTime.MAX;
		}

		return time.plusMinutes(minutes);
	}

	private int resolveRequestedDurationMinutes(AvailabilityDto request, Treatment treatment) {
		if (request != null && request.hasRequestedTimeRange()) {
			return (int) Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
		}

		return resolveEstimatedDurationMinutes(treatment);
	}

	private int resolveEstimatedDurationMinutes(Treatment treatment) {
		return treatment == null ? DEFAULT_APPOINTMENT_DURATION_MINUTES : treatment.resolveEstimatedDurationMinutes();
	}

	private int resolveBeforeMarginMinutes(Treatment treatment) {
		return treatment == null ? DEFAULT_BEFORE_MARGIN_MINUTES : treatment.resolveBeforeMarginMinutes();
	}

	private int resolveAfterMarginMinutes(Treatment treatment) {
		return treatment == null ? DEFAULT_AFTER_MARGIN_MINUTES : treatment.resolveAfterMarginMinutes();
	}

	private boolean hasMedicalAlert(Patient patient) {
		return patient != null && patient.getMedicalAlert() != null && !patient.getMedicalAlert().isBlank();
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

	private Treatment findTreatmentOrThrow(Long treatmentId) {
		if (treatmentId == null) {
			throw new IllegalArgumentException("treatmentId is required");
		}

		Treatment treatment = treatmentDao.findActiveById(treatmentId);

		if (treatment == null) {
			throw new IllegalArgumentException("Treatment not found");
		}

		return treatment;
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

		if (request.getTreatmentId() == null) {
			throw new IllegalArgumentException("treatmentId is required");
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

	private void validateDentistCanPerformTreatment(Dentist dentist, Treatment treatment, Clinic clinic) {
		if (dentist == null || treatment == null || clinic == null) {
			throw new IllegalArgumentException("Invalid dentist or treatment");
		}

		List<Dentist> dentistsForTreatment = dentistDao.findActiveByClinicIdAndTreatmentId(clinic.getId(),
				treatment.getId());

		boolean canPerformTreatment = dentistsForTreatment.stream()
				.anyMatch(availableDentist -> availableDentist.getId().equals(dentist.getId()));

		if (!canPerformTreatment) {
			throw new IllegalArgumentException("Dentist cannot perform selected treatment");
		}
	}
}