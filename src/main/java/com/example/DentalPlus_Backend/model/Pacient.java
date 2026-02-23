package com.example.DentalPlus_Backend.model;

import java.time.LocalDate;

public class Pacient extends Person {

	String IBAN; // Ej: “ES61 1234 3456 42 0456323532”

	String direction; // Ej: “Plaza Universidad, Nº5, Piso 3, Puerta 2”
	String medicalHistory; // Ej: “No important data”
	String reasonForConsultation; // Ej: “It hurts my molar!”
	String dentistNotes; // Ej: “The back molar has a caries”

	LocalDate dateOfBirth; // Ej: ”2026-02-29”
	LocalDate registrationDate; // Ej: ”2026-02-29”

	public Pacient(String name, String firstSurname, String secondSurname, int age, String gender, String email,
			String phonePrefix, int phoneNumber) {
		super(name, firstSurname, secondSurname, age, gender, email, phonePrefix, phoneNumber);
		// TODO Auto-generated constructor stub
	}

	public String getIBAN() {

		return IBAN;
	}

	public void setIBAN(String iBAN) {
		IBAN = iBAN;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getMedicalHistory() {
		return medicalHistory;
	}

	public void setMedicalHistory(String medicalHistory) {
		this.medicalHistory = medicalHistory;
	}

	public String getReasonForConsultation() {
		return reasonForConsultation;
	}

	public void setReasonForConsultation(String reasonForConsultation) {
		this.reasonForConsultation = reasonForConsultation;
	}

	public String getDentistNotes() {
		return dentistNotes;
	}

	public void setDentistNotes(String dentistNotes) {
		this.dentistNotes = dentistNotes;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public LocalDate getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(LocalDate registrationDate) {
		this.registrationDate = registrationDate;
	}

}
