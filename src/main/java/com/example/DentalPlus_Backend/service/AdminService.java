package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.dao.ClinicDao;
import com.example.DentalPlus_Backend.dao.PersonDao;
import com.example.DentalPlus_Backend.dao.UserDao;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.Person;
import com.example.DentalPlus_Backend.model.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

	private final AdminDao adminDao;
	private final UserDao userDao;
	private final PersonDao personDao;
	private final ClinicDao clinicDao;

	public AdminService(AdminDao adminDao, UserDao userDao, PersonDao personDao, ClinicDao clinicDao) {
		this.adminDao = adminDao;
		this.userDao = userDao;
		this.personDao = personDao;
		this.clinicDao = clinicDao;
	}

	public Admin findById(Long adminId) {
		Admin admin = adminDao.findById(adminId);

		if (admin == null) {
			throw new IllegalArgumentException("Admin not found");
		}

		return admin;
	}

	public Admin findByUserId(Long userId) {
		Admin admin = adminDao.findByUserId(userId);

		if (admin == null) {
			throw new IllegalArgumentException("Admin not found");
		}

		return admin;
	}

	public Admin findActiveByUserId(Long userId) {
		Admin admin = findByUserId(userId);

		if (!admin.getActive()) {
			throw new IllegalArgumentException("Inactive admin");
		}

		return admin;
	}

	public List<Admin> findByClinicId(Long clinicId) {
		return adminDao.findByClinicId(clinicId);
	}

	public List<Admin> findActiveByClinicId(Long clinicId) {
		return adminDao.findActiveByClinicId(clinicId);
	}

	public boolean isActiveAdmin(Long userId) {
		Admin admin = adminDao.findByUserId(userId);
		return admin != null && admin.getActive();
	}

	public boolean hasClinicAccess(Long userId, Long clinicId) {
		Admin admin = adminDao.findByUserId(userId);

		return admin != null && admin.getActive() && admin.getClinic() != null
				&& admin.getClinic().getId().equals(clinicId);
	}

	public void validateClinicAccess(Long userId, Long clinicId) {
		if (!hasClinicAccess(userId, clinicId)) {
			throw new IllegalArgumentException("Admin has no access to this clinic");
		}
	}

	@Transactional
	public Admin createAdminRole(Long personId, Long userId, Long clinicId, Boolean active, String notes) {
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

		if (!Admin.isNotesValid(notes)) {
			throw new IllegalArgumentException("Invalid notes");
		}

		Admin existingByUser = adminDao.findByUserId(userId);
		if (existingByUser != null) {
			throw new IllegalArgumentException("This user is already linked to an admin role");
		}

		Admin existingByPerson = adminDao.findByPersonId(personId);
		if (existingByPerson != null) {
			throw new IllegalArgumentException("This person is already linked to an admin role");
		}

		Admin admin = new Admin(person, user, clinic, active, notes);

		adminDao.save(admin);

		return admin;
	}

	@Transactional
	public Admin updateAdminRole(Long adminId, Long clinicId, Boolean active, String notes) {
		Admin admin = findById(adminId);

		if (clinicId != null) {
			Clinic clinic = clinicDao.findById(clinicId);
			if (clinic == null) {
				throw new IllegalArgumentException("Clinic not found");
			}
			admin.setClinic(clinic);
		}

		if (active != null) {
			admin.setActive(active);
		}

		if (notes != null) {
			if (!Admin.isNotesValid(notes)) {
				throw new IllegalArgumentException("Invalid notes");
			}
			admin.setNotes(notes);
		}

		return adminDao.update(admin);
	}

	@Transactional
	public Admin deactivateAdminRole(Long adminId) {
		Admin admin = findById(adminId);
		admin.setActive(false);

		return adminDao.update(admin);
	}
}