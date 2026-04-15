package com.hrm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeRequest {

    @NotBlank(message = "Mã loại phép không được để trống")
    private String code; // ANNUAL, SICK, UNPAID...

    @NotBlank(message = "Tên loại phép không được để trống")
    private String name; // Phép năm, Nghỉ bệnh...

    private Boolean isPaid; // Default: true

    private String description;
}
