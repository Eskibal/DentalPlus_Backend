package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.Document;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.service.SupabaseStorageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @PersistenceContext
    private EntityManager entityManager;

    private final SupabaseStorageService supabaseStorageService;

    public DocumentController(SupabaseStorageService supabaseStorageService) {
        this.supabaseStorageService = supabaseStorageService;
    }

    @PostMapping("/upload/{patientId}")
    @Transactional
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long patientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "notes", required = false) String notes
    ) {
        Patient patient = entityManager.find(Patient.class, patientId);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        if (!Document.isNameValid(name)) {
            return ResponseEntity.badRequest().body("Invalid name");
        }

        if (!Document.isDocumentTypeValid(documentType)) {
            return ResponseEntity.badRequest().body("Invalid documentType");
        }

        if (!Document.isNotesValid(notes)) {
            return ResponseEntity.badRequest().body("Invalid notes");
        }

        if (!SupabaseStorageService.isPdfValid(file)) {
            return ResponseEntity.badRequest().body("Invalid PDF file");
        }

        try {
            String folder = "patients/" + patientId;
            String storagePath = supabaseStorageService.uploadPdf(file, folder);

            Document document = new Document(
                    patient,
                    name,
                    storagePath,
                    "application/pdf",
                    documentType,
                    true,
                    notes
            );

            entityManager.persist(document);

            return ResponseEntity.status(HttpStatus.CREATED).body(document);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading PDF");
        }
    }

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = entityManager
                .createQuery("FROM Document", Document.class)
                .getResultList();

        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentById(@PathVariable Long id) {
        Document document = entityManager.find(Document.class, id);

        if (document == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found");
        }

        return ResponseEntity.ok(document);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getDocumentsByPatientId(@PathVariable Long patientId) {
        Patient patient = entityManager.find(Patient.class, patientId);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        List<Document> documents = entityManager
                .createQuery("FROM Document d WHERE d.patient.id = :patientId", Document.class)
                .setParameter("patientId", patientId)
                .getResultList();

        return ResponseEntity.ok(documents);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateDocument(
            @PathVariable Long id,
            @RequestBody Document updatedDocument
    ) {
        Document document = entityManager.find(Document.class, id);

        if (document == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found");
        }

        if (!Document.isNameValid(updatedDocument.getName())) {
            return ResponseEntity.badRequest().body("Invalid name");
        }

        if (!Document.isDocumentTypeValid(updatedDocument.getDocumentType())) {
            return ResponseEntity.badRequest().body("Invalid documentType");
        }

        if (!Document.isNotesValid(updatedDocument.getNotes())) {
            return ResponseEntity.badRequest().body("Invalid notes");
        }

        document.setName(updatedDocument.getName());
        document.setDocumentType(updatedDocument.getDocumentType());
        document.setActive(updatedDocument.getActive());
        document.setNotes(updatedDocument.getNotes());

        entityManager.merge(document);

        return ResponseEntity.ok(document);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        Document document = entityManager.find(Document.class, id);

        if (document == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found");
        }

        try {
            supabaseStorageService.deletePdf(document.getStoragePath());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting PDF from storage");
        }

        entityManager.remove(document);

        return ResponseEntity.ok("Document deleted successfully");
    }
}