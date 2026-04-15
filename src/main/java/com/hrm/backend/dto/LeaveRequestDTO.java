package com.hrm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body khi nhân viên gửi đơn xin nghỉ phép
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {

    @NotNull(message = "Loại phép không được để trống")
    private Integer leaveTypeId;

    @NotNull(message = "Ngày bắt đầu nghỉ không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc nghỉ không được để trống")
    private LocalDate endDate;

    @NotNull(message = "Số ngày nghỉ không được để trống")
    @Positive(message = "Số ngày nghỉ phải lớn hơn 0")
    private BigDecimal days; // Hỗ trợ 0.5 cho nửa ngày

    @NotBlank(message = "Lý do xin nghỉ không được để trống")
    private String reason;

    private String attachmentUrl; // File đính kèm (nullable)
}
