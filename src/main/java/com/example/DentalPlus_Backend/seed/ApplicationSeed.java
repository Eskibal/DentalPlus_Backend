package com.example.DentalPlus_Backend.seed;

import com.example.DentalPlus_Backend.DentalPlusApplication;
import com.example.DentalPlus_Backend.dao.DentalPieceDao;
import com.example.DentalPlus_Backend.dao.DentalSurfaceDao;
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
import com.example.DentalPlus_Backend.model.User;
import com.example.DentalPlus_Backend.service.AppointmentService;
import com.example.DentalPlus_Backend.service.CloudinaryService;
import com.example.DentalPlus_Backend.service.OdontogramService;
import com.example.DentalPlus_Backend.service.SupabaseStorageService;
import com.example.DentalPlus_Backend.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

@Component
public class ApplicationSeed {

	private static final String DEFAULT_PASSWORD = "Password123";

	private static final String SEED_MODE_DEMO = "DEMO";
	private static final String SEED_MODE_PERFORMANCE_LIGHT = "PERFORMANCE_LIGHT";

	private static final int PERFORMANCE_EXTRA_ADMIN_COUNT = 2;
	private static final int PERFORMANCE_EXTRA_RECEPTIONIST_COUNT = 3;
	private static final int PERFORMANCE_EXTRA_DENTIST_COUNT = 6;
	private static final int PERFORMANCE_PATIENT_COUNT = 30;
	private static final int PERFORMANCE_ODONTOGRAM_COUNT = 4;
	private static final int PERFORMANCE_APPOINTMENT_COUNT = 80;

	private static final int ROW_COUNT_WARNING_LIMIT = 4_000;
	private static final int ROW_COUNT_DANGER_LIMIT = 7_000;

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
	private final DentalPieceDao dentalPieceDao;
	private final DentalSurfaceDao dentalSurfaceDao;
	private final ResourceLoader resourceLoader;

	private final Scanner scanner = new Scanner(System.in);

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
			OdontogramService odontogramService, AppointmentService appointmentService, DentalPieceDao dentalPieceDao,
			DentalSurfaceDao dentalSurfaceDao, ResourceLoader resourceLoader) {
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.passwordEncoder = passwordEncoder;
		this.cloudinaryService = cloudinaryService;
		this.supabaseStorageService = supabaseStorageService;
		this.userService = userService;
		this.odontogramService = odontogramService;
		this.appointmentService = appointmentService;
		this.dentalPieceDao = dentalPieceDao;
		this.dentalSurfaceDao = dentalSurfaceDao;
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
		long totalStart = System.nanoTime();

		try {
			printHeader();

			if (!confirmDangerousReset()) {
				System.out.println("[CANCELLED] Seed process was cancelled.");
				return;
			}

			String seedMode = askSeedMode();

			System.out.println("[INFO] Cleaning external files registered in database...");
			measureMillis("External file cleanup", this::clearExternalFiles);

			System.out.println("[INFO] Resetting database...");
			measureMillis("Database reset", this::resetDatabase);

			System.out.println("[INFO] Loading seed data...");
			SeedState seedState = measureMillis("Total database seed loading",
					() -> transactionTemplate.execute(status -> loadSeedData(seedMode)));

			if (seedState == null) {
				throw new IllegalStateException("Seed data could not be loaded");
			}

			System.out.println("[INFO] Uploading optional seed files...");
			runOptionalStep("Base Cloudinary/Supabase uploads", () -> uploadOptionalSeedFiles(seedState));

			if (SEED_MODE_PERFORMANCE_LIGHT.equals(seedMode)) {
				System.out.println("[INFO] Uploading optional performance files...");
				runOptionalStep("Performance Cloudinary/Supabase uploads",
						() -> uploadOptionalPerformanceFiles(seedState));
			}

			System.out.println("[INFO] Running diagnostics...");
			measureMillis("Base diagnostics", () -> runDiagnostics(seedState));

			if (SEED_MODE_PERFORMANCE_LIGHT.equals(seedMode)) {
				runOptionalStep("Performance diagnostics", () -> runPerformanceDiagnostics(seedState));
			}

			runOptionalStep("Database load estimate", this::printDatabaseLoadEstimate);

			printPostmanSummary(seedState, seedMode);

			if (!askKeepSeededData()) {
				System.out.println("[INFO] Cleaning seeded external files...");
				measureMillis("Seeded external file cleanup", this::clearExternalFiles);

				System.out.println("[INFO] Resetting database to empty state...");
				measureMillis("Database cleanup reset", this::resetDatabase);

				System.out.println("[OK] Database and external files were cleaned. Environment is empty again.");
			} else {
				System.out.println("[OK] Seeded data kept. You can now test with Postman or frontend.");
			}

			long totalElapsedMillis = (System.nanoTime() - totalStart) / 1_000_000;
			System.out.println("[PERF] Total seed execution time: " + totalElapsedMillis + " ms");
		} catch (RuntimeException e) {
			handleSeedFailure(e);
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
		System.out.println("[INFO] DEMO keeps the current small Postman/frontend dataset.");
		System.out.println("[INFO] PERFORMANCE_LIGHT adds controlled data and timing diagnostics.");
		System.out.println("[INFO] Performance mode is conservative for small DB providers.");
		System.out.println("============================================================");
		System.out.println();
	}

	private boolean confirmDangerousReset() {
		System.out.println("Type SEED to delete current data and load demo data:");
		String confirmation = readLine();

		return "SEED".equals(confirmation);
	}

	private String askSeedMode() {
		System.out.println();
		System.out.println("Choose seed mode:");
		System.out.println("1. DEMO - current small dataset for Postman/frontend");
		System.out.println("2. PERFORMANCE_LIGHT - demo dataset plus controlled performance data");
		String answer = readLine();

		if ("2".equals(answer) || SEED_MODE_PERFORMANCE_LIGHT.equalsIgnoreCase(answer)) {
			return SEED_MODE_PERFORMANCE_LIGHT;
		}

		return SEED_MODE_DEMO;
	}

	private boolean askKeepSeededData() {
		System.out.println();
		System.out.println("Keep seeded data? [Y/n]");
		String answer = readLine();

		return answer == null || answer.isBlank() || answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
	}

	private String readLine() {
		return scanner.nextLine().trim();
	}

	private <T> T measureMillis(String label, Supplier<T> action) {
		long start = System.nanoTime();

		try {
			T result = action.get();
			long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
			System.out.println("[PERF] " + label + " completed in " + elapsedMillis + " ms");
			return result;
		} catch (RuntimeException e) {
			long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
			System.out.println("[PERF][FAIL] " + label + " failed after " + elapsedMillis + " ms");
			throw e;
		}
	}

	private void measureMillis(String label, Runnable action) {
		long start = System.nanoTime();

		try {
			action.run();
			long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
			System.out.println("[PERF] " + label + " completed in " + elapsedMillis + " ms");
		} catch (RuntimeException e) {
			long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
			System.out.println("[PERF][FAIL] " + label + " failed after " + elapsedMillis + " ms");
			throw e;
		}
	}

	private void runOptionalStep(String label, Runnable action) {
		try {
			measureMillis(label, action);
		} catch (RuntimeException e) {
			System.out.println("[WARN] Optional step failed: " + label);
			System.out.println("[WARN] " + e.getMessage());
		}
	}

	private void handleSeedFailure(RuntimeException e) {
		System.out.println();
		System.out.println("========== Seed Failure ==========");

		String message = e.getMessage() == null ? "" : e.getMessage();
		String lowerMessage = message.toLowerCase();

		if (lowerMessage.contains("table is full") || lowerMessage.contains("database is full")
				|| lowerMessage.contains("disk full") || lowerMessage.contains("no space left")
				|| lowerMessage.contains("size limit") || lowerMessage.contains("quota")
				|| lowerMessage.contains("too many connections") || lowerMessage.contains("max_questions")) {
			System.out.println("[ERROR] The database may be full or over its provider limit.");
			System.out.println("[ERROR] Seed was stopped to avoid leaving the environment unstable.");
			System.out.println("[TIP] Use DEMO mode or reduce PERFORMANCE_* constants.");
		} else {
			System.out.println("[ERROR] Seed failed with an unexpected error.");
		}

		System.out.println("[ERROR] " + message);
		System.out.println("==================================");
		System.out.println();

		throw e;
	}

	private void clearExternalFiles() {
		try {
			List<String> profileImages = transactionTemplate.execute(status -> findExistingProfileImages());

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

			List<String> documentPaths = transactionTemplate.execute(status -> findExistingDocumentPaths());

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

	private List<String> findExistingProfileImages() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Person> person = cq.from(Person.class);

		cq.select(person.get("profileImage"))
				.where(cb.and(cb.isNotNull(person.get("profileImage")),
						cb.notEqual(cb.trim(person.get("profileImage")), "")));

		return entityManager.createQuery(cq).getResultList();
	}

	private List<String> findExistingDocumentPaths() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Document> document = cq.from(Document.class);

		cq.select(document.get("storagePath"))
				.where(cb.and(cb.isNotNull(document.get("storagePath")),
						cb.notEqual(cb.trim(document.get("storagePath")), "")));

		return entityManager.createQuery(cq).getResultList();
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

	private SeedState loadSeedData(String seedMode) {
		LocalTime workStart = LocalTime.of(9, 0);
		LocalTime workEnd = LocalTime.of(18, 0);
		LocalTime breakStart = LocalTime.of(13, 0);
		LocalTime breakEnd = LocalTime.of(14, 0);

		Organization organization = measureMillis("Base organization loading", () -> persist(
				new Organization("Default Organization", true, "Seed organization for development and demonstration")));

		CalendarRule clinicCalendarRule = measureMillis("Base calendar rule loading",
				() -> persist(new CalendarRule(workStart, workEnd, workStart, workEnd, workStart, workEnd, workStart,
						workEnd, workStart, workEnd, null, null, null, null, true,
						"Default clinic working calendar")));

		Clinic clinic = measureMillis("Base clinic loading",
				() -> persist(new Clinic(organization, clinicCalendarRule, "Default Clinic", true, "Default Country",
						"Default City", "Default Address", "10000000000", "clinic@example.com", "UTC",
						"Seed clinic for development and demonstration")));

		measureMillis("Base calendar breaks and holiday loading", () -> {
			persistBreaks(clinicCalendarRule, breakStart, breakEnd);

			persist(new CalendarHoliday(clinicCalendarRule, "General Demo Holiday", LocalDate.of(2026, 5, 25),
					LocalDate.of(2026, 5, 25), "LOCAL", true, "Demo holiday for calendar diagnostics"));
		});

		List<Box> boxes = measureMillis("Base boxes loading", () -> {
			List<Box> createdBoxes = new ArrayList<>();
			createdBoxes.add(persist(new Box(clinic, "Box 1", true, "Primary demo box")));
			createdBoxes.add(persist(new Box(clinic, "Box 2", true, "Secondary demo box")));
			return createdBoxes;
		});

		Box boxOne = boxes.get(0);
		Box boxTwo = boxes.get(1);

		List<StaffSeed> baseStaff = measureMillis("Base staff loading", () -> {
			List<StaffSeed> staff = new ArrayList<>();

			staff.add(createStaff("admin", "admin@example.com", "Admin", "User", "Sample", clinic, "ADMIN", null));

			staff.add(createStaff("receptionist", "receptionist@example.com", "Reception", "User", "Sample", clinic,
					"RECEPTIONIST", null));

			staff.add(createStaff("dentist.primary", "dentist.primary@example.com", "Dentist", "Primary", "Sample",
					clinic, "DENTIST", "General Dentistry"));

			staff.add(createStaff("dentist.secondary", "dentist.secondary@example.com", "Dentist", "Secondary",
					"Sample", clinic, "DENTIST", "Restorative Dentistry"));

			return staff;
		});

		StaffSeed adminSeed = baseStaff.get(0);
		StaffSeed receptionistSeed = baseStaff.get(1);
		StaffSeed primaryDentistSeed = baseStaff.get(2);
		StaffSeed secondaryDentistSeed = baseStaff.get(3);

		List<Patient> basePatients = measureMillis("Base patients loading", () -> {
			List<Patient> patients = new ArrayList<>();

			patients.add(createPatient(clinic, "Patient", "One", "Sample", "patient.one@example.com", "10000000001",
					LocalDate.of(1990, 1, 1), "ALLERGY:PENICILLIN", "Routine dental checkup"));

			patients.add(createPatient(clinic, "Patient", "Two", "Sample", "patient.two@example.com", "10000000002",
					LocalDate.of(1988, 2, 2), null, "Preventive dental visit"));

			patients.add(createPatient(clinic, "Patient", "Three", "Sample", "patient.three@example.com",
					"10000000003", LocalDate.of(1995, 3, 3), "INFECTION_RISK:HEPATITIS_B", "Dental assessment"));

			patients.add(createPatient(clinic, "Patient", "Four", "Sample", "patient.four@example.com", "10000000004",
					LocalDate.of(1992, 4, 4), null, "Follow-up consultation"));

			return patients;
		});

		Patient patientOne = basePatients.get(0);
		Patient patientTwo = basePatients.get(1);
		Patient patientThree = basePatients.get(2);
		Patient patientFour = basePatients.get(3);

		List<Odontogram> baseOdontograms = measureMillis("Base odontograms loading", () -> {
			List<Odontogram> odontograms = new ArrayList<>();

			odontograms.add(createOdontogram(patientOne));
			odontograms.add(createOdontogram(patientTwo));
			odontograms.add(createOdontogram(patientThree));
			odontograms.add(createOdontogram(patientFour));

			return odontograms;
		});

		Odontogram odontogramOne = baseOdontograms.get(0);
		Odontogram odontogramTwo = baseOdontograms.get(1);
		Odontogram odontogramThree = baseOdontograms.get(2);
		Odontogram odontogramFour = baseOdontograms.get(3);

		measureMillis("Base dental marks loading",
				() -> createDemoDentalMarks(odontogramOne, odontogramTwo, odontogramThree, odontogramFour));

		List<Appointment> baseAppointments = measureMillis("Base appointments loading", () -> {
			List<Appointment> appointments = new ArrayList<>();

			appointments.add(persist(new Appointment(boxOne, primaryDentistSeed.dentist(), patientOne,
					LocalDateTime.of(2026, 5, 1, 10, 0), LocalDateTime.of(2026, 5, 1, 10, 30), "SCHEDULED",
					"Routine checkup", "Routine appointment created by ApplicationSeed", true)));

			appointments.add(persist(new Appointment(boxTwo, secondaryDentistSeed.dentist(), patientTwo,
					LocalDateTime.of(2026, 5, 1, 11, 0), LocalDateTime.of(2026, 5, 1, 11, 30), "SCHEDULED",
					"Dental cleaning", "Second routine appointment created by ApplicationSeed", true)));

			appointments.add(persist(new Appointment(boxOne, primaryDentistSeed.dentist(), patientThree,
					LocalDateTime.of(2026, 5, 4, 15, 0), LocalDateTime.of(2026, 5, 4, 15, 45), "SCHEDULED",
					"Follow-up consultation", "Follow-up appointment created by ApplicationSeed", true)));

			return appointments;
		});

		List<StaffSeed> performanceAdmins = new ArrayList<>();
		List<StaffSeed> performanceReceptionists = new ArrayList<>();
		List<StaffSeed> performanceDentists = new ArrayList<>();
		List<Patient> performancePatients = new ArrayList<>();
		List<Odontogram> performanceOdontograms = new ArrayList<>();
		List<Appointment> performanceAppointments = new ArrayList<>();

		if (SEED_MODE_PERFORMANCE_LIGHT.equals(seedMode)) {
			PerformanceSeedData performanceSeedData = createPerformanceData(clinic, boxOne, boxTwo, primaryDentistSeed,
					secondaryDentistSeed);

			performanceAdmins = performanceSeedData.admins();
			performanceReceptionists = performanceSeedData.receptionists();
			performanceDentists = performanceSeedData.dentists();
			performancePatients = performanceSeedData.patients();
			performanceOdontograms = performanceSeedData.odontograms();
			performanceAppointments = performanceSeedData.appointments();
		}

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
		this.firstAppointmentId = baseAppointments.get(0).getId();

		System.out.println("[OK] Organization created: " + organization.getId());
		System.out.println("[OK] Clinic created: " + clinic.getId());
		System.out.println("[OK] Boxes created: " + boxOne.getId() + ", " + boxTwo.getId());
		System.out.println("[OK] Base staff users created: 4");
		System.out.println("[OK] Base patients created: 4");
		System.out.println("[OK] Base odontograms created: 4");
		System.out.println("[OK] Base dental surface marks created.");
		System.out.println("[OK] Base appointments created: 3");

		if (SEED_MODE_PERFORMANCE_LIGHT.equals(seedMode)) {
			System.out.println("[OK] Performance admins created: " + performanceAdmins.size());
			System.out.println("[OK] Performance receptionists created: " + performanceReceptionists.size());
			System.out.println("[OK] Performance dentists created: " + performanceDentists.size());
			System.out.println("[OK] Performance patients created: " + performancePatients.size());
			System.out.println("[OK] Performance odontograms created: " + performanceOdontograms.size());
			System.out.println("[OK] Performance appointments created: " + performanceAppointments.size());
		}

		return new SeedState(organization, clinic, clinicCalendarRule, boxOne, boxTwo, adminSeed, receptionistSeed,
				primaryDentistSeed, secondaryDentistSeed, patientOne, patientTwo, patientThree, patientFour,
				odontogramOne, baseAppointments.get(0), performanceAdmins, performanceReceptionists,
				performanceDentists, performancePatients, performanceOdontograms, performanceAppointments);
	}

	private PerformanceSeedData createPerformanceData(Clinic clinic, Box boxOne, Box boxTwo, StaffSeed primaryDentistSeed,
			StaffSeed secondaryDentistSeed) {
		List<StaffSeed> performanceAdmins = measureMillis("Performance admins loading", () -> {
			List<StaffSeed> admins = new ArrayList<>();

			for (int i = 1; i <= PERFORMANCE_EXTRA_ADMIN_COUNT; i++) {
				String suffix = String.format("%02d", i);
				admins.add(createStaff("admin.perf." + suffix, "admin.perf." + suffix + "@example.com", "Admin",
						"Performance", suffix, clinic, "ADMIN", null));
			}

			return admins;
		});

		List<StaffSeed> performanceReceptionists = measureMillis("Performance receptionists loading", () -> {
			List<StaffSeed> receptionists = new ArrayList<>();

			for (int i = 1; i <= PERFORMANCE_EXTRA_RECEPTIONIST_COUNT; i++) {
				String suffix = String.format("%02d", i);
				receptionists.add(createStaff("receptionist.perf." + suffix,
						"receptionist.perf." + suffix + "@example.com", "Receptionist", "Performance", suffix, clinic,
						"RECEPTIONIST", null));
			}

			return receptionists;
		});

		List<StaffSeed> performanceDentists = measureMillis("Performance dentists loading", () -> {
			List<StaffSeed> dentists = new ArrayList<>();
			String[] specialties = { "General Dentistry", "Restorative Dentistry", "Endodontics", "Periodontics",
					"Prosthodontics", "Pediatric Dentistry" };

			for (int i = 1; i <= PERFORMANCE_EXTRA_DENTIST_COUNT; i++) {
				String suffix = String.format("%02d", i);
				String specialty = specialties[(i - 1) % specialties.length];

				dentists.add(createStaff("dentist.perf." + suffix, "dentist.perf." + suffix + "@example.com",
						"Dentist", "Performance", suffix, clinic, "DENTIST", specialty));
			}

			return dentists;
		});

		List<Patient> performancePatients = measureMillis("Performance patients loading", () -> {
			List<Patient> patients = new ArrayList<>();

			for (int i = 1; i <= PERFORMANCE_PATIENT_COUNT; i++) {
				String suffix = String.format("%03d", i);

				patients.add(createPatient(clinic, "Performance", "Patient", suffix,
						"patient.performance." + suffix + "@example.com", "20000000" + suffix,
						LocalDate.of(1980 + (i % 25), ((i - 1) % 12) + 1, ((i - 1) % 27) + 1),
						resolvePerformanceMedicalAlert(i), "Performance seed patient " + suffix));
			}

			return patients;
		});

		List<Odontogram> performanceOdontograms = measureMillis("Performance odontograms loading", () -> {
			List<Odontogram> odontograms = new ArrayList<>();
			int odontogramsToCreate = Math.min(PERFORMANCE_ODONTOGRAM_COUNT, performancePatients.size());

			for (int i = 0; i < odontogramsToCreate; i++) {
				odontograms.add(createOdontogram(performancePatients.get(i)));
			}

			return odontograms;
		});

		List<Appointment> performanceAppointments = measureMillis("Performance appointments loading", () -> {
			List<Appointment> appointments = new ArrayList<>();
			List<Dentist> dentists = new ArrayList<>();

			dentists.add(primaryDentistSeed.dentist());
			dentists.add(secondaryDentistSeed.dentist());

			for (StaffSeed performanceDentist : performanceDentists) {
				dentists.add(performanceDentist.dentist());
			}

			List<Box> boxes = List.of(boxOne, boxTwo);

			LocalTime[] appointmentTimes = { LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0),
					LocalTime.of(10, 30), LocalTime.of(11, 0), LocalTime.of(11, 30), LocalTime.of(12, 0),
					LocalTime.of(12, 30), LocalTime.of(14, 0), LocalTime.of(14, 30), LocalTime.of(15, 0),
					LocalTime.of(15, 30), LocalTime.of(16, 0), LocalTime.of(16, 30), LocalTime.of(17, 0) };

			String[] treatments = { "Routine checkup", "Dental cleaning", "Caries review", "Restoration follow-up",
					"Endodontic assessment", "Periodontal review", "Crown assessment", "Bridge review" };

			int created = 0;
			int dayOffset = 0;

			while (created < PERFORMANCE_APPOINTMENT_COUNT) {
				LocalDate date = LocalDate.of(2026, 5, 5).plusDays(dayOffset);

				for (int timeIndex = 0; timeIndex < appointmentTimes.length
						&& created < PERFORMANCE_APPOINTMENT_COUNT; timeIndex++) {
					for (int boxIndex = 0; boxIndex < boxes.size()
							&& created < PERFORMANCE_APPOINTMENT_COUNT; boxIndex++) {
						Box box = boxes.get(boxIndex);
						Dentist dentist = dentists.get((dayOffset + timeIndex + boxIndex) % dentists.size());
						Patient patient = performancePatients.get(created % performancePatients.size());
						LocalDateTime start = LocalDateTime.of(date, appointmentTimes[timeIndex]);
						LocalDateTime end = start.plusMinutes(30);
						String treatment = treatments[created % treatments.length];

						appointments.add(persist(new Appointment(box, dentist, patient, start, end, "SCHEDULED",
								treatment, "Performance appointment " + String.format("%03d", created + 1), true)));

						created++;
					}
				}

				dayOffset++;
			}

			return appointments;
		});

		return new PerformanceSeedData(performanceAdmins, performanceReceptionists, performanceDentists,
				performancePatients, performanceOdontograms, performanceAppointments);
	}

	private String resolvePerformanceMedicalAlert(int index) {
		if (index % 15 == 0) {
			return "ALLERGY:PENICILLIN|INFECTION_RISK:HEPATITIS_B";
		}

		if (index % 10 == 0) {
			return "BLEEDING_RISK:ANTICOAGULANTS";
		}

		if (index % 7 == 0) {
			return "INFECTION_RISK:HEPATITIS_B";
		}

		if (index % 5 == 0) {
			return "ALLERGY:PENICILLIN";
		}

		return null;
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
			String phoneNumber, LocalDate birthDate, String medicalAlert, String consultationReason) {
		Person person = persist(new Person(name, firstSurname, secondSurname, birthDate, "OTHER", email, "+1",
				phoneNumber, "Default Address", "Default City", null, true, consultationReason));

		return persist(new Patient(person, null, clinic, true, medicalAlert, consultationReason));
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
		return dentalPieceDao.findByOdontogramIdAndPieceNumber(odontogram.getId(), pieceNumber);
	}

	private DentalSurface findSeedDentalSurface(DentalPiece dentalPiece, String surfaceType) {
		return dentalSurfaceDao.findByDentalPieceIdAndSurfaceType(dentalPiece.getId(), surfaceType);
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

	private void uploadOptionalPerformanceFiles(SeedState seedState) {
		transactionTemplate.executeWithoutResult(status -> {
			seedState.performanceAdmins().stream().limit(1).forEach(staff -> uploadOptionalProfileImage(staff.person()));
			seedState.performanceReceptionists().stream().limit(1)
					.forEach(staff -> uploadOptionalProfileImage(staff.person()));
			seedState.performanceDentists().stream().limit(1)
					.forEach(staff -> uploadOptionalProfileImage(staff.person()));
		});

		seedState.performancePatients().stream().limit(1).forEach(patient -> uploadOptionalDocument(patient,
				GENERAL_CONSENT_RESOURCE, "Performance General Consent Document", "CONSENT"));
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

	private void uploadOptionalDocument(Patient patient, String resourcePath, String documentName, String documentType) {
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
		assertCondition(countEntities(DentalSurfaceMark.class) >= 7, "Dental surface marks were created");
		assertCondition(countEntities(DentalPieceState.class) >= 4, "Dental piece states were created");

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

	private void runPerformanceDiagnostics(SeedState seedState) {
		System.out.println();
		System.out.println("========== Performance Diagnostics ==========");

		measureMillis("Admin login performance", () -> {
			userService.login(new LoginRequest("admin@example.com", DEFAULT_PASSWORD));
		});

		measureMillis("Receptionist login performance", () -> {
			userService.login(new LoginRequest("receptionist@example.com", DEFAULT_PASSWORD));
		});

		measureMillis("Primary dentist login performance", () -> {
			userService.login(new LoginRequest("dentist.primary@example.com", DEFAULT_PASSWORD));
		});

		measureMillis("Admin appointment list by date", () -> {
			appointmentService.getAppointments(seedState.admin().user().getId(), LocalDate.of(2026, 5, 5), null, null,
					null);
		});

		measureMillis("Receptionist appointment list by date", () -> {
			appointmentService.getAppointments(seedState.receptionist().user().getId(), LocalDate.of(2026, 5, 5), null,
					null, null);
		});

		measureMillis("Primary dentist appointment list by date", () -> {
			appointmentService.getAppointments(seedState.primaryDentist().user().getId(), LocalDate.of(2026, 5, 5),
					null, null, null);
		});

		measureMillis("Admin availability by date and time", () -> {
			appointmentService.getAvailability(seedState.admin().user().getId(), LocalDate.of(2026, 5, 5),
					LocalTime.of(10, 0));
		});

		measureMillis("Receptionist availability by date and time", () -> {
			appointmentService.getAvailability(seedState.receptionist().user().getId(), LocalDate.of(2026, 5, 5),
					LocalTime.of(10, 0));
		});

		measureMillis("Primary dentist availability by date and time", () -> {
			appointmentService.getAvailability(seedState.primaryDentist().user().getId(), LocalDate.of(2026, 5, 5),
					LocalTime.of(10, 0));
		});

		measureMillis("Admin get base odontogram by patient", () -> {
			odontogramService.getOdontogramByPatientId(seedState.patientOne().getId(), seedState.admin().user().getId());
		});

		if (!seedState.performancePatients().isEmpty() && !seedState.performanceOdontograms().isEmpty()) {
			Patient performancePatient = seedState.performancePatients().get(0);

			measureMillis("Admin get performance odontogram by patient", () -> {
				odontogramService.getOdontogramByPatientId(performancePatient.getId(), seedState.admin().user().getId());
			});
		}

		System.out.println("=============================================");
		System.out.println();
	}

	private <T> Long countEntities(Class<T> entityClass) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<T> root = cq.from(entityClass);

		cq.select(cb.count(root));

		Long count = entityManager.createQuery(cq).getSingleResult();

		return count == null ? 0L : count;
	}

	private void printDatabaseLoadEstimate() {
		Long users = transactionTemplate.execute(status -> countEntities(User.class));
		Long persons = transactionTemplate.execute(status -> countEntities(Person.class));
		Long patients = transactionTemplate.execute(status -> countEntities(Patient.class));
		Long dentists = transactionTemplate.execute(status -> countEntities(Dentist.class));
		Long receptionists = transactionTemplate.execute(status -> countEntities(Receptionist.class));
		Long admins = transactionTemplate.execute(status -> countEntities(Admin.class));
		Long appointments = transactionTemplate.execute(status -> countEntities(Appointment.class));
		Long odontograms = transactionTemplate.execute(status -> countEntities(Odontogram.class));
		Long pieces = transactionTemplate.execute(status -> countEntities(DentalPiece.class));
		Long states = transactionTemplate.execute(status -> countEntities(DentalPieceState.class));
		Long surfaces = transactionTemplate.execute(status -> countEntities(DentalSurface.class));
		Long marks = transactionTemplate.execute(status -> countEntities(DentalSurfaceMark.class));
		Long documents = transactionTemplate.execute(status -> countEntities(Document.class));

		long totalRows = safeLong(users) + safeLong(persons) + safeLong(patients) + safeLong(dentists)
				+ safeLong(receptionists) + safeLong(admins) + safeLong(appointments) + safeLong(odontograms)
				+ safeLong(pieces) + safeLong(states) + safeLong(surfaces) + safeLong(marks) + safeLong(documents);

		System.out.println();
		System.out.println("========== Database Load Estimate ==========");
		System.out.println("users         = " + users);
		System.out.println("persons       = " + persons);
		System.out.println("admins        = " + admins);
		System.out.println("receptionists = " + receptionists);
		System.out.println("dentists      = " + dentists);
		System.out.println("patients      = " + patients);
		System.out.println("appointments  = " + appointments);
		System.out.println("odontograms   = " + odontograms);
		System.out.println("pieces        = " + pieces);
		System.out.println("states        = " + states);
		System.out.println("surfaces      = " + surfaces);
		System.out.println("marks         = " + marks);
		System.out.println("documents     = " + documents);
		System.out.println("tracked rows  = " + totalRows);

		if (totalRows >= ROW_COUNT_DANGER_LIMIT) {
			System.out.println("[WARN] Seed row count is high for a small database. Avoid increasing performance data.");
		} else if (totalRows >= ROW_COUNT_WARNING_LIMIT) {
			System.out.println("[WARN] Seed row count is above the preferred light performance target.");
		} else {
			System.out.println("[OK] Seed row count is within the controlled performance target.");
		}

		System.out.println("============================================");
		System.out.println();
	}

	private long safeLong(Long value) {
		return value == null ? 0L : value;
	}

	private void printPostmanSummary(SeedState seedState, String seedMode) {
		System.out.println();
		System.out.println("========== Postman Default Values ==========");
		System.out.println("seedMode     : " + seedMode);
		System.out.println("baseUrlRender: https://dentalplus-backend.onrender.com");
		System.out.println("baseUrlLocal : http://localhost:8080");
		System.out.println();
		System.out.println("Login credentials:");
		System.out.println("admin@example.com / " + DEFAULT_PASSWORD);
		System.out.println("receptionist@example.com / " + DEFAULT_PASSWORD);
		System.out.println("dentist.primary@example.com / " + DEFAULT_PASSWORD);
		System.out.println("dentist.secondary@example.com / " + DEFAULT_PASSWORD);

		if (SEED_MODE_PERFORMANCE_LIGHT.equals(seedMode)) {
			System.out.println();
			System.out.println("Performance login examples:");

			if (!seedState.performanceAdmins().isEmpty()) {
				System.out.println(seedState.performanceAdmins().get(0).person().getEmail() + " / " + DEFAULT_PASSWORD);
			}

			if (!seedState.performanceReceptionists().isEmpty()) {
				System.out.println(
						seedState.performanceReceptionists().get(0).person().getEmail() + " / " + DEFAULT_PASSWORD);
			}

			if (!seedState.performanceDentists().isEmpty()) {
				System.out.println(seedState.performanceDentists().get(0).person().getEmail() + " / " + DEFAULT_PASSWORD);
			}
		}

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
		System.out.println("Seed medical alerts:");
		System.out.println("Patient 1 -> ALLERGY:PENICILLIN");
		System.out.println("Patient 2 -> no medical alert");
		System.out.println("Patient 3 -> INFECTION_RISK:HEPATITIS_B");
		System.out.println("Patient 4 -> no medical alert");
		System.out.println();
		System.out.println("Seed appointment treatments:");
		System.out.println("Appointment 1 -> Routine checkup");
		System.out.println("Appointment 2 -> Dental cleaning");
		System.out.println("Appointment 3 -> Follow-up consultation");
		System.out.println();
		System.out.println("Seed dental examples:");
		System.out.println("Patient 1 / Piece 11 / MESIAL   -> CARIES / PENDING");
		System.out.println("Patient 1 / Piece 16 / OCCLUSAL -> FILLING / DONE");
		System.out.println("Patient 2 / Piece 21 / DISTAL   -> CARIES / PENDING");
		System.out.println("Patient 2 / Piece 36 / OCCLUSAL -> FISSURE_SEALANT / DONE");
		System.out.println("Patient 3 / Piece 46 / VESTIBULAR -> CROWN / PENDING");
		System.out.println("Patient 4 / Piece 14 / MESIAL   -> FILLING / DONE");
		System.out.println("Patient 4 / Piece 24 / DISTAL   -> CARIES / PENDING");

		if (SEED_MODE_PERFORMANCE_LIGHT.equals(seedMode)) {
			System.out.println();
			System.out.println("Performance dataset:");
			System.out.println("Extra admins        = " + seedState.performanceAdmins().size());
			System.out.println("Extra receptionists = " + seedState.performanceReceptionists().size());
			System.out.println("Extra dentists      = " + seedState.performanceDentists().size());
			System.out.println("Extra patients      = " + seedState.performancePatients().size());
			System.out.println("Extra odontograms   = " + seedState.performanceOdontograms().size());
			System.out.println("Extra appointments  = " + seedState.performanceAppointments().size());
		}

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
		return entity;
	}

	private record SeedState(Organization organization, Clinic clinic, CalendarRule clinicCalendarRule, Box boxOne,
			Box boxTwo, StaffSeed admin, StaffSeed receptionist, StaffSeed primaryDentist, StaffSeed secondaryDentist,
			Patient patientOne, Patient patientTwo, Patient patientThree, Patient patientFour, Odontogram odontogramOne,
			Appointment appointmentOne, List<StaffSeed> performanceAdmins, List<StaffSeed> performanceReceptionists,
			List<StaffSeed> performanceDentists, List<Patient> performancePatients,
			List<Odontogram> performanceOdontograms, List<Appointment> performanceAppointments) {
	}

	private record PerformanceSeedData(List<StaffSeed> admins, List<StaffSeed> receptionists, List<StaffSeed> dentists,
			List<Patient> patients, List<Odontogram> odontograms, List<Appointment> appointments) {
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