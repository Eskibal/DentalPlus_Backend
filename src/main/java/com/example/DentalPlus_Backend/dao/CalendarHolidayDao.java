package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.CalendarHoliday;

import java.time.LocalDate;
import java.util.List;

public interface CalendarHolidayDao {
	CalendarHoliday findById(Long id);

	List<CalendarHoliday> findActiveByCalendarRuleId(Long calendarRuleId);

	List<CalendarHoliday> findActiveByCalendarRuleIdAndDate(Long calendarRuleId, LocalDate date);

	void save(CalendarHoliday calendarHoliday);

	CalendarHoliday update(CalendarHoliday calendarHoliday);

	void delete(CalendarHoliday calendarHoliday);
}