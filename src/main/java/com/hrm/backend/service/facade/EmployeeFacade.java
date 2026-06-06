package com.hrm.backend.service.facade;

import com.hrm.backend.dto.EmployeeRequest;
import com.hrm.backend.dto.EmployeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface EmployeeFacade {
    Page<EmployeeResponse> getAllEmployees(String keyword, String status, Integer departmentId, Pageable pageable);
    EmployeeResponse getEmployeeById(Integer id);
    EmployeeResponse onboardEmployee(EmployeeRequest request);
    EmployeeResponse updateEmployee(Integer id, EmployeeRequest request);
    void resignEmployee(Integer id, LocalDate resignationDate);
    void deleteEmployee(Integer id);
}
