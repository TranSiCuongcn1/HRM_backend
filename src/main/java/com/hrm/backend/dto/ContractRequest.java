package com.hrm.backend.dto;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder(toBuilder = true)
public record ContractRequest(
    @NotNull(message = "Mã nhân viên không được để trống")
    Integer employeeId,

    @NotBlank(message = "Loại hợp đồng không được để trống")
    String contractType, // PROBATION, DEFINITE_1YR, INDEFINITE

    @NotNull(message = "Ngày bắt đầu không được để trống")
    LocalDate startDate,

    LocalDate endDate, // Nullable cho INDEFINITE

    @NotNull(message = "Lương cơ bản không được để trống")
    @Positive(message = "Lương cơ bản phải lớn hơn 0")
    BigDecimal basicSalary
) {}
