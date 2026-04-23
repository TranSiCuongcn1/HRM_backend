package com.hrm.backend.service;

import com.hrm.backend.dto.ShiftDto;
import java.util.List;

public interface ShiftService {
    List<ShiftDto> getAllShifts();
    ShiftDto getShiftById(Integer id);
    ShiftDto createShift(ShiftDto dto);
    ShiftDto updateShift(Integer id, ShiftDto dto);
    void deleteShift(Integer id);
    ShiftDto getDefaultShift();
}
