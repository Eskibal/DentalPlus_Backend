package com.example.DentalPlus_Backend;

import com.example.DentalPlus_Backend.model.Appointment;
import com.example.DentalPlus_Backend.model.CalendarBreak;
import com.example.DentalPlus_Backend.model.CalendarException;
import com.example.DentalPlus_Backend.model.CalendarHoliday;
import com.example.DentalPlus_Backend.model.CalendarRule;
import com.example.DentalPlus_Backend.model.DentalBridge;
import com.example.DentalPlus_Backend.model.DentalBridgePiece;
import com.example.DentalPlus_Backend.model.DentalPiece;
import com.example.DentalPlus_Backend.model.DentalPieceState;
import com.example.DentalPlus_Backend.model.DentalSurface;
import com.example.DentalPlus_Backend.model.DentalSurfaceMark;
import com.example.DentalPlus_Backend.model.Inventory;
import com.example.DentalPlus_Backend.model.Odontogram;
import com.example.DentalPlus_Backend.model.Person;
import com.example.DentalPlus_Backend.model.Product;
import com.example.DentalPlus_Backend.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(properties = { "spring.jpa.hibernate.ddl-auto=validate", "spring.sql.init.mode=never",
		"spring.jpa.open-in-view=false", "spring.jpa.show-sql=false" })
@AutoConfigureMockMvc
@ActiveProfiles({ "hibernate", "test" })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApplicationTest {

	private static final AtomicInteger PASSED_GROUPS = new AtomicInteger(0);

	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	static void printStartMessage() {
		System.out.println();
		System.out.println("========== DentalPlus Application Diagnostics ==========");
		System.out.println("[INFO] Running safe diagnostics.");
		System.out.println("[INFO] Database mode: validate.");
		System.out.println("[INFO] This test does not create, update or delete data.");
		System.out.println("========================================================");
		System.out.println();
	}

	@AfterAll
	static void printSummary() {
		System.out.println();
		System.out.println("========== DentalPlus Application Diagnostics Summary ==========");
		System.out.println("[OK] Passed diagnostic groups: " + PASSED_GROUPS.get());
		System.out.println("[OK] If Maven/JUnit reports no failures, the application passed the safe diagnostics.");
		System.out.println("================================================================");
		System.out.println();
	}

	@Test
	@Order(1)
	@DisplayName("Spring context loads successfully and database schema validates")
	void contextLoadsSuccessfullyAndDatabaseSchemaValidates() {
		printOk("Spring context loaded and Hibernate schema validation completed.");
	}

	@Test
	@Order(2)
	@DisplayName("Protected endpoints reject anonymous requests")
	void protectedEndpointsRejectAnonymousRequests() throws Exception {
		assertEndpointIsProtected("/user/me");
		assertEndpointIsProtected("/patient");
		assertEndpointIsProtected("/appointment");
		assertEndpointIsProtected("/document/patient/1");
		assertEndpointIsProtected("/patient/1/odontogram");
		assertEndpointIsProtected("/patient/odontogram/1");

		printOk("Protected endpoints reject requests without token.");
	}

	@Test
	@Order(3)
	@DisplayName("Login endpoint rejects invalid credentials without writing data")
	void loginEndpointRejectsInvalidCredentialsWithoutWritingData() throws Exception {
		mockMvc.perform(post("/user/login").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "identifier": "invalid.login@example.com",
				  "password": "Invalid123"
				}
				""")).andExpect(result -> {
			int responseStatus = result.getResponse().getStatus();

			assertTrue(responseStatus == 401 || responseStatus == 403,
					"Expected login with invalid credentials to return 401 or 403, but got " + responseStatus);
		});

		printOk("Login endpoint rejects invalid credentials safely.");
	}

	@Test
	@Order(4)
	@DisplayName("User and person validations work correctly")
	void userAndPersonValidationsWorkCorrectly() {
		assertAll(() -> assertTrue(User.isUsernameValid("test.user")), () -> assertFalse(User.isUsernameValid(" ")),
				() -> assertTrue(User.isPasswordValid("Password123")),
				() -> assertFalse(User.isPasswordValid("Password")),
				() -> assertFalse(User.isPasswordValid("12345678")), () -> assertFalse(User.isPasswordValid("Short1")),
				() -> assertTrue(User.isThemePreferenceValid("LIGHT")),
				() -> assertTrue(User.isThemePreferenceValid("DARK")),
				() -> assertTrue(User.isThemePreferenceValid("SYSTEM")),
				() -> assertFalse(User.isThemePreferenceValid("UNKNOWN_THEME")),
				() -> assertTrue(User.isLanguagePreferenceValid("en")), () -> assertTrue(Person.isNameValid("Test")),
				() -> assertFalse(Person.isNameValid(" ")), () -> assertTrue(Person.isFirstSurnameValid("User")),
				() -> assertTrue(Person.isSecondSurnameValid(null)), () -> assertTrue(Person.isGenderValid("OTHER")),
				() -> assertTrue(Person.isEmailValid("test@example.com")),
				() -> assertTrue(Person.isEmailValid("test.user+sample@example.com")),
				() -> assertFalse(Person.isEmailValid("invalid-email")),
				() -> assertTrue(Person.isPhonePrefixValid("+1")),
				() -> assertTrue(Person.isPhoneNumberValid("10000000000")),
				() -> assertTrue(Person.isAddressValid("Test Address")),
				() -> assertTrue(Person.isCityValid("Test City")),
				() -> assertTrue(Person.isProfileImageValid("https://example.com/profile.png")),
				() -> assertTrue(Person.isNotesValid("Valid notes")));

		printOk("User and person validations passed.");
	}

	@Test
	@Order(5)
	@DisplayName("Appointment validations work correctly")
	void appointmentValidationsWorkCorrectly() {
		LocalDateTime validStart = LocalDateTime.of(2026, 5, 1, 10, 0);
		LocalDateTime validEnd = LocalDateTime.of(2026, 5, 1, 10, 30);
		LocalDateTime invalidEnd = LocalDateTime.of(2026, 5, 1, 9, 30);

		assertAll(() -> assertTrue(Appointment.isStartDateTimeValid(validStart)),
				() -> assertTrue(Appointment.isEndDateTimeValid(validEnd)),
				() -> assertTrue(Appointment.isDateRangeValid(validStart, validEnd)),
				() -> assertFalse(Appointment.isDateRangeValid(validStart, validStart)),
				() -> assertFalse(Appointment.isDateRangeValid(validStart, invalidEnd)),
				() -> assertTrue(Appointment.isStatusValid("SCHEDULED")),
				() -> assertTrue(Appointment.isStatusValid("COMPLETED")),
				() -> assertTrue(Appointment.isStatusValid("CANCELLED")),
				() -> assertFalse(Appointment.isStatusValid("UNKNOWN")),
				() -> assertTrue(Appointment.isNotesValid("Routine appointment diagnostics")));

		printOk("Appointment validations passed.");
	}

	@Test
	@Order(6)
	@DisplayName("Calendar validations work correctly")
	void calendarValidationsWorkCorrectly() {
		LocalTime start = LocalTime.of(9, 0);
		LocalTime end = LocalTime.of(18, 0);
		LocalTime breakStart = LocalTime.of(13, 0);
		LocalTime breakEnd = LocalTime.of(14, 0);
		LocalDate startDate = LocalDate.of(2026, 5, 1);
		LocalDate endDate = LocalDate.of(2026, 5, 2);

		CalendarRule validRule = new CalendarRule(start, end, start, end, start, end, start, end, start, end, null,
				null, null, null, true, "Diagnostic calendar rule");

		CalendarRule invalidRule = new CalendarRule(end, start, start, end, start, end, start, end, start, end, null,
				null, null, null, true, "Invalid diagnostic calendar rule");

		assertAll(() -> assertTrue(CalendarRule.isDayRangeValid(start, end)),
				() -> assertTrue(CalendarRule.isDayRangeValid(null, null)),
				() -> assertFalse(CalendarRule.isDayRangeValid(start, null)),
				() -> assertFalse(CalendarRule.isDayRangeValid(end, start)),
				() -> assertTrue(CalendarRule.isWeeklyScheduleValid(validRule)),
				() -> assertFalse(CalendarRule.isWeeklyScheduleValid(invalidRule)),
				() -> assertTrue(CalendarBreak.isDayOfWeekValid("MONDAY")),
				() -> assertFalse(CalendarBreak.isDayOfWeekValid("UNKNOWN_DAY")),
				() -> assertTrue(CalendarBreak.isBreakRangeValid(breakStart, breakEnd)),
				() -> assertFalse(CalendarBreak.isBreakRangeValid(breakEnd, breakStart)),
				() -> assertTrue(CalendarHoliday.isNameValid("General Holiday")),
				() -> assertTrue(CalendarHoliday.isDateRangeValid(startDate, endDate)),
				() -> assertFalse(CalendarHoliday.isDateRangeValid(endDate, startDate)),
				() -> assertTrue(CalendarHoliday.isScopeValid("NATIONAL")),
				() -> assertTrue(CalendarHoliday.isScopeValid("REGIONAL")),
				() -> assertTrue(CalendarHoliday.isScopeValid("LOCAL")),
				() -> assertFalse(CalendarHoliday.isScopeValid("UNKNOWN_SCOPE")),
				() -> assertTrue(CalendarException.isExceptionTypeValid("UNAVAILABLE")),
				() -> assertTrue(CalendarException.isExceptionTypeValid("AVAILABLE")),
				() -> assertTrue(CalendarException.isExceptionTypeValid("SPECIAL_HOURS")),
				() -> assertFalse(CalendarException.isExceptionTypeValid("UNKNOWN_EXCEPTION")),
				() -> assertTrue(CalendarException.isTimeRangeValid(start, end)),
				() -> assertTrue(CalendarException.isTimeRangeValid(null, null)),
				() -> assertFalse(CalendarException.isTimeRangeValid(start, null)),
				() -> assertFalse(CalendarException.isTimeRangeValid(end, start)));

		printOk("Calendar validations passed.");
	}

	@Test
	@Order(7)
	@DisplayName("Odontogram and dental piece validations work correctly")
	void odontogramAndDentalPieceValidationsWorkCorrectly() {
		assertAll(() -> assertTrue(Odontogram.isViewModeValid("TEMPORARY")),
				() -> assertTrue(Odontogram.isViewModeValid("PERMANENT")),
				() -> assertTrue(Odontogram.isViewModeValid("MIXED")),
				() -> assertFalse(Odontogram.isViewModeValid("UNKNOWN_VIEW")),
				() -> assertTrue(DentalPiece.isPieceNumberValid(11)),
				() -> assertTrue(DentalPiece.isPieceNumberValid(18)),
				() -> assertTrue(DentalPiece.isPieceNumberValid(41)),
				() -> assertTrue(DentalPiece.isPieceNumberValid(48)),
				() -> assertTrue(DentalPiece.isPieceNumberValid(51)),
				() -> assertTrue(DentalPiece.isPieceNumberValid(55)),
				() -> assertTrue(DentalPiece.isPieceNumberValid(81)),
				() -> assertTrue(DentalPiece.isPieceNumberValid(85)),
				() -> assertFalse(DentalPiece.isPieceNumberValid(10)),
				() -> assertFalse(DentalPiece.isPieceNumberValid(19)),
				() -> assertFalse(DentalPiece.isPieceNumberValid(50)),
				() -> assertFalse(DentalPiece.isPieceNumberValid(86)),
				() -> assertFalse(DentalPiece.isPieceNumberValid(99)),
				() -> assertTrue("FRONT".equals(DentalPiece.resolvePieceKind(11))),
				() -> assertTrue("BACK".equals(DentalPiece.resolvePieceKind(16))));

		printOk("Odontogram and dental piece validations passed.");
	}

	@Test
	@Order(8)
	@DisplayName("Dental surface and mark validations work correctly")
	void dentalSurfaceAndMarkValidationsWorkCorrectly() {
		assertAll(() -> assertTrue(DentalSurface.isSurfaceTypeValid("MESIAL")),
				() -> assertTrue(DentalSurface.isSurfaceTypeValid("DISTAL")),
				() -> assertTrue(DentalSurface.isSurfaceTypeValid("VESTIBULAR")),
				() -> assertTrue(DentalSurface.isSurfaceTypeValid("LINGUAL")),
				() -> assertTrue(DentalSurface.isSurfaceTypeValid("OCCLUSAL")),
				() -> assertFalse(DentalSurface.isSurfaceTypeValid("UNKNOWN_SURFACE")),
				() -> assertTrue(DentalSurface.isSurfaceTypeValidForPieceKind("OCCLUSAL", "BACK")),
				() -> assertFalse(DentalSurface.isSurfaceTypeValidForPieceKind("OCCLUSAL", "FRONT")),
				() -> assertTrue(DentalSurfaceMark.isMarkTypeValid("CARIES")),
				() -> assertTrue(DentalSurfaceMark.isMarkTypeValid("FILLING")),
				() -> assertTrue(DentalSurfaceMark.isMarkTypeValid("RADIOGRAPH_CARIES")),
				() -> assertTrue(DentalSurfaceMark.isMarkTypeValid("FISSURE_SEALANT")),
				() -> assertTrue(DentalSurfaceMark.isMarkTypeValid("EXTRACTION")),
				() -> assertTrue(DentalSurfaceMark.isMarkTypeValid("CROWN")),
				() -> assertTrue(DentalSurfaceMark.isMarkTypeValid("ENDODONTICS")),
				() -> assertTrue(DentalSurfaceMark.isMarkTypeValid("BRIDGE")),
				() -> assertTrue(DentalSurfaceMark.isMarkTypeValid("NATURAL_ABSENCE")),
				() -> assertFalse(DentalSurfaceMark.isMarkTypeValid("UNKNOWN_MARK")),
				() -> assertTrue(DentalSurfaceMark.isMarkStateValid("PENDING")),
				() -> assertTrue(DentalSurfaceMark.isMarkStateValid("DONE")),
				() -> assertTrue(DentalSurfaceMark.isMarkStateValid("NATURAL")),
				() -> assertFalse(DentalSurfaceMark.isMarkStateValid("UNKNOWN_STATE")));

		printOk("Dental surface and mark validations passed.");
	}

	@Test
	@Order(9)
	@DisplayName("Dental state and bridge validations work correctly")
	void dentalStateAndBridgeValidationsWorkCorrectly() {
		assertAll(() -> assertTrue(DentalPieceState.isStateTypeValid("HEALTHY")),
				() -> assertTrue(DentalPieceState.isStateTypeValid("NATURAL_ABSENCE")),
				() -> assertTrue(DentalPieceState.isStateTypeValid("EXTRACTION_PENDING")),
				() -> assertTrue(DentalPieceState.isStateTypeValid("EXTRACTION_DONE")),
				() -> assertTrue(DentalPieceState.isStateTypeValid("CROWN_PENDING")),
				() -> assertTrue(DentalPieceState.isStateTypeValid("CROWN_DONE")),
				() -> assertTrue(DentalPieceState.isStateTypeValid("ENDODONTICS_PENDING")),
				() -> assertTrue(DentalPieceState.isStateTypeValid("ENDODONTICS_DONE")),
				() -> assertTrue(DentalPieceState.isStateTypeValid("BRIDGE_PENDING")),
				() -> assertTrue(DentalPieceState.isStateTypeValid("BRIDGE_DONE")),
				() -> assertTrue(DentalPieceState.isStateTypeValid("UNKNOWN")),
				() -> assertFalse(DentalPieceState.isStateTypeValid("BROKEN_STATE")),
				() -> assertTrue(DentalBridge.isBridgeStateValid("PENDING")),
				() -> assertTrue(DentalBridge.isBridgeStateValid("DONE")),
				() -> assertFalse(DentalBridge.isBridgeStateValid("UNKNOWN")),
				() -> assertTrue(DentalBridgePiece.isPieceRoleValid("ABUTMENT")),
				() -> assertTrue(DentalBridgePiece.isPieceRoleValid("PONTIC")),
				() -> assertFalse(DentalBridgePiece.isPieceRoleValid("UNKNOWN_ROLE")));

		printOk("Dental state and bridge validations passed.");
	}

	@Test
	@Order(10)
	@DisplayName("Product and inventory validations work correctly")
	void productAndInventoryValidationsWorkCorrectly() {
		assertAll(() -> assertTrue(Product.isNameValid("Diagnostic Product")),
				() -> assertFalse(Product.isNameValid(" ")),
				() -> assertTrue(Product.isDescriptionValid("Diagnostic product description")),
				() -> assertTrue(Inventory.isQuantityValid(0)), () -> assertTrue(Inventory.isQuantityValid(10)),
				() -> assertFalse(Inventory.isQuantityValid(-1)), () -> assertTrue(Inventory.isMinimumQuantityValid(0)),
				() -> assertTrue(Inventory.isMinimumQuantityValid(5)),
				() -> assertFalse(Inventory.isMinimumQuantityValid(-1)),
				() -> assertTrue(Inventory.isNotesValid("Diagnostic inventory notes")));

		printOk("Product and inventory validations passed.");
	}

	private void assertEndpointIsProtected(String endpoint) throws Exception {
		mockMvc.perform(get(endpoint)).andExpect(result -> {
			int responseStatus = result.getResponse().getStatus();

			assertTrue(responseStatus == 401 || responseStatus == 403,
					"Expected endpoint " + endpoint + " to return 401 or 403 without token, but got " + responseStatus);
		});
	}

	private static void printOk(String message) {
		PASSED_GROUPS.incrementAndGet();
		System.out.println("[OK] " + message);
	}
}