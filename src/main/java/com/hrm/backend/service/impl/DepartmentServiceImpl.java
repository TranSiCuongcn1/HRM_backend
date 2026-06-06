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
    private final com.hrm.backend.config.prototype.DepartmentPrototypeRegistry departmentPrototypeRegistry;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentPrototypeRegistry.getRootDepartmentsCloned().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Integer id) {
        Department department = departmentPrototypeRegistry.getDepartmentByIdCloned(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + id));
        return mapToResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Mã phòng ban '" + request.code() + "' đã tồn tại");
        }

        Department parent = null;
        if (request.parentId() != null) {
            parent = departmentRepository.findById(request.parentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban cha với ID: " + request.parentId()));
        }

        Employee manager = null;
        if (request.managerId() != null) {
            manager = employeeRepository.findById(request.managerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên làm trưởng phòng với ID: " + request.managerId()));
        }

        Department department = Department.builder()
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .parent(parent)
                .manager(manager)
                .build();

        Department saved = departmentRepository.save(department);
        log.info("Đã tạo phòng ban: {} - {}", saved.getCode(), saved.getName());
        departmentPrototypeRegistry.refreshCache(); // Làm mới registry mẫu sau khi thêm mới
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Integer id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID: " + id));

        if (!department.getCode().equals(request.code()) && departmentRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Mã phòng ban '" + request.code() + "' đã tồn tại");
        }

        // Kiểm tra vòng lặp cha-con (bao gồm cả vòng lặp sâu)
        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new IllegalArgumentException("Phòng ban cha không thể là chính nó");
            }
            if (isChildOf(request.parentId(), id)) {
                throw new IllegalArgumentException("Phòng ban cha không thể là phòng ban con của chính nó (tránh vòng lặp)");
            }
        }

        Department parent = null;
        if (request.parentId() != null) {
            parent = departmentRepository.findById(request.parentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban cha với ID: " + request.parentId()));
        }

        Employee manager = null;
        if (request.managerId() != null) {
            manager = employeeRepository.findById(request.managerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên làm trưởng phòng với ID: " + request.managerId()));
        }

        department.setCode(request.code());
        department.setName(request.name());
        department.setDescription(request.description());
        department.setParent(parent);
        department.setManager(manager);

        Department updated = departmentRepository.save(department);
        log.info("Đã cập nhật phòng ban: {}", updated.getCode());
        departmentPrototypeRegistry.refreshCache(); // Làm mới registry mẫu sau khi cập nhật
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
        departmentPrototypeRegistry.refreshCache(); // Làm mới registry mẫu sau khi xóa
    }

    private DepartmentResponse mapToResponse(Department department) {
        DepartmentResponse.ManagerInfo managerInfo = null;
        if (department.getManager() != null) {
            managerInfo = DepartmentResponse.ManagerInfo.builder()
                    .id(department.getManager().getId())
                    .code(department.getManager().getCode())
                    .name(department.getManager().getName())
                    .build();
        }

        DepartmentResponse.DepartmentSummary parentSummary = null;
        if (department.getParent() != null) {
            parentSummary = DepartmentResponse.DepartmentSummary.builder()
                    .id(department.getParent().getId())
                    .code(department.getParent().getCode())
                    .name(department.getParent().getName())
                    .build();
        }

        List<DepartmentResponse> children = null;
        if (department.getChildren() != null && !department.getChildren().isEmpty()) {
            children = department.getChildren().stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        return DepartmentResponse.builder()
                .id(department.getId())
                .code(department.getCode())
                .name(department.getName())
                .description(department.getDescription())
                .manager(managerInfo)
                .parent(parentSummary)
                .children(children)
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}
