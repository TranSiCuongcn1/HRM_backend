package com.hrm.backend.dto;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body khi nhân viên gửi đơn xin nghỉ phép
 */
@Builder
public record LeaveRequestDTO(
    @NotNull(message = "Loại phép không được để trống")
    Integer leaveTypeId,

    @NotNull(message = "Ngày bắt đầu nghỉ không được để trống")
    LocalDate startDate,

    @NotNull(message = "Ngày kết thúc nghỉ không được để trống")
    LocalDate endDate,

    @NotNull(message = "Số ngày nghỉ không được để trống")
    @Positive(message = "Số ngày nghỉ phải lớn hơn 0")
    BigDecimal days, // Hỗ trợ 0.5 cho nửa ngày

    String halfDaySession, // MORNING, AFTERNOON

    @NotBlank(message = "Lý do xin nghỉ không được để trống")
    String reason,

    String attachmentUrl // File đính kèm (nullable)
) {}
