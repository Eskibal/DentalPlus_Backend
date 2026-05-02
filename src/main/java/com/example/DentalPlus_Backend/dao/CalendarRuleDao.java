package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.CalendarRule;

public interface CalendarRuleDao {
	CalendarRule findById(Long id);

	CalendarRule findByClinicId(Long clinicId);

	CalendarRule findByDentistId(Long dentistId);

	CalendarRule findByReceptionistId(Long receptionistId);

	void save(CalendarRule calendarRule);

	CalendarRule update(CalendarRule calendarRule);

	void delete(CalendarRule calendarRule);
}