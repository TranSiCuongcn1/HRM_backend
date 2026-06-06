package com.hrm.backend.service.impl;

import com.hrm.backend.dto.DepartmentRequest;
import com.hrm.backend.dto.DepartmentResponse;
import com.hrm.backend.entity.Department;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.repository.DepartmentRepository;
import com.hrm.backend.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private DepartmentRequest standardRequest;
    private Department standardDepartment;

    @BeforeEach
    void setUp() {
        standardRequest = DepartmentRequest.builder()
                .code("IT")
                .name("Information Technology")
                .description("IT Department")
                .build();

        standardDepartment = Department.builder()
                .id(1)
                .code("IT")
                .name("Information Technology")
                .description("IT Department")
                .build();
    }

    @Test
    @DisplayName("Create Department - Duplicate department code should throw IllegalArgumentException")
    void createDepartment_DuplicateCode_ThrowsException() {
        when(departmentRepository.existsByCode("IT")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(standardRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mã phòng ban 'IT' đã tồn tại");

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    @DisplayName("Create Department - Valid request should successfully create department")
    void createDepartment_ValidRequest_Success() {
        when(departmentRepository.existsByCode("IT")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(standardDepartment);

        DepartmentResponse response = departmentService.createDepartment(standardRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1);
        assertThat(response.code()).isEqualTo("IT");
        assertThat(response.name()).isEqualTo("Information Technology");

        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("Update Department - Cyclic parent relationship should throw IllegalArgumentException")
    void updateDepartment_CyclicParent_ThrowsException() {
        // Mock existing department
        Department existingDept = Department.builder()
                .id(1)
                .code("IT")
                .name("Information Technology")
                .build();

        when(departmentRepository.findById(1)).thenReturn(Optional.of(existingDept));

        // Attempting to set parent to itself (id = 1)
        DepartmentRequest updateRequest = DepartmentRequest.builder()
                .code("IT")
                .name("Information Tech Updated")
                .parentId(1)
                .build();

        assertThatThrownBy(() -> departmentService.updateDepartment(1, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phòng ban cha không thể là chính nó");

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    @DisplayName("Update Department - Cyclical indirect parent relationship should throw IllegalArgumentException")
    void updateDepartment_IndirectCyclicParent_ThrowsException() {
        // Dept 1: Parent of Dept 2
        // Dept 2: Parent of Dept 3
        // Try to update Dept 1 to have Dept 3 as parent (Cyclic loop: 1 -> 3 -> 2 -> 1)
        Department dept1 = Department.builder().id(1).code("DEPT1").name("Dept 1").build();
        Department dept2 = Department.builder().id(2).code("DEPT2").name("Dept 2").parent(dept1).build();
        Department dept3 = Department.builder().id(3).code("DEPT3").name("Dept 3").parent(dept2).build();

        when(departmentRepository.findById(1)).thenReturn(Optional.of(dept1));
        // For isChildOf check: finds dept 3, potentialParent is dept3 (parent is dept2)
        when(departmentRepository.findById(3)).thenReturn(Optional.of(dept3));

        DepartmentRequest updateRequest = DepartmentRequest.builder()
                .code("DEPT1")
                .name("Dept 1 Updated")
                .parentId(3)
                .build();

        assertThatThrownBy(() -> departmentService.updateDepartment(1, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phòng ban cha không thể là phòng ban con của chính nó");

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    @DisplayName("Delete Department - Deleting department with active employees should throw RuntimeException")
    void deleteDepartment_WithActiveEmployees_ThrowsException() {
        when(departmentRepository.findById(1)).thenReturn(Optional.of(standardDepartment));
        // Mock having 5 active employees in the department
        when(employeeRepository.countByDepartment_Id(1)).thenReturn(5L);

        assertThatThrownBy(() -> departmentService.deleteDepartment(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Không thể xóa phòng ban đang có nhân viên.");

        verify(departmentRepository, never()).delete(any(Department.class));
    }

    @Test
    @DisplayName("Delete Department - Deleting department with sub-departments should throw RuntimeException")
    void deleteDepartment_WithChildDepartments_ThrowsException() {
        Department childDept = Department.builder().id(2).code("SUBIT").name("Sub IT").build();
        standardDepartment.setChildren(Collections.singletonList(childDept));

        when(departmentRepository.findById(1)).thenReturn(Optional.of(standardDepartment));
        when(employeeRepository.countByDepartment_Id(1)).thenReturn(0L);

        assertThatThrownBy(() -> departmentService.deleteDepartment(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Không thể xóa phòng ban có phòng ban con.");

        verify(departmentRepository, never()).delete(any(Department.class));
    }

    @Test
    @DisplayName("Delete Department - Empty department should be successfully deleted")
    void deleteDepartment_EmptyDepartment_Success() {
        standardDepartment.setChildren(new ArrayList<>());

        when(departmentRepository.findById(1)).thenReturn(Optional.of(standardDepartment));
        when(employeeRepository.countByDepartment_Id(1)).thenReturn(0L);

        departmentService.deleteDepartment(1);

        verify(departmentRepository).delete(standardDepartment);
    }
}
