package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.CalendarException;

import java.time.LocalDate;
import java.util.List;

public interface CalendarExceptionDao {
	CalendarException findById(Long id);

	List<CalendarException> findActiveByCalendarRuleId(Long calendarRuleId);

	List<CalendarException> findActiveByCalendarRuleIdAndDate(Long calendarRuleId, LocalDate date);

	void save(CalendarException calendarException);

	CalendarException update(CalendarException calendarException);

	void delete(CalendarException calendarException);
}