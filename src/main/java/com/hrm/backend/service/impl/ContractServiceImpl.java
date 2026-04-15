package com.hrm.backend.service.impl;

import com.hrm.backend.dto.ContractRequest;
import com.hrm.backend.dto.ContractResponse;
import com.hrm.backend.entity.Contract;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.repository.ContractRepository;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.service.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;

    // ========================================
    // 1. TẠO HỢP ĐỒNG MỚI (DRAFT)
    // ========================================

    @Override
    @Transactional
    public ContractResponse createContract(ContractRequest request) {
        // Validate nhân viên tồn tại
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy nhân viên với ID: " + request.getEmployeeId()));

        // Không cho tạo hợp đồng cho nhân viên đã nghỉ việc
        if ("RESIGNED".equals(employee.getStatus())) {
            throw new IllegalArgumentException(
                    "Không thể tạo hợp đồng cho nhân viên đã nghỉ việc: " + employee.getCode());
        }

        // Validate logic ngày tháng
        validateDates(request);

        // Tạo hợp đồng mới với trạng thái DRAFT
        Contract contract = Contract.builder()
                .employee(employee)
                .contractType(request.getContractType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .basicSalary(request.getBasicSalary())
                .status("DRAFT")
                .build();

        Contract saved = contractRepository.save(contract);
        log.info("Đã tạo hợp đồng DRAFT #{} cho nhân viên {} - Loại: {}, Lương: {}",
                saved.getId(), employee.getCode(), saved.getContractType(), saved.getBasicSalary());

        return mapToResponse(saved);
    }

    // ========================================
    // 2. CẬP NHẬT HỢP ĐỒNG (CHỈ KHI DRAFT)
    // ========================================

    @Override
    @Transactional
    public ContractResponse updateContract(Integer id, ContractRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng với ID: " + id));

        // Chỉ cho phép sửa hợp đồng đang ở trạng thái DRAFT
        if (!"DRAFT".equals(contract.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ có thể sửa hợp đồng ở trạng thái DRAFT. Trạng thái hiện tại: " + contract.getStatus());
        }

        // Validate logic ngày tháng
        validateDates(request);

        // Cập nhật thông tin
        contract.setContractType(request.getContractType());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setBasicSalary(request.getBasicSalary());

        Contract updated = contractRepository.save(contract);
        log.info("Đã cập nhật hợp đồng DRAFT #{} - Lương mới: {}", updated.getId(), updated.getBasicSalary());

        return mapToResponse(updated);
    }

    // ========================================
    // 3. KÍCH HOẠT HỢP ĐỒNG (DRAFT → ACTIVE)
    // ========================================

    @Override
    @Transactional
    public ContractResponse activateContract(Integer id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng với ID: " + id));

        if (!"DRAFT".equals(contract.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ có thể kích hoạt hợp đồng ở trạng thái DRAFT. Trạng thái hiện tại: " + contract.getStatus());
        }

        Integer employeeId = contract.getEmployee().getId();

        // Nếu nhân viên đã có hợp đồng ACTIVE → tự động chuyển sang EXPIRED
        contractRepository.findByEmployeeIdAndStatus(employeeId, "ACTIVE")
                .ifPresent(oldContract -> {
                    oldContract.setStatus("EXPIRED");
                    contractRepository.save(oldContract);
                    log.info("Tự động EXPIRED hợp đồng cũ #{} của nhân viên {}",
                            oldContract.getId(), contract.getEmployee().getCode());
                });

        // Kích hoạt hợp đồng mới
        contract.setStatus("ACTIVE");
        Contract activated = contractRepository.save(contract);
        log.info("Đã kích hoạt hợp đồng #{} cho nhân viên {} - Lương: {}",
                activated.getId(), contract.getEmployee().getCode(), activated.getBasicSalary());

        return mapToResponse(activated);
    }

    // ========================================
    // 4. CHẤM DỨT HỢP ĐỒNG TRƯỚC HẠN
    // ========================================

    @Override
    @Transactional
    public ContractResponse terminateContract(Integer id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng với ID: " + id));

        if (!"ACTIVE".equals(contract.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ có thể chấm dứt hợp đồng đang ACTIVE. Trạng thái hiện tại: " + contract.getStatus());
        }

        contract.setStatus("TERMINATED");
        Contract terminated = contractRepository.save(contract);
        log.info("Đã chấm dứt hợp đồng #{} của nhân viên {}", terminated.getId(),
                contract.getEmployee().getCode());

        return mapToResponse(terminated);
    }

    // ========================================
    // 5. XEM CHI TIẾT HỢP ĐỒNG
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getContractById(Integer id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng với ID: " + id));
        return mapToResponse(contract);
    }

    // ========================================
    // 6. LỊCH SỬ HỢP ĐỒNG CỦA NHÂN VIÊN
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByEmployee(Integer employeeId) {
        // Validate nhân viên tồn tại
        if (!employeeRepository.existsById(employeeId)) {
            throw new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId);
        }

        List<Contract> contracts = contractRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
        return contracts.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ========================================
    // 7. LẤY HỢP ĐỒNG ACTIVE (CHO PAYROLL)
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getActiveContract(Integer employeeId) {
        Contract contract = contractRepository.findByEmployeeIdAndStatus(employeeId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException(
                        "Nhân viên ID " + employeeId + " chưa có hợp đồng đang hiệu lực (ACTIVE)"));
        return mapToResponse(contract);
    }

    // ========================================
    // 8. CẢNH BÁO HỢP ĐỒNG SẮP HẾT HẠN
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getExpiringContracts(int days) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        List<Contract> contracts = contractRepository.findByStatusAndEndDateBetween("ACTIVE", today, deadline);
        return contracts.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ========================================
    // HELPER: Validate ngày tháng hợp đồng
    // ========================================

    private void validateDates(ContractRequest request) {
        // Nếu là hợp đồng INDEFINITE thì không cần endDate
        if ("INDEFINITE".equals(request.getContractType())) {
            return;
        }

        // Các loại hợp đồng có thời hạn bắt buộc phải có endDate
        if (request.getEndDate() == null) {
            throw new IllegalArgumentException(
                    "Hợp đồng loại " + request.getContractType() + " bắt buộc phải có ngày kết thúc (endDate)");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    // ========================================
    // HELPER: Entity → Response DTO
    // ========================================

    private ContractResponse mapToResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .employeeId(contract.getEmployee().getId())
                .employeeCode(contract.getEmployee().getCode())
                .employeeName(contract.getEmployee().getName())
                .contractType(contract.getContractType())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .basicSalary(contract.getBasicSalary())
                .status(contract.getStatus())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }
}
