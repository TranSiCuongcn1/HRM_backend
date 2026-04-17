package com.hrm.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeRequestRequest {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;
}
