package com.example.DentalPlus_Backend.model;

import java.time.LocalDate;

public class Document {

	int idDocument;
	String type;
	String fileUrl;
	LocalDate captureDate;

	public int getIdDocument() {
		return idDocument;
	}

	public void setIdDocument(int idDocument) {
		this.idDocument = idDocument;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public LocalDate getCaptureDate() {
		return captureDate;
	}

	public void setCaptureDate(LocalDate captureDate) {
		this.captureDate = captureDate;
	}

}
