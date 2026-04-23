package com.hrm.backend.service;

import com.hrm.backend.dto.HolidayDto;
import java.util.List;

public interface HolidayService {
    List<HolidayDto> getAllHolidays();
    HolidayDto createHoliday(HolidayDto dto);
    HolidayDto updateHoliday(Integer id, HolidayDto dto);
    void deleteHoliday(Integer id);
}
