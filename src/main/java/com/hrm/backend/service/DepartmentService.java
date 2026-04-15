package com.hrm.backend.service;

import com.hrm.backend.dto.DepartmentRequest;
import com.hrm.backend.dto.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    List<DepartmentResponse> getAllDepartments();
    DepartmentResponse getDepartmentById(Integer id);
    DepartmentResponse createDepartment(DepartmentRequest request);
    DepartmentResponse updateDepartment(Integer id, DepartmentRequest request);
    void deleteDepartment(Integer id);
}
