package com.hrm.backend.dto;

import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder(toBuilder = true)
public record OvertimeRequestRequest(
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String reason
) {}
