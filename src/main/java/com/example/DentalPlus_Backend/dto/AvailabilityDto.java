package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.Box;
import com.example.DentalPlus_Backend.model.Dentist;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailabilityDto {

	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
	private Long treatmentId;
	private Long patientId;

	private List<AvailableDentistDto> dentists;
	private List<AvailableBoxDto> boxes;
	private AvailabilitySuggestionDto suggestion;

	public AvailabilityDto() {
	}

	public AvailabilityDto(List<AvailableDentistDto> dentists, List<AvailableBoxDto> boxes) {
		this.dentists = dentists;
		this.boxes = boxes;
	}

	public AvailabilityDto(List<AvailableDentistDto> dentists, List<AvailableBoxDto> boxes,
			AvailabilitySuggestionDto suggestion) {
		this.dentists = dentists;
		this.boxes = boxes;
		this.suggestion = suggestion;
	}

	public AvailabilityDto(LocalDate date, LocalTime startTime, LocalTime endTime, Long treatmentId, Long patientId) {
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.treatmentId = treatmentId;
		this.patientId = patientId;
	}

	public LocalDate getDate() {
		return date;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public Long getTreatmentId() {
		return treatmentId;
	}

	public Long getPatientId() {
		return patientId;
	}

	public List<AvailableDentistDto> getDentists() {
		return dentists;
	}

	public List<AvailableBoxDto> getBoxes() {
		return boxes;
	}

	public AvailabilitySuggestionDto getSuggestion() {
		return suggestion;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public void setTreatmentId(Long treatmentId) {
		this.treatmentId = treatmentId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public void setDentists(List<AvailableDentistDto> dentists) {
		this.dentists = dentists;
	}

	public void setBoxes(List<AvailableBoxDto> boxes) {
		this.boxes = boxes;
	}

	public void setSuggestion(AvailabilitySuggestionDto suggestion) {
		this.suggestion = suggestion;
	}

	public boolean hasRequestedTimeRange() {
		return startTime != null && endTime != null;
	}

	public boolean hasIncompleteTimeRange() {
		return (startTime != null && endTime == null) || (startTime == null && endTime != null);
	}

	public boolean hasTreatment() {
		return treatmentId != null;
	}

	public boolean hasPatient() {
		return patientId != null;
	}

	public static class AvailableDentistDto {
		private Long id;
		private String fullName;
		private String speciality;
		private List<AvailableTimeRangeDto> availableRanges;

		public AvailableDentistDto() {
		}

		public AvailableDentistDto(Dentist dentist) {
			this.id = dentist.getId();
			this.fullName = dentist.getPerson() == null ? null
					: buildPersonFullName(dentist.getPerson().getName(), dentist.getPerson().getFirstSurname(),
							dentist.getPerson().getSecondSurname());
			this.speciality = dentist.getSpecialityName();
		}

		public AvailableDentistDto(Dentist dentist, List<AvailableTimeRangeDto> availableRanges) {
			this(dentist);
			this.availableRanges = availableRanges;
		}

		private String buildPersonFullName(String name, String firstSurname, String secondSurname) {
			StringBuilder fullName = new StringBuilder();

			if (name != null && !name.isBlank()) {
				fullName.append(name.trim());
			}

			if (firstSurname != null && !firstSurname.isBlank()) {
				if (!fullName.isEmpty()) {
					fullName.append(" ");
				}
				fullName.append(firstSurname.trim());
			}

			if (secondSurname != null && !secondSurname.isBlank()) {
				if (!fullName.isEmpty()) {
					fullName.append(" ");
				}
				fullName.append(secondSurname.trim());
			}

			return fullName.toString();
		}

		public Long getId() {
			return id;
		}

		public String getFullName() {
			return fullName;
		}

		public String getSpeciality() {
			return speciality;
		}

		public List<AvailableTimeRangeDto> getAvailableRanges() {
			return availableRanges;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public void setSpeciality(String speciality) {
			this.speciality = speciality;
		}

		public void setAvailableRanges(List<AvailableTimeRangeDto> availableRanges) {
			this.availableRanges = availableRanges;
		}
	}

	public static class AvailableBoxDto {
		private Long id;
		private String name;
		private List<AvailableTimeRangeDto> availableRanges;

		public AvailableBoxDto() {
		}

		public AvailableBoxDto(Box box) {
			this.id = box.getId();
			this.name = box.getName();
		}

		public AvailableBoxDto(Box box, List<AvailableTimeRangeDto> availableRanges) {
			this(box);
			this.availableRanges = availableRanges;
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public List<AvailableTimeRangeDto> getAvailableRanges() {
			return availableRanges;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setAvailableRanges(List<AvailableTimeRangeDto> availableRanges) {
			this.availableRanges = availableRanges;
		}
	}

	public static class AvailableTimeRangeDto {
		private LocalTime startTime;
		private LocalTime endTime;

		public AvailableTimeRangeDto() {
		}

		public AvailableTimeRangeDto(LocalTime startTime, LocalTime endTime) {
			this.startTime = startTime;
			this.endTime = endTime;
		}

		public LocalTime getStartTime() {
			return startTime;
		}

		public LocalTime getEndTime() {
			return endTime;
		}

		public void setStartTime(LocalTime startTime) {
			this.startTime = startTime;
		}

		public void setEndTime(LocalTime endTime) {
			this.endTime = endTime;
		}
	}

	public static class AvailabilitySuggestionDto {
		private LocalTime startTime;
		private LocalTime endTime;
		private String type;

		public AvailabilitySuggestionDto() {
		}

		public AvailabilitySuggestionDto(LocalTime startTime, LocalTime endTime, String type) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.type = type;
		}

		public LocalTime getStartTime() {
			return startTime;
		}

		public LocalTime getEndTime() {
			return endTime;
		}

		public String getType() {
			return type;
		}

		public void setStartTime(LocalTime startTime) {
			this.startTime = startTime;
		}

		public void setEndTime(LocalTime endTime) {
			this.endTime = endTime;
		}

		public void setType(String type) {
			this.type = type;
		}
	}
}