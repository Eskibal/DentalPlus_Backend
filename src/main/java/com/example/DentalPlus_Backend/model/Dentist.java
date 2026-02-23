package com.example.DentalPlus_Backend.model;

public class Dentist extends Person {

	String speciality;
	String visitWeekday;

	public Dentist(String name, String firstSurname, String secondSurname, int age, String gender, String email,
			String phonePrefix, int phoneNumber) {
		super(name, firstSurname, secondSurname, age, gender, email, phonePrefix, phoneNumber);
		// TODO Auto-generated constructor stub
	}

	public String getSpeciality() {
		return speciality;
	}

	public void setSpeciality(String speciality) {
		this.speciality = speciality;
	}

	public String getVisitWeekday() {
		return visitWeekday;
	}

	public void setVisitWeekday(String visitWeekday) {
		this.visitWeekday = visitWeekday;
	}

	public String toString() {
		return name + " " + firstSurname + " " + secondSurname + ", " + age + " (" + gender + ") | " + email + " | +"
				+ phonePrefix + " " + phoneNumber + " | " + speciality + " - " + visitWeekday;
	}

}
