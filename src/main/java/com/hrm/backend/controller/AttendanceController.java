package com.hrm.backend.controller;

import com.hrm.backend.dto.ApiResponse;
import com.hrm.backend.dto.AttendanceRequest;
import com.hrm.backend.dto.AttendanceResponse;
import com.hrm.backend.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "API chấm công (check-in/check-out)")
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ========================================
    // APIs CHO NHÂN VIÊN (TỰ CHẤM CÔNG)
    // ========================================

    /**
     * Nhân viên click button Check-in
     */
    @PostMapping("/check-in")
    @Operation(summary = "Check-in",
            description = "Nhân viên click button chấm công vào. Hệ thống ghi nhận giờ hiện tại và tự động đánh giá đúng giờ/đi trễ.")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(Authentication authentication) {

        String username = authentication.getName();
        AttendanceResponse response = attendanceService.checkIn(username);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Check-in thành công", response)
        );
    }

    /**
     * Nhân viên click button Check-out
     */
    @PostMapping("/check-out")
    @Operation(summary = "Check-out",
            description = "Nhân viên click button chấm công ra. Hệ thống tính giờ làm việc và giờ tăng ca.")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(Authentication authentication) {

        String username = authentication.getName();
        AttendanceResponse response = attendanceService.checkOut(username);
        return ResponseEntity.ok(
                ApiResponse.success("Check-out thành công", response)
        );
    }

    /**
     * Xem trạng thái chấm công hôm nay
     */
    @GetMapping("/today")
    @Operation(summary = "Trạng thái hôm nay",
            description = "Xem trạng thái chấm công ngày hôm nay của bạn (đã check-in chưa, giờ vào/ra).")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getMyToday(Authentication authentication) {

        String username = authentication.getName();
        AttendanceResponse response = attendanceService.getMyToday(username);
        return ResponseEntity.ok(
                ApiResponse.success("Trạng thái chấm công hôm nay", response)
        );
    }

    /**
     * Xem lịch sử chấm công của mình
     */
    @GetMapping("/my-records")
    @Operation(summary = "Lịch sử chấm công",
            description = "Xem lịch sử chấm công của bạn theo khoảng ngày.")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getMyRecords(
            Authentication authentication,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String username = authentication.getName();
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AttendanceResponse> records = attendanceService.getMyRecords(username, from, to, status, pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Lịch sử chấm công", records)
        );
    }

    // ========================================
    // APIs CHO ADMIN (QUẢN LÝ & SỬA LỖI)
    // ========================================

    /**
     * Admin sửa bản ghi chấm công
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sửa chấm công",
            description = "Admin sửa giờ vào/ra, trạng thái, ghi chú của bản ghi chấm công. Hệ thống tự tính lại giờ làm.")
    public ResponseEntity<ApiResponse<AttendanceResponse>> adminUpdateRecord(
            @PathVariable Integer id,
            @RequestBody AttendanceRequest request) {

        AttendanceResponse response = attendanceService.adminUpdateRecord(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Đã cập nhật bản ghi chấm công", response)
        );
    }

    /**
     * Admin đánh vắng mặt cho nhân viên
     */
    @PostMapping("/mark-absent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Đánh vắng mặt",
            description = "Admin đánh dấu nhân viên vắng mặt trong 1 ngày cụ thể.")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAbsent(
            @RequestParam Integer employeeId,
            @RequestParam LocalDate date,
            @RequestParam(required = false) String note) {

        AttendanceResponse response = attendanceService.markAbsent(employeeId, date, note);
        return ResponseEntity.ok(
                ApiResponse.success("Đã đánh vắng mặt", response)
        );
    }

    /**
     * Xem chấm công của 1 nhân viên theo khoảng ngày
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Chấm công theo nhân viên",
            description = "Xem lịch sử chấm công của 1 nhân viên trong khoảng ngày.")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getRecordsByEmployee(
            @PathVariable Integer employeeId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AttendanceResponse> records = attendanceService.getRecordsByEmployee(employeeId, from, to, status, pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Lịch sử chấm công nhân viên", records)
        );
    }

    /**
     * Bảng chấm công toàn công ty theo ngày
     */
    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Chấm công theo ngày",
            description = "Bảng chấm công toàn công ty trong 1 ngày cụ thể.")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getRecordsByDate(
            @RequestParam LocalDate date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean hasOvertime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employee.code") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AttendanceResponse> records = attendanceService.getRecordsByDate(date, status, keyword, hasOvertime, pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Bảng chấm công ngày " + date, records)
        );
    }

    /**
     * Thống kê chấm công tháng (Payroll sẽ dùng data này)
     */
    @GetMapping("/stats/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thống kê tháng",
            description = "Thống kê chấm công tháng: tổng ngày công, số lần đi trễ, tổng giờ tăng ca.")
    public ResponseEntity<ApiResponse<AttendanceResponse.MonthlyStats>> getMonthlyStats(
            @PathVariable Integer employeeId,
            @RequestParam int month,
            @RequestParam int year) {

        AttendanceResponse.MonthlyStats stats = attendanceService.getMonthlyStats(employeeId, month, year);
        return ResponseEntity.ok(
                ApiResponse.success("Thống kê chấm công tháng " + month + "/" + year, stats)
        );
    }
}
