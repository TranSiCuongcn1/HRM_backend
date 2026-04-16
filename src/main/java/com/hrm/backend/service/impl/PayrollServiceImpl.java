package com.hrm.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.backend.dto.*;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.Payroll;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.repository.PayrollRepository;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.service.AttendanceService;
import com.hrm.backend.service.ContractService;
import com.hrm.backend.service.LeaveRequestService;
import com.hrm.backend.service.PayrollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ContractService contractService;
    private final AttendanceService attendanceService;
    private final LeaveRequestService leaveRequestService;
    private final ObjectMapper objectMapper;

    // Hệ số OT: 150% lương giờ bình thường
    private static final BigDecimal OT_RATE = new BigDecimal("1.5");
    // Số giờ làm chuẩn mỗi ngày
    private static final BigDecimal STANDARD_HOURS_PER_DAY = new BigDecimal("8");
    // Ngày công tiêu chuẩn mặc định
    private static final BigDecimal DEFAULT_WORK_DAYS = new BigDecimal("22.0");

    // ========================================
    // 1. TỰ ĐỘNG TẠO BẢNG LƯƠNG (GENERATE)
    // ========================================

    @Override
    @Transactional
    public List<PayrollResponse> generatePayroll(int month, int year, BigDecimal workDays,
                                                  Map<String, BigDecimal> defaultAllowances,
                                                  Map<String, BigDecimal> defaultDeductions) {

        String monthStr = String.format("%d-%02d", year, month);
        BigDecimal standardWorkDays = (workDays != null) ? workDays : DEFAULT_WORK_DAYS;

        // Tính tổng phụ cấp/khấu trừ mặc định
        BigDecimal defaultTotalAllowances = sumMap(defaultAllowances);
        BigDecimal defaultTotalDeductions = sumMap(defaultDeductions);
        String defaultAllowancesJson = toJson(defaultAllowances);
        String defaultDeductionsJson = toJson(defaultDeductions);

        // Lấy tất cả NV đang ACTIVE
        List<Employee> activeEmployees = employeeRepository.findByStatus("ACTIVE");
        List<PayrollResponse> results = new ArrayList<>();
        int created = 0;
        int skipped = 0;

        for (Employee employee : activeEmployees) {
            // Bỏ qua nếu đã có phiếu lương cho tháng này
            if (payrollRepository.existsByEmployeeIdAndMonth(employee.getId(), monthStr)) {
                skipped++;
                continue;
            }

            // a. Lấy lương cơ bản từ Contract
            ContractResponse activeContract;
            try {
                activeContract = contractService.getActiveContract(employee.getId());
            } catch (Exception e) {
                log.warn("NV {} ({}) không có hợp đồng ACTIVE, bỏ qua. Lỗi: {}",
                        employee.getCode(), employee.getName(), e.getMessage());
                skipped++;
                continue;
            }
            BigDecimal basicSalary = activeContract.getBasicSalary();

            // b. Lấy thống kê chấm công
            AttendanceResponse.MonthlyStats stats = attendanceService.getMonthlyStats(
                    employee.getId(), month, year);
            BigDecimal actualWorkDays = BigDecimal.valueOf(stats.getTotalWorkDays());
            BigDecimal totalOvertimeHours = stats.getTotalOvertimeHours() != null
                    ? stats.getTotalOvertimeHours() : BigDecimal.ZERO;

            // c. Lấy ngày nghỉ có lương
            BigDecimal paidLeaveDays = leaveRequestService.getPaidLeaveDaysInMonth(
                    employee.getId(), month, year);

            // d. Tính tổng ngày công thực tế
            BigDecimal totalPaidDays = actualWorkDays.add(paidLeaveDays);

            // e. Tính tiền OT
            // overtimePay = totalOvertimeHours × (basicSalary / workDays / 8) × 1.5
            BigDecimal hourlyRate = basicSalary
                    .divide(standardWorkDays, 4, RoundingMode.HALF_UP)
                    .divide(STANDARD_HOURS_PER_DAY, 4, RoundingMode.HALF_UP);
            BigDecimal overtimePay = totalOvertimeHours
                    .multiply(hourlyRate)
                    .multiply(OT_RATE)
                    .setScale(2, RoundingMode.HALF_UP);

            // f. Tính grossSalary
            BigDecimal dailyRate = basicSalary.divide(standardWorkDays, 4, RoundingMode.HALF_UP);
            BigDecimal grossSalary = dailyRate.multiply(totalPaidDays)
                    .add(defaultTotalAllowances)
                    .add(overtimePay)
                    .setScale(2, RoundingMode.HALF_UP);

            // g. Tính netSalary
            BigDecimal netSalary = grossSalary.subtract(defaultTotalDeductions)
                    .setScale(2, RoundingMode.HALF_UP);

            // h. Lưu phiếu lương
            Payroll payroll = Payroll.builder()
                    .employee(employee)
                    .month(monthStr)
                    .basicSalary(basicSalary)
                    .workDays(standardWorkDays)
                    .actualDays(totalPaidDays)
                    .allowances(defaultAllowancesJson)
                    .totalAllowances(defaultTotalAllowances)
                    .overtimePay(overtimePay)
                    .grossSalary(grossSalary)
                    .deductions(defaultDeductionsJson)
                    .totalDeductions(defaultTotalDeductions)
                    .netSalary(netSalary)
                    .status("DRAFT")
                    .build();

            Payroll saved = payrollRepository.save(payroll);
            results.add(mapToResponse(saved));
            created++;
        }

        log.info("Generate bảng lương tháng {}: Tạo {} phiếu, bỏ qua {} NV",
                monthStr, created, skipped);

        return results;
    }

    // ========================================
    // 2. KẾ TOÁN SỬA PHIẾU LƯƠNG DRAFT
    // ========================================

    @Override
    @Transactional
    public PayrollResponse updatePayroll(Integer payrollId, PayrollUpdateRequest request) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu lương ID: " + payrollId));

        if (!"DRAFT".equals(payroll.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ có thể sửa phiếu lương DRAFT. Trạng thái hiện tại: " + payroll.getStatus());
        }

        // Cập nhật ngày công
        if (request.getWorkDays() != null) {
            payroll.setWorkDays(request.getWorkDays());
        }
        if (request.getActualDays() != null) {
            payroll.setActualDays(request.getActualDays());
        }

        // Cập nhật phụ cấp
        if (request.getAllowances() != null) {
            payroll.setAllowances(toJson(request.getAllowances()));
            payroll.setTotalAllowances(sumMap(request.getAllowances()));
        }

        // Cập nhật khấu trừ
        if (request.getDeductions() != null) {
            payroll.setDeductions(toJson(request.getDeductions()));
            payroll.setTotalDeductions(sumMap(request.getDeductions()));
        }

        // Override OT
        if (request.getOvertimePay() != null) {
            payroll.setOvertimePay(request.getOvertimePay());
        }

        // Tính lại grossSalary và netSalary
        recalculate(payroll);

        Payroll saved = payrollRepository.save(payroll);
        log.info("Kế toán sửa phiếu lương #{} - NV: {}", payrollId, payroll.getEmployee().getCode());

        return mapToResponse(saved);
    }

    // ========================================
    // 3. BULK UPDATE (CẬP NHẬT HÀNG LOẠT)
    // ========================================

    @Override
    @Transactional
    public List<PayrollResponse> bulkUpdatePayroll(PayrollUpdateRequest.BulkUpdateRequest request) {
        List<PayrollResponse> results = new ArrayList<>();

        for (Integer payrollId : request.getPayrollIds()) {
            Payroll payroll = payrollRepository.findById(payrollId).orElse(null);
            if (payroll == null || !"DRAFT".equals(payroll.getStatus())) {
                log.warn("Bỏ qua phiếu #{} (không tồn tại hoặc không phải DRAFT)", payrollId);
                continue;
            }

            // Merge phụ cấp mới vào phụ cấp hiện có
            if (request.getAllowances() != null && !request.getAllowances().isEmpty()) {
                Map<String, BigDecimal> current = fromJson(payroll.getAllowances());
                current.putAll(request.getAllowances()); // Merge (ghi đè nếu trùng key)
                payroll.setAllowances(toJson(current));
                payroll.setTotalAllowances(sumMap(current));
            }

            // Merge khấu trừ mới vào khấu trừ hiện có
            if (request.getDeductions() != null && !request.getDeductions().isEmpty()) {
                Map<String, BigDecimal> current = fromJson(payroll.getDeductions());
                current.putAll(request.getDeductions());
                payroll.setDeductions(toJson(current));
                payroll.setTotalDeductions(sumMap(current));
            }

            // Tính lại
            recalculate(payroll);
            Payroll saved = payrollRepository.save(payroll);
            results.add(mapToResponse(saved));
        }

        log.info("Bulk update {} phiếu lương thành công", results.size());
        return results;
    }

    // ========================================
    // 4. CHỐT LƯƠNG → CALCULATED
    // ========================================

    @Override
    @Transactional
    public PayrollResponse submitPayroll(Integer payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu lương ID: " + payrollId));

        if (!"DRAFT".equals(payroll.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ có thể chốt phiếu DRAFT. Trạng thái hiện tại: " + payroll.getStatus());
        }

        payroll.setStatus("CALCULATED");
        Payroll saved = payrollRepository.save(payroll);
        log.info("Chốt phiếu lương #{} - NV: {}, Net: {}",
                payrollId, payroll.getEmployee().getCode(), payroll.getNetSalary());

        return mapToResponse(saved);
    }

    // ========================================
    // 5. DUYỆT LƯƠNG → APPROVED
    // ========================================

    @Override
    @Transactional
    public PayrollResponse approvePayroll(String approverUsername, Integer payrollId) {
        Employee approver = getEmployeeByUsername(approverUsername);

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu lương ID: " + payrollId));

        if (!"CALCULATED".equals(payroll.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ duyệt phiếu đã chốt (CALCULATED). Trạng thái hiện tại: " + payroll.getStatus());
        }

        payroll.setStatus("APPROVED");
        payroll.setApprovedBy(approver);
        payroll.setApprovedAt(LocalDateTime.now());

        Payroll saved = payrollRepository.save(payroll);
        log.info("Giám đốc {} duyệt phiếu #{} - NV: {}, Net: {}",
                approver.getCode(), payrollId, payroll.getEmployee().getCode(), payroll.getNetSalary());

        return mapToResponse(saved);
    }

    // ========================================
    // 6. THANH TOÁN → PAID
    // ========================================

    @Override
    @Transactional
    public PayrollResponse markAsPaid(Integer payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu lương ID: " + payrollId));

        if (!"APPROVED".equals(payroll.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ thanh toán phiếu đã duyệt (APPROVED). Trạng thái hiện tại: " + payroll.getStatus());
        }

        payroll.setStatus("PAID");
        payroll.setPaidAt(LocalDateTime.now());

        Payroll saved = payrollRepository.save(payroll);
        log.info("Đã thanh toán phiếu #{} - NV: {}, Net: {}",
                payrollId, payroll.getEmployee().getCode(), payroll.getNetSalary());

        return mapToResponse(saved);
    }

    // ========================================
    // 7-9. XEM BẢNG LƯƠNG
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getPayrollsByMonth(String month) {
        return payrollRepository.findByMonthOrderByEmployeeCodeAsc(month)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getPayrollsByEmployee(Integer employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new RuntimeException("Không tìm thấy nhân viên ID: " + employeeId);
        }
        return payrollRepository.findByEmployeeIdOrderByMonthDesc(employeeId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollResponse getPayrollById(Integer id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu lương ID: " + id));
        return mapToResponse(payroll);
    }

    // ========================================
    // HELPER: Tính lại gross & net
    // ========================================

    private void recalculate(Payroll payroll) {
        BigDecimal dailyRate = payroll.getBasicSalary()
                .divide(payroll.getWorkDays(), 4, RoundingMode.HALF_UP);

        BigDecimal grossSalary = dailyRate.multiply(payroll.getActualDays())
                .add(payroll.getTotalAllowances())
                .add(payroll.getOvertimePay())
                .setScale(2, RoundingMode.HALF_UP);

        payroll.setGrossSalary(grossSalary);
        payroll.setNetSalary(grossSalary.subtract(payroll.getTotalDeductions())
                .setScale(2, RoundingMode.HALF_UP));
    }

    // ========================================
    // HELPER: Lấy Employee từ username
    // ========================================

    private Employee getEmployeeByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));
        if (user.getEmployee() == null) {
            throw new RuntimeException("Tài khoản " + username + " chưa liên kết với nhân viên");
        }
        return user.getEmployee();
    }

    // ========================================
    // HELPER: JSON ↔ Map conversion
    // ========================================

    private String toJson(Map<String, BigDecimal> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi chuyển đổi JSON: " + e.getMessage(), e);
        }
    }

    private Map<String, BigDecimal> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, BigDecimal>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi đọc JSON: " + e.getMessage(), e);
        }
    }

    private BigDecimal sumMap(Map<String, BigDecimal> map) {
        if (map == null || map.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return map.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ========================================
    // HELPER: Entity → Response DTO
    // ========================================

    private PayrollResponse mapToResponse(Payroll payroll) {
        String deptName = null;
        if (payroll.getEmployee().getDepartment() != null) {
            deptName = payroll.getEmployee().getDepartment().getName();
        }

        return PayrollResponse.builder()
                .id(payroll.getId())
                .employeeId(payroll.getEmployee().getId())
                .employeeCode(payroll.getEmployee().getCode())
                .employeeName(payroll.getEmployee().getName())
                .departmentName(deptName)
                .month(payroll.getMonth())
                .basicSalary(payroll.getBasicSalary())
                .workDays(payroll.getWorkDays())
                .actualDays(payroll.getActualDays())
                .allowances(fromJson(payroll.getAllowances()))
                .totalAllowances(payroll.getTotalAllowances())
                .overtimePay(payroll.getOvertimePay())
                .grossSalary(payroll.getGrossSalary())
                .deductions(fromJson(payroll.getDeductions()))
                .totalDeductions(payroll.getTotalDeductions())
                .netSalary(payroll.getNetSalary())
                .status(payroll.getStatus())
                .approvedByName(payroll.getApprovedBy() != null ? payroll.getApprovedBy().getName() : null)
                .approvedAt(payroll.getApprovedAt())
                .paidAt(payroll.getPaidAt())
                .createdAt(payroll.getCreatedAt())
                .updatedAt(payroll.getUpdatedAt())
                .build();
    }
}
