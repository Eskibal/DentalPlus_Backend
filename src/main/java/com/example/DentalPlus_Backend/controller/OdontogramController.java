package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.DentalBridge;
import com.example.DentalPlus_Backend.model.DentalBridgePiece;
import com.example.DentalPlus_Backend.model.DentalPiece;
import com.example.DentalPlus_Backend.model.DentalPieceState;
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
@RequestMapping("/patients/{patientId}/odontogram")
public class OdontogramController {

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping
    @Transactional
    public ResponseEntity<?> createOdontogram(@PathVariable Long patientId) {
        Patient patient = entityManager.find(Patient.class, patientId);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        Odontogram existingOdontogram = findOdontogramByPatientId(patientId);

        if (existingOdontogram != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Patient already has an odontogram");
        }

        Odontogram odontogram = new Odontogram(patient);
        odontogram.setViewMode("MIXED");
        entityManager.persist(odontogram);
        entityManager.flush();

        List<Integer> pieceNumbers = DentalPiece.getAllMixedPieceNumbers();

        for (Integer pieceNumber : pieceNumbers) {
            DentalPiece dentalPiece = new DentalPiece(odontogram, pieceNumber);
            entityManager.persist(dentalPiece);
            entityManager.flush();

            DentalPieceState initialState = new DentalPieceState(
                    dentalPiece,
                    "HEALTHY",
                    "Initial default state"
            );
            entityManager.persist(initialState);

            List<String> surfaceTypes = DentalSurface.getSurfaceTypesForPieceKind(dentalPiece.getPieceKind());

            for (String surfaceType : surfaceTypes) {
                DentalSurface dentalSurface = new DentalSurface(dentalPiece, surfaceType, null);
                entityManager.persist(dentalSurface);
            }
        }

        entityManager.flush();

        return ResponseEntity.status(HttpStatus.CREATED).body(findOdontogramByPatientId(patientId));
    }

    @GetMapping
    public ResponseEntity<?> getOdontogram(@PathVariable Long patientId) {
        Patient patient = entityManager.find(Patient.class, patientId);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        Odontogram odontogram = findOdontogramByPatientId(patientId);

        if (odontogram == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Odontogram not found");
        }

        return ResponseEntity.ok(odontogram);
    }

    @PutMapping("/view-mode")
    @Transactional
    public ResponseEntity<?> updateViewMode(
            @PathVariable Long patientId,
            @RequestBody Odontogram updatedOdontogram
    ) {
        Patient patient = entityManager.find(Patient.class, patientId);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        Odontogram odontogram = findOdontogramByPatientId(patientId);

        if (odontogram == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Odontogram not found");
        }

        if (updatedOdontogram == null || !Odontogram.isViewModeValid(updatedOdontogram.getViewMode())) {
            return ResponseEntity.badRequest().body("Invalid viewMode");
        }

        odontogram.setViewMode(updatedOdontogram.getViewMode());
        entityManager.merge(odontogram);

        return ResponseEntity.ok(odontogram);
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<?> deleteOdontogram(@PathVariable Long patientId) {
        Patient patient = entityManager.find(Patient.class, patientId);

        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        Odontogram odontogram = findOdontogramByPatientId(patientId);

        if (odontogram == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Odontogram not found");
        }

        deleteAllBridgeData(odontogram);
        deleteAllPieceData(odontogram);

        entityManager.remove(odontogram);

        return ResponseEntity.ok("Odontogram deleted successfully");
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

    private void deleteAllBridgeData(Odontogram odontogram) {
        TypedQuery<DentalBridge> bridgeQuery = entityManager.createQuery(
                "FROM DentalBridge b WHERE b.odontogram.id = :odontogramId",
                DentalBridge.class
        );
        bridgeQuery.setParameter("odontogramId", odontogram.getId());

        List<DentalBridge> bridges = bridgeQuery.getResultList();

        for (DentalBridge bridge : bridges) {
            TypedQuery<DentalBridgePiece> bridgePieceQuery = entityManager.createQuery(
                    "FROM DentalBridgePiece bp WHERE bp.dentalBridge.id = :bridgeId",
                    DentalBridgePiece.class
            );
            bridgePieceQuery.setParameter("bridgeId", bridge.getId());

            List<DentalBridgePiece> bridgePieces = bridgePieceQuery.getResultList();

            for (DentalBridgePiece bridgePiece : bridgePieces) {
                entityManager.remove(bridgePiece);
            }

            entityManager.remove(bridge);
        }
    }

    private void deleteAllPieceData(Odontogram odontogram) {
        TypedQuery<DentalPiece> pieceQuery = entityManager.createQuery(
                "FROM DentalPiece dp WHERE dp.odontogram.id = :odontogramId",
                DentalPiece.class
        );
        pieceQuery.setParameter("odontogramId", odontogram.getId());

        List<DentalPiece> pieces = pieceQuery.getResultList();

        for (DentalPiece piece : pieces) {
            TypedQuery<SurfaceMark> markQuery = entityManager.createQuery(
                    "SELECT sm FROM SurfaceMark sm WHERE sm.dentalSurface.dentalPiece.id = :pieceId",
                    SurfaceMark.class
            );
            markQuery.setParameter("pieceId", piece.getId());

            List<SurfaceMark> marks = markQuery.getResultList();

            for (SurfaceMark mark : marks) {
                entityManager.remove(mark);
            }

            TypedQuery<DentalSurface> surfaceQuery = entityManager.createQuery(
                    "FROM DentalSurface ds WHERE ds.dentalPiece.id = :pieceId",
                    DentalSurface.class
            );
            surfaceQuery.setParameter("pieceId", piece.getId());

            List<DentalSurface> surfaces = surfaceQuery.getResultList();

            for (DentalSurface surface : surfaces) {
                entityManager.remove(surface);
            }

            TypedQuery<DentalPieceState> stateQuery = entityManager.createQuery(
                    "FROM DentalPieceState dps WHERE dps.dentalPiece.id = :pieceId",
                    DentalPieceState.class
            );
            stateQuery.setParameter("pieceId", piece.getId());

            List<DentalPieceState> states = stateQuery.getResultList();

            for (DentalPieceState state : states) {
                entityManager.remove(state);
            }

            entityManager.remove(piece);
        }
    }
}