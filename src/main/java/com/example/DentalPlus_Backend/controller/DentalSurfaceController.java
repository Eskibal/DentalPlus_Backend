package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.DentalPiece;
import com.example.DentalPlus_Backend.model.DentalSurface;
import com.example.DentalPlus_Backend.model.Odontogram;
import com.example.DentalPlus_Backend.model.Patient;
import com.example.DentalPlus_Backend.model.SurfaceMark;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients/{patientId}/odontogram/pieces/{pieceNumber}/surfaces")
public class DentalSurfaceController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/{surfaceType}")
    public ResponseEntity<?> getSurface(
            @PathVariable Long patientId,
            @PathVariable Integer pieceNumber,
            @PathVariable String surfaceType
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

        String normalizedSurfaceType = DentalSurface.normalizeSurfaceType(surfaceType);

        if (!DentalSurface.isSurfaceTypeValid(normalizedSurfaceType)) {
            return ResponseEntity.badRequest().body("Invalid surfaceType");
        }

        DentalSurface surface = findSurfaceByPieceAndType(piece.getId(), normalizedSurfaceType);

        if (surface == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dental surface not found");
        }

        return ResponseEntity.ok(surface);
    }

    @GetMapping("/{surfaceType}/marks")
    public ResponseEntity<?> getSurfaceMarks(
            @PathVariable Long patientId,
            @PathVariable Integer pieceNumber,
            @PathVariable String surfaceType
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

        String normalizedSurfaceType = DentalSurface.normalizeSurfaceType(surfaceType);

        if (!DentalSurface.isSurfaceTypeValid(normalizedSurfaceType)) {
            return ResponseEntity.badRequest().body("Invalid surfaceType");
        }

        DentalSurface surface = findSurfaceByPieceAndType(piece.getId(), normalizedSurfaceType);

        if (surface == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dental surface not found");
        }

        TypedQuery<SurfaceMark> query = entityManager.createQuery(
                "FROM SurfaceMark sm WHERE sm.dentalSurface.id = :surfaceId ORDER BY sm.createdAt DESC",
                SurfaceMark.class
        );
        query.setParameter("surfaceId", surface.getId());

        return ResponseEntity.ok(query.getResultList());
    }

    @PostMapping("/{surfaceType}/marks")
    @Transactional
    public ResponseEntity<?> createSurfaceMark(
            @PathVariable Long patientId,
            @PathVariable Integer pieceNumber,
            @PathVariable String surfaceType,
            @RequestBody SurfaceMark incomingMark
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

        String normalizedSurfaceType = DentalSurface.normalizeSurfaceType(surfaceType);

        if (!DentalSurface.isSurfaceTypeValid(normalizedSurfaceType)) {
            return ResponseEntity.badRequest().body("Invalid surfaceType");
        }

        if (!DentalSurface.isSurfaceTypeValidForPieceKind(normalizedSurfaceType, piece.getPieceKind())) {
            return ResponseEntity.badRequest().body("Surface type is not valid for this dental piece");
        }

        DentalSurface surface = findSurfaceByPieceAndType(piece.getId(), normalizedSurfaceType);

        if (surface == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dental surface not found");
        }

        if (incomingMark == null || !SurfaceMark.isMarkTypeValid(incomingMark.getMarkType())) {
            return ResponseEntity.badRequest().body("Invalid markType");
        }

        if (!SurfaceMark.isMarkStateValid(incomingMark.getMarkState())) {
            return ResponseEntity.badRequest().body("Invalid markState");
        }

        if (!SurfaceMark.isNotesValid(incomingMark.getNotes())) {
            return ResponseEntity.badRequest().body("Invalid notes");
        }

        TypedQuery<SurfaceMark> activeQuery = entityManager.createQuery(
                "FROM SurfaceMark sm WHERE sm.dentalSurface.id = :surfaceId AND sm.active = true",
                SurfaceMark.class
        );
        activeQuery.setParameter("surfaceId", surface.getId());

        List<SurfaceMark> activeMarks = activeQuery.getResultList();

        for (SurfaceMark mark : activeMarks) {
            mark.setActive(false);
            entityManager.merge(mark);
        }

        SurfaceMark newMark = new SurfaceMark(
                surface,
                incomingMark.getMarkType(),
                incomingMark.getMarkState(),
                incomingMark.getNotes()
        );
        newMark.setActive(true);
        entityManager.persist(newMark);

        return ResponseEntity.status(HttpStatus.CREATED).body(newMark);
    }

    @GetMapping("/{surfaceType}/history")
    public ResponseEntity<?> getSurfaceHistory(
            @PathVariable Long patientId,
            @PathVariable Integer pieceNumber,
            @PathVariable String surfaceType
    ) {
        return getSurfaceMarks(patientId, pieceNumber, surfaceType);
    }

    @DeleteMapping("/{surfaceType}/marks/{markId}")
    @Transactional
    public ResponseEntity<?> deactivateSurfaceMark(
            @PathVariable Long patientId,
            @PathVariable Integer pieceNumber,
            @PathVariable String surfaceType,
            @PathVariable Long markId
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

        String normalizedSurfaceType = DentalSurface.normalizeSurfaceType(surfaceType);

        if (!DentalSurface.isSurfaceTypeValid(normalizedSurfaceType)) {
            return ResponseEntity.badRequest().body("Invalid surfaceType");
        }

        DentalSurface surface = findSurfaceByPieceAndType(piece.getId(), normalizedSurfaceType);

        if (surface == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dental surface not found");
        }

        SurfaceMark mark = entityManager.find(SurfaceMark.class, markId);

        if (mark == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Surface mark not found");
        }

        if (!mark.getDentalSurface().getId().equals(surface.getId())) {
            return ResponseEntity.badRequest().body("Mark does not belong to this surface");
        }

        mark.setActive(false);
        entityManager.merge(mark);

        return ResponseEntity.ok(mark);
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

    private DentalSurface findSurfaceByPieceAndType(Long pieceId, String surfaceType) {
        TypedQuery<DentalSurface> query = entityManager.createQuery(
                "FROM DentalSurface ds WHERE ds.dentalPiece.id = :pieceId AND ds.surfaceType = :surfaceType",
                DentalSurface.class
        );
        query.setParameter("pieceId", pieceId);
        query.setParameter("surfaceType", surfaceType);

        List<DentalSurface> results = query.getResultList();

        return results.isEmpty() ? null : results.get(0);
    }
}