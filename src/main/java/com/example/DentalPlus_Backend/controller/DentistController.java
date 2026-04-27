package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.model.Dentist;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dentist")
public class DentistController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping
    public ResponseEntity<List<Dentist>> getAllDentists() {
        List<Dentist> dentists = entityManager
                .createQuery("FROM Dentist", Dentist.class)
                .getResultList();

        return ResponseEntity.ok(dentists);
    }
}