package com.example.DentalPlus_Backend.dao;

import com.example.DentalPlus_Backend.model.CalendarBreak;

import java.util.List;

public interface CalendarBreakDao {
	CalendarBreak findById(Long id);

	List<CalendarBreak> findActiveByCalendarRuleId(Long calendarRuleId);

	List<CalendarBreak> findActiveByCalendarRuleIdAndDayOfWeek(Long calendarRuleId, String dayOfWeek);

	void save(CalendarBreak calendarBreak);

	CalendarBreak update(CalendarBreak calendarBreak);

	void delete(CalendarBreak calendarBreak);
}