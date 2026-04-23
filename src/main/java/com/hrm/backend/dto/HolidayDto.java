package com.hrm.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class HolidayDto {
    private Integer id;
    private String name;
    private LocalDate date;
    private Boolean isPaid;
}
