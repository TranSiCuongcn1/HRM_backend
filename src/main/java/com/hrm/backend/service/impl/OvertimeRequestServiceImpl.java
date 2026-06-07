package com.hrm.backend.service.impl;

import com.hrm.backend.dto.OvertimeRequestRequest;
import com.hrm.backend.dto.OvertimeRequestResponse;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.OvertimeRequest;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.repository.OvertimeRequestRepository;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.service.OvertimeRequestService;
import com.hrm.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OvertimeRequestServiceImpl implements OvertimeRequestService {

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public OvertimeRequestResponse createRequest(String username, OvertimeRequestRequest request) {
        Employee employee = getEmployeeByUsername(username);

        // Calculate hours from startTime and endTime
        Duration duration = Duration.between(request.startTime(), request.endTime());
        BigDecimal hours = BigDecimal.valueOf(duration.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        if (hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu");
        }

        // Kiểm tra trùng lặp thời gian đăng ký tăng ca
        long overlappingCount = overtimeRequestRepository.countOverlappingRequests(
                employee.getId(), request.date(), request.startTime(), request.endTime());
        if (overlappingCount > 0) {
            throw new IllegalArgumentException("Bạn đã có đơn đăng ký tăng ca (PENDING/APPROVED) trùng khoảng thời gian này");
        }

        OvertimeRequest orq = OvertimeRequest.builder()
                .employee(employee)
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .hours(hours)
                .reason(request.reason())
                .status("PENDING")
                .build();

        OvertimeRequest saved = overtimeRequestRepository.save(orq);
        log.info("NV {} đã gửi đơn đăng ký tăng ca ngày {} ({} - {})",
                employee.getCode(), request.date(), request.startTime(), request.endTime());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OvertimeRequestResponse> getMyRequests(String username, String status, String keyword, Pageable pageable) {
        Employee employee = getEmployeeByUsername(username);
        return overtimeRequestRepository.searchByEmployee(employee.getId(), status, keyword, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OvertimeRequestResponse> getAllRequests(String status, String keyword, Pageable pageable) {
        return overtimeRequestRepository.searchAll(status, keyword, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public OvertimeRequestResponse approveRequest(Integer requestId, String adminUsername) {
        Employee admin = getEmployeeByUsername(adminUsername);
        OvertimeRequest orq = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn tăng ca ID: " + requestId));

        if (!"PENDING".equals(orq.getStatus())) {
            throw new RuntimeException("Chỉ có thể duyệt đơn ở trạng thái PENDING");
        }

        orq.setStatus("APPROVED");
        orq.setApprovedBy(admin);
        orq.setApprovedAt(LocalDateTime.now());

        OvertimeRequest saved = overtimeRequestRepository.save(orq);
        log.info("Admin {} đã duyệt đơn tăng ca #{} của NV {}", admin.getCode(), requestId, orq.getEmployee().getCode());

        try {
            emailService.sendOvertimeStatusEmail(
                    saved.getEmployee().getEmail(),
                    saved.getEmployee().getName(),
                    saved.getDate().toString(),
                    saved.getHours(),
                    saved.getStatus(),
                    null
            );
        } catch (Exception e) {
            log.error("Failed to trigger overtime status email: {}", e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public OvertimeRequestResponse rejectRequest(Integer requestId, String adminUsername, String reason) {
        Employee admin = getEmployeeByUsername(adminUsername);
        OvertimeRequest orq = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn tăng ca ID: " + requestId));

        if (!"PENDING".equals(orq.getStatus())) {
            throw new RuntimeException("Chỉ có thể từ chối đơn ở trạng thái PENDING");
        }

        orq.setStatus("REJECTED");
        orq.setApprovedBy(admin);
        orq.setApprovedAt(LocalDateTime.now());
        orq.setRejectionReason(reason);

        OvertimeRequest saved = overtimeRequestRepository.save(orq);
        log.info("Admin {} đã từ chối đơn tăng ca #{} của NV {} - Lý do: {}", 
                admin.getCode(), requestId, orq.getEmployee().getCode(), reason);

        try {
            emailService.sendOvertimeStatusEmail(
                    saved.getEmployee().getEmail(),
                    saved.getEmployee().getName(),
                    saved.getDate().toString(),
                    saved.getHours(),
                    saved.getStatus(),
                    saved.getRejectionReason()
            );
        } catch (Exception e) {
            log.error("Failed to trigger overtime status email: {}", e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void cancelRequest(Integer requestId, String username) {
        Employee employee = getEmployeeByUsername(username);
        OvertimeRequest orq = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn tăng ca ID: " + requestId));

        if (!orq.getEmployee().getId().equals(employee.getId())) {
            throw new RuntimeException("Bạn không có quyền hủy đơn của người khác");
        }

        if (!"PENDING".equals(orq.getStatus())) {
            throw new RuntimeException("Chỉ có thể hủy đơn đang ở trạng thái PENDING");
        }

        orq.setStatus("CANCELLED");
        overtimeRequestRepository.save(orq);
        log.info("NV {} đã hủy đơn tăng ca #{}", employee.getCode(), requestId);
    }

    private Employee getEmployeeByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));
        if (user.getEmployee() == null) {
            throw new RuntimeException("Tài khoản chưa liên kết với nhân viên");
        }
        return user.getEmployee();
    }

    private OvertimeRequestResponse mapToResponse(OvertimeRequest orq) {
        return OvertimeRequestResponse.builder()
                .id(orq.getId())
                .employeeId(orq.getEmployee().getId())
                .employeeCode(orq.getEmployee().getCode())
                .employeeName(orq.getEmployee().getName())
                .date(orq.getDate())
                .startTime(orq.getStartTime())
                .endTime(orq.getEndTime())
                .hours(orq.getHours())
                .reason(orq.getReason())
                .status(orq.getStatus())
                .approvedById(orq.getApprovedBy() != null ? orq.getApprovedBy().getId() : null)
                .approvedByName(orq.getApprovedBy() != null ? orq.getApprovedBy().getName() : null)
                .approvedAt(orq.getApprovedAt())
                .rejectionReason(orq.getRejectionReason())
                .createdAt(orq.getCreatedAt())
                .updatedAt(orq.getUpdatedAt())
                .build();
    }
}
