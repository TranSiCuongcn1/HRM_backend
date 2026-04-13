package com.hrm.backend.service.impl;

import com.hrm.backend.dto.EmployeeRequest;
import com.hrm.backend.dto.EmployeeResponse;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "Hrm@123456";

    // ========================================
    // 1. LẤY DANH SÁCH NHÂN VIÊN (PHÂN TRANG + TÌM KIẾM)
    // ========================================

    @Override
    public Page<EmployeeResponse> getAllEmployees(String keyword, String status, Pageable pageable) {
        Page<Employee> employeePage = employeeRepository.searchEmployees(keyword, status, pageable);
        return employeePage.map(this::mapToResponse);
    }

    // ========================================
    // 2. XEM CHI TIẾT HỒ SƠ NHÂN VIÊN
    // ========================================

    @Override
    public EmployeeResponse getEmployeeById(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));
        return mapToResponse(employee);
    }

    // ========================================
    // 3. THÊM MỚI NHÂN VIÊN + TỰ ĐỘNG TẠO TÀI KHOẢN
    // ========================================

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        // Kiểm tra trùng mã nhân viên
        if (employeeRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã nhân viên '" + request.getCode() + "' đã tồn tại");
        }

        // Kiểm tra trùng email
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' đã được sử dụng");
        }

        // --- Bước 1: Tạo Employee ---
        Employee employee = Employee.builder()
                .code(request.getCode())
                .name(request.getName())
                .avatar(request.getAvatar())
                .email(request.getEmail())
                .phone(request.getPhone())
                .birthday(request.getBirthday())
                .address(request.getAddress())
                .joinDate(request.getJoinDate())
                .departmentId(request.getDepartmentId())
                .status("ACTIVE")
                .build();

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Đã tạo nhân viên: {} - {}", savedEmployee.getCode(), savedEmployee.getName());

        // --- Bước 2: Tự động tạo tài khoản User ---
        String username = savedEmployee.getCode(); // Username = Mã nhân viên (VD: EMP002)

        User user = User.builder()
                .employee(savedEmployee)
                .username(username)
                .email(savedEmployee.getEmail())
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role("EMPLOYEE")
                .isActive(true)
                .build();

        userRepository.save(user);
        log.info("Đã tự động tạo tài khoản đăng nhập cho nhân viên: {} (username: {})",
                savedEmployee.getName(), username);

        // --- Bước 3: Trả về Response kèm thông tin tài khoản ---
        EmployeeResponse response = mapToResponse(savedEmployee);
        response.setGeneratedAccount(
                EmployeeResponse.AccountInfo.builder()
                        .username(username)
                        .defaultPassword(DEFAULT_PASSWORD)
                        .role("EMPLOYEE")
                        .build()
        );

        return response;
    }

    // ========================================
    // 4. CẬP NHẬT THÔNG TIN HỒ SƠ NHÂN VIÊN
    // ========================================

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Integer id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));

        // Kiểm tra nếu đổi email thì email mới không được trùng với người khác
        if (!employee.getEmail().equals(request.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' đã được sử dụng bởi nhân viên khác");
        }

        // Kiểm tra nếu đổi mã nhân viên thì mã mới không được trùng
        if (!employee.getCode().equals(request.getCode())
                && employeeRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã nhân viên '" + request.getCode() + "' đã tồn tại");
        }

        // Cập nhật thông tin Employee
        employee.setCode(request.getCode());
        employee.setName(request.getName());
        employee.setAvatar(request.getAvatar());
        employee.setPhone(request.getPhone());
        employee.setBirthday(request.getBirthday());
        employee.setAddress(request.getAddress());
        employee.setJoinDate(request.getJoinDate());
        employee.setDepartmentId(request.getDepartmentId());

        // Nếu email thay đổi → cập nhật cả bên bảng User
        if (!employee.getEmail().equals(request.getEmail())) {
            String oldEmail = employee.getEmail();
            employee.setEmail(request.getEmail());

            userRepository.findByEmail(oldEmail).ifPresent(user -> {
                user.setEmail(request.getEmail());
                userRepository.save(user);
                log.info("Đã đồng bộ email mới '{}' cho tài khoản user: {}",
                        request.getEmail(), user.getUsername());
            });
        }

        Employee updated = employeeRepository.save(employee);
        log.info("Đã cập nhật hồ sơ nhân viên: {} - {}", updated.getCode(), updated.getName());

        return mapToResponse(updated);
    }

    // ========================================
    // 5. CHO NHÂN VIÊN NGHỈ VIỆC
    // ========================================

    @Override
    @Transactional
    public void resignEmployee(Integer id, java.time.LocalDate resignationDate) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + id));

        // Cập nhật trạng thái nhân viên
        employee.setStatus("RESIGNED");
        employee.setResignationDate(resignationDate != null ? resignationDate : java.time.LocalDate.now());
        employeeRepository.save(employee);
        log.info("Đã cập nhật trạng thái nghỉ việc cho nhân viên: {}", employee.getCode());

        // Khóa tài khoản đăng nhập tương ứng
        userRepository.findByUsername(employee.getCode()).ifPresent(user -> {
            user.setIsActive(false);
            userRepository.save(user);
            log.info("Đã khóa tài khoản đăng nhập: {}", user.getUsername());
        });
    }

    // ========================================
    // HELPER: Chuyển đổi Entity → Response DTO
    // ========================================

    private EmployeeResponse mapToResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .code(employee.getCode())
                .name(employee.getName())
                .avatar(employee.getAvatar())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .birthday(employee.getBirthday())
                .address(employee.getAddress())
                .joinDate(employee.getJoinDate())
                .departmentId(employee.getDepartmentId())
                .status(employee.getStatus())
                .resignationDate(employee.getResignationDate())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
