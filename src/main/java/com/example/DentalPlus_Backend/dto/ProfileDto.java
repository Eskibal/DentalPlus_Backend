package com.example.DentalPlus_Backend.dto;

import com.example.DentalPlus_Backend.model.User;

import java.util.List;

public class ProfileDto {

	private Long id;
	private String username;
	private Boolean active;
	private String themePreference;
	private String languagePreference;
	private String notes;

	private PersonDto person;
	private List<RoleDto> roles;
	private WeeklyCalendarDto weeklyCalendar;

	public ProfileDto() {
	}

	public ProfileDto(User user, PersonDto person, List<RoleDto> roles, WeeklyCalendarDto weeklyCalendar) {
		if (user != null) {
			this.id = user.getId();
			this.username = user.getUsername();
			this.active = user.getActive();
			this.themePreference = user.getThemePreference();
			this.languagePreference = user.getLanguagePreference();
			this.notes = user.getNotes();
		}

		this.person = person;
		this.roles = roles;
		this.weeklyCalendar = weeklyCalendar;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getThemePreference() {
		return themePreference;
	}

	public void setThemePreference(String themePreference) {
		this.themePreference = themePreference;
	}

	public String getLanguagePreference() {
		return languagePreference;
	}

	public void setLanguagePreference(String languagePreference) {
		this.languagePreference = languagePreference;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public PersonDto getPerson() {
		return person;
	}

	public void setPerson(PersonDto person) {
		this.person = person;
	}

	public List<RoleDto> getRoles() {
		return roles;
	}

	public void setRoles(List<RoleDto> roles) {
		this.roles = roles;
	}

	public WeeklyCalendarDto getWeeklyCalendar() {
		return weeklyCalendar;
	}

	public void setWeeklyCalendar(WeeklyCalendarDto weeklyCalendar) {
		this.weeklyCalendar = weeklyCalendar;
	}
}