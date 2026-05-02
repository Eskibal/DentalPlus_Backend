package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.CalendarRuleDao;
import com.example.DentalPlus_Backend.dao.ClinicDao;
import com.example.DentalPlus_Backend.dao.PersonDao;
import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.dao.UserDao;
import com.example.DentalPlus_Backend.model.CalendarRule;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.Person;
import com.example.DentalPlus_Backend.model.Receptionist;
import com.example.DentalPlus_Backend.model.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReceptionistService {

	private final ReceptionistDao receptionistDao;
	private final UserDao userDao;
	private final PersonDao personDao;
	private final ClinicDao clinicDao;
	private final CalendarRuleDao calendarRuleDao;

	public ReceptionistService(ReceptionistDao receptionistDao, UserDao userDao, PersonDao personDao,
			ClinicDao clinicDao, CalendarRuleDao calendarRuleDao) {
		this.receptionistDao = receptionistDao;
		this.userDao = userDao;
		this.personDao = personDao;
		this.clinicDao = clinicDao;
		this.calendarRuleDao = calendarRuleDao;
	}

	public Receptionist findById(Long receptionistId) {
		Receptionist receptionist = receptionistDao.findById(receptionistId);

		if (receptionist == null) {
			throw new IllegalArgumentException("Receptionist not found");
		}

		return receptionist;
	}

	public Receptionist findByUserId(Long userId) {
		Receptionist receptionist = receptionistDao.findByUserId(userId);

		if (receptionist == null) {
			throw new IllegalArgumentException("Receptionist not found");
		}

		return receptionist;
	}

	public Receptionist findActiveByUserId(Long userId) {
		Receptionist receptionist = findByUserId(userId);

		if (!receptionist.getActive()) {
			throw new IllegalArgumentException("Inactive receptionist");
		}

		return receptionist;
	}

	public List<Receptionist> findByClinicId(Long clinicId) {
		return receptionistDao.findByClinicId(clinicId);
	}

	public List<Receptionist> findActiveByClinicId(Long clinicId) {
		return receptionistDao.findActiveByClinicId(clinicId);
	}

	public boolean isActiveReceptionist(Long userId) {
		Receptionist receptionist = receptionistDao.findByUserId(userId);
		return receptionist != null && receptionist.getActive();
	}

	public boolean hasClinicAccess(Long userId, Long clinicId) {
		Receptionist receptionist = receptionistDao.findByUserId(userId);

		return receptionist != null && receptionist.getActive() && receptionist.getClinic() != null
				&& receptionist.getClinic().getId().equals(clinicId);
	}

	public void validateClinicAccess(Long userId, Long clinicId) {
		if (!hasClinicAccess(userId, clinicId)) {
			throw new IllegalArgumentException("Receptionist has no access to this clinic");
		}
	}

	@Transactional
	public Receptionist createReceptionistRole(Long personId, Long userId, Long clinicId, Long calendarRuleId,
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

		if (!Receptionist.isNotesValid(notes)) {
			throw new IllegalArgumentException("Invalid notes");
		}

		Receptionist existingByUser = receptionistDao.findByUserId(userId);
		if (existingByUser != null) {
			throw new IllegalArgumentException("This user is already linked to a receptionist role");
		}

		Receptionist existingByPerson = receptionistDao.findByPersonId(personId);
		if (existingByPerson != null) {
			throw new IllegalArgumentException("This person is already linked to a receptionist role");
		}

		Receptionist receptionist = new Receptionist(person, user, clinic, calendarRule, active, notes);

		receptionistDao.save(receptionist);

		return receptionist;
	}

	@Transactional
	public Receptionist updateReceptionistRole(Long receptionistId, Long clinicId, Long calendarRuleId, Boolean active,
			String notes) {
		Receptionist receptionist = findById(receptionistId);

		if (clinicId != null) {
			Clinic clinic = clinicDao.findById(clinicId);
			if (clinic == null) {
				throw new IllegalArgumentException("Clinic not found");
			}
			receptionist.setClinic(clinic);
		}

		if (calendarRuleId != null) {
			CalendarRule calendarRule = calendarRuleDao.findById(calendarRuleId);
			if (calendarRule == null) {
				throw new IllegalArgumentException("CalendarRule not found");
			}
			receptionist.setCalendarRule(calendarRule);
		}

		if (active != null) {
			receptionist.setActive(active);
		}

		if (notes != null) {
			if (!Receptionist.isNotesValid(notes)) {
				throw new IllegalArgumentException("Invalid notes");
			}
			receptionist.setNotes(notes);
		}

		return receptionistDao.update(receptionist);
	}

	@Transactional
	public Receptionist removeReceptionistCalendarRule(Long receptionistId) {
		Receptionist receptionist = findById(receptionistId);
		receptionist.setCalendarRule(null);

		return receptionistDao.update(receptionist);
	}

	@Transactional
	public Receptionist deactivateReceptionistRole(Long receptionistId) {
		Receptionist receptionist = findById(receptionistId);
		receptionist.setActive(false);

		return receptionistDao.update(receptionist);
	}
}