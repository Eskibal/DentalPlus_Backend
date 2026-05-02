package com.example.DentalPlus_Backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

import java.time.LocalDate;

@JsonPropertyOrder({ "id", "name", "firstSurname", "secondSurname", "birthDate", "gender", "email", "phonePrefix",
		"phoneNumber", "address", "city", "profileImage", "active", "notes" })
@Entity
@Table(name = "person")
public class Person {

	private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
	private static final int MAX_EMAIL_LENGTH = 120;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@Column(nullable = false, length = 80)
	private String name;

	@Column(nullable = false, length = 80)
	private String firstSurname;

	@Column(length = 80)
	private String secondSurname;

	@Column
	private LocalDate birthDate;

	@Column(length = 30)
	private String gender;

	@Column(length = MAX_EMAIL_LENGTH)
	private String email;

	@Column(length = 10)
	private String phonePrefix;

	@Column(length = 20)
	private String phoneNumber;

	@Column(length = 150)
	private String address;

	@Column(length = 100)
	private String city;

	@Column(length = 255)
	private String profileImage;

	@Column(nullable = false)
	private Boolean active;

	@Column(length = 500)
	private String notes;

	public Person() {
	}

	public Person(String name, String firstSurname, String secondSurname, LocalDate birthDate, String gender,
			String email, String phonePrefix, String phoneNumber, String address, String city, String profileImage,
			Boolean active, String notes) {
		this.name = normalizeText(name);
		this.firstSurname = normalizeText(firstSurname);
		this.secondSurname = normalizeText(secondSurname);
		this.birthDate = birthDate;
		this.gender = normalizeText(gender);
		this.email = normalizeEmail(email);
		this.phonePrefix = normalizeText(phonePrefix);
		this.phoneNumber = normalizeText(phoneNumber);
		this.address = normalizeText(address);
		this.city = normalizeText(city);
		this.profileImage = normalizeText(profileImage);
		this.active = active != null ? active : true;
		this.notes = normalizeText(notes);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = normalizeText(name);
	}

	public String getFirstSurname() {
		return firstSurname;
	}

	public void setFirstSurname(String firstSurname) {
		this.firstSurname = normalizeText(firstSurname);
	}

	public String getSecondSurname() {
		return secondSurname;
	}

	public void setSecondSurname(String secondSurname) {
		this.secondSurname = normalizeText(secondSurname);
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = normalizeText(gender);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = normalizeEmail(email);
	}

	public String getPhonePrefix() {
		return phonePrefix;
	}

	public void setPhonePrefix(String phonePrefix) {
		this.phonePrefix = normalizeText(phonePrefix);
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = normalizeText(phoneNumber);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = normalizeText(address);
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = normalizeText(city);
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = normalizeText(profileImage);
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active != null ? active : true;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = normalizeText(notes);
	}

	public static boolean isNameValid(String name) {
		return name != null && !name.isBlank() && name.trim().length() <= 80;
	}

	public static boolean isFirstSurnameValid(String firstSurname) {
		return firstSurname != null && !firstSurname.isBlank() && firstSurname.trim().length() <= 80;
	}

	public static boolean isSecondSurnameValid(String secondSurname) {
		return secondSurname == null || secondSurname.isBlank() || secondSurname.trim().length() <= 80;
	}

	public static boolean isGenderValid(String gender) {
		return gender == null || gender.isBlank() || gender.trim().length() <= 30;
	}

	public static boolean isEmailValid(String email) {
		if (email == null || email.isBlank()) {
			return true;
		}

		String normalized = email.trim().toLowerCase();

		return normalized.length() <= MAX_EMAIL_LENGTH && normalized.matches(EMAIL_REGEX);
	}

	public static boolean isPhonePrefixValid(String phonePrefix) {
		return phonePrefix == null || phonePrefix.isBlank() || phonePrefix.trim().length() <= 10;
	}

	public static boolean isPhoneNumberValid(String phoneNumber) {
		return phoneNumber == null || phoneNumber.isBlank() || phoneNumber.trim().length() <= 20;
	}

	public static boolean isAddressValid(String address) {
		return address == null || address.isBlank() || address.trim().length() <= 150;
	}

	public static boolean isCityValid(String city) {
		return city == null || city.isBlank() || city.trim().length() <= 100;
	}

	public static boolean isProfileImageValid(String profileImage) {
		return profileImage == null || profileImage.isBlank() || profileImage.trim().length() <= 255;
	}

	public static boolean isNotesValid(String notes) {
		return notes == null || notes.isBlank() || notes.trim().length() <= 500;
	}

	public static String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

	public static String normalizeText(String text) {
		return text == null ? null : text.trim();
	}
}