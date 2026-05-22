package com.hrm.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_records", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "date"},
                name = "uk_attendance_employee_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "check_in")
    private LocalTime checkIn;

    @Column(name = "check_out")
    private LocalTime checkOut;

    /**
     * ON_TIME - Đúng giờ
     * LATE - Đi trễ (check-in sau 08:00)
     * EARLY_LEAVE - Về sớm (check-out trước 17:00)
     * ABSENT - Vắng mặt (Admin đánh dấu)
     * HALF_DAY - Làm nửa ngày
     */
    @Column(length = 20)
    private String status;

    @Column(name = "overtime_hours", precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "work_hours", precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal workHours = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "late_minutes")
    @Builder.Default
    private Integer lateMinutes = 0;

    @Column(name = "early_leave_minutes")
    @Builder.Default
    private Integer earlyLeaveMinutes = 0;

    @Column(name = "check_in_ip", length = 50)
    private String checkInIp;

    @Column(name = "check_in_lat", precision = 10, scale = 8)
    private BigDecimal checkInLat;

    @Column(name = "check_in_lng", precision = 11, scale = 8)
    private BigDecimal checkInLng;

    @Column(name = "check_out_ip", length = 50)
    private String checkOutIp;

    @Column(name = "check_out_lat", precision = 10, scale = 8)
    private BigDecimal checkOutLat;

    @Column(name = "check_out_lng", precision = 11, scale = 8)
    private BigDecimal checkOutLng;

    @Column(name = "check_in_gps_valid")
    private Boolean checkInGpsValid;

    @Column(name = "check_in_ip_valid")
    private Boolean checkInIpValid;

    @Column(name = "check_out_gps_valid")
    private Boolean checkOutGpsValid;

    @Column(name = "check_out_ip_valid")
    private Boolean checkOutIpValid;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

