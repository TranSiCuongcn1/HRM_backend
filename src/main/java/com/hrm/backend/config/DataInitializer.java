package com.hrm.backend.config;

import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.User;
import com.hrm.backend.entity.Department;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hrm.backend.repository.EmployeeRepository;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Tạo dữ liệu mẫu khi khởi động ứng dụng.
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                initDepartmentsAndAdmin();
                log.info("=== Dữ liệu mẫu đã được khởi tạo ===");
                log.info("Admin Username: admin / Password: admin123");
                log.info("============================================");
            } else {
                log.info("Dữ liệu đã tồn tại, bỏ qua khởi tạo.");
            }
        };
    }

    @Transactional
    protected void initDepartmentsAndAdmin() {
        // 1. Tạo các phòng ban mẫu (kiểm tra trước khi tạo để tránh lỗi trùng lặp khi chạy lại)
        Department boardOfDirectors = departmentRepository.findByCode("BOD")
                .orElseGet(() -> departmentRepository.save(Department.builder()
                        .code("BOD")
                        .name("Ban Giám Đốc")
                        .description("Cấp quản lý cao nhất của công ty")
                        .build()));

        Department itDept = departmentRepository.findByCode("IT")
                .orElseGet(() -> departmentRepository.save(Department.builder()
                        .code("IT")
                        .name("Phòng Công nghệ thông tin")
                        .description("Quản lý hệ thống và phần mềm")
                        .parent(boardOfDirectors)
                        .build()));

        Department hrDept = departmentRepository.findByCode("HR")
                .orElseGet(() -> departmentRepository.save(Department.builder()
                        .code("HR")
                        .name("Phòng Hành chính nhân sự")
                        .description("Quản lý con người và chế độ")
                        .parent(boardOfDirectors)
                        .build()));

        // 2. Tạo Employee Admin (thuộc Ban Giám Đốc)
        Employee adminEmployee = employeeRepository.findByCode("EMP001")
                .orElseGet(() -> employeeRepository.save(Employee.builder()
                        .code("EMP001")
                        .name("System Administrator")
                        .email("admin@hrm.com")
                        .joinDate(java.time.LocalDate.now())
                        .status("ACTIVE")
                        .department(boardOfDirectors)
                        .build()));

        // 3. Gán Admin làm trưởng phòng BOD
        boardOfDirectors.setManager(adminEmployee);
        departmentRepository.save(boardOfDirectors);

        // 4. Tạo User Admin
        User adminUser = User.builder()
                .employee(adminEmployee)
                .username("admin")
                .email("admin@hrm.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role("ADMIN")
                .build();
        userRepository.save(adminUser);
    }
}
