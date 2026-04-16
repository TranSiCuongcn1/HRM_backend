package com.hrm.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "month"},
                name = "uk_payroll_employee_month")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(length = 7, nullable = false) // Format: "2026-04"
    private String month;

    @Column(name = "basic_salary", precision = 12, scale = 2, nullable = false)
    private BigDecimal basicSalary;

    @Column(name = "work_days", precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal workDays = new BigDecimal("22.0"); // Ngày công tiêu chuẩn

    @Column(name = "actual_days", precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal actualDays = BigDecimal.ZERO; // Ngày công thực tế + nghỉ có lương

    /**
     * Chi tiết phụ cấp dạng JSON.
     * Lưu trữ linh hoạt: {"Ăn trưa": 500000, "Xăng xe": 300000}
     * PostgreSQL: jsonb, các DB khác: TEXT
     */
    @Column(columnDefinition = "TEXT")
    private String allowances;

    @Column(name = "total_allowances", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAllowances = BigDecimal.ZERO;

    @Column(name = "overtime_pay", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal overtimePay = BigDecimal.ZERO;

    @Column(name = "gross_salary", precision = 12, scale = 2, nullable = false)
    private BigDecimal grossSalary;

    /**
     * Chi tiết khấu trừ dạng JSON.
     * Lưu trữ linh hoạt: {"BHXH": 1760000, "Thuế TNCN": 500000}
     */
    @Column(columnDefinition = "TEXT")
    private String deductions;

    @Column(name = "total_deductions", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_salary", precision = 12, scale = 2, nullable = false)
    private BigDecimal netSalary;

    /**
     * DRAFT - Bảng nháp (Kế toán đang sửa)
     * CALCULATED - Đã chốt (khóa sửa)
     * APPROVED - Giám đốc duyệt
     * PAID - Đã thanh toán
     */
    @Column(length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
