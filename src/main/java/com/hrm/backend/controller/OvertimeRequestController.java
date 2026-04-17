package com.hrm.backend.controller;

import com.hrm.backend.dto.ApiResponse;
import com.hrm.backend.dto.OvertimeRequestRequest;
import com.hrm.backend.dto.OvertimeRequestResponse;
import com.hrm.backend.service.OvertimeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/overtime-requests")
@RequiredArgsConstructor
@Tag(name = "Overtime Request Management", description = "API đăng ký và phê duyệt tăng ca")
public class OvertimeRequestController {

    private final OvertimeRequestService overtimeRequestService;

    @PostMapping
    @Operation(summary = "Gửi đơn đăng ký tăng ca")
    public ResponseEntity<ApiResponse<OvertimeRequestResponse>> createRequest(
            Authentication authentication,
            @Valid @RequestBody OvertimeRequestRequest request) {
        String username = authentication.getName();
        OvertimeRequestResponse response = overtimeRequestService.createRequest(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Đã gửi đơn đăng ký tăng ca thành công", response)
        );
    }

    @GetMapping("/my")
    @Operation(summary = "Đơn của tôi")
    public ResponseEntity<ApiResponse<Page<OvertimeRequestResponse>>> getMyRequests(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String username = authentication.getName();
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OvertimeRequestResponse> responses = overtimeRequestService.getMyRequests(username, status, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("Danh sách đơn tăng ca của bạn", responses));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tất cả đơn (Admin)")
    public ResponseEntity<ApiResponse<Page<OvertimeRequestResponse>>> getAllRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OvertimeRequestResponse> responses = overtimeRequestService.getAllRequests(status, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("Danh sách đơn tăng ca hệ thống", responses));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duyệt đơn")
    public ResponseEntity<ApiResponse<OvertimeRequestResponse>> approveRequest(
            Authentication authentication,
            @PathVariable Integer id) {
        String adminUsername = authentication.getName();
        OvertimeRequestResponse response = overtimeRequestService.approveRequest(id, adminUsername);
        return ResponseEntity.ok(ApiResponse.success("Đã duyệt đơn tăng ca", response));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Từ chối đơn")
    public ResponseEntity<ApiResponse<OvertimeRequestResponse>> rejectRequest(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestParam String reason) {
        String adminUsername = authentication.getName();
        OvertimeRequestResponse response = overtimeRequestService.rejectRequest(id, adminUsername, reason);
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối đơn tăng ca", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hủy đơn (NV)")
    public ResponseEntity<ApiResponse<Void>> cancelRequest(
            Authentication authentication,
            @PathVariable Integer id) {
        String username = authentication.getName();
        overtimeRequestService.cancelRequest(id, username);
        return ResponseEntity.ok(ApiResponse.success("Đã hủy đơn tăng ca", null));
    }
}
