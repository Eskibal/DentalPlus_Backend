package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.Patient;

import java.time.LocalDate;
import java.util.List;

public class PatientDto {
	private Long patientId;
	private Long userId;
	private Long clinicId;
	private String clinicName;
	private LocalDate registrationDate;
	private Boolean active;
	private String notes;
	private PersonDto person;
	private List<DocumentDto> documents;

	public PatientDto() {
	}

	public PatientDto(Patient patient) {
		this.patientId = patient.getId();
		this.userId = patient.getUser() == null ? null : patient.getUser().getId();
		this.clinicId = patient.getClinic() == null ? null : patient.getClinic().getId();
		this.clinicName = patient.getClinic() == null ? null : patient.getClinic().getName();
		this.registrationDate = patient.getRegistrationDate();
		this.active = patient.getActive();
		this.notes = patient.getNotes();
		this.person = patient.getPerson() == null ? null : new PersonDto(patient.getPerson());
	}

	public PatientDto(Patient patient, List<DocumentDto> documents) {
		this(patient);
		this.documents = documents;
	}

	public Long getPatientId() {
		return patientId;
	}

	public Long getUserId() {
		return userId;
	}

	public Long getClinicId() {
		return clinicId;
	}

	public String getClinicName() {
		return clinicName;
	}

	public LocalDate getRegistrationDate() {
		return registrationDate;
	}

	public Boolean getActive() {
		return active;
	}

	public String getNotes() {
		return notes;
	}

	public PersonDto getPerson() {
		return person;
	}

	public List<DocumentDto> getDocuments() {
		return documents;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public void setClinicId(Long clinicId) {
		this.clinicId = clinicId;
	}

	public void setClinicName(String clinicName) {
		this.clinicName = clinicName;
	}

	public void setRegistrationDate(LocalDate registrationDate) {
		this.registrationDate = registrationDate;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setPerson(PersonDto person) {
		this.person = person;
	}

	public void setDocuments(List<DocumentDto> documents) {
		this.documents = documents;
	}
}