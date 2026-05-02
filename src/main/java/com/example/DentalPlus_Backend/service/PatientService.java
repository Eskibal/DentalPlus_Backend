package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.dao.AppointmentDao;
import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.dao.DocumentDao;
import com.example.DentalPlus_Backend.dao.PatientDao;
import com.example.DentalPlus_Backend.dao.PersonDao;
import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.dto.DocumentDto;
import com.example.DentalPlus_Backend.dto.PatientDto;
import com.example.DentalPlus_Backend.dto.PersonDto;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.Appointment;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.Person;
import com.example.DentalPlus_Backend.model.Receptionist;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {
	private final PatientDao patientDao;
	private final PersonDao personDao;
	private final DentistDao dentistDao;
	private final ReceptionistDao receptionistDao;
	private final AdminDao adminDao;
	private final AppointmentDao appointmentDao;
	private final DocumentDao documentDao;
	private final SupabaseStorageService supabaseStorageService;

	public PatientService(PatientDao patientDao, PersonDao personDao, DentistDao dentistDao,
			ReceptionistDao receptionistDao, AdminDao adminDao, AppointmentDao appointmentDao, DocumentDao documentDao,
			SupabaseStorageService supabaseStorageService) {
		this.patientDao = patientDao;
		this.personDao = personDao;
		this.dentistDao = dentistDao;
		this.receptionistDao = receptionistDao;
		this.adminDao = adminDao;
		this.appointmentDao = appointmentDao;
		this.documentDao = documentDao;
		this.supabaseStorageService = supabaseStorageService;
	}

	public List<PatientDto> getVisiblePatients(Long callerUserId, String search) {
		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);

		List<Patient> patients = patientDao.findByClinicIdWithSearch(clinic.getId(), search);
		Dentist dentist = dentistDao.findByUserId(callerUserId);

		if (dentist != null && dentist.getActive()) {
			patients = sortPatientsForDentistView(patients, dentist);
		}

		return patients.stream().map(PatientDto::new).toList();
	}

	public PatientDto getPatientById(Long patientId, Long callerUserId) {
		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);
		Patient patient = findPatientOrThrow(patientId);

		if (patient.getClinic() == null || !patient.getClinic().getId().equals(clinic.getId())) {
			throw new IllegalArgumentException("Patient not found in caller clinic");
		}

		List<DocumentDto> documents = documentDao.findActiveByPatientId(patient.getId()).stream()
				.map(this::buildDocumentDto).toList();

		return new PatientDto(patient, documents);
	}

	@Transactional
	public PatientDto createPatient(PatientDto request, Long callerUserId) {
		if (request == null || request.getPerson() == null) {
			throw new IllegalArgumentException("Person data is required");
		}

		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);
		Person person = buildPersonFromDto(request.getPerson());

		validatePersonForPatientCreation(person);

		if (person.getEmail() != null && !person.getEmail().isBlank()) {
			Person existingPerson = personDao.findByEmail(person.getEmail());
			if (existingPerson != null) {
				throw new IllegalArgumentException("Email already in use");
			}
		}

		personDao.save(person);

		Patient patient = new Patient(person, null, clinic, request.getActive(), request.getNotes());

		if (!Patient.isNotesValid(patient.getNotes())) {
			throw new IllegalArgumentException("Invalid notes");
		}

		patientDao.save(patient);

		return new PatientDto(patient);
	}

	@Transactional
	public PatientDto updatePatient(Long patientId, PatientDto request, Long callerUserId) {
		if (request == null) {
			throw new IllegalArgumentException("Request body is required");
		}

		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);
		Patient patient = findPatientOrThrow(patientId);

		if (patient.getClinic() == null || !patient.getClinic().getId().equals(clinic.getId())) {
			throw new IllegalArgumentException("Patient not found in caller clinic");
		}

		if (request.getPerson() != null) {
			updatePatientPerson(patient.getPerson(), request.getPerson());
			personDao.update(patient.getPerson());
		}

		if (request.getNotes() != null) {
			if (!Patient.isNotesValid(request.getNotes())) {
				throw new IllegalArgumentException("Invalid notes");
			}
			patient.setNotes(request.getNotes());
		}

		if (request.getActive() != null) {
			patient.setActive(request.getActive());
		}

		patientDao.update(patient);

		return getPatientById(patient.getId(), callerUserId);
	}

	private Patient findPatientOrThrow(Long patientId) {
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

	private List<Patient> sortPatientsForDentistView(List<Patient> patients, Dentist dentist) {
		LocalDateTime now = LocalDateTime.now();
		List<Appointment> appointments = appointmentDao.findActiveByDentistIdAndClinicId(dentist.getId(),
				dentist.getClinic().getId());

		Map<Long, PatientAppointmentDistance> closestAppointmentByPatientId = new LinkedHashMap<>();

		for (Appointment appointment : appointments) {
			if (appointment.getPatient() == null) {
				continue;
			}

			Long patientId = appointment.getPatient().getId();
			long distance = Math.abs(Duration.between(now, appointment.getStartDateTime()).toMillis());

			PatientAppointmentDistance current = closestAppointmentByPatientId.get(patientId);

			if (current == null || distance < current.distanceFromNow()) {
				closestAppointmentByPatientId.put(patientId, new PatientAppointmentDistance(patientId, distance));
			}
		}

		Map<Long, Patient> patientsById = new LinkedHashMap<>();
		for (Patient patient : patients) {
			patientsById.put(patient.getId(), patient);
		}

		List<Patient> prioritizedPatients = closestAppointmentByPatientId.values().stream()
				.filter(distance -> patientsById.containsKey(distance.patientId()))
				.sorted(Comparator.comparingLong(PatientAppointmentDistance::distanceFromNow))
				.map(distance -> patientsById.remove(distance.patientId())).toList();

		List<Patient> result = new ArrayList<>(prioritizedPatients);
		result.addAll(patientsById.values());

		return result;
	}

	private Person buildPersonFromDto(PersonDto request) {
		return new Person(request.getName(), request.getFirstSurname(), request.getSecondSurname(),
				request.getBirthDate(), request.getGender(), request.getEmail(), request.getPhonePrefix(),
				request.getPhoneNumber(), request.getAddress(), request.getCity(), request.getProfileImage(), true,
				request.getNotes());
	}

	private void validatePersonForPatientCreation(Person person) {
		if (!Person.isNameValid(person.getName())) {
			throw new IllegalArgumentException("Invalid name");
		}

		if (!Person.isFirstSurnameValid(person.getFirstSurname())) {
			throw new IllegalArgumentException("Invalid firstSurname");
		}

		if (!Person.isSecondSurnameValid(person.getSecondSurname())) {
			throw new IllegalArgumentException("Invalid secondSurname");
		}

		if (!Person.isGenderValid(person.getGender())) {
			throw new IllegalArgumentException("Invalid gender");
		}

		if (!Person.isEmailValid(person.getEmail())) {
			throw new IllegalArgumentException("Invalid email");
		}

		if (!Person.isPhonePrefixValid(person.getPhonePrefix())) {
			throw new IllegalArgumentException("Invalid phonePrefix");
		}

		if (!Person.isPhoneNumberValid(person.getPhoneNumber())) {
			throw new IllegalArgumentException("Invalid phoneNumber");
		}

		if (!Person.isAddressValid(person.getAddress())) {
			throw new IllegalArgumentException("Invalid address");
		}

		if (!Person.isCityValid(person.getCity())) {
			throw new IllegalArgumentException("Invalid city");
		}

		if (!Person.isProfileImageValid(person.getProfileImage())) {
			throw new IllegalArgumentException("Invalid profileImage");
		}

		if (!Person.isNotesValid(person.getNotes())) {
			throw new IllegalArgumentException("Invalid notes");
		}
	}

	private void updatePatientPerson(Person person, PersonDto request) {
		if (request.getName() != null) {
			if (!Person.isNameValid(request.getName())) {
				throw new IllegalArgumentException("Invalid name");
			}
			person.setName(request.getName());
		}

		if (request.getFirstSurname() != null) {
			if (!Person.isFirstSurnameValid(request.getFirstSurname())) {
				throw new IllegalArgumentException("Invalid firstSurname");
			}
			person.setFirstSurname(request.getFirstSurname());
		}

		if (request.getSecondSurname() != null) {
			if (!Person.isSecondSurnameValid(request.getSecondSurname())) {
				throw new IllegalArgumentException("Invalid secondSurname");
			}
			person.setSecondSurname(request.getSecondSurname());
		}

		if (request.getBirthDate() != null) {
			person.setBirthDate(request.getBirthDate());
		}

		if (request.getGender() != null) {
			if (!Person.isGenderValid(request.getGender())) {
				throw new IllegalArgumentException("Invalid gender");
			}
			person.setGender(request.getGender());
		}

		if (request.getEmail() != null) {
			if (!Person.isEmailValid(request.getEmail())) {
				throw new IllegalArgumentException("Invalid email");
			}

			Person existingPerson = personDao.findByEmail(request.getEmail());
			if (existingPerson != null && !existingPerson.getId().equals(person.getId())) {
				throw new IllegalArgumentException("Email already in use");
			}

			person.setEmail(request.getEmail());
		}

		if (request.getPhonePrefix() != null) {
			if (!Person.isPhonePrefixValid(request.getPhonePrefix())) {
				throw new IllegalArgumentException("Invalid phonePrefix");
			}
			person.setPhonePrefix(request.getPhonePrefix());
		}

		if (request.getPhoneNumber() != null) {
			if (!Person.isPhoneNumberValid(request.getPhoneNumber())) {
				throw new IllegalArgumentException("Invalid phoneNumber");
			}
			person.setPhoneNumber(request.getPhoneNumber());
		}

		if (request.getAddress() != null) {
			if (!Person.isAddressValid(request.getAddress())) {
				throw new IllegalArgumentException("Invalid address");
			}
			person.setAddress(request.getAddress());
		}

		if (request.getCity() != null) {
			if (!Person.isCityValid(request.getCity())) {
				throw new IllegalArgumentException("Invalid city");
			}
			person.setCity(request.getCity());
		}

		if (request.getProfileImage() != null) {
			if (!Person.isProfileImageValid(request.getProfileImage())) {
				throw new IllegalArgumentException("Invalid profileImage");
			}
			person.setProfileImage(request.getProfileImage());
		}

		if (request.getNotes() != null) {
			if (!Person.isNotesValid(request.getNotes())) {
				throw new IllegalArgumentException("Invalid notes");
			}
			person.setNotes(request.getNotes());
		}
	}

	private DocumentDto buildDocumentDto(com.example.DentalPlus_Backend.model.Document document) {
		return new DocumentDto(document, supabaseStorageService.getPublicUrl(document.getStoragePath()));
	}

	private record PatientAppointmentDistance(Long patientId, long distanceFromNow) {
	}
}