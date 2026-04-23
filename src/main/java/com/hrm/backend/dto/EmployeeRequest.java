package com.hrm.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "Mã nhân viên không được để trống")
    private String code;

    @NotBlank(message = "Tên nhân viên không được để trống")
    private String name;

    private String avatar;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String phone;

    private LocalDate birthday;

    private String address;

    @NotNull(message = "Ngày vào làm không được để trống")
    private LocalDate joinDate;

    private Integer departmentId;

    private Integer dependentCount;
}
