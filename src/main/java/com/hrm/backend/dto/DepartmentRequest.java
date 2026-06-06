package com.hrm.backend.dto;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;

@Builder(toBuilder = true)
public record DepartmentRequest(
    @NotBlank(message = "Mã phòng ban không được để trống")
    String code,

    @NotBlank(message = "Tên phòng ban không được để trống")
    String name,

    String description,
    Integer managerId, // ID của trưởng phòng
    Integer parentId  // ID của phòng ban cha
) {}
