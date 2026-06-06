package com.hrm.backend.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record EmployeeResponse(
    Integer id,
    String code,
    String name,
    String avatar,
    String email,
    String phone,
    LocalDate birthday,
    String address,
    LocalDate joinDate,
    Integer departmentId,
    String departmentName,
    String status,
    LocalDate resignationDate,
    Integer dependentCount,
    BigDecimal currentSalary,
    BigDecimal latestNetSalary,
    String lastPayrollMonth,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    AccountInfo generatedAccount
) {
    /**
     * Thông tin tài khoản đi kèm (chỉ trả về khi tạo mới nhân viên)
     */
    @Builder
    public record AccountInfo(
        String username,
        String defaultPassword,
        String role
    ) {}
}
