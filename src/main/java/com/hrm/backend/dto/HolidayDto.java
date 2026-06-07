package com.hrm.backend.dto;

import lombok.Builder;
import java.time.LocalDate;

@Builder(toBuilder = true)
public record HolidayDto(
    Integer id,
    String name,
    LocalDate date,
    Boolean isPaid
) {}
