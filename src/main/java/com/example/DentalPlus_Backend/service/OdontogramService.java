package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.dao.DentalBridgeDao;
import com.example.DentalPlus_Backend.dao.DentalBridgePieceDao;
import com.example.DentalPlus_Backend.dao.DentalPieceDao;
import com.example.DentalPlus_Backend.dao.DentalPieceStateDao;
import com.example.DentalPlus_Backend.dao.DentalSurfaceDao;
import com.example.DentalPlus_Backend.dao.DentalSurfaceMarkDao;
import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.dao.OdontogramDao;
import com.example.DentalPlus_Backend.dao.PatientDao;
import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.dto.DentalBridgeDto;
import com.example.DentalPlus_Backend.dto.DentalBridgePieceDto;
import com.example.DentalPlus_Backend.dto.DentalPieceDto;
import com.example.DentalPlus_Backend.dto.DentalPieceStateDto;
import com.example.DentalPlus_Backend.dto.DentalSurfaceDto;
import com.example.DentalPlus_Backend.dto.DentalSurfaceMarkDto;
import com.example.DentalPlus_Backend.dto.OdontogramDto;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.DentalBridge;
import com.example.DentalPlus_Backend.model.DentalBridgePiece;
import com.example.DentalPlus_Backend.model.DentalPiece;
import com.example.DentalPlus_Backend.model.DentalPieceState;
import com.example.DentalPlus_Backend.model.DentalSurface;
import com.example.DentalPlus_Backend.model.DentalSurfaceMark;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Odontogram;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.Receptionist;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OdontogramService {

	private final OdontogramDao odontogramDao;
	private final DentalPieceDao dentalPieceDao;
	private final DentalSurfaceDao dentalSurfaceDao;
	private final DentalPieceStateDao dentalPieceStateDao;
	private final DentalSurfaceMarkDao dentalSurfaceMarkDao;
	private final DentalBridgeDao dentalBridgeDao;
	private final DentalBridgePieceDao dentalBridgePieceDao;
	private final PatientDao patientDao;
	private final AdminDao adminDao;
	private final ReceptionistDao receptionistDao;
	private final DentistDao dentistDao;

	public OdontogramService(OdontogramDao odontogramDao, DentalPieceDao dentalPieceDao,
			DentalSurfaceDao dentalSurfaceDao, DentalPieceStateDao dentalPieceStateDao,
			DentalSurfaceMarkDao dentalSurfaceMarkDao, DentalBridgeDao dentalBridgeDao,
			DentalBridgePieceDao dentalBridgePieceDao, PatientDao patientDao, AdminDao adminDao,
			ReceptionistDao receptionistDao, DentistDao dentistDao) {
		this.odontogramDao = odontogramDao;
		this.dentalPieceDao = dentalPieceDao;
		this.dentalSurfaceDao = dentalSurfaceDao;
		this.dentalPieceStateDao = dentalPieceStateDao;
		this.dentalSurfaceMarkDao = dentalSurfaceMarkDao;
		this.dentalBridgeDao = dentalBridgeDao;
		this.dentalBridgePieceDao = dentalBridgePieceDao;
		this.patientDao = patientDao;
		this.adminDao = adminDao;
		this.receptionistDao = receptionistDao;
		this.dentistDao = dentistDao;
	}

	@Transactional
	public OdontogramDto createOdontogramByPatientId(Long patientId, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);

		Patient patient = findPatientInCallerClinicOrThrow(patientId, callerUserId);

		if (odontogramDao.existsByPatientId(patientId)) {
			throw new IllegalArgumentException("Patient already has an odontogram");
		}

		Odontogram odontogram = new Odontogram(patient);
		odontogram.setViewMode("MIXED");
		odontogramDao.save(odontogram);

		createInitialDentalStructure(odontogram);

		return buildOdontogramDto(odontogram);
	}

	public OdontogramDto getOdontogramByPatientId(Long patientId, Long callerUserId) {
		findPatientInCallerClinicOrThrow(patientId, callerUserId);

		Odontogram odontogram = odontogramDao.findByPatientId(patientId);

		if (odontogram == null) {
			throw new IllegalArgumentException("Odontogram not found");
		}

		return buildOdontogramDto(odontogram);
	}

	public OdontogramDto getOdontogramById(Long odontogramId, Long callerUserId) {
		Odontogram odontogram = findOdontogramOrThrow(odontogramId);
		validateOdontogramBelongsToCallerClinic(odontogram, callerUserId);

		return buildOdontogramDto(odontogram);
	}

	@Transactional
	public OdontogramDto updateViewModeByPatientId(Long patientId, String viewMode, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);
		findPatientInCallerClinicOrThrow(patientId, callerUserId);

		Odontogram odontogram = odontogramDao.findByPatientId(patientId);

		if (odontogram == null) {
			throw new IllegalArgumentException("Odontogram not found");
		}

		updateViewMode(odontogram, viewMode);

		return buildOdontogramDto(odontogram);
	}

	@Transactional
	public OdontogramDto updateViewModeByOdontogramId(Long odontogramId, String viewMode, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);

		Odontogram odontogram = findOdontogramOrThrow(odontogramId);
		validateOdontogramBelongsToCallerClinic(odontogram, callerUserId);

		updateViewMode(odontogram, viewMode);

		return buildOdontogramDto(odontogram);
	}

	private void createInitialDentalStructure(Odontogram odontogram) {
		List<Integer> pieceNumbers = DentalPiece.getAllMixedPieceNumbers();

		for (Integer pieceNumber : pieceNumbers) {
			DentalPiece dentalPiece = new DentalPiece(odontogram, pieceNumber);
			dentalPieceDao.save(dentalPiece);

			DentalPieceState initialState = new DentalPieceState(dentalPiece, "HEALTHY", "Initial default state");
			dentalPieceStateDao.save(initialState);

			List<String> surfaceTypes = DentalSurface.getSurfaceTypesForPieceKind(dentalPiece.getPieceKind());

			for (String surfaceType : surfaceTypes) {
				DentalSurface dentalSurface = new DentalSurface(dentalPiece, surfaceType, null);
				dentalSurfaceDao.save(dentalSurface);
			}
		}
	}

	private void updateViewMode(Odontogram odontogram, String viewMode) {
		if (!Odontogram.isViewModeValid(viewMode)) {
			throw new IllegalArgumentException("Invalid viewMode");
		}

		odontogram.setViewMode(viewMode);
		odontogramDao.update(odontogram);
	}

	private OdontogramDto buildOdontogramDto(Odontogram odontogram) {
		List<DentalPieceDto> pieces = dentalPieceDao.findByOdontogramId(odontogram.getId()).stream()
				.map(this::buildDentalPieceDto).toList();

		List<DentalBridgeDto> bridges = dentalBridgeDao.findActiveByOdontogramId(odontogram.getId()).stream()
				.map(this::buildDentalBridgeDto).toList();

		return new OdontogramDto(odontogram, pieces, bridges);
	}

	private DentalPieceDto buildDentalPieceDto(DentalPiece dentalPiece) {
		DentalPieceState activeState = dentalPieceStateDao.findActiveByDentalPieceId(dentalPiece.getId());

		List<DentalSurfaceDto> surfaces = dentalSurfaceDao.findByDentalPieceId(dentalPiece.getId()).stream()
				.map(this::buildDentalSurfaceDto).toList();

		return new DentalPieceDto(dentalPiece, activeState == null ? null : new DentalPieceStateDto(activeState),
				surfaces);
	}

	private DentalSurfaceDto buildDentalSurfaceDto(DentalSurface dentalSurface) {
		DentalSurfaceMark activeMark = dentalSurfaceMarkDao.findActiveByDentalSurfaceId(dentalSurface.getId());

		return new DentalSurfaceDto(dentalSurface, activeMark == null ? null : new DentalSurfaceMarkDto(activeMark));
	}

	private DentalBridgeDto buildDentalBridgeDto(DentalBridge dentalBridge) {
		List<DentalBridgePieceDto> bridgePieces = dentalBridgePieceDao.findByDentalBridgeId(dentalBridge.getId())
				.stream().map(DentalBridgePieceDto::new).toList();

		return new DentalBridgeDto(dentalBridge, bridgePieces);
	}

	private Odontogram findOdontogramOrThrow(Long odontogramId) {
		Odontogram odontogram = odontogramDao.findById(odontogramId);

		if (odontogram == null) {
			throw new IllegalArgumentException("Odontogram not found");
		}

		return odontogram;
	}

	private Patient findPatientInCallerClinicOrThrow(Long patientId, Long callerUserId) {
		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);
		Patient patient = patientDao.findById(patientId);

		if (patient == null) {
			throw new IllegalArgumentException("Patient not found");
		}

		if (patient.getClinic() == null || !patient.getClinic().getId().equals(clinic.getId())) {
			throw new IllegalArgumentException("Patient not found in caller clinic");
		}

		return patient;
	}

	private void validateOdontogramBelongsToCallerClinic(Odontogram odontogram, Long callerUserId) {
		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);

		if (odontogram.getPatient() == null || odontogram.getPatient().getClinic() == null
				|| !odontogram.getPatient().getClinic().getId().equals(clinic.getId())) {
			throw new IllegalArgumentException("Odontogram not found in caller clinic");
		}
	}

	private void validateCanModifyOdontogram(Long callerUserId) {
		Admin admin = adminDao.findByUserId(callerUserId);
		if (admin != null && admin.getActive()) {
			return;
		}

		Dentist dentist = dentistDao.findByUserId(callerUserId);
		if (dentist != null && dentist.getActive()) {
			return;
		}

		throw new IllegalArgumentException("Caller cannot modify odontogram");
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
}