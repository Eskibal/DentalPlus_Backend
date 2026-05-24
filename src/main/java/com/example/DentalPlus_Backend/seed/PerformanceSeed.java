package com.example.DentalPlus_Backend.seed;

import com.example.DentalPlus_Backend.dto.LoginRequest;
import com.example.DentalPlus_Backend.model.Appointment;
import com.example.DentalPlus_Backend.model.Box;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Odontogram;
import com.example.DentalPlus_Backend.model.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

class PerformanceSeed {

	private final ApplicationSeed seed;

	PerformanceSeed(ApplicationSeed seed) {
		this.seed = seed;
	}

	ApplicationSeed.PerformanceSeedData load(Clinic clinic, Box boxOne, Box boxTwo,
			ApplicationSeed.StaffSeed primaryDentistSeed,
			ApplicationSeed.StaffSeed secondaryDentistSeed) {

		List<ApplicationSeed.StaffSeed> performanceAdmins = seed.measureMillis("Performance admins loading", () -> {
			List<ApplicationSeed.StaffSeed> admins = new ArrayList<>();

			for (int i = 1; i <= ApplicationSeed.PERFORMANCE_EXTRA_ADMIN_COUNT; i++) {
				String suffix = String.format("%02d", i);

				admins.add(seed.createStaff(
						"admin.perf." + suffix,
						"admin.perf." + suffix + "@example.com",
						"Admin",
						"Performance",
						suffix,
						clinic,
						"ADMIN",
						null
				));
			}

			return admins;
		});

		List<ApplicationSeed.StaffSeed> performanceReceptionists = seed.measureMillis(
				"Performance receptionists loading", () -> {
					List<ApplicationSeed.StaffSeed> receptionists = new ArrayList<>();

					for (int i = 1; i <= ApplicationSeed.PERFORMANCE_EXTRA_RECEPTIONIST_COUNT; i++) {
						String suffix = String.format("%02d", i);

						receptionists.add(seed.createStaff(
								"receptionist.perf." + suffix,
								"receptionist.perf." + suffix + "@example.com",
								"Receptionist",
								"Performance",
								suffix,
								clinic,
								"RECEPTIONIST",
								null
						));
					}

					return receptionists;
				});

		List<ApplicationSeed.StaffSeed> performanceDentists = seed.measureMillis(
				"Performance dentists loading", () -> {
					List<ApplicationSeed.StaffSeed> dentists = new ArrayList<>();

					String[] specialities = {
							"General Dentistry",
							"Restorative Dentistry",
							"Endodontics",
							"Periodontics",
							"Prosthodontics",
							"Pediatric Dentistry"
					};

					for (int i = 1; i <= ApplicationSeed.PERFORMANCE_EXTRA_DENTIST_COUNT; i++) {
						String suffix = String.format("%02d", i);
						String speciality = specialities[(i - 1) % specialities.length];

						dentists.add(seed.createStaff(
								"dentist.perf." + suffix,
								"dentist.perf." + suffix + "@example.com",
								"Dentist",
								"Performance",
								suffix,
								clinic,
								"DENTIST",
								speciality
						));
					}

					return dentists;
				});

		List<Patient> performancePatients = seed.measureMillis("Performance patients loading", () -> {
			List<Patient> patients = new ArrayList<>();

			for (int i = 1; i <= ApplicationSeed.PERFORMANCE_PATIENT_COUNT; i++) {
				String suffix = String.format("%03d", i);

				patients.add(seed.createPatient(
						clinic,
						"Performance",
						"Patient",
						suffix,
						"patient.performance." + suffix + "@example.com",
						"20000000" + suffix,
						LocalDate.of(1980 + (i % 25), ((i - 1) % 12) + 1, ((i - 1) % 27) + 1),
						resolvePerformanceMedicalAlert(i),
						"Performance seed patient " + suffix
				));
			}

			return patients;
		});

		List<Odontogram> performanceOdontograms = seed.measureMillis("Performance odontograms loading", () -> {
			List<Odontogram> odontograms = new ArrayList<>();
			int odontogramsToCreate = Math.min(ApplicationSeed.PERFORMANCE_ODONTOGRAM_COUNT,
					performancePatients.size());

			for (int i = 0; i < odontogramsToCreate; i++) {
				odontograms.add(seed.createOdontogram(performancePatients.get(i)));
			}

			return odontograms;
		});

		List<Appointment> performanceAppointments = seed.measureMillis("Performance appointments loading", () -> {
			List<Appointment> appointments = new ArrayList<>();
			List<Dentist> dentists = new ArrayList<>();

			dentists.add(primaryDentistSeed.dentist());
			dentists.add(secondaryDentistSeed.dentist());

			for (ApplicationSeed.StaffSeed performanceDentist : performanceDentists) {
				dentists.add(performanceDentist.dentist());
			}

			List<Box> boxes = List.of(boxOne, boxTwo);

			LocalTime[] appointmentTimes = {
					LocalTime.of(9, 0),
					LocalTime.of(9, 30),
					LocalTime.of(10, 0),
					LocalTime.of(10, 30),
					LocalTime.of(11, 0),
					LocalTime.of(11, 30),
					LocalTime.of(12, 0),
					LocalTime.of(12, 30),
					LocalTime.of(14, 0),
					LocalTime.of(14, 30),
					LocalTime.of(15, 0),
					LocalTime.of(15, 30),
					LocalTime.of(16, 0),
					LocalTime.of(16, 30),
					LocalTime.of(17, 0)
			};

			String[] treatments = {
					"Routine checkup",
					"Dental cleaning",
					"Caries review",
					"Restoration follow-up",
					"Endodontic assessment",
					"Periodontal review",
					"Crown assessment",
					"Bridge review"
			};

			int created = 0;
			int dayOffset = 0;

			while (created < ApplicationSeed.PERFORMANCE_APPOINTMENT_COUNT) {
				LocalDate date = LocalDate.of(2026, 5, 5).plusDays(dayOffset);

				for (int timeIndex = 0; timeIndex < appointmentTimes.length
						&& created < ApplicationSeed.PERFORMANCE_APPOINTMENT_COUNT; timeIndex++) {
					for (int boxIndex = 0; boxIndex < boxes.size()
							&& created < ApplicationSeed.PERFORMANCE_APPOINTMENT_COUNT; boxIndex++) {

						Box box = boxes.get(boxIndex);
						Dentist dentist = dentists.get((dayOffset + timeIndex + boxIndex) % dentists.size());
						Patient patient = performancePatients.get(created % performancePatients.size());

						LocalDateTime start = LocalDateTime.of(date, appointmentTimes[timeIndex]);
						LocalDateTime end = start.plusMinutes(30);
						String treatment = treatments[created % treatments.length];

						appointments.add(seed.persist(new Appointment(
								box,
								dentist,
								patient,
								start,
								end,
								"SCHEDULED",
								treatment,
								"Performance appointment " + String.format("%03d", created + 1),
								true
						)));

						created++;
					}
				}

				dayOffset++;
			}

			return appointments;
		});

		return new ApplicationSeed.PerformanceSeedData(
				performanceAdmins,
				performanceReceptionists,
				performanceDentists,
				performancePatients,
				performanceOdontograms,
				performanceAppointments
		);
	}

	void uploadOptionalFiles(ApplicationSeed.SeedState seedState) {
		seed.transactionTemplate().executeWithoutResult(status -> {
			seedState.performanceAdmins().stream()
					.limit(1)
					.forEach(staff -> seed.uploadOptionalProfileImage(staff.person()));

			seedState.performanceReceptionists().stream()
					.limit(1)
					.forEach(staff -> seed.uploadOptionalProfileImage(staff.person()));

			seedState.performanceDentists().stream()
					.limit(1)
					.forEach(staff -> seed.uploadOptionalProfileImage(staff.person()));
		});

		seedState.performancePatients().stream()
				.limit(1)
				.forEach(patient -> seed.uploadOptionalDocument(
						patient,
						ApplicationSeed.GENERAL_CONSENT_RESOURCE,
						"Performance General Consent Document",
						"CONSENT"
				));
	}

	void runDiagnostics(ApplicationSeed.SeedState seedState) {
		System.out.println();
		System.out.println("========== Performance Diagnostics ==========");

		seed.measureMillis("Admin login performance", () -> {
			seed.userService().login(new LoginRequest("admin@dentalplus.demo", ApplicationSeed.DEFAULT_PASSWORD));
		});

		seed.measureMillis("Receptionist login performance", () -> {
			seed.userService().login(new LoginRequest("reception@dentalplus.demo", ApplicationSeed.DEFAULT_PASSWORD));
		});

		seed.measureMillis("Primary dentist login performance", () -> {
			seed.userService().login(new LoginRequest("dentist.general@dentalplus.demo", ApplicationSeed.DEFAULT_PASSWORD));
		});

		seed.measureMillis("Admin appointment list by date", () -> {
			seed.appointmentService().getAppointments(seedState.admin().user().getId(), LocalDate.of(2026, 5, 5),
					null, null, null);
		});

		seed.measureMillis("Receptionist appointment list by date", () -> {
			seed.appointmentService().getAppointments(seedState.receptionist().user().getId(),
					LocalDate.of(2026, 5, 5), null, null, null);
		});

		seed.measureMillis("Primary dentist appointment list by date", () -> {
			seed.appointmentService().getAppointments(seedState.primaryDentist().user().getId(),
					LocalDate.of(2026, 5, 5), null, null, null);
		});

		seed.measureMillis("Admin availability by date and time", () -> {
			seed.appointmentService().getAvailability(seedState.admin().user().getId(), LocalDate.of(2026, 5, 5),
					LocalTime.of(10, 0));
		});

		seed.measureMillis("Receptionist availability by date and time", () -> {
			seed.appointmentService().getAvailability(seedState.receptionist().user().getId(),
					LocalDate.of(2026, 5, 5), LocalTime.of(10, 0));
		});

		seed.measureMillis("Primary dentist availability by date and time", () -> {
			seed.appointmentService().getAvailability(seedState.primaryDentist().user().getId(),
					LocalDate.of(2026, 5, 5), LocalTime.of(10, 0));
		});

		seed.measureMillis("Admin get base odontogram by patient", () -> {
			seed.odontogramService().getOdontogramByPatientId(seedState.patientOne().getId(),
					seedState.admin().user().getId());
		});

		if (!seedState.performancePatients().isEmpty() && !seedState.performanceOdontograms().isEmpty()) {
			Patient performancePatient = seedState.performancePatients().get(0);

			seed.measureMillis("Admin get performance odontogram by patient", () -> {
				seed.odontogramService().getOdontogramByPatientId(performancePatient.getId(),
						seedState.admin().user().getId());
			});
		}

		System.out.println("=============================================");
		System.out.println();
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
}