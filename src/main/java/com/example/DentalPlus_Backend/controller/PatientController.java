package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.dto.PatientDto;
import com.example.DentalPlus_Backend.service.JwtService;
import com.example.DentalPlus_Backend.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patient")
public class PatientController {

	private final PatientService patientService;
	private final JwtService jwtService;

	public PatientController(PatientService patientService, JwtService jwtService) {
		this.patientService = patientService;
		this.jwtService = jwtService;
	}

	@GetMapping
	public ResponseEntity<?> getPatients(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestParam(required = false) String search) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(patientService.getVisiblePatients(callerUserId, search));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getPatientById(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long id) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(patientService.getPatientById(id, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@PostMapping
	public ResponseEntity<?> createPatient(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestBody PatientDto request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			PatientDto response = patientService.createPatient(request, callerUserId);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updatePatient(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Long id,
			@RequestBody PatientDto request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(patientService.updatePatient(id, request, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	private Long getAuthenticatedUserId(String authorizationHeader) {
		String token = jwtService.extractToken(authorizationHeader);

		if (token == null || !jwtService.validateToken(token)) {
			return null;
		}

		return jwtService.extractUserId(token);
	}
}