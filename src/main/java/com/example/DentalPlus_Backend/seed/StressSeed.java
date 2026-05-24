package com.example.DentalPlus_Backend.seed;

import com.example.DentalPlus_Backend.model.Appointment;
import com.example.DentalPlus_Backend.model.Box;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.Odontogram;
import com.example.DentalPlus_Backend.model.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

class StressSeed {

	private static final int STRESS_PATIENT_COUNT = 1000;
	private static final int STRESS_ODONTOGRAM_COUNT = 50;
	private static final int STRESS_APPOINTMENT_COUNT = 5000;

	private static final LocalDate STRESS_APPOINTMENT_START_DATE = LocalDate.of(2026, 6, 1);

	private final ApplicationSeed seed;

	StressSeed(ApplicationSeed seed) {
		this.seed = seed;
	}

	ApplicationSeed.StressSeedData load(Clinic clinic, List<Box> boxes, List<ApplicationSeed.StaffSeed> dentists,
			List<Patient> existingPatients) {
		List<Patient> stressPatients = seed.measureMillis("Stress patients loading",
				() -> createStressPatients(clinic));

		List<Odontogram> stressOdontograms = seed.measureMillis("Stress odontograms loading",
				() -> createStressOdontograms(stressPatients));

		seed.measureMillis("Stress dental marks loading", () -> createStressDentalMarks(stressOdontograms));

		List<Appointment> stressAppointments = seed.measureMillis("Stress appointments loading",
				() -> createStressAppointments(boxes, dentists, stressPatients, existingPatients));

		System.out.println("[OK] Stress patients created: " + stressPatients.size());
		System.out.println("[OK] Stress odontograms created: " + stressOdontograms.size());
		System.out.println("[OK] Stress appointments created: " + stressAppointments.size());

		return new ApplicationSeed.StressSeedData(stressPatients, stressOdontograms, stressAppointments);
	}

	private List<Patient> createStressPatients(Clinic clinic) {
		List<Patient> patients = new ArrayList<>();

		for (int i = 1; i <= STRESS_PATIENT_COUNT; i++) {
			PatientIdentity identity = resolvePatientIdentity(i);
			String suffix = String.format("%04d", i);

			Patient patient = seed.createPatient(clinic, identity.name(), identity.firstSurname(),
					identity.secondSurname(), "stress.patient." + suffix + "@dentalplus.demo", resolvePhoneNumber(i),
					resolveBirthDate(i), resolveMedicalAlert(i), resolveConsultationReason(i));

			patients.add(patient);
		}

		return patients;
	}

	private List<Odontogram> createStressOdontograms(List<Patient> stressPatients) {
		List<Odontogram> odontograms = new ArrayList<>();
		int odontogramsToCreate = Math.min(STRESS_ODONTOGRAM_COUNT, stressPatients.size());

		for (int i = 0; i < odontogramsToCreate; i++) {
			odontograms.add(seed.createOdontogram(stressPatients.get(i)));
		}

		return odontograms;
	}

	private void createStressDentalMarks(List<Odontogram> odontograms) {
		for (int i = 0; i < odontograms.size(); i++) {
			Odontogram odontogram = odontograms.get(i);
			int pattern = i % 10;

			if (pattern == 0) {
				seed.createSurfaceMark(odontogram, 11, "MESIAL", "CARIES", "PENDING",
						"Stress finding: mesial caries pending review");
				seed.createSurfaceMark(odontogram, 16, "OCCLUSAL", "FILLING", "DONE",
						"Stress finding: previous occlusal restoration");
				seed.createPieceState(odontogram, 26, "CROWN_PENDING", "Stress finding: crown planned");
				seed.createSurfaceMark(odontogram, 36, "OCCLUSAL", "FISSURE_SEALANT", "DONE",
						"Stress finding: preventive fissure sealant");
			} else if (pattern == 1) {
				seed.createSurfaceMark(odontogram, 21, "DISTAL", "CARIES", "PENDING",
						"Stress finding: distal caries observation");
				seed.createSurfaceMark(odontogram, 46, "VESTIBULAR", "CROWN", "PENDING",
						"Stress finding: vestibular crown assessment");
				seed.createPieceState(odontogram, 15, "ENDODONTICS_DONE", "Stress finding: previous endodontics");
			} else if (pattern == 2) {
				seed.createSurfaceMark(odontogram, 14, "MESIAL", "FILLING", "DONE",
						"Stress finding: existing mesial filling");
				seed.createSurfaceMark(odontogram, 24, "DISTAL", "CARIES", "PENDING",
						"Stress finding: distal caries follow-up");
				seed.createPieceState(odontogram, 55, "NATURAL_ABSENCE", "Stress finding: natural temporary absence");
			} else if (pattern == 3) {
				seed.createSurfaceMark(odontogram, 12, "LINGUAL", "CARIES", "PENDING",
						"Stress finding: lingual caries");
				seed.createSurfaceMark(odontogram, 22, "VESTIBULAR", "FILLING", "DONE",
						"Stress finding: vestibular restoration");
				seed.createPieceState(odontogram, 36, "PERIODONTAL_TREATMENT_PENDING",
						"Stress finding: periodontal review needed");
			} else if (pattern == 4) {
				seed.createSurfaceMark(odontogram, 17, "OCCLUSAL", "CARIES", "PENDING",
						"Stress finding: occlusal caries");
				seed.createSurfaceMark(odontogram, 27, "OCCLUSAL", "FILLING", "DONE",
						"Stress finding: molar filling");
				seed.createPieceState(odontogram, 47, "EXTRACTION_PENDING", "Stress finding: extraction assessment");
			} else if (pattern == 5) {
				seed.createSurfaceMark(odontogram, 31, "VESTIBULAR", "FILLING", "DONE",
						"Stress finding: anterior restoration");
				seed.createSurfaceMark(odontogram, 41, "LINGUAL", "CARIES", "PENDING",
						"Stress finding: lingual caries observation");
				seed.createPieceState(odontogram, 46, "CROWN_DONE", "Stress finding: crown already placed");
			} else if (pattern == 6) {
				seed.createSurfaceMark(odontogram, 54, "OCCLUSAL", "CARIES", "PENDING",
						"Stress finding: temporary molar caries");
				seed.createSurfaceMark(odontogram, 64, "OCCLUSAL", "FISSURE_SEALANT", "DONE",
						"Stress finding: temporary molar sealant");
				seed.createPieceState(odontogram, 84, "FILLING_PENDING", "Stress finding: filling pending");
			} else if (pattern == 7) {
				seed.createSurfaceMark(odontogram, 35, "OCCLUSAL", "FILLING", "DONE",
						"Stress finding: premolar filling");
				seed.createSurfaceMark(odontogram, 45, "DISTAL", "CARIES", "PENDING",
						"Stress finding: distal premolar caries");
				seed.createPieceState(odontogram, 37, "ENDODONTICS_PENDING", "Stress finding: endodontics planned");
			} else if (pattern == 8) {
				seed.createSurfaceMark(odontogram, 13, "MESIAL", "CARIES", "PENDING",
						"Stress finding: canine mesial caries");
				seed.createSurfaceMark(odontogram, 23, "DISTAL", "FILLING", "DONE",
						"Stress finding: canine distal filling");
				seed.createPieceState(odontogram, 33, "HEALTHY", "Stress finding: healthy follow-up");
			} else {
				seed.createSurfaceMark(odontogram, 18, "OCCLUSAL", "CARIES", "PENDING",
						"Stress finding: wisdom tooth caries");
				seed.createSurfaceMark(odontogram, 28, "OCCLUSAL", "FILLING", "DONE",
						"Stress finding: wisdom tooth restoration");
				seed.createPieceState(odontogram, 48, "EXTRACTION_DONE", "Stress finding: previous extraction");
			}
		}
	}

	private List<Appointment> createStressAppointments(List<Box> boxes, List<ApplicationSeed.StaffSeed> dentists,
			List<Patient> stressPatients, List<Patient> existingPatients) {
		if (boxes == null || boxes.isEmpty()) {
			throw new IllegalStateException("Stress appointments require at least one box");
		}

		List<ApplicationSeed.StaffSeed> availableDentists = dentists == null ? List.of()
				: dentists.stream().filter(staff -> staff != null && staff.dentist() != null).toList();

		if (availableDentists.isEmpty()) {
			throw new IllegalStateException("Stress appointments require at least one dentist");
		}

		List<Patient> appointmentPatients = new ArrayList<>();
		appointmentPatients.addAll(stressPatients);

		if (existingPatients != null) {
			appointmentPatients.addAll(existingPatients);
		}

		if (appointmentPatients.isEmpty()) {
			throw new IllegalStateException("Stress appointments require at least one patient");
		}

		List<Appointment> appointments = new ArrayList<>();
		LocalTime[] appointmentTimes = buildAppointmentTimes();
		String[] treatments = buildTreatments();
		String[] statuses = { "SCHEDULED", "SCHEDULED", "SCHEDULED", "COMPLETED", "CANCELLED" };

		int created = 0;
		int dayOffset = 0;

		while (created < STRESS_APPOINTMENT_COUNT) {
			LocalDate date = nextBusinessDate(STRESS_APPOINTMENT_START_DATE, dayOffset);

			for (int timeIndex = 0; timeIndex < appointmentTimes.length
					&& created < STRESS_APPOINTMENT_COUNT; timeIndex++) {
				for (int boxIndex = 0; boxIndex < boxes.size() && created < STRESS_APPOINTMENT_COUNT; boxIndex++) {
					Box box = boxes.get(boxIndex);
					ApplicationSeed.StaffSeed dentistSeed = availableDentists
							.get((created + timeIndex + boxIndex) % availableDentists.size());
					Patient patient = appointmentPatients.get(created % appointmentPatients.size());

					LocalDateTime start = LocalDateTime.of(date, appointmentTimes[timeIndex]);
					LocalDateTime end = start.plusMinutes(resolveAppointmentDurationMinutes(created));
					String treatment = treatments[created % treatments.length];
					String status = statuses[created % statuses.length];

					Appointment appointment = seed.persist(new Appointment(box, dentistSeed.dentist(), patient, start,
							end, status, treatment, "Stress appointment " + String.format("%05d", created + 1), true));

					appointments.add(appointment);
					created++;
				}
			}

			dayOffset++;
		}

		return appointments;
	}

	private LocalTime[] buildAppointmentTimes() {
		return new LocalTime[] { LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(10, 30),
				LocalTime.of(11, 0), LocalTime.of(11, 30), LocalTime.of(12, 0), LocalTime.of(12, 30),
				LocalTime.of(14, 0), LocalTime.of(14, 30), LocalTime.of(15, 0), LocalTime.of(15, 30),
				LocalTime.of(16, 0), LocalTime.of(16, 30), LocalTime.of(17, 0), LocalTime.of(17, 30) };
	}

	private String[] buildTreatments() {
		return new String[] { "Routine checkup", "Dental cleaning", "Caries review", "Composite restoration",
				"Endodontic assessment", "Periodontal maintenance", "Crown assessment", "Bridge review",
				"Implant follow-up", "Orthodontic review", "Emergency consultation", "Pediatric checkup",
				"Tooth extraction review", "Whitening consultation", "Prosthetic adjustment" };
	}

	private LocalDate nextBusinessDate(LocalDate startDate, int businessDayOffset) {
		LocalDate date = startDate;
		int remaining = businessDayOffset;

		while (remaining > 0) {
			date = date.plusDays(1);

			if (isBusinessDay(date)) {
				remaining--;
			}
		}

		while (!isBusinessDay(date)) {
			date = date.plusDays(1);
		}

		return date;
	}

	private boolean isBusinessDay(LocalDate date) {
		return date.getDayOfWeek().getValue() >= 1 && date.getDayOfWeek().getValue() <= 5;
	}

	private int resolveAppointmentDurationMinutes(int index) {
		if (index % 11 == 0) {
			return 60;
		}

		if (index % 7 == 0) {
			return 45;
		}

		return 30;
	}

	private PatientIdentity resolvePatientIdentity(int index) {
		String[] names = { "Lucía", "Martín", "Sofía", "Mateo", "Valentina", "Leo", "Julia", "Hugo", "Emma", "Daniel",
				"Alba", "Pablo", "Carla", "Alejandro", "Noa", "Adrián", "Claudia", "Diego", "Vera", "Mario", "Aina",
				"Marc", "Nerea", "Pol", "Laia", "Nil", "Paula", "Jan", "Mireia", "Arnau" };

		String[] firstSurnames = { "García", "Martínez", "López", "Sánchez", "Pérez", "González", "Rodríguez",
				"Fernández", "Moreno", "Muñoz", "Romero", "Navarro", "Torres", "Domínguez", "Vázquez", "Ramos", "Gil",
				"Serrano", "Molina", "Ortega" };

		String[] secondSurnames = { "Vidal", "Soler", "Costa", "Riera", "Pons", "Serra", "Ferrer", "Castro", "Iglesias",
				"Medina", "Cabrera", "Campos", "Reyes", "Herrera", "Marín", "Fuentes", "Aguilar", "León", "Cano",
				"Suárez" };

		String name = names[index % names.length];
		String firstSurname = firstSurnames[(index * 3) % firstSurnames.length];
		String secondSurname = secondSurnames[(index * 7) % secondSurnames.length];

		return new PatientIdentity(name, firstSurname, secondSurname);
	}

	private String resolvePhoneNumber(int index) {
		return "6" + String.format("%08d", 10000000 + index);
	}

	private LocalDate resolveBirthDate(int index) {
		int year = 1945 + (index % 60);
		int month = ((index - 1) % 12) + 1;
		int day = ((index - 1) % 27) + 1;

		return LocalDate.of(year, month, day);
	}

	private String resolveMedicalAlert(int index) {
		if (index % 97 == 0) {
			return "ALLERGY:PENICILLIN|BLEEDING_RISK:ANTICOAGULANTS|INFECTION_RISK:HEPATITIS_B";
		}

		if (index % 53 == 0) {
			return "DIABETES";
		}

		if (index % 41 == 0) {
			return "CARDIAC_RISK";
		}

		if (index % 29 == 0) {
			return "BLEEDING_RISK:ANTICOAGULANTS";
		}

		if (index % 17 == 0) {
			return "INFECTION_RISK:HEPATITIS_B";
		}

		if (index % 11 == 0) {
			return "ALLERGY:PENICILLIN";
		}

		return null;
	}

	private String resolveConsultationReason(int index) {
		String[] reasons = { "Routine dental checkup", "Dental cleaning and prevention", "Caries assessment",
				"Gum sensitivity review", "Restoration follow-up", "Orthodontic review", "Crown consultation",
				"Implant follow-up", "Emergency tooth pain", "Pediatric dental checkup", "Aesthetic consultation",
				"Periodontal maintenance" };

		return reasons[index % reasons.length];
	}

	void runDiagnostics(ApplicationSeed.SeedState seedState) {
		System.out.println();
		System.out.println("========== Stress Diagnostics ==========");

		seed.assertCondition(seedState.stressPatients().size() == STRESS_PATIENT_COUNT,
				"Stress patients expected: " + STRESS_PATIENT_COUNT);

		seed.assertCondition(seedState.stressOdontograms().size() == STRESS_ODONTOGRAM_COUNT,
				"Stress odontograms expected: " + STRESS_ODONTOGRAM_COUNT);

		seed.assertCondition(seedState.stressAppointments().size() == STRESS_APPOINTMENT_COUNT,
				"Stress appointments expected: " + STRESS_APPOINTMENT_COUNT);

		if (!seedState.stressOdontograms().isEmpty()) {
			Patient firstStressPatient = seedState.stressPatients().get(0);

			seed.measureMillis("Admin get first stress odontogram by patient", () -> {
				seed.odontogramService().getOdontogramByPatientId(firstStressPatient.getId(),
						seedState.admin().user().getId());
			});
		}

		if (seedState.stressOdontograms().size() > 25) {
			Patient middleStressPatient = seedState.stressPatients().get(25);

			seed.measureMillis("Admin get middle stress odontogram by patient", () -> {
				seed.odontogramService().getOdontogramByPatientId(middleStressPatient.getId(),
						seedState.admin().user().getId());
			});
		}

		if (seedState.stressOdontograms().size() > 49) {
			Patient lastStressOdontogramPatient = seedState.stressPatients().get(49);

			seed.measureMillis("Admin get last stress odontogram by patient", () -> {
				seed.odontogramService().getOdontogramByPatientId(lastStressOdontogramPatient.getId(),
						seedState.admin().user().getId());
			});
		}

		seed.measureMillis("Admin stress appointment list by date", () -> {
			seed.appointmentService().getAppointments(seedState.admin().user().getId(), STRESS_APPOINTMENT_START_DATE,
					null, null, null);
		});

		System.out.println("[OK] Stress diagnostics completed.");
		System.out.println("========================================");
		System.out.println();
	}

	private record PatientIdentity(String name, String firstSurname, String secondSurname) {
	}
}