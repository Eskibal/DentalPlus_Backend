package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.DentalPiece;
import com.example.DentalPlus_Backend.model.DentalPieceState;
import com.example.DentalPlus_Backend.model.Odontogram;
import com.example.DentalPlus_Backend.model.Patient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients/{patientId}/odontogram/pieces")
public class DentalPieceController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/{pieceNumber}")
    public ResponseEntity<?> getPiece(
            @PathVariable Long patientId,
            @PathVariable Integer pieceNumber
    ) {
        Patient patient = entityManager.find(Patient.class, patientId);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        Odontogram odontogram = findOdontogramByPatientId(patientId);

        if (odontogram == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Odontogram not found");
        }

        DentalPiece piece = findPieceByOdontogramAndNumber(odontogram.getId(), pieceNumber);

        if (piece == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dental piece not found");
        }

        return ResponseEntity.ok(piece);
    }

    @GetMapping("/{pieceNumber}/states")
    public ResponseEntity<?> getPieceStates(
            @PathVariable Long patientId,
            @PathVariable Integer pieceNumber
    ) {
        Patient patient = entityManager.find(Patient.class, patientId);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        Odontogram odontogram = findOdontogramByPatientId(patientId);

        if (odontogram == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Odontogram not found");
        }

        DentalPiece piece = findPieceByOdontogramAndNumber(odontogram.getId(), pieceNumber);

        if (piece == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dental piece not found");
        }

        TypedQuery<DentalPieceState> query = entityManager.createQuery(
                "FROM DentalPieceState dps WHERE dps.dentalPiece.id = :pieceId ORDER BY dps.createdAt DESC",
                DentalPieceState.class
        );
        query.setParameter("pieceId", piece.getId());

        return ResponseEntity.ok(query.getResultList());
    }

    @PostMapping("/{pieceNumber}/states")
    @Transactional
    public ResponseEntity<?> createPieceState(
            @PathVariable Long patientId,
            @PathVariable Integer pieceNumber,
            @RequestBody DentalPieceState incomingState
    ) {
        Patient patient = entityManager.find(Patient.class, patientId);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        Odontogram odontogram = findOdontogramByPatientId(patientId);

        if (odontogram == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Odontogram not found");
        }

        DentalPiece piece = findPieceByOdontogramAndNumber(odontogram.getId(), pieceNumber);

        if (piece == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dental piece not found");
        }

        if (incomingState == null || !DentalPieceState.isStateTypeValid(incomingState.getStateType())) {
            return ResponseEntity.badRequest().body("Invalid stateType");
        }

        if (!DentalPieceState.isNotesValid(incomingState.getNotes())) {
            return ResponseEntity.badRequest().body("Invalid notes");
        }

        TypedQuery<DentalPieceState> activeQuery = entityManager.createQuery(
                "FROM DentalPieceState dps WHERE dps.dentalPiece.id = :pieceId AND dps.active = true",
                DentalPieceState.class
        );
        activeQuery.setParameter("pieceId", piece.getId());

        List<DentalPieceState> activeStates = activeQuery.getResultList();

        for (DentalPieceState state : activeStates) {
            state.setActive(false);
            entityManager.merge(state);
        }

        DentalPieceState newState = new DentalPieceState(
                piece,
                incomingState.getStateType(),
                incomingState.getNotes()
        );
        newState.setActive(true);
        entityManager.persist(newState);

        return ResponseEntity.status(HttpStatus.CREATED).body(newState);
    }

    @GetMapping("/{pieceNumber}/history")
    public ResponseEntity<?> getPieceHistory(
            @PathVariable Long patientId,
            @PathVariable Integer pieceNumber
    ) {
        Patient patient = entityManager.find(Patient.class, patientId);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        Odontogram odontogram = findOdontogramByPatientId(patientId);

        if (odontogram == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Odontogram not found");
        }

        DentalPiece piece = findPieceByOdontogramAndNumber(odontogram.getId(), pieceNumber);

        if (piece == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dental piece not found");
        }

        TypedQuery<DentalPieceState> query = entityManager.createQuery(
                "FROM DentalPieceState dps WHERE dps.dentalPiece.id = :pieceId ORDER BY dps.createdAt DESC",
                DentalPieceState.class
        );
        query.setParameter("pieceId", piece.getId());

        return ResponseEntity.ok(query.getResultList());
    }

    private Odontogram findOdontogramByPatientId(Long patientId) {
        TypedQuery<Odontogram> query = entityManager.createQuery(
                "FROM Odontogram o WHERE o.patient.id = :patientId",
                Odontogram.class
        );
        query.setParameter("patientId", patientId);

        List<Odontogram> results = query.getResultList();

        return results.isEmpty() ? null : results.get(0);
    }

    private DentalPiece findPieceByOdontogramAndNumber(Long odontogramId, Integer pieceNumber) {
        TypedQuery<DentalPiece> query = entityManager.createQuery(
                "FROM DentalPiece dp WHERE dp.odontogram.id = :odontogramId AND dp.pieceNumber = :pieceNumber",
                DentalPiece.class
        );
        query.setParameter("odontogramId", odontogramId);
        query.setParameter("pieceNumber", pieceNumber);

        List<DentalPiece> results = query.getResultList();

        return results.isEmpty() ? null : results.get(0);
    }
}