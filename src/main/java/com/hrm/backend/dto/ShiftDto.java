package com.hrm.backend.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class ShiftDto {
    private Integer id;
    private String code;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private Boolean isDefault;
    private Boolean isActive;
}
