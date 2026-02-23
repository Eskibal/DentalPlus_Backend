package com.example.DentalPlus_Backend.model;

public abstract class Person {

	String name; // ej: “Pablo”
	String firstSurname; // ej: “Martinez”
	String secondSurname; // ej:“Lopez”
	int age; // ej: “23”
	String gender; // ej: “Male” o “Female”
	String email; // ej: “example@gmail.com”
	String phonePrefix; // ej: “+34”;
	int phoneNumber; // ej: “123456789”

	public Person(String name, String firstSurname, String secondSurname, int age, String gender, String email,
			String phonePrefix, int phoneNumber) {
		this.name = name;
		this.firstSurname = firstSurname;
		this.secondSurname = secondSurname;
		this.age = age;
		this.gender = gender;
		this.email = email;
		this.phonePrefix = phonePrefix;
		this.phoneNumber = phoneNumber;
	}

	@Override
	public String toString() {
		return name + " " + firstSurname + " " + secondSurname + ", " + age + " (" + gender + ") | " + email + " | +"
				+ phonePrefix + " " + phoneNumber;
	}

}
