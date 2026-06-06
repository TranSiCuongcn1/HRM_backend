package com.hrm.backend.service.impl;

import com.hrm.backend.dto.EmployeeRequest;
import com.hrm.backend.entity.Department;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.repository.DepartmentRepository;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployeesRaw(String keyword, String status, Integer departmentId, Pageable pageable) {
        return employeeRepository.searchEmployees(keyword, status, departmentId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeEntityById(Integer id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));
    }

    @Override
    @Transactional
    public Employee createEmployeeEntity(EmployeeRequest request) {
        String code = request.code();
        if (code == null || code.trim().isEmpty()) {
            long count = employeeRepository.count();
            code = String.format("EMP%04d", count + 1);
            while (employeeRepository.existsByCode(code)) {
                count++;
                code = String.format("EMP%04d", count + 1);
            }
        } else {
            if (employeeRepository.existsByCode(code)) {
                throw new IllegalArgumentException("Mã nhân viên '" + code + "' đã tồn tại");
            }
        }

        if (employeeRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email '" + request.email() + "' đã được sử dụng");
        }

        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + request.departmentId()));
        }

        Employee employee = Employee.builder()
                .code(code)
                .name(request.name())
                .avatar(request.avatar())
                .email(request.email())
                .phone(request.phone())
                .birthday(request.birthday())
                .address(request.address())
                .joinDate(request.joinDate())
                .department(department)
                .status("ACTIVE")
                .dependentCount(request.dependentCount() != null ? request.dependentCount() : 0)
                .build();

        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Employee updateEmployeeEntity(Integer id, EmployeeRequest request) {
        Employee employee = getEmployeeEntityById(id);

        if (!employee.getEmail().equals(request.email())
                && employeeRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email '" + request.email() + "' đã được sử dụng bởi nhân viên khác");
        }

        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + request.departmentId()));
        }

        employee.setName(request.name());
        employee.setAvatar(request.avatar());
        employee.setPhone(request.phone());
        employee.setBirthday(request.birthday());
        employee.setAddress(request.address());
        employee.setJoinDate(request.joinDate());
        employee.setDependentCount(request.dependentCount() != null ? request.dependentCount() : 0);
        employee.setDepartment(department);

        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Employee resignEmployeeEntity(Integer id, LocalDate resignationDate) {
        Employee employee = getEmployeeEntityById(id);
        employee.setStatus("RESIGNED");
        employee.setResignationDate(resignationDate != null ? resignationDate : LocalDate.now());
        
        // Remove manager reference
        List<Department> managedDepartments = departmentRepository.findByManager_Id(id);
        for (Department dept : managedDepartments) {
            dept.setManager(null);
            departmentRepository.save(dept);
        }

        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void deleteEmployeeEntity(Integer id) {
        Employee employee = getEmployeeEntityById(id);

        // Remove manager reference
        List<Department> managedDepartments = departmentRepository.findByManager_Id(id);
        for (Department dept : managedDepartments) {
            dept.setManager(null);
            departmentRepository.save(dept);
        }

        employeeRepository.delete(employee);
    }
}
