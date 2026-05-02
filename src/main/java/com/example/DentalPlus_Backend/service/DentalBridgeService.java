package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.dao.DentalBridgeDao;
import com.example.DentalPlus_Backend.dao.DentalBridgePieceDao;
import com.example.DentalPlus_Backend.dao.DentalPieceDao;
import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.dao.OdontogramDao;
import com.example.DentalPlus_Backend.dao.PatientDao;
import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.dto.DentalBridgeDto;
import com.example.DentalPlus_Backend.dto.DentalBridgePieceDto;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.DentalBridge;
import com.example.DentalPlus_Backend.model.DentalBridgePiece;
import com.example.DentalPlus_Backend.model.DentalPiece;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Odontogram;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.Receptionist;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DentalBridgeService {

	private final OdontogramDao odontogramDao;
	private final DentalBridgeDao dentalBridgeDao;
	private final DentalBridgePieceDao dentalBridgePieceDao;
	private final DentalPieceDao dentalPieceDao;
	private final PatientDao patientDao;
	private final AdminDao adminDao;
	private final ReceptionistDao receptionistDao;
	private final DentistDao dentistDao;

	public DentalBridgeService(OdontogramDao odontogramDao, DentalBridgeDao dentalBridgeDao,
			DentalBridgePieceDao dentalBridgePieceDao, DentalPieceDao dentalPieceDao, PatientDao patientDao,
			AdminDao adminDao, ReceptionistDao receptionistDao, DentistDao dentistDao) {
		this.odontogramDao = odontogramDao;
		this.dentalBridgeDao = dentalBridgeDao;
		this.dentalBridgePieceDao = dentalBridgePieceDao;
		this.dentalPieceDao = dentalPieceDao;
		this.patientDao = patientDao;
		this.adminDao = adminDao;
		this.receptionistDao = receptionistDao;
		this.dentistDao = dentistDao;
	}

	public List<DentalBridgeDto> getActiveBridgesByPatientId(Long patientId, Long callerUserId) {
		Odontogram odontogram = findOdontogramByPatientIdInCallerClinicOrThrow(patientId, callerUserId);

		return dentalBridgeDao.findActiveByOdontogramId(odontogram.getId()).stream().map(this::buildDentalBridgeDto)
				.toList();
	}

	public List<DentalBridgeDto> getActiveBridgesByOdontogramId(Long odontogramId, Long callerUserId) {
		Odontogram odontogram = findOdontogramByIdInCallerClinicOrThrow(odontogramId, callerUserId);

		return dentalBridgeDao.findActiveByOdontogramId(odontogram.getId()).stream().map(this::buildDentalBridgeDto)
				.toList();
	}

	@Transactional
	public DentalBridgeDto createBridgeByPatientId(Long patientId, DentalBridgeDto request, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);

		Odontogram odontogram = findOdontogramByPatientIdInCallerClinicOrThrow(patientId, callerUserId);

		return createBridge(odontogram, request);
	}

	@Transactional
	public DentalBridgeDto createBridgeByOdontogramId(Long odontogramId, DentalBridgeDto request, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);

		Odontogram odontogram = findOdontogramByIdInCallerClinicOrThrow(odontogramId, callerUserId);

		return createBridge(odontogram, request);
	}

	@Transactional
	public DentalBridgeDto updateBridge(Long bridgeId, DentalBridgeDto request, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);

		DentalBridge bridge = findBridgeOrThrow(bridgeId);
		validateOdontogramBelongsToCallerClinic(bridge.getOdontogram(), callerUserId);

		if (request == null) {
			throw new IllegalArgumentException("Bridge data is required");
		}

		if (request.getBridgeState() != null) {
			if (!DentalBridge.isBridgeStateValid(request.getBridgeState())) {
				throw new IllegalArgumentException("Invalid bridgeState");
			}
			bridge.setBridgeState(request.getBridgeState());
		}

		if (request.getNotes() != null) {
			if (!DentalBridge.isNotesValid(request.getNotes())) {
				throw new IllegalArgumentException("Invalid notes");
			}
			bridge.setNotes(request.getNotes());
		}

		if (request.getActive() != null) {
			bridge.setActive(request.getActive());
		}

		dentalBridgeDao.update(bridge);

		if (request.getPieces() != null) {
			replaceBridgePieces(bridge, request.getPieces());
		}

		return buildDentalBridgeDto(bridge);
	}

	@Transactional
	public DentalBridgeDto deactivateBridge(Long bridgeId, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);

		DentalBridge bridge = findBridgeOrThrow(bridgeId);
		validateOdontogramBelongsToCallerClinic(bridge.getOdontogram(), callerUserId);

		bridge.setActive(false);
		dentalBridgeDao.update(bridge);

		return buildDentalBridgeDto(bridge);
	}

	private DentalBridgeDto createBridge(Odontogram odontogram, DentalBridgeDto request) {
		validateBridgeRequest(request);

		DentalBridge bridge = new DentalBridge(odontogram, request.getBridgeState(), request.getNotes());
		bridge.setActive(true);

		dentalBridgeDao.save(bridge);
		createBridgePieces(bridge, request.getPieces());

		return buildDentalBridgeDto(bridge);
	}

	private void replaceBridgePieces(DentalBridge bridge, List<DentalBridgePieceDto> pieces) {
		validateBridgePieces(bridge.getOdontogram(), pieces);

		dentalBridgePieceDao.deleteByDentalBridgeId(bridge.getId());
		createBridgePieces(bridge, pieces);
	}

	private void createBridgePieces(DentalBridge bridge, List<DentalBridgePieceDto> pieces) {
		validateBridgePieces(bridge.getOdontogram(), pieces);

		for (DentalBridgePieceDto pieceRequest : pieces) {
			DentalPiece dentalPiece = resolveDentalPieceForBridge(bridge.getOdontogram(), pieceRequest);

			DentalBridgePiece bridgePiece = new DentalBridgePiece(bridge, dentalPiece, pieceRequest.getPieceRole());

			dentalBridgePieceDao.save(bridgePiece);
		}
	}

	private void validateBridgeRequest(DentalBridgeDto request) {
		if (request == null) {
			throw new IllegalArgumentException("Bridge data is required");
		}

		if (!DentalBridge.isBridgeStateValid(request.getBridgeState())) {
			throw new IllegalArgumentException("Invalid bridgeState");
		}

		if (!DentalBridge.isNotesValid(request.getNotes())) {
			throw new IllegalArgumentException("Invalid notes");
		}

		if (request.getPieces() == null || request.getPieces().size() < 2) {
			throw new IllegalArgumentException("Bridge requires at least two pieces");
		}
	}

	private void validateBridgePieces(Odontogram odontogram, List<DentalBridgePieceDto> pieces) {
		if (pieces == null || pieces.size() < 2) {
			throw new IllegalArgumentException("Bridge requires at least two pieces");
		}

		Set<Long> usedPieceIds = new HashSet<>();
		int abutmentCount = 0;

		for (DentalBridgePieceDto pieceRequest : pieces) {
			if (pieceRequest == null) {
				throw new IllegalArgumentException("Invalid bridge piece");
			}

			if (!DentalBridgePiece.isPieceRoleValid(pieceRequest.getPieceRole())) {
				throw new IllegalArgumentException("Invalid pieceRole");
			}

			DentalPiece dentalPiece = resolveDentalPieceForBridge(odontogram, pieceRequest);

			if (!usedPieceIds.add(dentalPiece.getId())) {
				throw new IllegalArgumentException("Bridge cannot contain duplicated pieces");
			}

			if ("ABUTMENT".equals(DentalBridgePiece.normalizePieceRole(pieceRequest.getPieceRole()))) {
				abutmentCount++;
			}
		}

		if (abutmentCount < 2) {
			throw new IllegalArgumentException("Bridge requires at least two abutment pieces");
		}
	}

	private DentalPiece resolveDentalPieceForBridge(Odontogram odontogram, DentalBridgePieceDto pieceRequest) {
		DentalPiece dentalPiece;

		if (pieceRequest.getDentalPieceId() != null) {
			dentalPiece = dentalPieceDao.findById(pieceRequest.getDentalPieceId());
		} else {
			if (!DentalPiece.isPieceNumberValid(pieceRequest.getPieceNumber())) {
				throw new IllegalArgumentException("Invalid pieceNumber");
			}

			dentalPiece = dentalPieceDao.findByOdontogramIdAndPieceNumber(odontogram.getId(),
					pieceRequest.getPieceNumber());
		}

		if (dentalPiece == null) {
			throw new IllegalArgumentException("Dental piece not found");
		}

		if (dentalPiece.getOdontogram() == null || !dentalPiece.getOdontogram().getId().equals(odontogram.getId())) {
			throw new IllegalArgumentException("Dental piece does not belong to this odontogram");
		}

		return dentalPiece;
	}

	private DentalBridgeDto buildDentalBridgeDto(DentalBridge bridge) {
		List<DentalBridgePieceDto> pieces = dentalBridgePieceDao.findByDentalBridgeId(bridge.getId()).stream()
				.map(DentalBridgePieceDto::new).toList();

		return new DentalBridgeDto(bridge, pieces);
	}

	private DentalBridge findBridgeOrThrow(Long bridgeId) {
		DentalBridge bridge = dentalBridgeDao.findById(bridgeId);

		if (bridge == null) {
			throw new IllegalArgumentException("Bridge not found");
		}

		return bridge;
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