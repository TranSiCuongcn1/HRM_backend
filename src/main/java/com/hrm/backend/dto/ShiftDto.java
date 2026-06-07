package com.hrm.backend.dto;

import lombok.Builder;
import java.time.LocalTime;

@Builder(toBuilder = true)
public record ShiftDto(
    Integer id,
    String code,
    String name,
    LocalTime startTime,
    LocalTime endTime,
    LocalTime breakStartTime,
    LocalTime breakEndTime,
    Boolean isDefault,
    Boolean isActive
) {}
