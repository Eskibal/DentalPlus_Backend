package com.example.DentalPlus_Backend.seed;

import com.example.DentalPlus_Backend.DentalPlusApplication;
import com.example.DentalPlus_Backend.dto.LoginRequest;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.Appointment;
import com.example.DentalPlus_Backend.model.Box;
import com.example.DentalPlus_Backend.model.CalendarBreak;
import com.example.DentalPlus_Backend.model.CalendarHoliday;
import com.example.DentalPlus_Backend.model.CalendarRule;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.DentalPiece;
import com.example.DentalPlus_Backend.model.DentalPieceState;
import com.example.DentalPlus_Backend.model.DentalSurface;
import com.example.DentalPlus_Backend.model.DentalSurfaceMark;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Document;
import com.example.DentalPlus_Backend.model.Odontogram;
import com.example.DentalPlus_Backend.model.Organization;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.Person;
import com.example.DentalPlus_Backend.model.Receptionist;
import com.example.DentalPlus_Backend.model.Treatment;
import com.example.DentalPlus_Backend.model.User;
import com.example.DentalPlus_Backend.service.AppointmentService;
import com.example.DentalPlus_Backend.service.CloudinaryService;
import com.example.DentalPlus_Backend.service.OdontogramService;
import com.example.DentalPlus_Backend.service.SupabaseStorageService;
import com.example.DentalPlus_Backend.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

@Component
public class ApplicationSeed {

	private static final String DEFAULT_PASSWORD = "Password123";

	private static final String PROFILE_IMAGE_RESOURCE = "classpath:seed/profile-image.png";
	private static final String GENERAL_CONSENT_RESOURCE = "classpath:seed/general-consent.pdf";
	private static final String TREATMENT_PLAN_RESOURCE = "classpath:seed/treatment-plan.pdf";

	private static final String[] TABLES_TO_TRUNCATE = { "dental_bridge_piece", "dental_bridge", "dental_surface_mark",
			"dental_surface", "dental_piece_state", "dental_piece", "odontogram", "document", "appointment",
			"inventory", "product", "treatment", "patient", "dentist", "receptionist", "admin", "calendar_exception",
			"calendar_holiday", "calendar_break", "box", "clinic", "calendar_rule", "person", "user", "organization" };

	@PersistenceContext
	private EntityManager entityManager;

	private final TransactionTemplate transactionTemplate;
	private final BCryptPasswordEncoder passwordEncoder;
	private final CloudinaryService cloudinaryService;
	private final SupabaseStorageService supabaseStorageService;
	private final UserService userService;
	private final OdontogramService odontogramService;
	private final AppointmentService appointmentService;
	private final ResourceLoader resourceLoader;

	private Long adminUserId;
	private Long receptionistUserId;
	private Long primaryDentistUserId;
	private Long secondaryDentistUserId;

	private Long adminId;
	private Long receptionistId;
	private Long primaryDentistId;
	private Long secondaryDentistId;

	private Long firstPatientId;
	private Long firstOdontogramId;
	private Long firstBoxId;
	private Long firstAppointmentId;
	private Long firstDocumentId;

	public ApplicationSeed(PlatformTransactionManager transactionManager, BCryptPasswordEncoder passwordEncoder,
			CloudinaryService cloudinaryService, SupabaseStorageService supabaseStorageService, UserService userService,
			OdontogramService odontogramService, AppointmentService appointmentService, ResourceLoader resourceLoader) {
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.passwordEncoder = passwordEncoder;
		this.cloudinaryService = cloudinaryService;
		this.supabaseStorageService = supabaseStorageService;
		this.userService = userService;
		this.odontogramService = odontogramService;
		this.appointmentService = appointmentService;
		this.resourceLoader = resourceLoader;
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DentalPlusApplication.class, args);

		try {
			context.getBean(ApplicationSeed.class).run();
		} finally {
			context.close();
		}
	}

	public void run() {
		printHeader();

		if (!confirmDangerousReset()) {
			System.out.println("[CANCELLED] Seed process was cancelled.");
			return;
		}

		System.out.println("[INFO] Cleaning external files registered in database...");
		clearExternalFiles();

		System.out.println("[INFO] Resetting database...");
		resetDatabase();

		System.out.println("[INFO] Loading seed data...");
		SeedState seedState = transactionTemplate.execute(status -> loadSeedData());

		if (seedState == null) {
			throw new IllegalStateException("Seed data could not be loaded");
		}

		System.out.println("[INFO] Uploading optional seed files...");
		uploadOptionalSeedFiles(seedState);

		System.out.println("[INFO] Running diagnostics...");
		runDiagnostics(seedState);

		printPostmanSummary();

		if (!askKeepSeededData()) {
			System.out.println("[INFO] Cleaning seeded external files...");
			clearExternalFiles();

			System.out.println("[INFO] Resetting database to empty state...");
			resetDatabase();

			System.out.println("[OK] Database and external files were cleaned. Environment is empty again.");
		} else {
			System.out.println("[OK] Seeded data kept. You can now test with Postman or frontend.");
		}
	}

	private void printHeader() {
		System.out.println();
		System.out.println("============================================================");
		System.out.println("                 DentalPlus Application Seed");
		System.out.println("============================================================");
		System.out.println("[WARNING] This process is destructive.");
		System.out.println("[WARNING] It will delete database data and external files");
		System.out.println("[WARNING] registered in Cloudinary/Supabase when possible.");
		System.out.println("[INFO] It uses the same application.properties as the normal app.");
		System.out.println("============================================================");
		System.out.println();
	}

	private boolean confirmDangerousReset() {
		System.out.println("Type SEED to delete current data and load demo data:");
		String confirmation = readLine();

		return "SEED".equals(confirmation);
	}

	private boolean askKeepSeededData() {
		System.out.println();
		System.out.println("Keep seeded data? [Y/n]");
		String answer = readLine();

		return answer == null || answer.isBlank() || answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
	}

	private String readLine() {
		Scanner scanner = new Scanner(System.in);
		return scanner.nextLine().trim();
	}

	private void clearExternalFiles() {
		try {
			List<String> profileImages = transactionTemplate.execute(status -> entityManager.createQuery("""
					SELECT p.profileImage
					FROM Person p
					WHERE p.profileImage IS NOT NULL
					  AND TRIM(p.profileImage) <> ''
					""", String.class).getResultList());

			if (profileImages != null) {
				for (String profileImage : profileImages) {
					try {
						cloudinaryService.deleteImageByUrl(profileImage);
						System.out.println("[OK] Deleted profile image: " + profileImage);
					} catch (RuntimeException e) {
						System.out.println("[WARN] Could not delete profile image: " + profileImage);
						System.out.println("[WARN] " + e.getMessage());
					}
				}
			}

			List<String> documentPaths = transactionTemplate.execute(status -> entityManager.createQuery("""
					SELECT d.storagePath
					FROM Document d
					WHERE d.storagePath IS NOT NULL
					  AND TRIM(d.storagePath) <> ''
					""", String.class).getResultList());

			if (documentPaths != null) {
				for (String documentPath : documentPaths) {
					try {
						supabaseStorageService.deletePdf(documentPath);
						System.out.println("[OK] Deleted document file: " + documentPath);
					} catch (RuntimeException e) {
						System.out.println("[WARN] Could not delete document file: " + documentPath);
						System.out.println("[WARN] " + e.getMessage());
					}
				}
			}
		} catch (RuntimeException e) {
			System.out.println("[WARN] External cleanup could not be completed.");
			System.out.println("[WARN] " + e.getMessage());
		}
	}

	private void resetDatabase() {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
			} catch (RuntimeException e) {
				System.out.println("[WARN] Could not disable foreign key checks. Continuing anyway.");
			}

			for (String table : TABLES_TO_TRUNCATE) {
				try {
					entityManager.createNativeQuery("TRUNCATE TABLE `" + table + "`").executeUpdate();
					System.out.println("[OK] Truncated table: " + table);
				} catch (RuntimeException e) {
					System.out.println("[WARN] Could not truncate table: " + table);
					System.out.println("[WARN] " + e.getMessage());
				}
			}

			try {
				entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
			} catch (RuntimeException e) {
				System.out.println("[WARN] Could not enable foreign key checks.");
			}
		});
	}

	private SeedState loadSeedData() {
		LocalTime workStart = LocalTime.of(9, 0);
		LocalTime workEnd = LocalTime.of(18, 0);
		LocalTime breakStart = LocalTime.of(13, 0);
		LocalTime breakEnd = LocalTime.of(14, 0);

		Organization organization = persist(
				new Organization("Default Organization", true, "Seed organization for development and demonstration"));

		CalendarRule clinicCalendarRule = persist(
				new CalendarRule(workStart, workEnd, workStart, workEnd, workStart, workEnd, workStart, workEnd,
						workStart, workEnd, null, null, null, null, true, "Default clinic working calendar"));

		Clinic clinic = persist(new Clinic(organization, clinicCalendarRule, "Default Clinic", true, "Default Country",
				"Default City", "Default Address", "10000000000", "clinic@example.com", "UTC",
				"Seed clinic for development and demonstration"));

		persistBreaks(clinicCalendarRule, breakStart, breakEnd);

		persist(new CalendarHoliday(clinicCalendarRule, "General Demo Holiday", LocalDate.of(2026, 5, 25),
				LocalDate.of(2026, 5, 25), "LOCAL", true, "Demo holiday for calendar diagnostics"));

		Box boxOne = persist(new Box(clinic, "Box 1", true, "Primary demo box"));
		Box boxTwo = persist(new Box(clinic, "Box 2", true, "Secondary demo box"));

		StaffSeed adminSeed = createStaff("admin", "admin@example.com", "Admin", "User", "Sample", clinic, "ADMIN",
				null);

		StaffSeed receptionistSeed = createStaff("receptionist", "receptionist@example.com", "Reception", "User",
				"Sample", clinic, "RECEPTIONIST", null);

		StaffSeed primaryDentistSeed = createStaff("dentist.primary", "dentist.primary@example.com", "Dentist",
				"Primary", "Sample", clinic, "DENTIST", "General Dentistry");

		StaffSeed secondaryDentistSeed = createStaff("dentist.secondary", "dentist.secondary@example.com", "Dentist",
				"Secondary", "Sample", clinic, "DENTIST", "Restorative Dentistry");

		Patient patientOne = createPatient(clinic, "Patient", "One", "Sample", "patient.one@example.com", "10000000001",
				LocalDate.of(1990, 1, 1), "Routine dental checkup");

		Patient patientTwo = createPatient(clinic, "Patient", "Two", "Sample", "patient.two@example.com", "10000000002",
				LocalDate.of(1988, 2, 2), "Preventive dental visit");

		Patient patientThree = createPatient(clinic, "Patient", "Three", "Sample", "patient.three@example.com",
				"10000000003", LocalDate.of(1995, 3, 3), "Dental assessment");

		Patient patientFour = createPatient(clinic, "Patient", "Four", "Sample", "patient.four@example.com",
				"10000000004", LocalDate.of(1992, 4, 4), "Follow-up consultation");

		Odontogram odontogramOne = createOdontogram(patientOne);
		Odontogram odontogramTwo = createOdontogram(patientTwo);
		Odontogram odontogramThree = createOdontogram(patientThree);
		Odontogram odontogramFour = createOdontogram(patientFour);

		createDemoDentalMarks(odontogramOne, odontogramTwo, odontogramThree, odontogramFour);

		Appointment appointmentOne = persist(new Appointment(boxOne, primaryDentistSeed.dentist(), patientOne,
				LocalDateTime.of(2026, 5, 1, 10, 0), LocalDateTime.of(2026, 5, 1, 10, 30), "SCHEDULED",
				"Routine appointment created by ApplicationSeed", true));

		persist(new Appointment(boxTwo, secondaryDentistSeed.dentist(), patientTwo, LocalDateTime.of(2026, 5, 1, 11, 0),
				LocalDateTime.of(2026, 5, 1, 11, 30), "SCHEDULED",
				"Second routine appointment created by ApplicationSeed", true));

		persist(new Appointment(boxOne, primaryDentistSeed.dentist(), patientThree, LocalDateTime.of(2026, 5, 4, 15, 0),
				LocalDateTime.of(2026, 5, 4, 15, 45), "SCHEDULED", "Follow-up appointment created by ApplicationSeed",
				true));

		persist(new Treatment("Routine Checkup", "General diagnostic treatment available for demo data", 30, true,
				"Seed treatment"));

		entityManager.flush();

		this.adminUserId = adminSeed.user().getId();
		this.receptionistUserId = receptionistSeed.user().getId();
		this.primaryDentistUserId = primaryDentistSeed.user().getId();
		this.secondaryDentistUserId = secondaryDentistSeed.user().getId();

		this.adminId = adminSeed.admin() == null ? null : adminSeed.admin().getId();
		this.receptionistId = receptionistSeed.receptionist() == null ? null : receptionistSeed.receptionist().getId();
		this.primaryDentistId = primaryDentistSeed.dentist() == null ? null : primaryDentistSeed.dentist().getId();
		this.secondaryDentistId = secondaryDentistSeed.dentist() == null ? null
				: secondaryDentistSeed.dentist().getId();

		this.firstPatientId = patientOne.getId();
		this.firstOdontogramId = odontogramOne.getId();
		this.firstBoxId = boxOne.getId();
		this.firstAppointmentId = appointmentOne.getId();

		System.out.println("[OK] Organization created: " + organization.getId());
		System.out.println("[OK] Clinic created: " + clinic.getId());
		System.out.println("[OK] Boxes created: " + boxOne.getId() + ", " + boxTwo.getId());
		System.out.println("[OK] Staff users created.");
		System.out.println("[OK] Patients created: 4");
		System.out.println("[OK] Odontograms created: 4");
		System.out.println("[OK] Dental surface marks created.");
		System.out.println("[OK] Appointments created: 3");

		return new SeedState(organization, clinic, clinicCalendarRule, boxOne, boxTwo, adminSeed, receptionistSeed,
				primaryDentistSeed, secondaryDentistSeed, patientOne, patientTwo, patientThree, patientFour,
				odontogramOne, appointmentOne);
	}

	private void persistBreaks(CalendarRule calendarRule, LocalTime breakStart, LocalTime breakEnd) {
		persist(new CalendarBreak(calendarRule, "MONDAY", breakStart, breakEnd, true, "Default lunch break"));
		persist(new CalendarBreak(calendarRule, "TUESDAY", breakStart, breakEnd, true, "Default lunch break"));
		persist(new CalendarBreak(calendarRule, "WEDNESDAY", breakStart, breakEnd, true, "Default lunch break"));
		persist(new CalendarBreak(calendarRule, "THURSDAY", breakStart, breakEnd, true, "Default lunch break"));
		persist(new CalendarBreak(calendarRule, "FRIDAY", breakStart, breakEnd, true, "Default lunch break"));
	}

	private StaffSeed createStaff(String username, String email, String name, String firstSurname, String secondSurname,
			Clinic clinic, String role, String speciality) {
		User user = persist(
				new User(username, passwordEncoder.encode(DEFAULT_PASSWORD), "SYSTEM", "en", true, "Seed user"));

		Person person = persist(new Person(name, firstSurname, secondSurname, LocalDate.of(1990, 1, 1), "OTHER", email,
				"+1", "10000000000", "Default Address", "Default City", null, true, "Seed staff person"));

		if ("ADMIN".equals(role)) {
			Admin admin = persist(new Admin(person, user, clinic, true, "Seed admin"));
			return new StaffSeed(user, person, admin, null, null);
		}

		if ("RECEPTIONIST".equals(role)) {
			Receptionist receptionist = persist(
					new Receptionist(person, user, clinic, null, true, "Seed receptionist"));
			return new StaffSeed(user, person, null, receptionist, null);
		}

		if ("DENTIST".equals(role)) {
			Dentist dentist = persist(new Dentist(person, user, clinic, null, speciality, true, "Seed dentist"));
			return new StaffSeed(user, person, null, null, dentist);
		}

		throw new IllegalArgumentException("Unsupported staff role: " + role);
	}

	private Patient createPatient(Clinic clinic, String name, String firstSurname, String secondSurname, String email,
			String phoneNumber, LocalDate birthDate, String consultationReason) {
		Person person = persist(new Person(name, firstSurname, secondSurname, birthDate, "OTHER", email, "+1",
				phoneNumber, "Default Address", "Default City", null, true, consultationReason));

		return persist(new Patient(person, null, clinic, true, consultationReason));
	}

	private Odontogram createOdontogram(Patient patient) {
		Odontogram odontogram = persist(new Odontogram(patient));
		odontogram.setViewMode("MIXED");

		List<Integer> pieceNumbers = DentalPiece.getAllMixedPieceNumbers();

		for (Integer pieceNumber : pieceNumbers) {
			DentalPiece dentalPiece = persist(new DentalPiece(odontogram, pieceNumber));

			persist(new DentalPieceState(dentalPiece, "HEALTHY", "Initial default state"));

			List<String> surfaceTypes = DentalSurface.getSurfaceTypesForPieceKind(dentalPiece.getPieceKind());

			for (String surfaceType : surfaceTypes) {
				persist(new DentalSurface(dentalPiece, surfaceType, null));
			}
		}

		return odontogram;
	}

	private void createDemoDentalMarks(Odontogram odontogramOne, Odontogram odontogramTwo, Odontogram odontogramThree,
			Odontogram odontogramFour) {
		createSurfaceMark(odontogramOne, 11, "MESIAL", "CARIES", "PENDING", "Initial caries observation");
		createSurfaceMark(odontogramOne, 16, "OCCLUSAL", "FILLING", "DONE", "Existing restoration");
		createPieceState(odontogramOne, 26, "CROWN_PENDING", "Crown treatment planned");

		createSurfaceMark(odontogramTwo, 21, "DISTAL", "CARIES", "PENDING", "Interproximal caries observation");
		createSurfaceMark(odontogramTwo, 36, "OCCLUSAL", "FISSURE_SEALANT", "DONE", "Preventive fissure sealant");

		createSurfaceMark(odontogramThree, 46, "VESTIBULAR", "CROWN", "PENDING", "Crown assessment pending");
		createPieceState(odontogramThree, 15, "ENDODONTICS_DONE", "Previous endodontic treatment");

		createSurfaceMark(odontogramFour, 14, "MESIAL", "FILLING", "DONE", "Existing filling");
		createSurfaceMark(odontogramFour, 24, "DISTAL", "CARIES", "PENDING", "Caries follow-up required");
		createPieceState(odontogramFour, 55, "NATURAL_ABSENCE", "Temporary tooth naturally absent");
	}

	private void createSurfaceMark(Odontogram odontogram, Integer pieceNumber, String surfaceType, String markType,
			String markState, String notes) {
		DentalPiece dentalPiece = findSeedDentalPiece(odontogram, pieceNumber);
		if (dentalPiece == null) {
			throw new IllegalStateException("Seed dental piece not found: " + pieceNumber);
		}
		DentalSurface dentalSurface = findSeedDentalSurface(dentalPiece, surfaceType);
		if (dentalSurface == null) {
			throw new IllegalStateException("Seed dental surface not found: " + pieceNumber + " " + surfaceType);
		}
		DentalSurfaceMark dentalSurfaceMark = new DentalSurfaceMark(dentalSurface, markType, markState, notes);
		dentalSurfaceMark.setActive(true);
		persist(dentalSurfaceMark);
	}

	private void createPieceState(Odontogram odontogram, Integer pieceNumber, String stateType, String notes) {
		DentalPiece dentalPiece = findSeedDentalPiece(odontogram, pieceNumber);

		if (dentalPiece == null) {
			throw new IllegalStateException("Seed dental piece not found: " + pieceNumber);
		}

		persist(new DentalPieceState(dentalPiece, stateType, notes));
	}

	private DentalPiece findSeedDentalPiece(Odontogram odontogram, Integer pieceNumber) {
		List<DentalPiece> pieces = entityManager.createQuery("""
				FROM DentalPiece dp
				WHERE dp.odontogram.id = :odontogramId
				  AND dp.pieceNumber = :pieceNumber
				""", DentalPiece.class).setParameter("odontogramId", odontogram.getId())
				.setParameter("pieceNumber", pieceNumber).setMaxResults(1).getResultList();

		return pieces.isEmpty() ? null : pieces.get(0);
	}

	private DentalSurface findSeedDentalSurface(DentalPiece dentalPiece, String surfaceType) {
		List<DentalSurface> surfaces = entityManager.createQuery("""
				FROM DentalSurface ds
				WHERE ds.dentalPiece.id = :dentalPieceId
				  AND ds.surfaceType = :surfaceType
				""", DentalSurface.class).setParameter("dentalPieceId", dentalPiece.getId())
				.setParameter("surfaceType", surfaceType).setMaxResults(1).getResultList();

		return surfaces.isEmpty() ? null : surfaces.get(0);
	}

	private void uploadOptionalSeedFiles(SeedState seedState) {
		transactionTemplate.executeWithoutResult(status -> {
			uploadOptionalProfileImage(seedState.admin().person());
			uploadOptionalProfileImage(seedState.receptionist().person());
			uploadOptionalProfileImage(seedState.primaryDentist().person());
			uploadOptionalProfileImage(seedState.secondaryDentist().person());
		});

		uploadOptionalDocument(seedState.patientOne(), GENERAL_CONSENT_RESOURCE, "General Consent Document", "CONSENT");
		uploadOptionalDocument(seedState.patientOne(), TREATMENT_PLAN_RESOURCE, "Treatment Plan Document", "REPORT");
	}

	private void uploadOptionalProfileImage(Person person) {
		Resource resource = resourceLoader.getResource(PROFILE_IMAGE_RESOURCE);

		if (!resource.exists()) {
			System.out.println("[SKIP] Optional profile image not found: src/main/resources/seed/profile-image.png");
			return;
		}

		try {
			MultipartFile file = multipartFromResource(resource, "profile-image.png", "image/png");
			String imageUrl = cloudinaryService.uploadProfileImage(file, person.getId());
			person.setProfileImage(imageUrl);
			entityManager.merge(person);
			System.out.println("[OK] Uploaded profile image for person id: " + person.getId());
		} catch (RuntimeException | IOException e) {
			System.out.println("[WARN] Profile image upload skipped for person id: " + person.getId());
			System.out.println("[WARN] " + e.getMessage());
		}
	}

	private void uploadOptionalDocument(Patient patient, String resourcePath, String documentName,
			String documentType) {
		Resource resource = resourceLoader.getResource(resourcePath);

		if (!resource.exists()) {
			System.out.println(
					"[SKIP] Optional document not found: " + resourcePath.replace("classpath:", "src/main/resources/"));
			return;
		}

		try {
			MultipartFile file = multipartFromResource(resource, resource.getFilename(), "application/pdf");
			String storagePath = supabaseStorageService.uploadPdf(file, "patients/" + patient.getId());

			transactionTemplate.executeWithoutResult(status -> {
				Document document = new Document(entityManager.find(Patient.class, patient.getId()), documentName,
						storagePath, "application/pdf", documentType, true, "Seed document");

				entityManager.persist(document);
				entityManager.flush();

				if (firstDocumentId == null) {
					firstDocumentId = document.getId();
				}
			});

			System.out.println("[OK] Uploaded document for patient id: " + patient.getId());
		} catch (RuntimeException | IOException e) {
			System.out.println("[WARN] Document upload skipped for patient id: " + patient.getId());
			System.out.println("[WARN] " + e.getMessage());
		}
	}

	private MultipartFile multipartFromResource(Resource resource, String fallbackFilename, String contentType)
			throws IOException {
		String filename = resource.getFilename() == null || resource.getFilename().isBlank() ? fallbackFilename
				: resource.getFilename();

		try (InputStream inputStream = resource.getInputStream()) {
			return new SeedMultipartFile("file", filename, contentType, inputStream.readAllBytes());
		}
	}

	private void runDiagnostics(SeedState seedState) {
		assertCondition(seedState.organization().getId() != null, "Organization was persisted");
		assertCondition(seedState.clinic().getId() != null, "Clinic was persisted");
		assertCondition(seedState.boxOne().getId() != null, "Primary box was persisted");
		assertCondition(seedState.admin().user().getId() != null, "Admin user was persisted");
		assertCondition(seedState.receptionist().user().getId() != null, "Receptionist user was persisted");
		assertCondition(seedState.primaryDentist().user().getId() != null, "Primary dentist user was persisted");
		assertCondition(seedState.patientOne().getUser() == null, "Patients are not login-enabled");
		assertCondition(seedState.odontogramOne().getId() != null, "Odontogram was created");
		assertCondition(countSeedSurfaceMarks() >= 7, "Dental surface marks were created");
		assertCondition(countSeedPieceStates() >= 4, "Dental piece states were created");

		try {
			userService.login(new LoginRequest("admin@example.com", DEFAULT_PASSWORD));
			System.out.println("[OK] Admin login works with Postman credentials.");
		} catch (RuntimeException e) {
			throw new IllegalStateException("Admin login diagnostic failed", e);
		}

		try {
			userService.login(new LoginRequest("receptionist@example.com", DEFAULT_PASSWORD));
			System.out.println("[OK] Receptionist login works with Postman credentials.");
		} catch (RuntimeException e) {
			throw new IllegalStateException("Receptionist login diagnostic failed", e);
		}

		try {
			userService.login(new LoginRequest("dentist.primary@example.com", DEFAULT_PASSWORD));
			System.out.println("[OK] Dentist login works with Postman credentials.");
		} catch (RuntimeException e) {
			throw new IllegalStateException("Dentist login diagnostic failed", e);
		}

		try {
			odontogramService.getOdontogramByPatientId(seedState.patientOne().getId(),
					seedState.admin().user().getId());
			System.out.println("[OK] Odontogram retrieval works.");
		} catch (RuntimeException e) {
			throw new IllegalStateException("Odontogram diagnostic failed", e);
		}

		try {
			appointmentService.getAvailability(seedState.admin().user().getId(), LocalDate.of(2026, 5, 1),
					LocalTime.of(10, 0));
			System.out.println("[OK] Appointment availability works.");
		} catch (RuntimeException e) {
			throw new IllegalStateException("Appointment availability diagnostic failed", e);
		}

		System.out.println("[OK] Seed diagnostics completed.");
	}

	private Long countSeedSurfaceMarks() {
		return entityManager.createQuery("SELECT COUNT(dsm) FROM DentalSurfaceMark dsm", Long.class).getSingleResult();
	}

	private Long countSeedPieceStates() {
		return entityManager.createQuery("SELECT COUNT(dps) FROM DentalPieceState dps", Long.class).getSingleResult();
	}

	private void printPostmanSummary() {
		System.out.println();
		System.out.println("========== Postman Default Values ==========");
		System.out.println("baseUrlRender: https://dentalplus-backend.onrender.com");
		System.out.println("baseUrlLocal : http://localhost:8080");
		System.out.println();
		System.out.println("Login credentials:");
		System.out.println("admin@example.com / " + DEFAULT_PASSWORD);
		System.out.println("receptionist@example.com / " + DEFAULT_PASSWORD);
		System.out.println("dentist.primary@example.com / " + DEFAULT_PASSWORD);
		System.out.println("dentist.secondary@example.com / " + DEFAULT_PASSWORD);
		System.out.println();
		System.out.println("Default ids expected by Postman:");
		System.out.println("patientId      = " + firstPatientId);
		System.out.println("odontogramId   = " + firstOdontogramId);
		System.out.println("dentistId      = " + primaryDentistId);
		System.out.println("boxId          = " + firstBoxId);
		System.out.println("appointmentId  = " + firstAppointmentId);
		System.out.println("documentId     = "
				+ (firstDocumentId == null ? "not created because optional PDF was not uploaded" : firstDocumentId));
		System.out.println("pieceNumber    = 11");
		System.out.println("surfaceType    = MESIAL");
		System.out.println();
		System.out.println("Seed dental examples:");
		System.out.println("Patient 1 / Piece 11 / MESIAL   -> CARIES / PENDING");
		System.out.println("Patient 1 / Piece 16 / OCCLUSAL -> FILLING / DONE");
		System.out.println("Patient 2 / Piece 21 / DISTAL   -> CARIES / PENDING");
		System.out.println("Patient 2 / Piece 36 / OCCLUSAL -> FISSURE_SEALANT / DONE");
		System.out.println("Patient 3 / Piece 46 / VESTIBULAR -> CROWN / PENDING");
		System.out.println("Patient 4 / Piece 14 / MESIAL   -> FILLING / DONE");
		System.out.println("Patient 4 / Piece 24 / DISTAL   -> CARIES / PENDING");
		System.out.println("============================================");
		System.out.println();
	}

	private void assertCondition(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException("[FAIL] " + message);
		}

		System.out.println("[OK] " + message);
	}

	private <T> T persist(T entity) {
		entityManager.persist(entity);
		entityManager.flush();
		return entity;
	}

	private record SeedState(Organization organization, Clinic clinic, CalendarRule clinicCalendarRule, Box boxOne,
			Box boxTwo, StaffSeed admin, StaffSeed receptionist, StaffSeed primaryDentist, StaffSeed secondaryDentist,
			Patient patientOne, Patient patientTwo, Patient patientThree, Patient patientFour, Odontogram odontogramOne,
			Appointment appointmentOne) {
	}

	private record StaffSeed(User user, Person person, Admin admin, Receptionist receptionist, Dentist dentist) {
	}

	private static class SeedMultipartFile implements MultipartFile {
		private final String name;
		private final String originalFilename;
		private final String contentType;
		private final byte[] content;

		private SeedMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
			this.name = name;
			this.originalFilename = originalFilename;
			this.contentType = contentType;
			this.content = content == null ? new byte[0] : content;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getOriginalFilename() {
			return originalFilename;
		}

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public boolean isEmpty() {
			return content.length == 0;
		}

		@Override
		public long getSize() {
			return content.length;
		}

		@Override
		public byte[] getBytes() {
			return content;
		}

		@Override
		public InputStream getInputStream() {
			return new java.io.ByteArrayInputStream(content);
		}

		@Override
		public void transferTo(java.io.File dest) throws IOException {
			java.nio.file.Files.write(dest.toPath(), content);
		}
	}
}