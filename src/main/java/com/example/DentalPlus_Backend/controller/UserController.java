package com.example.DentalPlus_Backend.controller;

import com.example.DentalPlus_Backend.dto.LoginRequest;
import com.example.DentalPlus_Backend.dto.LoginResponse;
import com.example.DentalPlus_Backend.dto.ProfileDto;
import com.example.DentalPlus_Backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		try {
			LoginResponse response = userService.login(request);
			return ResponseEntity.ok(response);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}

	@GetMapping("/me")
	public ResponseEntity<?> getMyProfile() {
		Long callerUserId = getAuthenticatedUserId();

		System.out.println("[USER/ME GET] callerUserId: " + callerUserId);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(userService.getMyProfile(callerUserId));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateMyProfileJson(@RequestBody ProfileDto request) {
		System.out.println("[USER/ME PUT JSON] Controller reached");

		Long callerUserId = getAuthenticatedUserId();

		System.out.println("[USER/ME PUT JSON] callerUserId: " + callerUserId);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(userService.updateMyProfile(callerUserId, request, null, false));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> updateMyProfileMultipart(@RequestPart("profile") ProfileDto request,
			@RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
			@RequestParam(value = "removeProfileImage", required = false, defaultValue = "false") Boolean removeProfileImage) {
		System.out.println("[USER/ME PUT MULTIPART] Controller reached");

		Long callerUserId = getAuthenticatedUserId();

		System.out.println("[USER/ME PUT MULTIPART] callerUserId: " + callerUserId);

		if (callerUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}

		try {
			return ResponseEntity.ok(userService.updateMyProfile(callerUserId, request, profileImage,
					Boolean.TRUE.equals(removeProfileImage)));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	private Long getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		System.out.println("[AUTH DEBUG] authentication: " + authentication);

		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}

		Object principal = authentication.getPrincipal();

		System.out.println("[AUTH DEBUG] principal: " + principal);
		System.out.println(
				"[AUTH DEBUG] principal class: " + (principal == null ? null : principal.getClass().getName()));

		if (principal instanceof Long) {
			return (Long) principal;
		}

		if (principal instanceof Integer) {
			return ((Integer) principal).longValue();
		}

		if (principal instanceof String) {
			try {
				return Long.parseLong((String) principal);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		return null;
	}
}