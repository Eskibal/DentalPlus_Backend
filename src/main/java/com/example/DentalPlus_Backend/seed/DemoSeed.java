package com.example.DentalPlus_Backend.seed;

import com.example.DentalPlus_Backend.model.Appointment;
import com.example.DentalPlus_Backend.model.Box;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.Odontogram;
import com.example.DentalPlus_Backend.model.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class DemoSeed {

	private final ApplicationSeed seed;

	DemoSeed(ApplicationSeed seed) {
		this.seed = seed;
	}

	ApplicationSeed.DemoSeedData load(Clinic clinic, Box boxOne, Box boxTwo,
			ApplicationSeed.StaffSeed primaryDentistSeed,
			ApplicationSeed.StaffSeed secondaryDentistSeed) {

		List<Patient> basePatients = seed.measureMillis("Base patients loading", () -> {
			List<Patient> patients = new ArrayList<>();

			patients.add(seed.createPatient(
					clinic,
					"Laura",
					"García",
					"Vidal",
					"laura.garcia@patients.demo",
					"600100001",
					LocalDate.of(1990, 4, 12),
					"ALLERGY:PENICILLIN",
					"Revisión general y sensibilidad dental"
			));

			patients.add(seed.createPatient(
					clinic,
					"Marc",
					"Torres",
					"Navarro",
					"marc.torres@patients.demo",
					"600100002",
					LocalDate.of(1988, 9, 3),
					null,
					"Control preventivo anual"
			));

			patients.add(seed.createPatient(
					clinic,
					"Núria",
					"López",
					"Martín",
					"nuria.lopez@patients.demo",
					"600100003",
					LocalDate.of(1995, 2, 18),
					"INFECTION_RISK:HEPATITIS_B",
					"Dolor en molar inferior derecho"
			));

			patients.add(seed.createPatient(
					clinic,
					"Ahmed",
					"Benali",
					"Ruiz",
					"ahmed.benali@patients.demo",
					"600100004",
					LocalDate.of(1992, 11, 27),
					"BLEEDING_RISK:ANTICOAGULANTS",
					"Valoración previa a extracción"
			));

			return patients;
		});

		Patient patientOne = basePatients.get(0);
		Patient patientTwo = basePatients.get(1);
		Patient patientThree = basePatients.get(2);
		Patient patientFour = basePatients.get(3);

		List<Odontogram> baseOdontograms = seed.measureMillis("Base odontograms loading", () -> {
			List<Odontogram> odontograms = new ArrayList<>();

			odontograms.add(seed.createOdontogram(patientOne));
			odontograms.add(seed.createOdontogram(patientTwo));
			odontograms.add(seed.createOdontogram(patientThree));
			odontograms.add(seed.createOdontogram(patientFour));

			return odontograms;
		});

		Odontogram odontogramOne = baseOdontograms.get(0);
		Odontogram odontogramTwo = baseOdontograms.get(1);
		Odontogram odontogramThree = baseOdontograms.get(2);
		Odontogram odontogramFour = baseOdontograms.get(3);

		seed.measureMillis("Base dental marks loading",
				() -> createDemoDentalMarks(odontogramOne, odontogramTwo, odontogramThree, odontogramFour));

		List<Appointment> baseAppointments = seed.measureMillis("Base appointments loading", () -> {
			List<Appointment> appointments = new ArrayList<>();

			appointments.add(seed.persist(new Appointment(
					boxOne,
					primaryDentistSeed.dentist(),
					patientOne,
					LocalDateTime.of(2026, 5, 1, 9, 30),
					LocalDateTime.of(2026, 5, 1, 10, 0),
					"SCHEDULED",
					"Routine checkup",
					"Revisión inicial por sensibilidad dental",
					true
			)));

			appointments.add(seed.persist(new Appointment(
					boxTwo,
					secondaryDentistSeed.dentist(),
					patientTwo,
					LocalDateTime.of(2026, 5, 1, 11, 0),
					LocalDateTime.of(2026, 5, 1, 11, 30),
					"SCHEDULED",
					"Dental cleaning",
					"Limpieza dental preventiva anual",
					true
			)));

			appointments.add(seed.persist(new Appointment(
					boxOne,
					primaryDentistSeed.dentist(),
					patientThree,
					LocalDateTime.of(2026, 5, 4, 15, 0),
					LocalDateTime.of(2026, 5, 4, 15, 45),
					"SCHEDULED",
					"Follow-up consultation",
					"Seguimiento por dolor en molar inferior derecho",
					true
			)));

			return appointments;
		});

		return new ApplicationSeed.DemoSeedData(
				basePatients,
				baseOdontograms,
				baseAppointments
		);
	}

	private void createDemoDentalMarks(Odontogram odontogramOne,
			Odontogram odontogramTwo,
			Odontogram odontogramThree,
			Odontogram odontogramFour) {

		seed.createSurfaceMark(odontogramOne, 11, "MESIAL", "CARIES", "PENDING",
				"Caries inicial en zona mesial");
		seed.createSurfaceMark(odontogramOne, 16, "OCCLUSAL", "FILLING", "DONE",
				"Restauración oclusal previa en buen estado");
		seed.createPieceState(odontogramOne, 26, "CROWN_PENDING",
				"Corona planificada por fractura parcial");

		seed.createSurfaceMark(odontogramTwo, 21, "DISTAL", "CARIES", "PENDING",
				"Caries interproximal pendiente de revisión");
		seed.createSurfaceMark(odontogramTwo, 36, "OCCLUSAL", "FISSURE_SEALANT", "DONE",
				"Sellador preventivo aplicado previamente");

		seed.createSurfaceMark(odontogramThree, 46, "VESTIBULAR", "CROWN", "PENDING",
				"Valoración de corona pendiente");
		seed.createPieceState(odontogramThree, 15, "ENDODONTICS_DONE",
				"Endodoncia realizada en tratamiento anterior");

		seed.createSurfaceMark(odontogramFour, 14, "MESIAL", "FILLING", "DONE",
				"Empaste mesial realizado previamente");
		seed.createSurfaceMark(odontogramFour, 24, "DISTAL", "CARIES", "PENDING",
				"Caries distal detectada en revisión");
		seed.createPieceState(odontogramFour, 55, "NATURAL_ABSENCE",
				"Pieza temporal ausente de forma natural");
	}
}