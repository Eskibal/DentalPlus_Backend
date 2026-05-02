package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.Person;

import java.time.LocalDate;

public class PersonDto {

	private Long id;
	private String name;
	private String firstSurname;
	private String secondSurname;
	private LocalDate birthDate;
	private String gender;
	private String email;
	private String phonePrefix;
	private String phoneNumber;
	private String address;
	private String city;
	private String profileImage;
	private String notes;

	public PersonDto() {
	}

	public PersonDto(Person person) {
		this.id = person.getId();
		this.name = person.getName();
		this.firstSurname = person.getFirstSurname();
		this.secondSurname = person.getSecondSurname();
		this.birthDate = person.getBirthDate();
		this.gender = person.getGender();
		this.email = person.getEmail();
		this.phonePrefix = person.getPhonePrefix();
		this.phoneNumber = person.getPhoneNumber();
		this.address = person.getAddress();
		this.city = person.getCity();
		this.profileImage = person.getProfileImage();
		this.notes = person.getNotes();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getFirstSurname() {
		return firstSurname;
	}

	public String getSecondSurname() {
		return secondSurname;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public String getGender() {
		return gender;
	}

	public String getEmail() {
		return email;
	}

	public String getPhonePrefix() {
		return phonePrefix;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public String getNotes() {
		return notes;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFirstSurname(String firstSurname) {
		this.firstSurname = firstSurname;
	}

	public void setSecondSurname(String secondSurname) {
		this.secondSurname = secondSurname;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPhonePrefix(String phonePrefix) {
		this.phonePrefix = phonePrefix;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}