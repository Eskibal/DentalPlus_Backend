package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.dao.DentalPieceDao;
import com.example.DentalPlus_Backend.dao.DentalPieceStateDao;
import com.example.DentalPlus_Backend.dao.DentalSurfaceDao;
import com.example.DentalPlus_Backend.dao.DentalSurfaceMarkDao;
import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.dao.OdontogramDao;
import com.example.DentalPlus_Backend.dao.PatientDao;
import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.dto.DentalPieceDto;
import com.example.DentalPlus_Backend.dto.DentalPieceStateDto;
import com.example.DentalPlus_Backend.dto.DentalSurfaceDto;
import com.example.DentalPlus_Backend.dto.DentalSurfaceMarkDto;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.Clinic;
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
public class DentalPieceService {

	private final OdontogramDao odontogramDao;
	private final DentalPieceDao dentalPieceDao;
	private final DentalPieceStateDao dentalPieceStateDao;
	private final DentalSurfaceDao dentalSurfaceDao;
	private final DentalSurfaceMarkDao dentalSurfaceMarkDao;
	private final PatientDao patientDao;
	private final AdminDao adminDao;
	private final ReceptionistDao receptionistDao;
	private final DentistDao dentistDao;

	public DentalPieceService(OdontogramDao odontogramDao, DentalPieceDao dentalPieceDao,
			DentalPieceStateDao dentalPieceStateDao, DentalSurfaceDao dentalSurfaceDao,
			DentalSurfaceMarkDao dentalSurfaceMarkDao, PatientDao patientDao, AdminDao adminDao,
			ReceptionistDao receptionistDao, DentistDao dentistDao) {
		this.odontogramDao = odontogramDao;
		this.dentalPieceDao = dentalPieceDao;
		this.dentalPieceStateDao = dentalPieceStateDao;
		this.dentalSurfaceDao = dentalSurfaceDao;
		this.dentalSurfaceMarkDao = dentalSurfaceMarkDao;
		this.patientDao = patientDao;
		this.adminDao = adminDao;
		this.receptionistDao = receptionistDao;
		this.dentistDao = dentistDao;
	}

	public DentalPieceDto getPieceByPatientId(Long patientId, Integer pieceNumber, Long callerUserId) {
		Odontogram odontogram = findOdontogramByPatientIdInCallerClinicOrThrow(patientId, callerUserId);
		DentalPiece piece = findPieceOrThrow(odontogram.getId(), pieceNumber);

		return buildDentalPieceDto(piece);
	}

	public DentalPieceDto getPieceByOdontogramId(Long odontogramId, Integer pieceNumber, Long callerUserId) {
		Odontogram odontogram = findOdontogramByIdInCallerClinicOrThrow(odontogramId, callerUserId);
		DentalPiece piece = findPieceOrThrow(odontogram.getId(), pieceNumber);

		return buildDentalPieceDto(piece);
	}

	public List<DentalPieceStateDto> getPieceStatesByPatientId(Long patientId, Integer pieceNumber, Long callerUserId) {
		Odontogram odontogram = findOdontogramByPatientIdInCallerClinicOrThrow(patientId, callerUserId);
		DentalPiece piece = findPieceOrThrow(odontogram.getId(), pieceNumber);

		return dentalPieceStateDao.findByDentalPieceId(piece.getId()).stream().map(DentalPieceStateDto::new).toList();
	}

	public List<DentalPieceStateDto> getPieceStatesByOdontogramId(Long odontogramId, Integer pieceNumber,
			Long callerUserId) {
		Odontogram odontogram = findOdontogramByIdInCallerClinicOrThrow(odontogramId, callerUserId);
		DentalPiece piece = findPieceOrThrow(odontogram.getId(), pieceNumber);

		return dentalPieceStateDao.findByDentalPieceId(piece.getId()).stream().map(DentalPieceStateDto::new).toList();
	}

	@Transactional
	public DentalPieceStateDto createPieceStateByPatientId(Long patientId, Integer pieceNumber,
			DentalPieceStateDto request, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);

		Odontogram odontogram = findOdontogramByPatientIdInCallerClinicOrThrow(patientId, callerUserId);
		DentalPiece piece = findPieceOrThrow(odontogram.getId(), pieceNumber);

		return createPieceState(piece, request);
	}

	@Transactional
	public DentalPieceStateDto createPieceStateByOdontogramId(Long odontogramId, Integer pieceNumber,
			DentalPieceStateDto request, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);

		Odontogram odontogram = findOdontogramByIdInCallerClinicOrThrow(odontogramId, callerUserId);
		DentalPiece piece = findPieceOrThrow(odontogram.getId(), pieceNumber);

		return createPieceState(piece, request);
	}

	private DentalPieceStateDto createPieceState(DentalPiece piece, DentalPieceStateDto request) {
		if (request == null) {
			throw new IllegalArgumentException("State data is required");
		}

		if (!DentalPieceState.isStateTypeValid(request.getStateType())) {
			throw new IllegalArgumentException("Invalid stateType");
		}

		if (!DentalPieceState.isNotesValid(request.getNotes())) {
			throw new IllegalArgumentException("Invalid notes");
		}

		dentalPieceStateDao.deactivateActiveByDentalPieceId(piece.getId());

		DentalPieceState newState = new DentalPieceState(piece, request.getStateType(), request.getNotes());
		newState.setActive(true);

		dentalPieceStateDao.save(newState);

		return new DentalPieceStateDto(newState);
	}

	private DentalPieceDto buildDentalPieceDto(DentalPiece piece) {
		DentalPieceState activeState = dentalPieceStateDao.findActiveByDentalPieceId(piece.getId());

		List<DentalSurfaceDto> surfaces = dentalSurfaceDao.findByDentalPieceId(piece.getId()).stream()
				.map(this::buildDentalSurfaceDto).toList();

		return new DentalPieceDto(piece, activeState == null ? null : new DentalPieceStateDto(activeState), surfaces);
	}

	private DentalSurfaceDto buildDentalSurfaceDto(DentalSurface surface) {
		DentalSurfaceMark activeMark = dentalSurfaceMarkDao.findActiveByDentalSurfaceId(surface.getId());

		return new DentalSurfaceDto(surface, activeMark == null ? null : new DentalSurfaceMarkDto(activeMark));
	}

	private DentalPiece findPieceOrThrow(Long odontogramId, Integer pieceNumber) {
		if (!DentalPiece.isPieceNumberValid(pieceNumber)) {
			throw new IllegalArgumentException("Invalid pieceNumber");
		}

		DentalPiece piece = dentalPieceDao.findByOdontogramIdAndPieceNumber(odontogramId, pieceNumber);

		if (piece == null) {
			throw new IllegalArgumentException("Dental piece not found");
		}

		return piece;
	}

	private Odontogram findOdontogramByPatientIdInCallerClinicOrThrow(Long patientId, Long callerUserId) {
		Patient patient = findPatientInCallerClinicOrThrow(patientId, callerUserId);
		Odontogram odontogram = odontogramDao.findByPatientId(patient.getId());

		if (odontogram == null) {
			throw new IllegalArgumentException("Odontogram not found");
		}

		return odontogram;
	}

	private Odontogram findOdontogramByIdInCallerClinicOrThrow(Long odontogramId, Long callerUserId) {
		Odontogram odontogram = odontogramDao.findById(odontogramId);

		if (odontogram == null) {
			throw new IllegalArgumentException("Odontogram not found");
		}

		validateOdontogramBelongsToCallerClinic(odontogram, callerUserId);

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