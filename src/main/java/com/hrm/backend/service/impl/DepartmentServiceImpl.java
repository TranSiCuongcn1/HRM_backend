package com.hrm.backend.service.impl;

import com.hrm.backend.dto.DepartmentRequest;
import com.hrm.backend.dto.DepartmentResponse;
import com.hrm.backend.entity.Department;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.repository.DepartmentRepository;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        // Sử dụng phương thức repository đã tối ưu hóa để lấy các phòng ban cấp cao nhất
        return departmentRepository.findAllByParentIsNull().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Integer id) {
        Department department = departmentRepository.findWithChildrenById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + id));
        return mapToResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        // ... (existing code for checking code and manager)
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã phòng ban '" + request.getCode() + "' đã tồn tại");
        }

        Department parent = null;
        if (request.getParentId() != null) {
            parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban cha với ID: " + request.getParentId()));
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên làm trưởng phòng với ID: " + request.getManagerId()));
        }

        Department department = Department.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .parent(parent)
                .manager(manager)
                .build();

        Department saved = departmentRepository.save(department);
        log.info("Đã tạo phòng ban: {} - {}", saved.getCode(), saved.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Integer id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + id));

        if (!department.getCode().equals(request.getCode()) && departmentRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã phòng ban '" + request.getCode() + "' đã tồn tại");
        }

        // Kiểm tra vòng lặp cha-con (bao gồm cả vòng lặp sâu)
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("Phòng ban cha không thể là chính nó");
            }
            if (isChildOf(request.getParentId(), id)) {
                throw new IllegalArgumentException("Phòng ban cha không thể là phòng ban con của chính nó (tránh vòng lặp)");
            }
        }

        Department parent = null;
        if (request.getParentId() != null) {
            parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban cha với ID: " + request.getParentId()));
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên làm trưởng phòng với ID: " + request.getManagerId()));
        }

        department.setCode(request.getCode());
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setParent(parent);
        department.setManager(manager);

        Department updated = departmentRepository.save(department);
        log.info("Đã cập nhật phòng ban: {}", updated.getCode());
        return mapToResponse(updated);
    }

    /**
     * Kiểm tra xem phòng ban A có phải là con (trực tiếp hoặc gián tiếp) của phòng ban B không.
     */
    private boolean isChildOf(Integer potentialParentId, Integer currentId) {
        Department potentialParent = departmentRepository.findById(potentialParentId).orElse(null);
        while (potentialParent != null && potentialParent.getParent() != null) {
            if (potentialParent.getParent().getId().equals(currentId)) {
                return true;
            }
            potentialParent = potentialParent.getParent();
        }
        return false;
    }

    @Override
    @Transactional
    public void deleteDepartment(Integer id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + id));

        // Kiểm tra xem có nhân viên thuộc phòng ban này không
        if (employeeRepository.countByDepartment_Id(id) > 0) {
            throw new RuntimeException("Không thể xóa phòng ban đang có nhân viên.");
        }

        // Kiểm tra xem có phòng ban con không
        if (department.getChildren() != null && !department.getChildren().isEmpty()) {
            throw new RuntimeException("Không thể xóa phòng ban có phòng ban con.");
        }

        departmentRepository.delete(department);
        log.info("Đã xóa phòng ban ID: {}", id);
    }

    private DepartmentResponse mapToResponse(Department department) {
        DepartmentResponse response = DepartmentResponse.builder()
                .id(department.getId())
                .code(department.getCode())
                .name(department.getName())
                .description(department.getDescription())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();

        if (department.getManager() != null) {
            response.setManager(DepartmentResponse.ManagerInfo.builder()
                    .id(department.getManager().getId())
                    .code(department.getManager().getCode())
                    .name(department.getManager().getName())
                    .build());
        }

        if (department.getParent() != null) {
            response.setParent(DepartmentResponse.DepartmentSummary.builder()
                    .id(department.getParent().getId())
                    .code(department.getParent().getCode())
                    .name(department.getParent().getName())
                    .build());
        }

        if (department.getChildren() != null && !department.getChildren().isEmpty()) {
            response.setChildren(department.getChildren().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
