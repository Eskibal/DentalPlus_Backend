package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.AppointmentDao;
import com.example.DentalPlus_Backend.dao.CalendarBreakDao;
import com.example.DentalPlus_Backend.dao.CalendarExceptionDao;
import com.example.DentalPlus_Backend.dao.CalendarHolidayDao;
import com.example.DentalPlus_Backend.dao.CalendarRuleDao;
import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.dao.PatientDao;
import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.dto.WeeklyCalendarDto;
import com.example.DentalPlus_Backend.model.Box;
import com.example.DentalPlus_Backend.model.CalendarBreak;
import com.example.DentalPlus_Backend.model.CalendarException;
import com.example.DentalPlus_Backend.model.CalendarHoliday;
import com.example.DentalPlus_Backend.model.CalendarRule;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.Receptionist;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class CalendarService {

	private final CalendarRuleDao calendarRuleDao;
	private final CalendarBreakDao calendarBreakDao;
	private final CalendarHolidayDao calendarHolidayDao;
	private final CalendarExceptionDao calendarExceptionDao;
	private final AppointmentDao appointmentDao;
	private final DentistDao dentistDao;
	private final ReceptionistDao receptionistDao;
	private final PatientDao patientDao;

	public CalendarService(CalendarRuleDao calendarRuleDao, CalendarBreakDao calendarBreakDao,
			CalendarHolidayDao calendarHolidayDao, CalendarExceptionDao calendarExceptionDao,
			AppointmentDao appointmentDao, DentistDao dentistDao, ReceptionistDao receptionistDao,
			PatientDao patientDao) {
		this.calendarRuleDao = calendarRuleDao;
		this.calendarBreakDao = calendarBreakDao;
		this.calendarHolidayDao = calendarHolidayDao;
		this.calendarExceptionDao = calendarExceptionDao;
		this.appointmentDao = appointmentDao;
		this.dentistDao = dentistDao;
		this.receptionistDao = receptionistDao;
		this.patientDao = patientDao;
	}

	public WeeklyCalendarDto getEffectiveWeeklyCalendarForUser(Long userId) {
		CalendarRule calendarRule = resolveCalendarRuleForUser(userId);

		if (calendarRule == null) {
			return null;
		}

		return new WeeklyCalendarDto(calendarRule);
	}

	public CalendarRule resolveCalendarRuleForUser(Long userId) {
		Dentist dentist = dentistDao.findByUserId(userId);
		if (dentist != null && dentist.getActive()) {
			return resolveDentistCalendarRule(dentist);
		}

		Receptionist receptionist = receptionistDao.findByUserId(userId);
		if (receptionist != null && receptionist.getActive()) {
			return resolveReceptionistCalendarRule(receptionist);
		}

		Patient patient = patientDao.findByUserId(userId);
		if (patient != null && patient.getActive() && patient.getClinic() != null) {
			return resolveClinicCalendarRule(patient.getClinic());
		}

		return null;
	}

	public CalendarRule resolveClinicCalendarRule(Clinic clinic) {
		if (clinic == null) {
			return null;
		}

		if (clinic.getCalendarRule() != null) {
			return clinic.getCalendarRule();
		}

		return calendarRuleDao.findByClinicId(clinic.getId());
	}

	public CalendarRule resolveDentistCalendarRule(Dentist dentist) {
		if (dentist == null) {
			return null;
		}

		if (dentist.getCalendarRule() != null) {
			return dentist.getCalendarRule();
		}

		CalendarRule dentistRule = calendarRuleDao.findByDentistId(dentist.getId());
		if (dentistRule != null) {
			return dentistRule;
		}

		return resolveClinicCalendarRule(dentist.getClinic());
	}

	public CalendarRule resolveReceptionistCalendarRule(Receptionist receptionist) {
		if (receptionist == null) {
			return null;
		}

		if (receptionist.getCalendarRule() != null) {
			return receptionist.getCalendarRule();
		}

		CalendarRule receptionistRule = calendarRuleDao.findByReceptionistId(receptionist.getId());
		if (receptionistRule != null) {
			return receptionistRule;
		}

		return resolveClinicCalendarRule(receptionist.getClinic());
	}

	public CalendarRule resolveBoxCalendarRule(Box box) {
		if (box == null || box.getClinic() == null) {
			return null;
		}

		return resolveClinicCalendarRule(box.getClinic());
	}

	public void validateAppointmentAvailability(Dentist dentist, Box box, Patient patient, LocalDateTime startDateTime,
			LocalDateTime endDateTime, Long excludedAppointmentId) {
		validateAppointmentDateRange(startDateTime, endDateTime);
		validateSameClinic(dentist, box, patient);

		if (!isDentistAvailable(dentist, startDateTime, endDateTime, excludedAppointmentId)) {
			throw new IllegalArgumentException("Dentist is not available in that time range");
		}

		if (!isBoxAvailable(box, startDateTime, endDateTime, excludedAppointmentId)) {
			throw new IllegalArgumentException("Box is not available in that time range");
		}

		if (patient != null && appointmentDao.existsOverlappingPatientAppointment(patient.getId(), startDateTime,
				endDateTime, excludedAppointmentId)) {
			throw new IllegalArgumentException("Patient already has an appointment in that time range");
		}
	}

	public boolean isDentistAvailable(Dentist dentist, LocalDateTime startDateTime, LocalDateTime endDateTime,
			Long excludedAppointmentId) {
		if (dentist == null || !dentist.getActive()) {
			return false;
		}

		CalendarRule calendarRule = resolveDentistCalendarRule(dentist);
		if (!isRangeAvailableByCalendarRule(calendarRule, startDateTime, endDateTime)) {
			return false;
		}

		return !appointmentDao.existsOverlappingDentistAppointment(dentist.getId(), startDateTime, endDateTime,
				excludedAppointmentId);
	}

	public boolean isBoxAvailable(Box box, LocalDateTime startDateTime, LocalDateTime endDateTime,
			Long excludedAppointmentId) {
		if (box == null || !box.getActive()) {
			return false;
		}

		CalendarRule calendarRule = resolveBoxCalendarRule(box);
		if (!isRangeAvailableByCalendarRule(calendarRule, startDateTime, endDateTime)) {
			return false;
		}

		return !appointmentDao.existsOverlappingBoxAppointment(box.getId(), startDateTime, endDateTime,
				excludedAppointmentId);
	}

	public boolean isRangeAvailableByCalendarRule(CalendarRule calendarRule, LocalDateTime startDateTime,
			LocalDateTime endDateTime) {
		validateAppointmentDateRange(startDateTime, endDateTime);

		if (calendarRule == null || !calendarRule.getActive()) {
			return false;
		}

		if (!startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
			return false;
		}

		LocalDate date = startDateTime.toLocalDate();
		LocalTime startTime = startDateTime.toLocalTime();
		LocalTime endTime = endDateTime.toLocalTime();

		List<CalendarException> exceptions = calendarExceptionDao
				.findActiveByCalendarRuleIdAndDate(calendarRule.getId(), date);

		if (hasBlockingUnavailableException(exceptions, startTime, endTime)) {
			return false;
		}

		if (hasAvailableException(exceptions, startTime, endTime)) {
			return true;
		}

		if (hasSpecialHoursException(exceptions)) {
			return fitsInsideSpecialHours(exceptions, startTime, endTime)
					&& !overlapsAnyBreak(calendarRule, date, startTime, endTime);
		}

		if (isHoliday(calendarRule, date)) {
			return false;
		}

		TimeRange weeklyRange = getWeeklyTimeRange(calendarRule, date.getDayOfWeek());

		if (weeklyRange == null || !weeklyRange.contains(startTime, endTime)) {
			return false;
		}

		return !overlapsAnyBreak(calendarRule, date, startTime, endTime);
	}

	private boolean hasBlockingUnavailableException(List<CalendarException> exceptions, LocalTime startTime,
			LocalTime endTime) {
		return exceptions.stream().filter(exception -> "UNAVAILABLE".equals(exception.getExceptionType()))
				.anyMatch(exception -> exceptionBlocksRange(exception, startTime, endTime));
	}

	private boolean hasAvailableException(List<CalendarException> exceptions, LocalTime startTime, LocalTime endTime) {
		return exceptions.stream().filter(exception -> "AVAILABLE".equals(exception.getExceptionType()))
				.anyMatch(exception -> exceptionContainsRange(exception, startTime, endTime));
	}

	private boolean hasSpecialHoursException(List<CalendarException> exceptions) {
		return exceptions.stream().anyMatch(exception -> "SPECIAL_HOURS".equals(exception.getExceptionType()));
	}

	private boolean fitsInsideSpecialHours(List<CalendarException> exceptions, LocalTime startTime, LocalTime endTime) {
		return exceptions.stream().filter(exception -> "SPECIAL_HOURS".equals(exception.getExceptionType()))
				.anyMatch(exception -> exceptionContainsRange(exception, startTime, endTime));
	}

	private boolean isHoliday(CalendarRule calendarRule, LocalDate date) {
		List<CalendarHoliday> holidays = calendarHolidayDao.findActiveByCalendarRuleIdAndDate(calendarRule.getId(),
				date);

		return !holidays.isEmpty();
	}

	private boolean overlapsAnyBreak(CalendarRule calendarRule, LocalDate date, LocalTime startTime,
			LocalTime endTime) {
		String dayOfWeek = date.getDayOfWeek().name();

		List<CalendarBreak> breaks = calendarBreakDao.findActiveByCalendarRuleIdAndDayOfWeek(calendarRule.getId(),
				dayOfWeek);

		return breaks.stream().anyMatch(calendarBreak -> overlaps(startTime, endTime, calendarBreak.getBreakStartTime(),
				calendarBreak.getBreakEndTime()));
	}

	private boolean exceptionBlocksRange(CalendarException exception, LocalTime startTime, LocalTime endTime) {
		if (exception.getStartTime() == null && exception.getEndTime() == null) {
			return true;
		}

		if (exception.getStartTime() == null || exception.getEndTime() == null) {
			return true;
		}

		return overlaps(startTime, endTime, exception.getStartTime(), exception.getEndTime());
	}

	private boolean exceptionContainsRange(CalendarException exception, LocalTime startTime, LocalTime endTime) {
		if (exception.getStartTime() == null && exception.getEndTime() == null) {
			return true;
		}

		if (exception.getStartTime() == null || exception.getEndTime() == null) {
			return false;
		}

		return !startTime.isBefore(exception.getStartTime()) && !endTime.isAfter(exception.getEndTime());
	}

	private boolean overlaps(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
		if (startA == null || endA == null || startB == null || endB == null) {
			return false;
		}

		return startA.isBefore(endB) && endA.isAfter(startB);
	}

	private TimeRange getWeeklyTimeRange(CalendarRule calendarRule, DayOfWeek dayOfWeek) {
		return switch (dayOfWeek) {
		case MONDAY -> buildRange(calendarRule.getMondayStartTime(), calendarRule.getMondayEndTime());
		case TUESDAY -> buildRange(calendarRule.getTuesdayStartTime(), calendarRule.getTuesdayEndTime());
		case WEDNESDAY -> buildRange(calendarRule.getWednesdayStartTime(), calendarRule.getWednesdayEndTime());
		case THURSDAY -> buildRange(calendarRule.getThursdayStartTime(), calendarRule.getThursdayEndTime());
		case FRIDAY -> buildRange(calendarRule.getFridayStartTime(), calendarRule.getFridayEndTime());
		case SATURDAY -> buildRange(calendarRule.getSaturdayStartTime(), calendarRule.getSaturdayEndTime());
		case SUNDAY -> buildRange(calendarRule.getSundayStartTime(), calendarRule.getSundayEndTime());
		};
	}

	private TimeRange buildRange(LocalTime startTime, LocalTime endTime) {
		if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
			return null;
		}

		return new TimeRange(startTime, endTime);
	}

	private void validateAppointmentDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		if (startDateTime == null || endDateTime == null || !endDateTime.isAfter(startDateTime)) {
			throw new IllegalArgumentException("Invalid appointment date range");
		}
	}

	private void validateSameClinic(Dentist dentist, Box box, Patient patient) {
		if (dentist == null || dentist.getClinic() == null) {
			throw new IllegalArgumentException("Dentist clinic is required");
		}

		Long clinicId = dentist.getClinic().getId();

		if (box == null || box.getClinic() == null || !box.getClinic().getId().equals(clinicId)) {
			throw new IllegalArgumentException("Box does not belong to dentist clinic");
		}

		if (patient == null || patient.getClinic() == null || !patient.getClinic().getId().equals(clinicId)) {
			throw new IllegalArgumentException("Patient does not belong to dentist clinic");
		}
	}

	private record TimeRange(LocalTime startTime, LocalTime endTime) {
		private boolean contains(LocalTime requestedStart, LocalTime requestedEnd) {
			return !requestedStart.isBefore(startTime) && !requestedEnd.isAfter(endTime);
		}
	}
}