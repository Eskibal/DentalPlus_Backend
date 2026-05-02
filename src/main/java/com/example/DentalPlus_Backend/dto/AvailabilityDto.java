package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.Box;
import com.example.DentalPlus_Backend.model.Dentist;

import java.util.List;

public class AvailabilityDto {
	private List<AvailableDentistDto> dentists;
	private List<AvailableBoxDto> boxes;

	public AvailabilityDto() {
	}

	public AvailabilityDto(List<AvailableDentistDto> dentists, List<AvailableBoxDto> boxes) {
		this.dentists = dentists;
		this.boxes = boxes;
	}

	public List<AvailableDentistDto> getDentists() {
		return dentists;
	}

	public List<AvailableBoxDto> getBoxes() {
		return boxes;
	}

	public void setDentists(List<AvailableDentistDto> dentists) {
		this.dentists = dentists;
	}

	public void setBoxes(List<AvailableBoxDto> boxes) {
		this.boxes = boxes;
	}

	public static class AvailableDentistDto {
		private Long id;
		private String fullName;
		private String speciality;

		public AvailableDentistDto() {
		}

		public AvailableDentistDto(Dentist dentist) {
			this.id = dentist.getId();
			this.fullName = dentist.getPerson() == null ? null
					: buildPersonFullName(dentist.getPerson().getName(), dentist.getPerson().getFirstSurname(),
							dentist.getPerson().getSecondSurname());
			this.speciality = dentist.getSpeciality();
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

		public void setId(Long id) {
			this.id = id;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public void setSpeciality(String speciality) {
			this.speciality = speciality;
		}
	}

	public static class AvailableBoxDto {
		private Long id;
		private String name;

		public AvailableBoxDto() {
		}

		public AvailableBoxDto(Box box) {
			this.id = box.getId();
			this.name = box.getName();
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}