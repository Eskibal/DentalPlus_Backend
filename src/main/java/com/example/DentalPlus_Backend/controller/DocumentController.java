package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.Document;
import com.example.DentalPlus_Backend.model.Patient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping
    @Transactional
    public ResponseEntity<?> createDocument(@RequestBody Document document) {

        String validationError = validateDocumentData(document);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        if (document.getPatient() == null || document.getPatient().getId() == null) {
            return ResponseEntity.badRequest().body("Patient id is required");
        }

        Patient existingPatient = entityManager.find(Patient.class, document.getPatient().getId());

        if (existingPatient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        Document newDocument = new Document();
        newDocument.setType(document.getType());
        newDocument.setFileUrl(document.getFileUrl());
        newDocument.setCaptureDate(document.getCaptureDate());
        newDocument.setPatient(existingPatient);

        entityManager.persist(newDocument);

        return ResponseEntity.status(HttpStatus.CREATED).body(newDocument);
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
    public ResponseEntity<?> updateDocument(@PathVariable Long id,
                                            @RequestBody Document updatedDocument) {

        Document document = entityManager.find(Document.class, id);

        if (document == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found");
        }

        String validationError = validateDocumentData(updatedDocument);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        if (updatedDocument.getPatient() == null || updatedDocument.getPatient().getId() == null) {
            return ResponseEntity.badRequest().body("Patient id is required");
        }

        Patient existingPatient = entityManager.find(Patient.class, updatedDocument.getPatient().getId());

        if (existingPatient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        document.setType(updatedDocument.getType());
        document.setFileUrl(updatedDocument.getFileUrl());
        document.setCaptureDate(updatedDocument.getCaptureDate());
        document.setPatient(existingPatient);

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

        entityManager.remove(document);

        return ResponseEntity.ok("Document deleted successfully");
    }

    private String validateDocumentData(Document document) {
        if (!Document.isTypeValid(document.getType())) {
            return "Invalid type";
        }

        if (!Document.isFileUrlValid(document.getFileUrl())) {
            return "Invalid fileUrl";
        }

        if (!Document.isCaptureDateValid(document.getCaptureDate())) {
            return "Invalid captureDate";
        }

        return null;
    }
}