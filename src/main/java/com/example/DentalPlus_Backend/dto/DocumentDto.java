package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.Document;

public class DocumentDto {
	private Long id;
	private Long patientId;
	private String name;
	private String storagePath;
	private String url;
	private String mimeType;
	private String documentType;
	private Boolean active;
	private String notes;

	public DocumentDto() {
	}

	public DocumentDto(Document document) {
		this.id = document.getId();
		this.patientId = document.getPatient() == null ? null : document.getPatient().getId();
		this.name = document.getName();
		this.storagePath = document.getStoragePath();
		this.mimeType = document.getMimeType();
		this.documentType = document.getDocumentType();
		this.active = document.getActive();
		this.notes = document.getNotes();
	}

	public DocumentDto(Document document, String url) {
		this(document);
		this.url = url;
	}

	public Long getId() {
		return id;
	}

	public Long getPatientId() {
		return patientId;
	}

	public String getName() {
		return name;
	}

	public String getStoragePath() {
		return storagePath;
	}

	public String getUrl() {
		return url;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getDocumentType() {
		return documentType;
	}

	public Boolean getActive() {
		return active;
	}

	public String getNotes() {
		return notes;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}