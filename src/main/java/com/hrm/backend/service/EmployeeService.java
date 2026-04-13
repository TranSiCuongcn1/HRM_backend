package com.hrm.backend.service;

import com.hrm.backend.dto.EmployeeRequest;
import com.hrm.backend.dto.EmployeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    /**
     * Lấy danh sách nhân viên có phân trang và tìm kiếm
     * @param keyword Từ khoá tìm kiếm (tên, mã, email)
     * @param status Lọc theo trạng thái (ACTIVE, RESIGNED,...)
     * @param pageable Thông tin phân trang
     */
    Page<EmployeeResponse> getAllEmployees(String keyword, String status, Pageable pageable);

    /**
     * Xem chi tiết hồ sơ nhân viên
     */
    EmployeeResponse getEmployeeById(Integer id);

    /**
     * Thêm mới nhân viên + tự động tạo tài khoản đăng nhập
     */
    EmployeeResponse createEmployee(EmployeeRequest request);

    /**
     * Cập nhật thông tin hồ sơ nhân viên
     */
    EmployeeResponse updateEmployee(Integer id, EmployeeRequest request);

    /**
     * Cho nhân viên nghỉ việc (Cập nhật trạng thái và khóa tài khoản)
     */
    void resignEmployee(Integer id, java.time.LocalDate resignationDate);
}
