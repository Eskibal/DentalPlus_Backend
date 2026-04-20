package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({
    "id",
    "odontogram",
    "pieceNumber",
    "pieceKind"
})
@Entity
@Table(
    name = "dental_piece",
    uniqueConstraints = @UniqueConstraint(columnNames = {"odontogram_id", "piece_number"})
)
public class DentalPiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "odontogram_id", nullable = false)
    private Odontogram odontogram;

    @Column(name = "piece_number", nullable = false)
    private Integer pieceNumber;

    @Column(nullable = false, length = 20)
    private String pieceKind;

    public DentalPiece() {
    }

    public DentalPiece(Odontogram odontogram, Integer pieceNumber) {
        this.odontogram = odontogram;
        this.pieceNumber = pieceNumber;
        this.pieceKind = resolvePieceKind(pieceNumber);
    }

    @PrePersist
    @PreUpdate
    protected void normalizeBeforeSave() {
        this.pieceKind = resolvePieceKind(this.pieceNumber);
    }

    public Long getId() {
        return id;
    }

    public Odontogram getOdontogram() {
        return odontogram;
    }

    public void setOdontogram(Odontogram odontogram) {
        this.odontogram = odontogram;
    }

    public Integer getPieceNumber() {
        return pieceNumber;
    }

    public void setPieceNumber(Integer pieceNumber) {
        this.pieceNumber = pieceNumber;
        this.pieceKind = resolvePieceKind(pieceNumber);
    }

    public String getPieceKind() {
        return pieceKind;
    }

    public static boolean isPieceNumberValid(Integer pieceNumber) {
        if (pieceNumber == null) {
            return false;
        }

        int quadrant = pieceNumber / 10;
        int tooth = pieceNumber % 10;

        boolean isPermanent = quadrant >= 1 && quadrant <= 4 && tooth >= 1 && tooth <= 8;
        boolean isTemporary = quadrant >= 5 && quadrant <= 8 && tooth >= 1 && tooth <= 5;

        return isPermanent || isTemporary;
    }

    public static boolean isTemporaryPiece(Integer pieceNumber) {
        if (!isPieceNumberValid(pieceNumber)) {
            return false;
        }

        int quadrant = pieceNumber / 10;
        return quadrant >= 5 && quadrant <= 8;
    }

    public static boolean isPermanentPiece(Integer pieceNumber) {
        if (!isPieceNumberValid(pieceNumber)) {
            return false;
        }

        int quadrant = pieceNumber / 10;
        return quadrant >= 1 && quadrant <= 4;
    }

    public static String resolvePieceKind(Integer pieceNumber) {
        if (!isPieceNumberValid(pieceNumber)) {
            return null;
        }

        int tooth = pieceNumber % 10;

        if (tooth >= 1 && tooth <= 3) {
            return "FRONT";
        }

        return "BACK";
    }

    public static List<Integer> getAllMixedPieceNumbers() {
        List<Integer> pieceNumbers = new ArrayList<>();

        for (int quadrant = 1; quadrant <= 4; quadrant++) {
            for (int tooth = 1; tooth <= 8; tooth++) {
                pieceNumbers.add((quadrant * 10) + tooth);
            }
        }

        for (int quadrant = 5; quadrant <= 8; quadrant++) {
            for (int tooth = 1; tooth <= 5; tooth++) {
                pieceNumbers.add((quadrant * 10) + tooth);
            }
        }

        return pieceNumbers;
    }
}