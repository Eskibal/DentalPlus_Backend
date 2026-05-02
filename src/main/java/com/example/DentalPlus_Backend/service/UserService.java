package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.dao.PatientDao;
import com.example.DentalPlus_Backend.dao.PersonDao;
import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.dao.UserDao;
import com.example.DentalPlus_Backend.dto.LoginRequest;
import com.example.DentalPlus_Backend.dto.LoginResponse;
import com.example.DentalPlus_Backend.dto.PersonDto;
import com.example.DentalPlus_Backend.dto.ProfileDto;
import com.example.DentalPlus_Backend.dto.RoleDto;
import com.example.DentalPlus_Backend.dto.WeeklyCalendarDto;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.Person;
import com.example.DentalPlus_Backend.model.Receptionist;
import com.example.DentalPlus_Backend.model.User;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
	private final UserDao userDao;
	private final PersonDao personDao;
	private final PatientDao patientDao;
	private final DentistDao dentistDao;
	private final ReceptionistDao receptionistDao;
	private final AdminDao adminDao;
	private final JwtService jwtService;
	private final CalendarService calendarService;
	private final CloudinaryService cloudinaryService;
	private final BCryptPasswordEncoder encoder;

	public UserService(UserDao userDao, PersonDao personDao, PatientDao patientDao, DentistDao dentistDao,
			ReceptionistDao receptionistDao, AdminDao adminDao, JwtService jwtService, CalendarService calendarService,
			CloudinaryService cloudinaryService, BCryptPasswordEncoder encoder) {
		this.userDao = userDao;
		this.personDao = personDao;
		this.patientDao = patientDao;
		this.dentistDao = dentistDao;
		this.receptionistDao = receptionistDao;
		this.adminDao = adminDao;
		this.jwtService = jwtService;
		this.calendarService = calendarService;
		this.cloudinaryService = cloudinaryService;
		this.encoder = encoder;
	}

	public LoginResponse login(LoginRequest request) {
		if (request == null || request.getIdentifier() == null || request.getIdentifier().isBlank()
				|| request.getPassword() == null || request.getPassword().isBlank()) {
			throw new IllegalArgumentException("Invalid credentials");
		}

		User user = findWorkerUserByUsernameOrPersonEmail(request.getIdentifier());

		if (user == null) {
			throw new IllegalArgumentException("Invalid credentials");
		}

		if (!user.getActive()) {
			throw new IllegalStateException("Inactive user");
		}

		if (!encoder.matches(request.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Invalid credentials");
		}

		String token = jwtService.generateToken(user.getId());
		ProfileDto profile = getMyProfile(user.getId());

		return new LoginResponse(token, user.getId(), profile);
	}

	public ProfileDto getMyProfile(Long callerUserId) {
		User user = findActiveUserOrThrow(callerUserId);
		Person person = resolvePersonByUserId(user.getId());

		List<RoleDto> roles = buildRoles(user.getId());
		WeeklyCalendarDto weeklyCalendar = calendarService.getEffectiveWeeklyCalendarForUser(user.getId());

		return new ProfileDto(user, person == null ? null : new PersonDto(person), roles, weeklyCalendar);
	}

	@Transactional
	public ProfileDto updateMyProfile(Long callerUserId, ProfileDto request, MultipartFile profileImage,
			Boolean removeProfileImage) {
		if (request == null) {
			throw new IllegalArgumentException("Invalid profile data");
		}

		boolean hasProfileImage = profileImage != null && !profileImage.isEmpty();
		boolean wantsRemoveProfileImage = Boolean.TRUE.equals(removeProfileImage);

		if (hasProfileImage && wantsRemoveProfileImage) {
			throw new IllegalArgumentException("Cannot upload and remove profile image at the same time");
		}

		User user = findActiveUserOrThrow(callerUserId);
		Person person = resolvePersonByUserId(user.getId());

		updateUserOwnFields(user, request);

		if (person != null && request.getPerson() != null) {
			updatePersonOwnFields(person, request.getPerson());
		}

		if (person != null) {
			updateProfileImage(user.getId(), person, profileImage, wantsRemoveProfileImage);
			personDao.update(person);
		}

		userDao.update(user);

		return getMyProfile(user.getId());
	}

	public User findActiveUserOrThrow(Long userId) {
		User user = userDao.findById(userId);

		if (user == null || !user.getActive()) {
			throw new IllegalArgumentException("Authenticated user not found");
		}

		return user;
	}

	private User findWorkerUserByUsernameOrPersonEmail(String identifier) {
		String trimmedIdentifier = identifier.trim();

		User user = userDao.findByUsername(trimmedIdentifier);
		if (user != null) {
			return user;
		}

		String normalizedEmail = Person.normalizeEmail(trimmedIdentifier);

		user = dentistDao.findUserByPersonEmail(normalizedEmail);
		if (user != null) {
			return user;
		}

		user = receptionistDao.findUserByPersonEmail(normalizedEmail);
		if (user != null) {
			return user;
		}

		return adminDao.findUserByPersonEmail(normalizedEmail);
	}

	private Person resolvePersonByUserId(Long userId) {
		List<Person> persons = new ArrayList<>();

		Patient patient = patientDao.findByUserId(userId);
		if (patient != null && patient.getPerson() != null) {
			persons.add(patient.getPerson());
		}

		Dentist dentist = dentistDao.findByUserId(userId);
		if (dentist != null && dentist.getPerson() != null) {
			persons.add(dentist.getPerson());
		}

		Receptionist receptionist = receptionistDao.findByUserId(userId);
		if (receptionist != null && receptionist.getPerson() != null) {
			persons.add(receptionist.getPerson());
		}

		Admin admin = adminDao.findByUserId(userId);
		if (admin != null && admin.getPerson() != null) {
			persons.add(admin.getPerson());
		}

		if (persons.isEmpty()) {
			return null;
		}

		Person firstPerson = persons.get(0);

		for (Person person : persons) {
			if (!person.getId().equals(firstPerson.getId())) {
				throw new IllegalStateException("User is linked to different person records across roles");
			}
		}

		return firstPerson;
	}

	private List<RoleDto> buildRoles(Long userId) {
		List<RoleDto> roles = new ArrayList<>();

		Patient patient = patientDao.findByUserId(userId);
		if (patient != null) {
			roles.add(new RoleDto("PATIENT", patient.getId(),
					patient.getClinic() == null ? null : patient.getClinic().getId(),
					patient.getClinic() == null ? null : patient.getClinic().getName(), patient.getActive(), null));
		}

		Dentist dentist = dentistDao.findByUserId(userId);
		if (dentist != null) {
			roles.add(new RoleDto("DENTIST", dentist.getId(),
					dentist.getClinic() == null ? null : dentist.getClinic().getId(),
					dentist.getClinic() == null ? null : dentist.getClinic().getName(), dentist.getActive(),
					dentist.getSpeciality()));
		}

		Receptionist receptionist = receptionistDao.findByUserId(userId);
		if (receptionist != null) {
			roles.add(new RoleDto("RECEPTIONIST", receptionist.getId(),
					receptionist.getClinic() == null ? null : receptionist.getClinic().getId(),
					receptionist.getClinic() == null ? null : receptionist.getClinic().getName(),
					receptionist.getActive(), null));
		}

		Admin admin = adminDao.findByUserId(userId);
		if (admin != null) {
			roles.add(new RoleDto("ADMIN", admin.getId(), admin.getClinic() == null ? null : admin.getClinic().getId(),
					admin.getClinic() == null ? null : admin.getClinic().getName(), admin.getActive(), null));
		}

		return roles;
	}

	private void updateUserOwnFields(User user, ProfileDto request) {
		if (request.getUsername() != null) {
			if (!User.isUsernameValid(request.getUsername())) {
				throw new IllegalArgumentException("Invalid username");
			}

			User existingUser = userDao.findByUsername(request.getUsername());
			if (existingUser != null && !existingUser.getId().equals(user.getId())) {
				throw new IllegalArgumentException("Username already in use");
			}

			user.setUsername(request.getUsername());
		}

		if (request.getThemePreference() != null) {
			if (!User.isThemePreferenceValid(request.getThemePreference())) {
				throw new IllegalArgumentException("Invalid themePreference");
			}

			user.setThemePreference(request.getThemePreference());
		}

		if (request.getLanguagePreference() != null) {
			if (!User.isLanguagePreferenceValid(request.getLanguagePreference())) {
				throw new IllegalArgumentException("Invalid languagePreference");
			}

			user.setLanguagePreference(request.getLanguagePreference());
		}
	}

	private void updatePersonOwnFields(Person person, PersonDto request) {
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

		if (request.getNotes() != null) {
			if (!Person.isNotesValid(request.getNotes())) {
				throw new IllegalArgumentException("Invalid notes");
			}
			person.setNotes(request.getNotes());
		}
	}

	private void updateProfileImage(Long userId, Person person, MultipartFile profileImage,
			Boolean removeProfileImage) {
		boolean hasProfileImage = profileImage != null && !profileImage.isEmpty();
		boolean emptyProfileImageWasSent = profileImage != null && profileImage.isEmpty();
		boolean wantsRemoveProfileImage = Boolean.TRUE.equals(removeProfileImage) || emptyProfileImageWasSent;

		if (wantsRemoveProfileImage) {
			if (person.getProfileImage() != null && !person.getProfileImage().isBlank()) {
				cloudinaryService.deleteImageByUrl(person.getProfileImage());
			}

			person.setProfileImage(null);
			return;
		}

		if (!hasProfileImage) {
			return;
		}

		try {
			if (person.getProfileImage() != null && !person.getProfileImage().isBlank()) {
				cloudinaryService.deleteImageByUrl(person.getProfileImage());
			}

			String imageUrl = cloudinaryService.uploadProfileImage(profileImage, userId);
			person.setProfileImage(imageUrl);
		} catch (IOException e) {
			throw new IllegalStateException("Error uploading profile image");
		}
	}
}