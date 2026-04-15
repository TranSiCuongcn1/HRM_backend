package com.hrm.backend.service.impl;

import com.hrm.backend.dto.LeaveBalanceResponse;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.LeaveBalance;
import com.hrm.backend.entity.LeaveType;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.repository.LeaveBalanceRepository;
import com.hrm.backend.repository.LeaveTypeRepository;
import com.hrm.backend.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    // Số ngày phép năm mặc định theo Luật Lao Động VN
    private static final BigDecimal DEFAULT_ANNUAL_DAYS = new BigDecimal("12.0");
    private static final BigDecimal DEFAULT_SICK_DAYS = new BigDecimal("30.0");

    // ========================================
    // 1. CẤP PHÉP ĐẦU NĂM CHO NHÂN VIÊN
    // ========================================

    @Override
    @Transactional
    public void initBalanceForEmployee(Integer employeeId, int year) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên ID: " + employeeId));

        List<LeaveType> allTypes = leaveTypeRepository.findAll();

        for (LeaveType type : allTypes) {
            // Bỏ qua nếu đã tồn tại
            if (leaveBalanceRepository.existsByEmployeeIdAndLeaveTypeIdAndYear(employeeId, type.getId(), year)) {
                continue;
            }

            BigDecimal totalDays = BigDecimal.ZERO;
            // Cấp số ngày mặc định theo loại phép
            if ("ANNUAL".equals(type.getCode())) {
                totalDays = DEFAULT_ANNUAL_DAYS;
            } else if ("SICK".equals(type.getCode())) {
                totalDays = DEFAULT_SICK_DAYS;
            } else if ("WEDDING".equals(type.getCode())) {
                totalDays = new BigDecimal("3.0");
            } else if ("BEREAVEMENT".equals(type.getCode())) {
                totalDays = new BigDecimal("3.0");
            }
            // UNPAID và MATERNITY: totalDays = 0 (không giới hạn hoặc theo quy định riêng)

            LeaveBalance balance = LeaveBalance.builder()
                    .employee(employee)
                    .leaveType(type)
                    .year(year)
                    .totalDays(totalDays)
                    .usedDays(BigDecimal.ZERO)
                    .carryOverDays(BigDecimal.ZERO)
                    .build();

            leaveBalanceRepository.save(balance);
            log.info("Cấp phép cho NV {} - Loại: {}, Số ngày: {}, Năm: {}",
                    employee.getCode(), type.getCode(), totalDays, year);
        }
    }

    // ========================================
    // 2. XEM SỐ DƯ PHÉP CỦA NHÂN VIÊN
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getBalancesByEmployee(Integer employeeId, int year) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new RuntimeException("Không tìm thấy nhân viên ID: " + employeeId);
        }

        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year);
        return balances.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ========================================
    // 3. ADMIN CẬP NHẬT SỐ PHÉP THỦ CÔNG
    // ========================================

    @Override
    @Transactional
    public LeaveBalanceResponse updateBalance(Integer balanceId, BigDecimal totalDays, BigDecimal carryOverDays) {
        LeaveBalance balance = leaveBalanceRepository.findById(balanceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy balance ID: " + balanceId));

        if (totalDays != null) {
            balance.setTotalDays(totalDays);
        }
        if (carryOverDays != null) {
            balance.setCarryOverDays(carryOverDays);
        }

        LeaveBalance updated = leaveBalanceRepository.save(balance);
        log.info("Admin cập nhật phép: NV {} - Loại {}, totalDays={}, carryOver={}",
                balance.getEmployee().getCode(), balance.getLeaveType().getCode(),
                updated.getTotalDays(), updated.getCarryOverDays());

        return mapToResponse(updated);
    }

    // ========================================
    // 4. TRỪ PHÉP (KHI ĐƠN ĐƯỢC DUYỆT)
    // ========================================

    @Override
    @Transactional
    public void deductBalance(Integer employeeId, Integer leaveTypeId, int year, BigDecimal days) {
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseThrow(() -> new RuntimeException(
                        "Nhân viên chưa được cấp phép loại này cho năm " + year));

        balance.setUsedDays(balance.getUsedDays().add(days));
        leaveBalanceRepository.save(balance);
        log.info("Trừ {} ngày phép cho NV ID {} - Loại phép ID {}", days, employeeId, leaveTypeId);
    }

    // ========================================
    // 5. HOÀN PHÉP (NẾU CẦN)
    // ========================================

    @Override
    @Transactional
    public void refundBalance(Integer employeeId, Integer leaveTypeId, int year, BigDecimal days) {
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseThrow(() -> new RuntimeException(
                        "Nhân viên chưa được cấp phép loại này cho năm " + year));

        BigDecimal newUsed = balance.getUsedDays().subtract(days);
        balance.setUsedDays(newUsed.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newUsed);
        leaveBalanceRepository.save(balance);
        log.info("Hoàn {} ngày phép cho NV ID {} - Loại phép ID {}", days, employeeId, leaveTypeId);
    }

    // ========================================
    // HELPER: Entity → Response DTO
    // ========================================

    private LeaveBalanceResponse mapToResponse(LeaveBalance balance) {
        BigDecimal remaining = balance.getTotalDays()
                .add(balance.getCarryOverDays())
                .subtract(balance.getUsedDays());

        return LeaveBalanceResponse.builder()
                .id(balance.getId())
                .employeeId(balance.getEmployee().getId())
                .employeeCode(balance.getEmployee().getCode())
                .employeeName(balance.getEmployee().getName())
                .leaveTypeId(balance.getLeaveType().getId())
                .leaveTypeCode(balance.getLeaveType().getCode())
                .leaveTypeName(balance.getLeaveType().getName())
                .year(balance.getYear())
                .totalDays(balance.getTotalDays())
                .usedDays(balance.getUsedDays())
                .carryOverDays(balance.getCarryOverDays())
                .remainingDays(remaining)
                .build();
    }
}
