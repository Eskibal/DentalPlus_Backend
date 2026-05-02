package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.dto.DocumentDto;
import com.example.DentalPlus_Backend.service.DocumentService;
import com.example.DentalPlus_Backend.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/document")
public class DocumentController {

	private final DocumentService documentService;
	private final JwtService jwtService;

	public DocumentController(DocumentService documentService, JwtService jwtService) {
		this.documentService = documentService;
		this.jwtService = jwtService;
	}

	@GetMapping("/patient/{patientId}")
	public ResponseEntity<?> getDocumentsByPatient(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(documentService.getDocumentsByPatient(patientId, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@PostMapping("/patient/{patientId}")
	public ResponseEntity<?> uploadDocument(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId, @RequestParam("file") MultipartFile file, @RequestParam("name") String name,
			@RequestParam("documentType") String documentType,
			@RequestParam(value = "notes", required = false) String notes) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			DocumentDto response = documentService.uploadDocument(patientId, file, name, documentType, notes,
					callerUserId);

			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteDocument(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long id) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			documentService.deleteDocument(id, callerUserId);
			return ResponseEntity.ok("Document deleted successfully");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
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