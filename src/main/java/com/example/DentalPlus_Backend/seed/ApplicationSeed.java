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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Supplier;

@Component
public class ApplicationSeed {

	static final String DEFAULT_PASSWORD = "Password123";

	static final String SEED_MODE_DEMO = "DEMO";
	static final String SEED_MODE_PERFORMANCE_LIGHT = "PERFORMANCE_LIGHT";
	static final String SEED_MODE_STRESS = "STRESS";

	static final int PERFORMANCE_EXTRA_ADMIN_COUNT = 2;
	static final int PERFORMANCE_EXTRA_RECEPTIONIST_COUNT = 3;
	static final int PERFORMANCE_EXTRA_DENTIST_COUNT = 6;
	static final int PERFORMANCE_PATIENT_COUNT = 30;
	static final int PERFORMANCE_ODONTOGRAM_COUNT = 4;
	static final int PERFORMANCE_APPOINTMENT_COUNT = 80;

	private static final int ROW_COUNT_WARNING_LIMIT = 4_000;
	private static final int ROW_COUNT_DANGER_LIMIT = 7_000;

	private static final int PROFILE_IMAGE_COUNT = 50;
	private static final String PROFILE_IMAGE_RESOURCE_PATTERN = "classpath:seed/profile-images/profile-%02d.jpg";

	static final String GENERAL_CONSENT_RESOURCE = "classpath:seed/general-consent.pdf";
	static final String TREATMENT_PLAN_RESOURCE = "classpath:seed/treatment-plan.pdf";

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
	private final Random profileImageRandom = new Random();

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

			System.out.println("[INFO] Uploading seed profile images and PDF documents...");
			runOptionalStep("Seed external file uploads", () -> uploadSeedExternalFiles(seedState, seedMode));

			System.out.println("[INFO] Running diagnostics...");
			measureMillis("Base diagnostics", () -> runDiagnostics(seedState));

			if (isPerformanceOrStress(seedMode)) {
				PerformanceSeed performanceSeed = new PerformanceSeed(this);
				runOptionalStep("Performance diagnostics", () -> performanceSeed.runDiagnostics(seedState));
			}

			if (SEED_MODE_STRESS.equals(seedMode)) {
				StressSeed stressSeed = new StressSeed(this);
				runOptionalStep("Stress diagnostics", () -> stressSeed.runDiagnostics(seedState));
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
		System.out.println("[INFO] DEMO loads a small realistic dataset.");
		System.out.println("[INFO] PERFORMANCE_LIGHT loads demo + controlled performance data.");
		System.out.println("[INFO] STRESS loads demo + performance + massive stress data.");
		System.out.println("[INFO] All modes upload profile images and patient PDF documents when resources exist.");
		System.out.println("============================================================");
		System.out.println();
	}

	private boolean confirmDangerousReset() {
		System.out.println("Type SEED to delete current data and load seed data:");
		String confirmation = readLine();

		return "SEED".equals(confirmation);
	}

	private String askSeedMode() {
		System.out.println();
		System.out.println("Choose seed mode:");
		System.out.println("1. DEMO - small realistic dataset for Postman/frontend");
		System.out.println("2. PERFORMANCE_LIGHT - demo + controlled performance data");
		System.out.println("3. STRESS - demo + performance + massive stress data");
		String answer = readLine();

		if ("3".equals(answer) || SEED_MODE_STRESS.equalsIgnoreCase(answer)) {
			return SEED_MODE_STRESS;
		}

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

	String readLine() {
		return scanner.nextLine().trim();
	}

	private boolean isPerformanceOrStress(String seedMode) {
		return SEED_MODE_PERFORMANCE_LIGHT.equals(seedMode) || SEED_MODE_STRESS.equals(seedMode);
	}

	<T> T measureMillis(String label, Supplier<T> action) {
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

	void measureMillis(String label, Runnable action) {
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

	void runOptionalStep(String label, Runnable action) {
		try {
			measureMillis(label, action);
		} catch (RuntimeException e) {
			System.out.println("[WARN] Optional step failed: " + label);
			System.out.println("[WARN] " + e.getMessage());
		}
	}

	private void printRate(String label, int itemCount, long elapsedMillis) {
		if (itemCount <= 0) {
			System.out.println("[PERF] " + label + " processed 0 items.");
			return;
		}

		double seconds = elapsedMillis / 1000.0;
		double itemsPerSecond = seconds <= 0 ? itemCount : itemCount / seconds;
		double averageMillis = elapsedMillis / (double) itemCount;

		System.out.println("[PERF] " + label + " processed " + itemCount + " items in " + elapsedMillis + " ms");
		System.out.println("[PERF] " + label + " average: " + String.format("%.2f", averageMillis) + " ms/item");
		System.out.println("[PERF] " + label + " rate: " + String.format("%.2f", itemsPerSecond) + " items/s");
	}

	private void handleSeedFailure(RuntimeException e) {
		System.out.println();
		System.out.println("========== Seed Failure ==========");

		String message = e.getMessage() == null ? "" : e.getMessage();
		String lowerMessage = message.toLowerCase();

		if (lowerMessage.contains("table is full") || lowerMessage.contains("database is full")
				|| lowerMessage.contains("disk full") || lowerMessage.contains("no space left")
				|| lowerMessage.contains("size limit") || lowerMessage.contains("quota")
				|| lowerMessage.contains("too many connections") || lowerMessage.contains("max_questions")
				|| lowerMessage.contains("timeout")) {
			System.out.println("[ERROR] The database/storage provider may be full, slow or over its limit.");
			System.out.println("[ERROR] Seed was stopped to avoid leaving the environment unstable.");
			System.out.println("[TIP] Use DEMO mode or reduce performance/stress constants.");
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

		Organization organization = measureMillis("Base organization loading",
				() -> persist(new Organization("DentalPlus Group", true,
						"Demo organization for DentalPlus development and presentation")));

		CalendarRule clinicCalendarRule = measureMillis("Base calendar rule loading",
				() -> persist(new CalendarRule(workStart, workEnd, workStart, workEnd, workStart, workEnd, workStart,
						workEnd, workStart, workEnd, null, null, null, null, true,
						"Default clinic working calendar")));

		Clinic clinic = measureMillis("Base clinic loading",
				() -> persist(new Clinic(organization, clinicCalendarRule, "DentalPlus Barcelona Centre", true,
						"Spain", "Barcelona", "Carrer de Mallorca 245", "930000001",
						"clinic.barcelona@dentalplus.demo", "Europe/Madrid", "Main demo clinic for DentalPlus")));

		measureMillis("Base calendar breaks and holiday loading", () -> {
			persistBreaks(clinicCalendarRule, breakStart, breakEnd);

			persist(new CalendarHoliday(clinicCalendarRule, "General Demo Holiday", LocalDate.of(2026, 5, 25),
					LocalDate.of(2026, 5, 25), "LOCAL", true, "Demo holiday for calendar diagnostics"));
		});

		List<Box> boxes = measureMillis("Base boxes loading", () -> {
			List<Box> createdBoxes = new ArrayList<>();
			createdBoxes.add(persist(new Box(clinic, "Gabinete 1 - General", true, "General dentistry room")));
			createdBoxes.add(persist(new Box(clinic, "Gabinete 2 - Surgery", true, "Surgery and extraction room")));
			return createdBoxes;
		});

		Box boxOne = boxes.get(0);
		Box boxTwo = boxes.get(1);

		List<StaffSeed> baseStaff = measureMillis("Base staff loading", () -> {
			List<StaffSeed> staff = new ArrayList<>();

			staff.add(createStaff("clara.mendez", "admin@dentalplus.demo", "Clara", "Méndez", "Soler", clinic,
					"ADMIN", null));

			staff.add(createStaff("marta.ruiz", "reception@dentalplus.demo", "Marta", "Ruiz", "Pons", clinic,
					"RECEPTIONIST", null));

			staff.add(createStaff("daniel.ortega", "dentist.general@dentalplus.demo", "Daniel", "Ortega", "Vidal",
					clinic, "DENTIST", "General Dentistry"));

			staff.add(createStaff("irene.navarro", "dentist.surgery@dentalplus.demo", "Irene", "Navarro", "Costa",
					clinic, "DENTIST", "Restorative Dentistry"));

			return staff;
		});

		StaffSeed adminSeed = baseStaff.get(0);
		StaffSeed receptionistSeed = baseStaff.get(1);
		StaffSeed primaryDentistSeed = baseStaff.get(2);
		StaffSeed secondaryDentistSeed = baseStaff.get(3);

		DemoSeed demoSeed = new DemoSeed(this);
		DemoSeedData demoSeedData = demoSeed.load(clinic, boxOne, boxTwo, primaryDentistSeed, secondaryDentistSeed);

		List<Patient> basePatients = demoSeedData.patients();
		List<Odontogram> baseOdontograms = demoSeedData.odontograms();
		List<Appointment> baseAppointments = demoSeedData.appointments();

		Patient patientOne = basePatients.get(0);
		Patient patientTwo = basePatients.get(1);
		Patient patientThree = basePatients.get(2);
		Patient patientFour = basePatients.get(3);

		Odontogram odontogramOne = baseOdontograms.get(0);

		List<StaffSeed> performanceAdmins = new ArrayList<>();
		List<StaffSeed> performanceReceptionists = new ArrayList<>();
		List<StaffSeed> performanceDentists = new ArrayList<>();
		List<Patient> performancePatients = new ArrayList<>();
		List<Odontogram> performanceOdontograms = new ArrayList<>();
		List<Appointment> performanceAppointments = new ArrayList<>();

		if (isPerformanceOrStress(seedMode)) {
			PerformanceSeed performanceSeed = new PerformanceSeed(this);
			PerformanceSeedData performanceSeedData = performanceSeed.load(clinic, boxOne, boxTwo, primaryDentistSeed,
					secondaryDentistSeed);

			performanceAdmins = performanceSeedData.admins();
			performanceReceptionists = performanceSeedData.receptionists();
			performanceDentists = performanceSeedData.dentists();
			performancePatients = performanceSeedData.patients();
			performanceOdontograms = performanceSeedData.odontograms();
			performanceAppointments = performanceSeedData.appointments();
		}

		List<Patient> stressPatients = new ArrayList<>();
		List<Odontogram> stressOdontograms = new ArrayList<>();
		List<Appointment> stressAppointments = new ArrayList<>();

		if (SEED_MODE_STRESS.equals(seedMode)) {
			List<StaffSeed> availableDentists = new ArrayList<>();
			availableDentists.add(primaryDentistSeed);
			availableDentists.add(secondaryDentistSeed);
			availableDentists.addAll(performanceDentists);

			List<Patient> existingPatients = new ArrayList<>();
			existingPatients.addAll(basePatients);
			existingPatients.addAll(performancePatients);

			StressSeed stressSeed = new StressSeed(this);
			StressSeedData stressSeedData = stressSeed.load(clinic, boxes, availableDentists, existingPatients);

			stressPatients = stressSeedData.patients();
			stressOdontograms = stressSeedData.odontograms();
			stressAppointments = stressSeedData.appointments();
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

		List<Person> allPersons = new ArrayList<>();
		addStaffPersons(allPersons, baseStaff);
		addPatientPersons(allPersons, basePatients);
		addStaffPersons(allPersons, performanceAdmins);
		addStaffPersons(allPersons, performanceReceptionists);
		addStaffPersons(allPersons, performanceDentists);
		addPatientPersons(allPersons, performancePatients);
		addPatientPersons(allPersons, stressPatients);

		List<Patient> allPatients = new ArrayList<>();
		allPatients.addAll(basePatients);
		allPatients.addAll(performancePatients);
		allPatients.addAll(stressPatients);

		System.out.println("[OK] Organization created: " + organization.getId());
		System.out.println("[OK] Clinic created: " + clinic.getId());
		System.out.println("[OK] Boxes created: " + boxOne.getId() + ", " + boxTwo.getId());
		System.out.println("[OK] Base staff users created: 4");
		System.out.println("[OK] Base patients created: " + basePatients.size());
		System.out.println("[OK] Base odontograms created: " + baseOdontograms.size());
		System.out.println("[OK] Base dental surface marks created.");
		System.out.println("[OK] Base appointments created: " + baseAppointments.size());

		if (isPerformanceOrStress(seedMode)) {
			System.out.println("[OK] Performance admins created: " + performanceAdmins.size());
			System.out.println("[OK] Performance receptionists created: " + performanceReceptionists.size());
			System.out.println("[OK] Performance dentists created: " + performanceDentists.size());
			System.out.println("[OK] Performance patients created: " + performancePatients.size());
			System.out.println("[OK] Performance odontograms created: " + performanceOdontograms.size());
			System.out.println("[OK] Performance appointments created: " + performanceAppointments.size());
		}

		if (SEED_MODE_STRESS.equals(seedMode)) {
			System.out.println("[OK] Stress patients created: " + stressPatients.size());
			System.out.println("[OK] Stress odontograms created: " + stressOdontograms.size());
			System.out.println("[OK] Stress appointments created: " + stressAppointments.size());
		}

		System.out.println("[OK] Persons eligible for profile images: " + allPersons.size());
		System.out.println("[OK] Patients eligible for PDF documents: " + allPatients.size());

		return new SeedState(organization, clinic, clinicCalendarRule, boxOne, boxTwo, adminSeed, receptionistSeed,
				primaryDentistSeed, secondaryDentistSeed, patientOne, patientTwo, patientThree, patientFour,
				odontogramOne, baseAppointments.get(0), performanceAdmins, performanceReceptionists,
				performanceDentists, performancePatients, performanceOdontograms, performanceAppointments,
				stressPatients, stressOdontograms, stressAppointments, allPersons, allPatients);
	}

	private void addStaffPersons(List<Person> persons, List<StaffSeed> staffSeeds) {
		for (StaffSeed staffSeed : staffSeeds) {
			if (staffSeed != null && staffSeed.person() != null) {
				persons.add(staffSeed.person());
			}
		}
	}

	private void addPatientPersons(List<Person> persons, List<Patient> patients) {
		for (Patient patient : patients) {
			if (patient != null && patient.getPerson() != null) {
				persons.add(patient.getPerson());
			}
		}
	}

	void persistBreaks(CalendarRule calendarRule, LocalTime breakStart, LocalTime breakEnd) {
		persist(new CalendarBreak(calendarRule, "MONDAY", breakStart, breakEnd, true, "Default lunch break"));
		persist(new CalendarBreak(calendarRule, "TUESDAY", breakStart, breakEnd, true, "Default lunch break"));
		persist(new CalendarBreak(calendarRule, "WEDNESDAY", breakStart, breakEnd, true, "Default lunch break"));
		persist(new CalendarBreak(calendarRule, "THURSDAY", breakStart, breakEnd, true, "Default lunch break"));
		persist(new CalendarBreak(calendarRule, "FRIDAY", breakStart, breakEnd, true, "Default lunch break"));
	}

	StaffSeed createStaff(String username, String email, String name, String firstSurname, String secondSurname,
			Clinic clinic, String role, String speciality) {
		User user = persist(
				new User(username, passwordEncoder.encode(DEFAULT_PASSWORD), "SYSTEM", "en", true, "Seed user"));

		Person person = persist(new Person(name, firstSurname, secondSurname, LocalDate.of(1990, 1, 1), "OTHER", email,
				"+34", "600000000", "Default Address", "Barcelona", null, true, "Seed staff person"));

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

	Patient createPatient(Clinic clinic, String name, String firstSurname, String secondSurname, String email,
			String phoneNumber, LocalDate birthDate, String medicalAlert, String consultationReason) {
		Person person = persist(new Person(name, firstSurname, secondSurname, birthDate, "OTHER", email, "+34",
				phoneNumber, "Default Address", "Barcelona", null, true, consultationReason));

		return persist(new Patient(person, null, clinic, true, medicalAlert, consultationReason));
	}

	Odontogram createOdontogram(Patient patient) {
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

	void createSurfaceMark(Odontogram odontogram, Integer pieceNumber, String surfaceType, String markType,
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

	void createPieceState(Odontogram odontogram, Integer pieceNumber, String stateType, String notes) {
		DentalPiece dentalPiece = findSeedDentalPiece(odontogram, pieceNumber);

		if (dentalPiece == null) {
			throw new IllegalStateException("Seed dental piece not found: " + pieceNumber);
		}

		persist(new DentalPieceState(dentalPiece, stateType, notes));
	}

	DentalPiece findSeedDentalPiece(Odontogram odontogram, Integer pieceNumber) {
		return dentalPieceDao.findByOdontogramIdAndPieceNumber(odontogram.getId(), pieceNumber);
	}

	DentalSurface findSeedDentalSurface(DentalPiece dentalPiece, String surfaceType) {
		return dentalSurfaceDao.findByDentalPieceIdAndSurfaceType(dentalPiece.getId(), surfaceType);
	}

	private void uploadSeedExternalFiles(SeedState seedState, String seedMode) {
		System.out.println();
		System.out.println("========== External File Uploads ==========");
		System.out.println("[INFO] Mode: " + seedMode);
		System.out.println("[INFO] Profile image pool: " + PROFILE_IMAGE_COUNT + " files");
		System.out.println("[INFO] Persons to upload profile images: " + seedState.allPersons().size());
		System.out.println("[INFO] Patients to upload PDFs: " + seedState.allPatients().size());
		System.out.println("[INFO] Expected PDF uploads: " + (seedState.allPatients().size() * 2));

		int uploadedImages = uploadProfileImagesForPersons(seedState.allPersons());
		int uploadedDocuments = uploadDocumentsForPatients(seedState.allPatients());

		System.out.println("[OK] Profile images uploaded: " + uploadedImages + "/" + seedState.allPersons().size());
		System.out.println("[OK] PDF documents uploaded: " + uploadedDocuments + "/"
				+ (seedState.allPatients().size() * 2));
		System.out.println("===========================================");
		System.out.println();
	}

	private int uploadProfileImagesForPersons(List<Person> persons) {
		long start = System.nanoTime();
		int uploaded = 0;

		for (Person person : persons) {
			int imageIndex = resolveRandomProfileImageIndex();

			if (uploadOptionalProfileImage(person, imageIndex)) {
				uploaded++;
			}
		}

		long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
		printRate("Profile image uploads", uploaded, elapsedMillis);

		return uploaded;
	}

	private int uploadDocumentsForPatients(List<Patient> patients) {
		long start = System.nanoTime();
		int uploaded = 0;

		for (Patient patient : patients) {
			if (uploadOptionalDocument(patient, GENERAL_CONSENT_RESOURCE, "General Consent Document", "CONSENT")) {
				uploaded++;
			}

			if (uploadOptionalDocument(patient, TREATMENT_PLAN_RESOURCE, "Treatment Plan Document", "REPORT")) {
				uploaded++;
			}
		}

		long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
		printRate("PDF document uploads", uploaded, elapsedMillis);

		return uploaded;
	}

	private int resolveRandomProfileImageIndex() {
		return profileImageRandom.nextInt(PROFILE_IMAGE_COUNT) + 1;
	}

	private String resolveProfileImageResourcePath(int imageIndex) {
		return String.format(PROFILE_IMAGE_RESOURCE_PATTERN, imageIndex);
	}

	boolean uploadOptionalProfileImage(Person person) {
		return uploadOptionalProfileImage(person, resolveRandomProfileImageIndex());
	}

	boolean uploadOptionalProfileImage(Person person, int imageIndex) {
		if (person == null || person.getId() == null) {
			System.out.println("[WARN] Profile image upload skipped because person is missing or not persisted.");
			return false;
		}

		String resourcePath = resolveProfileImageResourcePath(imageIndex);
		Resource resource = resourceLoader.getResource(resourcePath);

		if (!resource.exists()) {
			System.out.println("[SKIP] Optional profile image not found: "
					+ resourcePath.replace("classpath:", "src/main/resources/"));
			return false;
		}

		try {
			String fallbackFilename = String.format("profile-%02d.jpg", imageIndex);
			MultipartFile file = multipartFromResource(resource, fallbackFilename, "image/jpeg");
			String imageUrl = cloudinaryService.uploadProfileImage(file, person.getId());

			transactionTemplate.executeWithoutResult(status -> {
				Person managedPerson = entityManager.find(Person.class, person.getId());

				if (managedPerson != null) {
					managedPerson.setProfileImage(imageUrl);
					entityManager.merge(managedPerson);
				}
			});

			System.out.println("[OK] Uploaded profile image " + fallbackFilename + " for person id: " + person.getId());
			return true;
		} catch (RuntimeException | IOException e) {
			System.out.println("[WARN] Profile image upload skipped for person id: " + person.getId());
			System.out.println("[WARN] " + e.getMessage());
			return false;
		}
	}

	boolean uploadOptionalDocument(Patient patient, String resourcePath, String documentName, String documentType) {
		if (patient == null || patient.getId() == null) {
			System.out.println("[WARN] Document upload skipped because patient is missing or not persisted.");
			return false;
		}

		Resource resource = resourceLoader.getResource(resourcePath);

		if (!resource.exists()) {
			System.out.println(
					"[SKIP] Optional document not found: " + resourcePath.replace("classpath:", "src/main/resources/"));
			return false;
		}

		try {
			String fallbackFilename = resourcePath.contains("general-consent") ? "general-consent.pdf"
					: "treatment-plan.pdf";

			MultipartFile file = multipartFromResource(resource, fallbackFilename, "application/pdf");
			String storagePath = supabaseStorageService.uploadPdf(file, "patients/" + patient.getId());

			transactionTemplate.executeWithoutResult(status -> {
				Patient managedPatient = entityManager.find(Patient.class, patient.getId());

				if (managedPatient == null) {
					throw new IllegalStateException("Patient not found for document upload: " + patient.getId());
				}

				Document document = new Document(managedPatient, documentName, storagePath, "application/pdf",
						documentType, true, "Seed document");

				entityManager.persist(document);
				entityManager.flush();

				if (firstDocumentId == null) {
					firstDocumentId = document.getId();
				}
			});

			System.out.println("[OK] Uploaded " + documentName + " for patient id: " + patient.getId());
			return true;
		} catch (RuntimeException | IOException e) {
			System.out.println("[WARN] Document upload skipped for patient id: " + patient.getId());
			System.out.println("[WARN] " + e.getMessage());
			return false;
		}
	}

	MultipartFile multipartFromResource(Resource resource, String fallbackFilename, String contentType)
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
			userService.login(new LoginRequest("admin@dentalplus.demo", DEFAULT_PASSWORD));
			System.out.println("[OK] Admin login works with Postman credentials.");
		} catch (RuntimeException e) {
			throw new IllegalStateException("Admin login diagnostic failed", e);
		}

		try {
			userService.login(new LoginRequest("reception@dentalplus.demo", DEFAULT_PASSWORD));
			System.out.println("[OK] Receptionist login works with Postman credentials.");
		} catch (RuntimeException e) {
			throw new IllegalStateException("Receptionist login diagnostic failed", e);
		}

		try {
			userService.login(new LoginRequest("dentist.general@dentalplus.demo", DEFAULT_PASSWORD));
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

	<T> Long countEntities(Class<T> entityClass) {
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
			System.out.println("[WARN] Seed row count is high for a small database.");
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
		System.out.println("admin@dentalplus.demo / " + DEFAULT_PASSWORD);
		System.out.println("reception@dentalplus.demo / " + DEFAULT_PASSWORD);
		System.out.println("dentist.general@dentalplus.demo / " + DEFAULT_PASSWORD);
		System.out.println("dentist.surgery@dentalplus.demo / " + DEFAULT_PASSWORD);

		if (isPerformanceOrStress(seedMode)) {
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
		System.out.println("Patient 4 -> BLEEDING_RISK:ANTICOAGULANTS");
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

		if (isPerformanceOrStress(seedMode)) {
			System.out.println();
			System.out.println("Performance dataset:");
			System.out.println("Extra admins        = " + seedState.performanceAdmins().size());
			System.out.println("Extra receptionists = " + seedState.performanceReceptionists().size());
			System.out.println("Extra dentists      = " + seedState.performanceDentists().size());
			System.out.println("Extra patients      = " + seedState.performancePatients().size());
			System.out.println("Extra odontograms   = " + seedState.performanceOdontograms().size());
			System.out.println("Extra appointments  = " + seedState.performanceAppointments().size());
		}

		if (SEED_MODE_STRESS.equals(seedMode)) {
			System.out.println();
			System.out.println("Stress dataset:");
			System.out.println("Stress patients      = " + seedState.stressPatients().size());
			System.out.println("Stress odontograms   = " + seedState.stressOdontograms().size());
			System.out.println("Stress appointments  = " + seedState.stressAppointments().size());
		}

		System.out.println();
		System.out.println("External file targets:");
		System.out.println("Persons with possible profile image = " + seedState.allPersons().size());
		System.out.println("Patients with possible PDFs         = " + seedState.allPatients().size());
		System.out.println("Expected patient PDFs               = " + (seedState.allPatients().size() * 2));

		System.out.println("============================================");
		System.out.println();
	}

	void assertCondition(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException("[FAIL] " + message);
		}

		System.out.println("[OK] " + message);
	}

	<T> T persist(T entity) {
		entityManager.persist(entity);
		return entity;
	}

	TransactionTemplate transactionTemplate() {
		return transactionTemplate;
	}

	UserService userService() {
		return userService;
	}

	AppointmentService appointmentService() {
		return appointmentService;
	}

	OdontogramService odontogramService() {
		return odontogramService;
	}

	record SeedState(Organization organization, Clinic clinic, CalendarRule clinicCalendarRule, Box boxOne,
			Box boxTwo, StaffSeed admin, StaffSeed receptionist, StaffSeed primaryDentist, StaffSeed secondaryDentist,
			Patient patientOne, Patient patientTwo, Patient patientThree, Patient patientFour, Odontogram odontogramOne,
			Appointment appointmentOne, List<StaffSeed> performanceAdmins, List<StaffSeed> performanceReceptionists,
			List<StaffSeed> performanceDentists, List<Patient> performancePatients,
			List<Odontogram> performanceOdontograms, List<Appointment> performanceAppointments,
			List<Patient> stressPatients, List<Odontogram> stressOdontograms,
			List<Appointment> stressAppointments, List<Person> allPersons, List<Patient> allPatients) {
	}

	record DemoSeedData(List<Patient> patients, List<Odontogram> odontograms, List<Appointment> appointments) {
	}

	record PerformanceSeedData(List<StaffSeed> admins, List<StaffSeed> receptionists, List<StaffSeed> dentists,
			List<Patient> patients, List<Odontogram> odontograms, List<Appointment> appointments) {
	}

	record StressSeedData(List<Patient> patients, List<Odontogram> odontograms, List<Appointment> appointments) {
	}

	record StaffSeed(User user, Person person, Admin admin, Receptionist receptionist, Dentist dentist) {
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