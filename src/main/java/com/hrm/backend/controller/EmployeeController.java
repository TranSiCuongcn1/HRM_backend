package com.hrm.backend.controller;

import com.hrm.backend.dto.ApiResponse;
import com.hrm.backend.dto.EmployeeRequest;
import com.hrm.backend.dto.EmployeeResponse;
import com.hrm.backend.service.EmployeeService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "API quản lý hồ sơ nhân sự")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * Lấy danh sách nhân viên (phân trang + tìm kiếm + lọc)
     * GET /api/v1/employees?keyword=&status=ACTIVE&page=0&size=10&sortBy=name&sortDir=asc
     */
    @GetMapping
    @Operation(summary = "Danh sách nhân viên",
            description = "Lấy danh sách nhân viên, hỗ trợ tìm kiếm theo tên/mã/email, lọc theo trạng thái, phân trang")
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> getAllEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EmployeeResponse> employees = employeeService.getAllEmployees(keyword, status, departmentId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách nhân viên thành công", employees)
        );
    }

    /**
     * Xem chi tiết hồ sơ nhân viên
     * GET /api/v1/employees/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết nhân viên", description = "Xem thông tin chi tiết hồ sơ một nhân viên theo ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Integer id) {
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy thông tin nhân viên thành công", employee)
        );
    }

    /**
     * Thêm mới nhân viên (tự động tạo tài khoản đăng nhập)
     * POST /api/v1/employees
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm nhân viên", description = "Thêm mới nhân viên và tự động tạo tài khoản đăng nhập (username = mã nhân viên, mật khẩu mặc định)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {

        EmployeeResponse created = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Thêm nhân viên thành công. Tài khoản đăng nhập đã được tạo tự động.", created)
        );
    }

    /**
     * Cập nhật thông tin hồ sơ nhân viên
     * PUT /api/v1/employees/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật nhân viên", description = "Cập nhật thông tin hồ sơ nhân viên. Nếu đổi email sẽ tự động đồng bộ email đăng nhập.")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Integer id,
            @Valid @RequestBody EmployeeRequest request) {

        EmployeeResponse updated = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật thông tin nhân viên thành công", updated)
        );
    }

    /**
     * Chuyển trạng thái nhân viên sang nghỉ việc và khóa tài khoản
     * PUT /api/v1/employees/{id}/resign
     */
    @PutMapping("/{id}/resign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cho nhân viên nghỉ việc", description = "Cập nhật trạng thái thành RESIGNED, thêm ngày nghỉ việc, và tự động khóa tài khoản đăng nhập")
    public ResponseEntity<ApiResponse<Void>> resignEmployee(
            @PathVariable Integer id,
            @RequestParam(required = false) java.time.LocalDate resignationDate) {
            
        employeeService.resignEmployee(id, resignationDate);
        return ResponseEntity.ok(
                ApiResponse.success("Đã hoàn tất thủ tục nghỉ việc và khóa tài khoản cho nhân viên")
        );
    }

        /**
         * Xóa nhân viên (chỉ khi chưa phát sinh dữ liệu nghiệp vụ)
         * DELETE /api/v1/employees/{id}
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Xóa nhân viên", description = "Xóa hồ sơ nhân viên khi chưa phát sinh dữ liệu chấm công/nghỉ phép")
        public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Integer id) {
                employeeService.deleteEmployee(id);
                return ResponseEntity.ok(
                                ApiResponse.success("Xóa nhân viên thành công")
                );
        }
}
