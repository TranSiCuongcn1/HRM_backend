package com.hrm.backend.controller;

import com.hrm.backend.dto.ApiResponse;
import com.hrm.backend.dto.LeaveTypeRequest;
import com.hrm.backend.entity.LeaveType;
import com.hrm.backend.service.LeaveTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-types")
@RequiredArgsConstructor
@Tag(name = "Leave Type Management", description = "API quản lý danh mục loại phép")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo loại phép", description = "Thêm loại phép mới vào hệ thống")
    public ResponseEntity<ApiResponse<LeaveType>> createLeaveType(
            @Valid @RequestBody LeaveTypeRequest request) {
        LeaveType created = leaveTypeService.createLeaveType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Tạo loại phép thành công", created)
        );
    }

    @GetMapping
    @Operation(summary = "Danh sách loại phép", description = "Lấy tất cả loại phép trong hệ thống")
    public ResponseEntity<ApiResponse<List<LeaveType>>> getAllLeaveTypes() {
        List<LeaveType> types = leaveTypeService.getAllLeaveTypes();
        return ResponseEntity.ok(
                ApiResponse.success("Danh sách loại phép", types)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sửa loại phép", description = "Cập nhật thông tin loại phép")
    public ResponseEntity<ApiResponse<LeaveType>> updateLeaveType(
            @PathVariable Integer id,
            @Valid @RequestBody LeaveTypeRequest request) {
        LeaveType updated = leaveTypeService.updateLeaveType(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật loại phép thành công", updated)
        );
    }
}
