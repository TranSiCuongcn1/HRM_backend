package com.hrm.backend.service.impl;

import com.hrm.backend.dto.LeaveTypeRequest;
import com.hrm.backend.entity.LeaveType;
import com.hrm.backend.repository.LeaveTypeRepository;
import com.hrm.backend.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    @Override
    @Transactional
    public LeaveType createLeaveType(LeaveTypeRequest request) {
        if (leaveTypeRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã loại phép '" + request.getCode() + "' đã tồn tại");
        }

        LeaveType leaveType = LeaveType.builder()
                .code(request.getCode())
                .name(request.getName())
                .isPaid(request.getIsPaid() != null ? request.getIsPaid() : true)
                .description(request.getDescription())
                .build();

        LeaveType saved = leaveTypeRepository.save(leaveType);
        log.info("Đã tạo loại phép: {} - {} (Có lương: {})", saved.getCode(), saved.getName(), saved.getIsPaid());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    @Override
    @Transactional
    public LeaveType updateLeaveType(Integer id, LeaveTypeRequest request) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại phép với ID: " + id));

        leaveType.setCode(request.getCode());
        leaveType.setName(request.getName());
        if (request.getIsPaid() != null) {
            leaveType.setIsPaid(request.getIsPaid());
        }
        leaveType.setDescription(request.getDescription());

        LeaveType updated = leaveTypeRepository.save(leaveType);
        log.info("Đã cập nhật loại phép: {} - {}", updated.getCode(), updated.getName());
        return updated;
    }
}
