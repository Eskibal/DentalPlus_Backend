package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.dao.DentalPieceDao;
import com.example.DentalPlus_Backend.dao.DentalSurfaceDao;
import com.example.DentalPlus_Backend.dao.DentalSurfaceMarkDao;
import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.dao.OdontogramDao;
import com.example.DentalPlus_Backend.dao.PatientDao;
import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.dto.DentalSurfaceDto;
import com.example.DentalPlus_Backend.dto.DentalSurfaceMarkDto;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.DentalPiece;
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
public class DentalSurfaceService {

	private final OdontogramDao odontogramDao;
	private final DentalPieceDao dentalPieceDao;
	private final DentalSurfaceDao dentalSurfaceDao;
	private final DentalSurfaceMarkDao dentalSurfaceMarkDao;
	private final PatientDao patientDao;
	private final AdminDao adminDao;
	private final ReceptionistDao receptionistDao;
	private final DentistDao dentistDao;

	public DentalSurfaceService(OdontogramDao odontogramDao, DentalPieceDao dentalPieceDao,
			DentalSurfaceDao dentalSurfaceDao, DentalSurfaceMarkDao dentalSurfaceMarkDao, PatientDao patientDao,
			AdminDao adminDao, ReceptionistDao receptionistDao, DentistDao dentistDao) {
		this.odontogramDao = odontogramDao;
		this.dentalPieceDao = dentalPieceDao;
		this.dentalSurfaceDao = dentalSurfaceDao;
		this.dentalSurfaceMarkDao = dentalSurfaceMarkDao;
		this.patientDao = patientDao;
		this.adminDao = adminDao;
		this.receptionistDao = receptionistDao;
		this.dentistDao = dentistDao;
	}

	public DentalSurfaceDto getSurfaceByPatientId(Long patientId, Integer pieceNumber, String surfaceType,
			Long callerUserId) {
		Odontogram odontogram = findOdontogramByPatientIdInCallerClinicOrThrow(patientId, callerUserId);
		DentalPiece piece = findPieceOrThrow(odontogram.getId(), pieceNumber);
		DentalSurface surface = findSurfaceOrThrow(piece, surfaceType);

		return buildDentalSurfaceDto(surface);
	}

	public DentalSurfaceDto getSurfaceByOdontogramId(Long odontogramId, Integer pieceNumber, String surfaceType,
			Long callerUserId) {
		Odontogram odontogram = findOdontogramByIdInCallerClinicOrThrow(odontogramId, callerUserId);
		DentalPiece piece = findPieceOrThrow(odontogram.getId(), pieceNumber);
		DentalSurface surface = findSurfaceOrThrow(piece, surfaceType);

		return buildDentalSurfaceDto(surface);
	}

	public List<DentalSurfaceMarkDto> getSurfaceMarksByPatientId(Long patientId, Integer pieceNumber,
			String surfaceType, Long callerUserId) {
		Odontogram odontogram = findOdontogramByPatientIdInCallerClinicOrThrow(patientId, callerUserId);
		DentalPiece piece = findPieceOrThrow(odontogram.getId(), pieceNumber);
		DentalSurface surface = findSurfaceOrThrow(piece, surfaceType);

		return dentalSurfaceMarkDao.findByDentalSurfaceId(surface.getId()).stream().map(DentalSurfaceMarkDto::new)
				.toList();
	}

	public List<DentalSurfaceMarkDto> getSurfaceMarksByOdontogramId(Long odontogramId, Integer pieceNumber,
			String surfaceType, Long callerUserId) {
		Odontogram odontogram = findOdontogramByIdInCallerClinicOrThrow(odontogramId, callerUserId);
		DentalPiece piece = findPieceOrThrow(odontogram.getId(), pieceNumber);
		DentalSurface surface = findSurfaceOrThrow(piece, surfaceType);

		return dentalSurfaceMarkDao.findByDentalSurfaceId(surface.getId()).stream().map(DentalSurfaceMarkDto::new)
				.toList();
	}

	@Transactional
	public DentalSurfaceMarkDto createSurfaceMarkByPatientId(Long patientId, Integer pieceNumber, String surfaceType,
			DentalSurfaceMarkDto request, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);

		Odontogram odontogram = findOdontogramByPatientIdInCallerClinicOrThrow(patientId, callerUserId);
		DentalPiece piece = findPieceOrThrow(odontogram.getId(), pieceNumber);
		DentalSurface surface = findSurfaceOrThrow(piece, surfaceType);

		return createSurfaceMark(surface, request);
	}

	@Transactional
	public DentalSurfaceMarkDto createSurfaceMarkByOdontogramId(Long odontogramId, Integer pieceNumber,
			String surfaceType, DentalSurfaceMarkDto request, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);

		Odontogram odontogram = findOdontogramByIdInCallerClinicOrThrow(odontogramId, callerUserId);
		DentalPiece piece = findPieceOrThrow(odontogram.getId(), pieceNumber);
		DentalSurface surface = findSurfaceOrThrow(piece, surfaceType);

		return createSurfaceMark(surface, request);
	}

	@Transactional
	public DentalSurfaceMarkDto updateSurfaceMark(Long markId, DentalSurfaceMarkDto request, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);

		DentalSurfaceMark mark = findMarkOrThrow(markId);
		validateSurfaceBelongsToCallerClinic(mark.getDentalSurface(), callerUserId);

		if (request == null) {
			throw new IllegalArgumentException("Mark data is required");
		}

		if (request.getMarkType() != null) {
			if (!DentalSurfaceMark.isMarkTypeValid(request.getMarkType())) {
				throw new IllegalArgumentException("Invalid markType");
			}
			mark.setMarkType(request.getMarkType());
		}

		if (request.getMarkState() != null) {
			if (!DentalSurfaceMark.isMarkStateValid(request.getMarkState())) {
				throw new IllegalArgumentException("Invalid markState");
			}
			mark.setMarkState(request.getMarkState());
		}

		if (request.getNotes() != null) {
			if (!DentalSurfaceMark.isNotesValid(request.getNotes())) {
				throw new IllegalArgumentException("Invalid notes");
			}
			mark.setNotes(request.getNotes());
		}

		if (request.getActive() != null) {
			mark.setActive(request.getActive());
		}

		dentalSurfaceMarkDao.update(mark);

		return new DentalSurfaceMarkDto(mark);
	}

	@Transactional
	public DentalSurfaceMarkDto deactivateSurfaceMark(Long markId, Long callerUserId) {
		validateCanModifyOdontogram(callerUserId);

		DentalSurfaceMark mark = findMarkOrThrow(markId);
		validateSurfaceBelongsToCallerClinic(mark.getDentalSurface(), callerUserId);

		mark.setActive(false);
		dentalSurfaceMarkDao.update(mark);

		return new DentalSurfaceMarkDto(mark);
	}

	private DentalSurfaceMarkDto createSurfaceMark(DentalSurface surface, DentalSurfaceMarkDto request) {
		if (request == null) {
			throw new IllegalArgumentException("Mark data is required");
		}

		if (!DentalSurfaceMark.isMarkTypeValid(request.getMarkType())) {
			throw new IllegalArgumentException("Invalid markType");
		}

		if (!DentalSurfaceMark.isMarkStateValid(request.getMarkState())) {
			throw new IllegalArgumentException("Invalid markState");
		}

		if (!DentalSurfaceMark.isNotesValid(request.getNotes())) {
			throw new IllegalArgumentException("Invalid notes");
		}

		dentalSurfaceMarkDao.deactivateActiveByDentalSurfaceId(surface.getId());

		DentalSurfaceMark newMark = new DentalSurfaceMark(surface, request.getMarkType(), request.getMarkState(),
				request.getNotes());
		newMark.setActive(true);

		dentalSurfaceMarkDao.save(newMark);

		return new DentalSurfaceMarkDto(newMark);
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

	private DentalSurface findSurfaceOrThrow(DentalPiece piece, String surfaceType) {
		String normalizedSurfaceType = DentalSurface.normalizeSurfaceType(surfaceType);

		if (!DentalSurface.isSurfaceTypeValid(normalizedSurfaceType)) {
			throw new IllegalArgumentException("Invalid surfaceType");
		}

		if (!DentalSurface.isSurfaceTypeValidForPieceKind(normalizedSurfaceType, piece.getPieceKind())) {
			throw new IllegalArgumentException("Surface type is not valid for this dental piece");
		}

		DentalSurface surface = dentalSurfaceDao.findByDentalPieceIdAndSurfaceType(piece.getId(),
				normalizedSurfaceType);

		if (surface == null) {
			throw new IllegalArgumentException("Dental surface not found");
		}

		return surface;
	}

	private DentalSurfaceMark findMarkOrThrow(Long markId) {
		DentalSurfaceMark mark = dentalSurfaceMarkDao.findById(markId);

		if (mark == null) {
			throw new IllegalArgumentException("Surface mark not found");
		}

		return mark;
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

	private void validateSurfaceBelongsToCallerClinic(DentalSurface surface, Long callerUserId) {
		if (surface == null || surface.getDentalPiece() == null || surface.getDentalPiece().getOdontogram() == null) {
			throw new IllegalArgumentException("Dental surface not found");
		}

		validateOdontogramBelongsToCallerClinic(surface.getDentalPiece().getOdontogram(), callerUserId);
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