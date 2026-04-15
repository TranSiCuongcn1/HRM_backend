package com.hrm.backend.controller;

import com.hrm.backend.dto.ApiResponse;
import com.hrm.backend.dto.LeaveBalanceResponse;
import com.hrm.backend.service.LeaveBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hrm.backend.entity.User;
import com.hrm.backend.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-balances")
@RequiredArgsConstructor
@Tag(name = "Leave Balance Management", description = "API quản lý số dư phép")
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;
    private final UserRepository userRepository;

    @PostMapping("/init")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cấp phép đầu năm",
            description = "Cấp số ngày phép mặc định cho nhân viên theo năm. Nếu đã cấp trước đó thì bỏ qua.")
    public ResponseEntity<ApiResponse<Void>> initBalance(
            @RequestParam Integer employeeId,
            @RequestParam int year) {
        leaveBalanceService.initBalanceForEmployee(employeeId, year);
        return ResponseEntity.ok(
                ApiResponse.success("Đã cấp phép năm " + year + " cho nhân viên ID " + employeeId)
        );
    }

    @GetMapping("/my")
    @Operation(summary = "Số dư phép của tôi",
            description = "Nhân viên xem số dư phép các loại của mình trong 1 năm")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getMyBalances(
            Authentication authentication,
            @RequestParam int year) {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));

        List<LeaveBalanceResponse> balances = leaveBalanceService
                .getBalancesByEmployee(user.getEmployee().getId(), year);
        return ResponseEntity.ok(
                ApiResponse.success("Số dư phép năm " + year, balances)
        );
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Số dư phép của nhân viên",
            description = "Admin xem số dư phép của 1 nhân viên")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getEmployeeBalances(
            @PathVariable Integer employeeId,
            @RequestParam int year) {

        List<LeaveBalanceResponse> balances = leaveBalanceService.getBalancesByEmployee(employeeId, year);
        return ResponseEntity.ok(
                ApiResponse.success("Số dư phép nhân viên ID " + employeeId, balances)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sửa số phép",
            description = "Admin cập nhật số ngày phép thủ công (totalDays, carryOverDays)")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> updateBalance(
            @PathVariable Integer id,
            @RequestParam(required = false) BigDecimal totalDays,
            @RequestParam(required = false) BigDecimal carryOverDays) {

        LeaveBalanceResponse updated = leaveBalanceService.updateBalance(id, totalDays, carryOverDays);
        return ResponseEntity.ok(
                ApiResponse.success("Đã cập nhật số dư phép", updated)
        );
    }
}
