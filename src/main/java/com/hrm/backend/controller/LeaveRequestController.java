package com.hrm.backend.controller;

import com.hrm.backend.dto.ApiResponse;
import com.hrm.backend.dto.LeaveRequestDTO;
import com.hrm.backend.dto.LeaveRequestResponse;
import com.hrm.backend.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
@Tag(name = "Leave Request Management", description = "API xin phép và duyệt phép")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    // ========================================
    // APIs CHO NHÂN VIÊN
    // ========================================

    @PostMapping
    @Operation(summary = "Gửi đơn xin phép",
            description = "Nhân viên gửi đơn xin nghỉ phép. Hệ thống kiểm tra số dư phép trước khi tạo đơn.")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> submitRequest(
            Authentication authentication,
            @Valid @RequestBody LeaveRequestDTO request) {

        String username = authentication.getName();
        LeaveRequestResponse response = leaveRequestService.submitRequest(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Gửi đơn xin phép thành công. Đang chờ duyệt.", response)
        );
    }

    @GetMapping("/my")
    @Operation(summary = "Đơn của tôi",
            description = "Nhân viên xem danh sách đơn xin phép của mình (mới nhất trước)")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getMyRequests(
            Authentication authentication) {

        String username = authentication.getName();
        List<LeaveRequestResponse> responses = leaveRequestService.getMyRequests(username);
        return ResponseEntity.ok(
                ApiResponse.success("Danh sách đơn xin phép của bạn", responses)
        );
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Hủy đơn",
            description = "Nhân viên hủy đơn xin phép của mình (chỉ khi đơn đang ở trạng thái PENDING)")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> cancelRequest(
            Authentication authentication,
            @PathVariable Integer id) {

        String username = authentication.getName();
        LeaveRequestResponse response = leaveRequestService.cancelRequest(username, id);
        return ResponseEntity.ok(
                ApiResponse.success("Đã hủy đơn xin phép", response)
        );
    }

    // ========================================
    // APIs CHO ADMIN
    // ========================================

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Đơn chờ duyệt",
            description = "Admin xem tất cả đơn đang chờ duyệt (sắp xếp theo thời gian gửi)")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getPendingRequests() {

        List<LeaveRequestResponse> responses = leaveRequestService.getPendingRequests();
        return ResponseEntity.ok(
                ApiResponse.success("Danh sách đơn chờ duyệt", responses)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tất cả đơn xin phép",
            description = "Admin xem tất cả đơn xin phép, có lọc theo trạng thái")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getAllRequests(
            @RequestParam(required = false) String status) {

        List<LeaveRequestResponse> responses = leaveRequestService.getAllRequests(status);
        return ResponseEntity.ok(
                ApiResponse.success("Danh sách đơn xin phép", responses)
        );
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duyệt đơn",
            description = "Admin duyệt đơn xin phép. Hệ thống tự động trừ số dư phép của nhân viên.")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> approveRequest(
            Authentication authentication,
            @PathVariable Integer id) {

        String adminUsername = authentication.getName();
        LeaveRequestResponse response = leaveRequestService.approveRequest(adminUsername, id);
        return ResponseEntity.ok(
                ApiResponse.success("Đã duyệt đơn xin phép", response)
        );
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Từ chối đơn",
            description = "Admin từ chối đơn xin phép với lý do")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> rejectRequest(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestParam String reason) {

        String adminUsername = authentication.getName();
        LeaveRequestResponse response = leaveRequestService.rejectRequest(adminUsername, id, reason);
        return ResponseEntity.ok(
                ApiResponse.success("Đã từ chối đơn xin phép", response)
        );
    }
}
