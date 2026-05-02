package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.dto.AppointmentDto;
import com.example.DentalPlus_Backend.dto.AvailabilityDto;
import com.example.DentalPlus_Backend.service.AppointmentService;
import com.example.DentalPlus_Backend.service.JwtService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

	private final AppointmentService appointmentService;
	private final JwtService jwtService;

	public AppointmentController(AppointmentService appointmentService, JwtService jwtService) {
		this.appointmentService = appointmentService;
		this.jwtService = jwtService;
	}

	@GetMapping
	public ResponseEntity<?> getAppointments(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) Long patientId, @RequestParam(required = false) Long dentistId,
			@RequestParam(required = false) Long boxId) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity
					.ok(appointmentService.getAppointments(callerUserId, date, patientId, dentistId, boxId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getAppointmentById(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long id) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(appointmentService.getAppointmentById(id, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@PostMapping
	public ResponseEntity<?> createAppointment(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestBody AppointmentDto request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			AppointmentDto response = appointmentService.createAppointment(request, callerUserId);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateAppointment(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Long id,
			@RequestBody AppointmentDto request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(appointmentService.updateAppointment(id, request, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteAppointment(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long id) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			appointmentService.deleteAppointment(id, callerUserId);
			return ResponseEntity.ok("Appointment deleted successfully");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@GetMapping("/availability")
	public ResponseEntity<?> getAvailability(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			AvailabilityDto response = appointmentService.getAvailability(callerUserId, date, time);
			return ResponseEntity.ok(response);
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