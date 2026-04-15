package com.hrm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractRequest {

    @NotNull(message = "Mã nhân viên không được để trống")
    private Integer employeeId;

    @NotBlank(message = "Loại hợp đồng không được để trống")
    private String contractType; // PROBATION, DEFINITE_1YR, INDEFINITE

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    private LocalDate endDate; // Nullable cho INDEFINITE

    @NotNull(message = "Lương cơ bản không được để trống")
    @Positive(message = "Lương cơ bản phải lớn hơn 0")
    private BigDecimal basicSalary;
}
