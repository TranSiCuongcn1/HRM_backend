package com.hrm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, unique = true, nullable = false)
    private String code; // ANNUAL, SICK, UNPAID, MATERNITY, WEDDING, BEREAVEMENT

    @Column(length = 100, nullable = false)
    private String name; // Phép năm, Nghỉ bệnh...

    @Column(name = "is_paid")
    @Builder.Default
    private Boolean isPaid = true; // Có lương hay không (quan trọng cho Payroll)

    @Column(columnDefinition = "TEXT")
    private String description;
}
