package com.hrm.backend.service.facade.impl;

import com.hrm.backend.dto.ContractResponse;
import com.hrm.backend.dto.EmployeeRequest;
import com.hrm.backend.dto.EmployeeResponse;
import com.hrm.backend.dto.PayrollResponse;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.AttendanceRepository;
import com.hrm.backend.repository.LeaveBalanceRepository;
import com.hrm.backend.repository.LeaveRequestRepository;
import com.hrm.backend.service.*;
import com.hrm.backend.service.facade.EmployeeFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeFacadeImpl implements EmployeeFacade {

    private final EmployeeService employeeService;
    private final UserService userService;
    private final LeaveBalanceService leaveBalanceService;
    private final ContractService contractService;
    private final PayrollService payrollService;

    // Các repository phụ dùng để validate ràng buộc liên kết hệ thống con trước khi xóa thực thể Employee
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    private static final String DEFAULT_PASSWORD = "Hrm@123456";

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(String keyword, String status, Integer departmentId, Pageable pageable) {
        return employeeService.getAllEmployeesRaw(keyword, status, departmentId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Integer id) {
        Employee employee = employeeService.getEmployeeEntityById(id);
        return mapToResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse onboardEmployee(EmployeeRequest request) {
        // 1. Tạo hồ sơ nhân sự thô
        Employee employee = employeeService.createEmployeeEntity(request);

        // 2. Tạo tài khoản đăng nhập (Subsystem User)
        User user = userService.createUserAccount(employee, DEFAULT_PASSWORD);

        // 3. Khởi tạo tự động số dư phép năm (Subsystem Leave)
        int currentYear = LocalDate.now().getYear();
        leaveBalanceService.initBalanceForEmployee(employee.getId(), currentYear);

        // Dựng DTO trả về kết quả
        EmployeeResponse response = mapToResponse(employee).toBuilder()
                .generatedAccount(
                        EmployeeResponse.AccountInfo.builder()
                                .username(user.getUsername())
                                .defaultPassword(DEFAULT_PASSWORD)
                                .role(user.getRole())
                                .build()
                )
                .build();

        log.info("Onboarding hoàn tất cho nhân viên: {}", employee.getCode());
        return response;
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Integer id, EmployeeRequest request) {
        String oldEmail = employeeService.getEmployeeEntityById(id).getEmail();
        
        // 1. Cập nhật hồ sơ nhân sự
        Employee employee = employeeService.updateEmployeeEntity(id, request);

        // 2. Đồng bộ email đăng nhập nếu thay đổi (Subsystem User)
        if (!oldEmail.equals(employee.getEmail())) {
            userService.updateEmail(oldEmail, employee.getEmail());
        }

        return mapToResponse(employee);
    }

    @Override
    @Transactional
    public void resignEmployee(Integer id, LocalDate resignationDate) {
        Employee employee = employeeService.getEmployeeEntityById(id);
        
        // 1. Đổi trạng thái nhân sự thô
        employeeService.resignEmployeeEntity(id, resignationDate);

        // 2. Khóa tài khoản đăng nhập tương ứng (Subsystem User)
        userService.deactivateAccount(employee.getCode());
        log.info("Đã hoàn tất thủ tục thôi việc và khóa tài khoản cho: {}", employee.getCode());
    }

    @Override
    @Transactional
    public void deleteEmployee(Integer id) {
        Employee employee = employeeService.getEmployeeEntityById(id);

        // Kiểm tra ràng buộc dữ liệu liên kết chéo trước khi xóa
        if (attendanceRepository.existsByEmployeeId(id)
                || leaveRequestRepository.existsByEmployeeId(id)
                || leaveBalanceRepository.existsByEmployeeId(id)) {
            throw new IllegalStateException(
                    "Không thể xóa nhân viên vì đã phát sinh dữ liệu chấm công hoặc nghỉ phép. Vui lòng dùng chức năng nghỉ việc.");
        }

        // 1. Xóa tài khoản đăng nhập (Subsystem User)
        userService.deleteAccountByEmployeeId(id);

        // 2. Xóa thực thể Employee
        employeeService.deleteEmployeeEntity(id);
        log.info("Đã xóa nhân viên {} - {}", employee.getCode(), employee.getName());
    }

    // HELPER kết hợp thông tin đa dạng của các Subsystem tạo thành EmployeeResponse DTO hoàn chỉnh
    private EmployeeResponse mapToResponse(Employee employee) {
        var activeContract = contractService.findActiveContract(employee.getId());
        var latestPayroll = payrollService.findLatestPayroll(employee.getId());

        String deptName = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
        Integer deptId = employee.getDepartment() != null ? employee.getDepartment().getId() : null;

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
                .departmentId(deptId)
                .departmentName(deptName)
                .status(employee.getStatus())
                .resignationDate(employee.getResignationDate())
                .dependentCount(employee.getDependentCount())
                .currentSalary(activeContract.map(ContractResponse::basicSalary).orElse(null))
                .latestNetSalary(latestPayroll.map(PayrollResponse::netSalary).orElse(null))
                .lastPayrollMonth(latestPayroll.map(PayrollResponse::month).orElse(null))
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
