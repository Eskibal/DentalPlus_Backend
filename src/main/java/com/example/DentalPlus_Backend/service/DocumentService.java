package com.example.DentalPlus_Backend.service;

import com.example.DentalPlus_Backend.dao.AdminDao;
import com.example.DentalPlus_Backend.dao.DentistDao;
import com.example.DentalPlus_Backend.dao.DocumentDao;
import com.example.DentalPlus_Backend.dao.PatientDao;
import com.example.DentalPlus_Backend.dao.ReceptionistDao;
import com.example.DentalPlus_Backend.dto.DocumentDto;
import com.example.DentalPlus_Backend.model.Admin;
import com.example.DentalPlus_Backend.model.Clinic;
import com.example.DentalPlus_Backend.model.Dentist;
import com.example.DentalPlus_Backend.model.Document;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.Receptionist;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {
	private final DocumentDao documentDao;
	private final PatientDao patientDao;
	private final AdminDao adminDao;
	private final ReceptionistDao receptionistDao;
	private final DentistDao dentistDao;
	private final SupabaseStorageService supabaseStorageService;

	public DocumentService(DocumentDao documentDao, PatientDao patientDao, AdminDao adminDao,
			ReceptionistDao receptionistDao, DentistDao dentistDao, SupabaseStorageService supabaseStorageService) {
		this.documentDao = documentDao;
		this.patientDao = patientDao;
		this.adminDao = adminDao;
		this.receptionistDao = receptionistDao;
		this.dentistDao = dentistDao;
		this.supabaseStorageService = supabaseStorageService;
	}

	public List<DocumentDto> getDocumentsByPatient(Long patientId, Long callerUserId) {
		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);
		Patient patient = findPatientOrThrow(patientId);

		validatePatientBelongsToClinic(patient, clinic);

		return documentDao.findActiveByPatientId(patientId).stream().map(this::buildDocumentDto).toList();
	}

	@Transactional
	public DocumentDto uploadDocument(Long patientId, MultipartFile file, String name, String documentType,
			String notes, Long callerUserId) {
		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);
		Patient patient = findPatientOrThrow(patientId);

		validatePatientBelongsToClinic(patient, clinic);
		validateDocumentData(file, name, documentType, notes);

		try {
			String folder = "patients/" + patientId;
			String storagePath = supabaseStorageService.uploadPdf(file, folder);

			Document document = new Document(patient, name, storagePath, "application/pdf", documentType, true, notes);

			documentDao.save(document);

			return buildDocumentDto(document);
		} catch (IOException e) {
			throw new IllegalStateException("Error uploading PDF");
		}
	}

	@Transactional
	public void deleteDocument(Long documentId, Long callerUserId) {
		Clinic clinic = resolveCallerClinicOrThrow(callerUserId);
		Document document = findDocumentOrThrow(documentId);

		validatePatientBelongsToClinic(document.getPatient(), clinic);

		try {
			supabaseStorageService.deletePdf(document.getStoragePath());
		} catch (RuntimeException e) {
			throw new IllegalStateException("Error deleting PDF from storage");
		}

		documentDao.delete(document);
	}

	private DocumentDto buildDocumentDto(Document document) {
		return new DocumentDto(document, supabaseStorageService.getPublicUrl(document.getStoragePath()));
	}

	private void validateDocumentData(MultipartFile file, String name, String documentType, String notes) {
		if (!Document.isNameValid(name)) {
			throw new IllegalArgumentException("Invalid name");
		}

		if (!Document.isDocumentTypeValid(documentType)) {
			throw new IllegalArgumentException("Invalid documentType");
		}

		if (!Document.isNotesValid(notes)) {
			throw new IllegalArgumentException("Invalid notes");
		}

		if (!SupabaseStorageService.isPdfValid(file)) {
			throw new IllegalArgumentException("Invalid PDF file");
		}
	}

	private Patient findPatientOrThrow(Long patientId) {
		Patient patient = patientDao.findById(patientId);

		if (patient == null) {
			throw new IllegalArgumentException("Patient not found");
		}

		return patient;
	}

	private Document findDocumentOrThrow(Long documentId) {
		Document document = documentDao.findById(documentId);

		if (document == null) {
			throw new IllegalArgumentException("Document not found");
		}

		return document;
	}

	private void validatePatientBelongsToClinic(Patient patient, Clinic clinic) {
		if (patient == null || patient.getClinic() == null || !patient.getClinic().getId().equals(clinic.getId())) {
			throw new IllegalArgumentException("Patient not found in caller clinic");
		}
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