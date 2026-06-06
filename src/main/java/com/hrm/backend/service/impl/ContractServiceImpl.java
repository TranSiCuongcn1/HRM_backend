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
import java.util.Optional;
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
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy nhân viên với ID: " + request.employeeId()));

        // Không cho tạo hợp đồng cho nhân viên đã nghỉ việc
        if ("RESIGNED".equals(employee.getStatus())) {
            throw new IllegalArgumentException(
                    "Không thể tạo hợp đồng cho nhân viên đã nghỉ việc: " + employee.getCode());
        }

        // Validate logic ngày tháng
        validateDates(request, null);

        // Tạo hợp đồng mới với trạng thái DRAFT
        Contract contract = Contract.builder()
                .employee(employee)
                .contractType(request.contractType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .basicSalary(request.basicSalary())
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
        validateDates(request, id);

        // Cập nhật thông tin
        contract.setContractType(request.contractType());
        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());
        contract.setBasicSalary(request.basicSalary());

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
                    // Cập nhật endDate của hợp đồng cũ về ngày liền trước ngày bắt đầu hợp đồng mới
                    LocalDate dayBeforeNewStart = contract.getStartDate().minusDays(1);
                    if (oldContract.getEndDate() == null || oldContract.getEndDate().isAfter(dayBeforeNewStart)) {
                        oldContract.setEndDate(dayBeforeNewStart);
                    }
                    contractRepository.save(oldContract);
                    log.info("Tự động EXPIRED và cập nhật ngày kết thúc thực tế cho hợp đồng cũ #{} của nhân viên {}",
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

    @Override
    @Transactional(readOnly = true)
    public Optional<ContractResponse> findActiveContract(Integer employeeId) {
        return contractRepository.findByEmployeeIdAndStatus(employeeId, "ACTIVE")
                .map(this::mapToResponse);
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

    private void validateDates(ContractRequest request, Integer contractId) {
        // Các loại hợp đồng có thời hạn bắt buộc phải có endDate
        if (!"INDEFINITE".equals(request.contractType())) {
            if (request.endDate() == null) {
                throw new IllegalArgumentException(
                        "Hợp đồng loại " + request.contractType() + " bắt buộc phải có ngày kết thúc (endDate)");
            }
            if (request.endDate().isBefore(request.startDate())) {
                throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
            }
        }

        // 1. Giới hạn Thử việc không quá 180 ngày
        if ("PROBATION".equals(request.contractType())) {
            if (request.endDate() != null) {
                long probationDays = java.time.temporal.ChronoUnit.DAYS.between(request.startDate(), request.endDate());
                if (probationDays > 180) {
                    throw new IllegalArgumentException("Thời gian thử việc tối đa theo luật định là 180 ngày.");
                }
            }
        }

        // 2. Chặn trùng khoảng ngày (Overlapping Dates Check)
        checkOverlapping(request.employeeId(), request.startDate(), "INDEFINITE".equals(request.contractType()) ? null : request.endDate(), contractId);

        // 3. Giới hạn số lần ký hợp đồng xác định thời hạn (DEFINITE_1YR)
        if ("DEFINITE_1YR".equals(request.contractType())) {
            checkDefiniteContractLimit(request.employeeId(), contractId);
        }
    }

    private void checkDefiniteContractLimit(Integer employeeId, Integer contractId) {
        List<Contract> contracts = contractRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
        long definiteCount = contracts.stream()
                .filter(c -> !c.getId().equals(contractId)) // Bỏ qua chính nó khi sửa
                .filter(c -> !"DRAFT".equals(c.getStatus()) && !"TERMINATED".equals(c.getStatus())) // Chỉ tính các hợp đồng đã ký/kích hoạt hiệu lực
                .filter(c -> !"PROBATION".equals(c.getContractType()) && !"INDEFINITE".equals(c.getContractType()))
                .count();
        if (definiteCount >= 2) {
            throw new IllegalArgumentException(
                    "Theo Bộ luật Lao động Việt Nam, người lao động không được ký quá 2 lần hợp đồng xác định thời hạn. Vui lòng ký hợp đồng vô thời hạn (INDEFINITE).");
        }
    }

    private void checkOverlapping(Integer employeeId, LocalDate newStart, LocalDate newEnd, Integer contractId) {
        List<Contract> contracts = contractRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
        for (Contract existing : contracts) {
            // Bỏ qua chính nó (khi sửa DRAFT)
            if (contractId != null && existing.getId().equals(contractId)) {
                continue;
            }
            // Bỏ qua hợp đồng DRAFT (chưa hiệu lực) và TERMINATED
            if ("DRAFT".equals(existing.getStatus()) || "TERMINATED".equals(existing.getStatus())) {
                continue;
            }

            LocalDate existStart = existing.getStartDate();
            LocalDate existEnd = existing.getEndDate();

            // Kiểm tra giao nhau: (StartA <= EndB) và (StartB <= EndA)
            boolean overlap = (existEnd == null || !newStart.isAfter(existEnd)) &&
                              (newEnd == null || !existStart.isAfter(newEnd));

            if (overlap) {
                throw new IllegalArgumentException(
                        String.format("Thời gian hợp đồng bị trùng lặp với hợp đồng số #%d (%s) từ %s đến %s",
                                existing.getId(),
                                existing.getContractType(),
                                existStart,
                                existEnd != null ? existEnd : "Vô thời hạn"));
            }
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
