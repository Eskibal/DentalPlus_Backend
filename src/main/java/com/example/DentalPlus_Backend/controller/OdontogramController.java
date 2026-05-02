package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.dto.DentalBridgeDto;
import com.example.DentalPlus_Backend.dto.DentalPieceStateDto;
import com.example.DentalPlus_Backend.dto.DentalSurfaceMarkDto;
import com.example.DentalPlus_Backend.service.DentalBridgeService;
import com.example.DentalPlus_Backend.service.DentalPieceService;
import com.example.DentalPlus_Backend.service.DentalSurfaceService;
import com.example.DentalPlus_Backend.service.JwtService;
import com.example.DentalPlus_Backend.service.OdontogramService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patient")
public class OdontogramController {

	private final OdontogramService odontogramService;
	private final DentalPieceService dentalPieceService;
	private final DentalSurfaceService dentalSurfaceService;
	private final DentalBridgeService dentalBridgeService;
	private final JwtService jwtService;

	public OdontogramController(OdontogramService odontogramService, DentalPieceService dentalPieceService,
			DentalSurfaceService dentalSurfaceService, DentalBridgeService dentalBridgeService, JwtService jwtService) {
		this.odontogramService = odontogramService;
		this.dentalPieceService = dentalPieceService;
		this.dentalSurfaceService = dentalSurfaceService;
		this.dentalBridgeService = dentalBridgeService;
		this.jwtService = jwtService;
	}

	@PostMapping("/{patientId}/odontogram")
	public ResponseEntity<?> createOdontogramByPatientId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(odontogramService.createOdontogramByPatientId(patientId, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/{patientId}/odontogram")
	public ResponseEntity<?> getOdontogramByPatientId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(odontogramService.getOdontogramByPatientId(patientId, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@GetMapping("/odontogram/{odontogramId}")
	public ResponseEntity<?> getOdontogramById(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long odontogramId) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(odontogramService.getOdontogramById(odontogramId, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@PutMapping("/{patientId}/odontogram/view-mode")
	public ResponseEntity<?> updateViewModeByPatientId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId, @RequestBody ViewModeRequest request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			String viewMode = request == null ? null : request.getViewMode();
			return ResponseEntity.ok(odontogramService.updateViewModeByPatientId(patientId, viewMode, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PutMapping("/odontogram/{odontogramId}/view-mode")
	public ResponseEntity<?> updateViewModeByOdontogramId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long odontogramId, @RequestBody ViewModeRequest request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			String viewMode = request == null ? null : request.getViewMode();
			return ResponseEntity
					.ok(odontogramService.updateViewModeByOdontogramId(odontogramId, viewMode, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/{patientId}/odontogram/piece/{pieceNumber}")
	public ResponseEntity<?> getPieceByPatientId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId, @PathVariable Integer pieceNumber) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(dentalPieceService.getPieceByPatientId(patientId, pieceNumber, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@GetMapping("/odontogram/{odontogramId}/piece/{pieceNumber}")
	public ResponseEntity<?> getPieceByOdontogramId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long odontogramId, @PathVariable Integer pieceNumber) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity
					.ok(dentalPieceService.getPieceByOdontogramId(odontogramId, pieceNumber, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@GetMapping("/{patientId}/odontogram/piece/{pieceNumber}/state")
	public ResponseEntity<?> getPieceStateByPatientId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId, @PathVariable Integer pieceNumber) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity
					.ok(dentalPieceService.getPieceStatesByPatientId(patientId, pieceNumber, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@GetMapping("/odontogram/{odontogramId}/piece/{pieceNumber}/state")
	public ResponseEntity<?> getPieceStateByOdontogramId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long odontogramId, @PathVariable Integer pieceNumber) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity
					.ok(dentalPieceService.getPieceStatesByOdontogramId(odontogramId, pieceNumber, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@PostMapping("/{patientId}/odontogram/piece/{pieceNumber}/state")
	public ResponseEntity<?> createPieceStateByPatientId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId, @PathVariable Integer pieceNumber, @RequestBody DentalPieceStateDto request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(
					dentalPieceService.createPieceStateByPatientId(patientId, pieceNumber, request, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/odontogram/{odontogramId}/piece/{pieceNumber}/state")
	public ResponseEntity<?> createPieceStateByOdontogramId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long odontogramId, @PathVariable Integer pieceNumber,
			@RequestBody DentalPieceStateDto request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(dentalPieceService
					.createPieceStateByOdontogramId(odontogramId, pieceNumber, request, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/{patientId}/odontogram/piece/{pieceNumber}/surface/{surfaceType}")
	public ResponseEntity<?> getSurfaceByPatientId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId, @PathVariable Integer pieceNumber, @PathVariable String surfaceType) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity
					.ok(dentalSurfaceService.getSurfaceByPatientId(patientId, pieceNumber, surfaceType, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@GetMapping("/odontogram/{odontogramId}/piece/{pieceNumber}/surface/{surfaceType}")
	public ResponseEntity<?> getSurfaceByOdontogramId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long odontogramId, @PathVariable Integer pieceNumber, @PathVariable String surfaceType) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(dentalSurfaceService.getSurfaceByOdontogramId(odontogramId, pieceNumber,
					surfaceType, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@GetMapping("/{patientId}/odontogram/piece/{pieceNumber}/surface/{surfaceType}/mark")
	public ResponseEntity<?> getSurfaceMarkByPatientId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId, @PathVariable Integer pieceNumber, @PathVariable String surfaceType) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(
					dentalSurfaceService.getSurfaceMarksByPatientId(patientId, pieceNumber, surfaceType, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@GetMapping("/odontogram/{odontogramId}/piece/{pieceNumber}/surface/{surfaceType}/mark")
	public ResponseEntity<?> getSurfaceMarkByOdontogramId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long odontogramId, @PathVariable Integer pieceNumber, @PathVariable String surfaceType) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(dentalSurfaceService.getSurfaceMarksByOdontogramId(odontogramId, pieceNumber,
					surfaceType, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@PostMapping("/{patientId}/odontogram/piece/{pieceNumber}/surface/{surfaceType}/mark")
	public ResponseEntity<?> createSurfaceMarkByPatientId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId, @PathVariable Integer pieceNumber, @PathVariable String surfaceType,
			@RequestBody DentalSurfaceMarkDto request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(dentalSurfaceService
					.createSurfaceMarkByPatientId(patientId, pieceNumber, surfaceType, request, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/odontogram/{odontogramId}/piece/{pieceNumber}/surface/{surfaceType}/mark")
	public ResponseEntity<?> createSurfaceMarkByOdontogramId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long odontogramId, @PathVariable Integer pieceNumber, @PathVariable String surfaceType,
			@RequestBody DentalSurfaceMarkDto request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(dentalSurfaceService
					.createSurfaceMarkByOdontogramId(odontogramId, pieceNumber, surfaceType, request, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PutMapping("/odontogram/mark/{markId}")
	public ResponseEntity<?> updateSurfaceMark(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long markId, @RequestBody DentalSurfaceMarkDto request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(dentalSurfaceService.updateSurfaceMark(markId, request, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@DeleteMapping("/odontogram/mark/{markId}")
	public ResponseEntity<?> deactivateSurfaceMark(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long markId) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(dentalSurfaceService.deactivateSurfaceMark(markId, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@GetMapping("/{patientId}/odontogram/bridge")
	public ResponseEntity<?> getBridgeByPatientId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(dentalBridgeService.getActiveBridgesByPatientId(patientId, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@GetMapping("/odontogram/{odontogramId}/bridge")
	public ResponseEntity<?> getBridgeByOdontogramId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long odontogramId) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(dentalBridgeService.getActiveBridgesByOdontogramId(odontogramId, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	@PostMapping("/{patientId}/odontogram/bridge")
	public ResponseEntity<?> createBridgeByPatientId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long patientId, @RequestBody DentalBridgeDto request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(dentalBridgeService.createBridgeByPatientId(patientId, request, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/odontogram/{odontogramId}/bridge")
	public ResponseEntity<?> createBridgeByOdontogramId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long odontogramId, @RequestBody DentalBridgeDto request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(dentalBridgeService.createBridgeByOdontogramId(odontogramId, request, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PutMapping("/odontogram/bridge/{bridgeId}")
	public ResponseEntity<?> updateBridge(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long bridgeId, @RequestBody DentalBridgeDto request) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(dentalBridgeService.updateBridge(bridgeId, request, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@DeleteMapping("/odontogram/bridge/{bridgeId}")
	public ResponseEntity<?> deactivateBridge(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@PathVariable Long bridgeId) {
		Long callerUserId = getAuthenticatedUserId(authorizationHeader);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(dentalBridgeService.deactivateBridge(bridgeId, callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	private Long getAuthenticatedUserId(String authorizationHeader) {
		String token = jwtService.extractToken(authorizationHeader);

		if (token == null || !jwtService.validateToken(token)) {
			return null;
		}

		return jwtService.extractUserId(token);
	}

	public static class ViewModeRequest {
		private String viewMode;

		public String getViewMode() {
			return viewMode;
		}

		public void setViewMode(String viewMode) {
			this.viewMode = viewMode;
		}
	}
}