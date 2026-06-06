package com.hrm.backend.dto;

import lombok.Builder;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Builder(toBuilder = true)
public record EmployeeRequest(
    String code,

    @NotBlank(message = "Tên nhân viên không được để trống")
    String name,

    String avatar,

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    String email,

    String phone,
    LocalDate birthday,
    String address,

    @NotNull(message = "Ngày vào làm không được để trống")
    LocalDate joinDate,

    Integer departmentId,
    Integer dependentCount
) {}
