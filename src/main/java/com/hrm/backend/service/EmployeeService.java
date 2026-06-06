package com.hrm.backend.service;

import com.hrm.backend.dto.EmployeeRequest;
import com.hrm.backend.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface EmployeeService {
    Page<Employee> getAllEmployeesRaw(String keyword, String status, Integer departmentId, Pageable pageable);
    Employee getEmployeeEntityById(Integer id);
    Employee createEmployeeEntity(EmployeeRequest request);
    Employee updateEmployeeEntity(Integer id, EmployeeRequest request);
    Employee resignEmployeeEntity(Integer id, LocalDate resignationDate);
    void deleteEmployeeEntity(Integer id);
}
