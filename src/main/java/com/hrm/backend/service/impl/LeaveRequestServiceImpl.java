package com.hrm.backend.service.impl;

import com.hrm.backend.dto.LeaveRequestDTO;
import com.hrm.backend.dto.LeaveRequestResponse;
import com.hrm.backend.entity.*;
import com.hrm.backend.repository.*;
import com.hrm.backend.service.LeaveBalanceService;
import com.hrm.backend.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final LeaveBalanceService leaveBalanceService;

    // ========================================
    // 1. NV GỬI ĐƠN XIN PHÉP
    // ========================================

    @Override
    @Transactional
    public LeaveRequestResponse submitRequest(String username, LeaveRequestDTO request) {
        Employee employee = getEmployeeByUsername(username);

        // Validate loại phép
        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy loại phép ID: " + request.getLeaveTypeId()));

        // Validate ngày tháng
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }

        // Validate số dư phép (chỉ check cho phép có giới hạn, UNPAID không cần check)
        if (!"UNPAID".equals(leaveType.getCode())) {
            int year = request.getStartDate().getYear();
            LeaveBalance balance = leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeIdAndYear(employee.getId(), leaveType.getId(), year)
                    .orElseThrow(() -> new RuntimeException(
                            "Nhân viên chưa được cấp phép loại '" + leaveType.getName() + "' cho năm " + year
                                    + ". Vui lòng liên hệ Admin."));

            BigDecimal remaining = balance.getTotalDays()
                    .add(balance.getCarryOverDays())
                    .subtract(balance.getUsedDays());

            if (remaining.compareTo(request.getDays()) < 0) {
                throw new IllegalArgumentException(
                        "Không đủ số dư phép '" + leaveType.getName() + "'. "
                                + "Còn lại: " + remaining + " ngày, yêu cầu: " + request.getDays() + " ngày");
            }
        }

        // Tạo đơn PENDING
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .days(request.getDays())
                .reason(request.getReason())
                .attachmentUrl(request.getAttachmentUrl())
                .status("PENDING")
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("NV {} gửi đơn xin {} {} ngày ({} → {})",
                employee.getCode(), leaveType.getName(), request.getDays(),
                request.getStartDate(), request.getEndDate());

        return mapToResponse(saved);
    }

    // ========================================
    // 2. NV HỦY ĐƠN (CHỈ KHI PENDING)
    // ========================================

    @Override
    @Transactional
    public LeaveRequestResponse cancelRequest(String username, Integer requestId) {
        Employee employee = getEmployeeByUsername(username);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn xin phép ID: " + requestId));

        // Chỉ NV chính chủ mới được hủy
        if (!leaveRequest.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn xin phép này");
        }

        // Chỉ hủy được khi PENDING
        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ có thể hủy đơn đang ở trạng thái PENDING. Trạng thái hiện tại: " + leaveRequest.getStatus());
        }

        leaveRequest.setStatus("CANCELLED");
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("NV {} đã hủy đơn xin phép #{}", employee.getCode(), requestId);

        return mapToResponse(saved);
    }

    // ========================================
    // 3. ADMIN DUYỆT ĐƠN
    // ========================================

    @Override
    @Transactional
    public LeaveRequestResponse approveRequest(String adminUsername, Integer requestId) {
        Employee admin = getEmployeeByUsername(adminUsername);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn xin phép ID: " + requestId));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ có thể duyệt đơn đang PENDING. Trạng thái hiện tại: " + leaveRequest.getStatus());
        }

        // Chuyển status → APPROVED
        leaveRequest.setStatus("APPROVED");
        leaveRequest.setApprovedBy(admin);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        // Tự động trừ số dư phép
        int year = leaveRequest.getStartDate().getYear();
        leaveBalanceService.deductBalance(
                leaveRequest.getEmployee().getId(),
                leaveRequest.getLeaveType().getId(),
                year,
                leaveRequest.getDays()
        );

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Admin {} duyệt đơn xin phép #{} của NV {} - {} {} ngày",
                admin.getCode(), requestId, leaveRequest.getEmployee().getCode(),
                leaveRequest.getLeaveType().getName(), leaveRequest.getDays());

        return mapToResponse(saved);
    }

    // ========================================
    // 4. ADMIN TỪ CHỐI ĐƠN
    // ========================================

    @Override
    @Transactional
    public LeaveRequestResponse rejectRequest(String adminUsername, Integer requestId, String reason) {
        Employee admin = getEmployeeByUsername(adminUsername);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn xin phép ID: " + requestId));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ có thể từ chối đơn đang PENDING. Trạng thái hiện tại: " + leaveRequest.getStatus());
        }

        leaveRequest.setStatus("REJECTED");
        leaveRequest.setApprovedBy(admin);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setRejectionReason(reason);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Admin {} từ chối đơn #{} của NV {} - Lý do: {}",
                admin.getCode(), requestId, leaveRequest.getEmployee().getCode(), reason);

        return mapToResponse(saved);
    }

    // ========================================
    // 5. NV XEM DANH SÁCH ĐƠN CỦA MÌNH
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getMyRequests(String username) {
        Employee employee = getEmployeeByUsername(username);
        List<LeaveRequest> requests = leaveRequestRepository
                .findByEmployeeIdOrderByCreatedAtDesc(employee.getId());
        return requests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ========================================
    // 6. ADMIN XEM ĐƠN CHỜ DUYỆT
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getPendingRequests() {
        List<LeaveRequest> requests = leaveRequestRepository
                .findByStatusOrderByCreatedAtAsc("PENDING");
        return requests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ========================================
    // 7. ADMIN XEM TẤT CẢ ĐƠN (CÓ LỌC)
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getAllRequests(String status) {
        List<LeaveRequest> requests;
        if (status != null && !status.isBlank()) {
            requests = leaveRequestRepository.findByStatusOrderByCreatedAtAsc(status);
        } else {
            requests = leaveRequestRepository.findAllByOrderByCreatedAtDesc();
        }
        return requests.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ========================================
    // 8. PAYROLL: TỔNG NGÀY NGHỈ CÓ LƯƠNG TRONG THÁNG
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPaidLeaveDaysInMonth(Integer employeeId, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        return leaveRequestRepository.sumApprovedPaidLeaveDays(employeeId, startOfMonth, endOfMonth);
    }

    // ========================================
    // HELPER: Lấy Employee từ JWT username
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
    // HELPER: Entity → Response DTO
    // ========================================

    private LeaveRequestResponse mapToResponse(LeaveRequest request) {
        return LeaveRequestResponse.builder()
                .id(request.getId())
                .employeeId(request.getEmployee().getId())
                .employeeCode(request.getEmployee().getCode())
                .employeeName(request.getEmployee().getName())
                .leaveTypeId(request.getLeaveType().getId())
                .leaveTypeCode(request.getLeaveType().getCode())
                .leaveTypeName(request.getLeaveType().getName())
                .isPaidLeave(request.getLeaveType().getIsPaid())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .days(request.getDays())
                .reason(request.getReason())
                .attachmentUrl(request.getAttachmentUrl())
                .status(request.getStatus())
                .approvedByName(request.getApprovedBy() != null ? request.getApprovedBy().getName() : null)
                .approvedAt(request.getApprovedAt())
                .rejectionReason(request.getRejectionReason())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
