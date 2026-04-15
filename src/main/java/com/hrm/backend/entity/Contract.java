package com.hrm.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts", uniqueConstraints = {
        // Đảm bảo mỗi nhân viên chỉ có tối đa 1 hợp đồng ACTIVE
        // (xử lý bổ sung ở tầng Service vì DB constraint không hỗ trợ partial unique trên PostgreSQL dễ dàng)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * PROBATION - Thử việc
     * DEFINITE_1YR - Có thời hạn 1 năm
     * INDEFINITE - Vô thời hạn
     */
    @Column(name = "contract_type", length = 50, nullable = false)
    private String contractType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate; // Nullable cho hợp đồng vô thời hạn

    @Column(name = "basic_salary", precision = 12, scale = 2, nullable = false)
    private BigDecimal basicSalary;

    /**
     * DRAFT - Nháp (mới tạo, chưa có hiệu lực)
     * ACTIVE - Đang có hiệu lực
     * EXPIRED - Đã hết hạn (bị thay thế bởi hợp đồng mới hoặc hết thời hạn)
     * TERMINATED - Chấm dứt trước hạn
     */
    @Column(length = 50)
    @Builder.Default
    private String status = "DRAFT";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
