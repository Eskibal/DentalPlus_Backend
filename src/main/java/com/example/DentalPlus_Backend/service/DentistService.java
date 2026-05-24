package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.CalendarRuleDao;
import com.example.DentalPlus_Backend.dao.ClinicDao;
import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.dao.PersonDao;
import com.example.DentalPlus_Backend.dao.SpecialityDao;
import com.example.DentalPlus_Backend.dao.UserDao;
import com.example.DentalPlus_Backend.model.CalendarRule;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Person;
import com.example.DentalPlus_Backend.model.Speciality;
import com.example.DentalPlus_Backend.model.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DentistService {

	private final DentistDao dentistDao;
	private final UserDao userDao;
	private final PersonDao personDao;
	private final ClinicDao clinicDao;
	private final CalendarRuleDao calendarRuleDao;
	private final SpecialityDao specialityDao;

	public DentistService(DentistDao dentistDao, UserDao userDao, PersonDao personDao, ClinicDao clinicDao,
			CalendarRuleDao calendarRuleDao, SpecialityDao specialityDao) {
		this.dentistDao = dentistDao;
		this.userDao = userDao;
		this.personDao = personDao;
		this.clinicDao = clinicDao;
		this.calendarRuleDao = calendarRuleDao;
		this.specialityDao = specialityDao;
	}

	public Dentist findById(Long dentistId) {
		Dentist dentist = dentistDao.findById(dentistId);

		if (dentist == null) {
			throw new IllegalArgumentException("Dentist not found");
		}

		return dentist;
	}

	public Dentist findByUserId(Long userId) {
		Dentist dentist = dentistDao.findByUserId(userId);

		if (dentist == null) {
			throw new IllegalArgumentException("Dentist not found");
		}

		return dentist;
	}

	public Dentist findActiveByUserId(Long userId) {
		Dentist dentist = findByUserId(userId);

		if (!dentist.getActive()) {
			throw new IllegalArgumentException("Inactive dentist");
		}

		return dentist;
	}

	public List<Dentist> findByClinicId(Long clinicId) {
		return dentistDao.findByClinicId(clinicId);
	}

	public List<Dentist> findActiveByClinicId(Long clinicId) {
		return dentistDao.findActiveByClinicId(clinicId);
	}

	public boolean isActiveDentist(Long userId) {
		Dentist dentist = dentistDao.findByUserId(userId);
		return dentist != null && dentist.getActive();
	}

	public boolean hasClinicAccess(Long userId, Long clinicId) {
		Dentist dentist = dentistDao.findByUserId(userId);

		return dentist != null && dentist.getActive() && dentist.getClinic() != null
				&& dentist.getClinic().getId().equals(clinicId);
	}

	public void validateClinicAccess(Long userId, Long clinicId) {
		if (!hasClinicAccess(userId, clinicId)) {
			throw new IllegalArgumentException("Dentist has no access to this clinic");
		}
	}

	@Transactional
	public Dentist createDentistRole(Long personId, Long userId, Long clinicId, Long calendarRuleId, String speciality,
			Boolean active, String notes) {
		Person person = personDao.findById(personId);
		if (person == null) {
			throw new IllegalArgumentException("Person not found");
		}

		User user = userDao.findById(userId);
		if (user == null) {
			throw new IllegalArgumentException("User not found");
		}

		Clinic clinic = clinicDao.findById(clinicId);
		if (clinic == null) {
			throw new IllegalArgumentException("Clinic not found");
		}

		CalendarRule calendarRule = null;
		if (calendarRuleId != null) {
			calendarRule = calendarRuleDao.findById(calendarRuleId);
			if (calendarRule == null) {
				throw new IllegalArgumentException("CalendarRule not found");
			}
		}

		Speciality specialityEntity = findSpecialityByNameOrThrow(speciality);

		if (!Dentist.isSpecialityValid(specialityEntity)) {
			throw new IllegalArgumentException("Invalid speciality");
		}

		if (!Dentist.isNotesValid(notes)) {
			throw new IllegalArgumentException("Invalid notes");
		}

		Dentist existingByUser = dentistDao.findByUserId(userId);
		if (existingByUser != null) {
			throw new IllegalArgumentException("This user is already linked to a dentist role");
		}

		Dentist existingByPerson = dentistDao.findByPersonId(personId);
		if (existingByPerson != null) {
			throw new IllegalArgumentException("This person is already linked to a dentist role");
		}

		Dentist dentist = new Dentist(person, user, clinic, calendarRule, specialityEntity, active, notes);

		dentistDao.save(dentist);

		return dentist;
	}

	@Transactional
	public Dentist updateDentistRole(Long dentistId, Long clinicId, Long calendarRuleId, String speciality,
			Boolean active, String notes) {
		Dentist dentist = findById(dentistId);

		if (clinicId != null) {
			Clinic clinic = clinicDao.findById(clinicId);
			if (clinic == null) {
				throw new IllegalArgumentException("Clinic not found");
			}
			dentist.setClinic(clinic);
		}

		if (calendarRuleId != null) {
			CalendarRule calendarRule = calendarRuleDao.findById(calendarRuleId);
			if (calendarRule == null) {
				throw new IllegalArgumentException("CalendarRule not found");
			}
			dentist.setCalendarRule(calendarRule);
		}

		if (speciality != null) {
			Speciality specialityEntity = findSpecialityByNameOrThrow(speciality);

			if (!Dentist.isSpecialityValid(specialityEntity)) {
				throw new IllegalArgumentException("Invalid speciality");
			}

			dentist.setSpeciality(specialityEntity);
		}

		if (active != null) {
			dentist.setActive(active);
		}

		if (notes != null) {
			if (!Dentist.isNotesValid(notes)) {
				throw new IllegalArgumentException("Invalid notes");
			}
			dentist.setNotes(notes);
		}

		return dentistDao.update(dentist);
	}

	@Transactional
	public Dentist removeDentistCalendarRule(Long dentistId) {
		Dentist dentist = findById(dentistId);
		dentist.setCalendarRule(null);

		return dentistDao.update(dentist);
	}

	@Transactional
	public Dentist deactivateDentistRole(Long dentistId) {
		Dentist dentist = findById(dentistId);
		dentist.setActive(false);

		return dentistDao.update(dentist);
	}

	private Speciality findSpecialityByNameOrThrow(String specialityName) {
		if (specialityName == null || specialityName.isBlank()) {
			throw new IllegalArgumentException("speciality is required");
		}

		Speciality speciality = specialityDao.findByName(specialityName);

		if (speciality == null || !Boolean.TRUE.equals(speciality.getActive())) {
			throw new IllegalArgumentException("Speciality not found");
		}

		return speciality;
	}
}