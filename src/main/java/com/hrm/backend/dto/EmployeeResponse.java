package com.hrm.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    private Integer id;
    private String code;
    private String name;
    private String avatar;
    private String email;
    private String phone;
    private LocalDate birthday;
    private String address;
    private LocalDate joinDate;
    private Integer departmentId;
    private String departmentName;
    private String status;
    private LocalDate resignationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Thông tin tài khoản đi kèm (chỉ trả về khi tạo mới nhân viên)
     */
    private AccountInfo generatedAccount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountInfo {
        private String username;
        private String defaultPassword;
        private String role;
    }
}
