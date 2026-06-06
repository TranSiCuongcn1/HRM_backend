package com.hrm.backend.dto;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;

@Builder
public record LeaveTypeRequest(
    @NotBlank(message = "Mã loại phép không được để trống")
    String code, // ANNUAL, SICK, UNPAID...

    @NotBlank(message = "Tên loại phép không được để trống")
    String name, // Phép năm, Nghỉ bệnh...

    Boolean isPaid, // Default: true

    String description
) {}
