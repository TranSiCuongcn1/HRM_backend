package com.hrm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {

    @NotBlank(message = "Mã phòng ban không được để trống")
    private String code;

    @NotBlank(message = "Tên phòng ban không được để trống")
    private String name;

    private String description;

    private Integer managerId; // ID của trưởng phòng

    private Integer parentId; // ID của phòng ban cha
}
