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
    private final com.hrm.backend.config.prototype.LeaveTypePrototypeRegistry leaveTypePrototypeRegistry;

    @Override
    @Transactional
    public LeaveType createLeaveType(LeaveTypeRequest request) {
        if (leaveTypeRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Mã loại phép '" + request.code() + "' đã tồn tại");
        }

        LeaveType leaveType = LeaveType.builder()
                .code(request.code())
                .name(request.name())
                .isPaid(request.isPaid() != null ? request.isPaid() : true)
                .description(request.description())
                .build();

        LeaveType saved = leaveTypeRepository.save(leaveType);
        log.info("Đã tạo loại phép: {} - {} (Có lương: {})", saved.getCode(), saved.getName(), saved.getIsPaid());
        leaveTypePrototypeRegistry.refreshCache(); // Làm mới registry sau khi thêm
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypePrototypeRegistry.getAllCloned();
    }

    @Override
    @Transactional
    public LeaveType updateLeaveType(Integer id, LeaveTypeRequest request) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại phép với ID: " + id));

        leaveType.setCode(request.code());
        leaveType.setName(request.name());
        if (request.isPaid() != null) {
            leaveType.setIsPaid(request.isPaid());
        }
        leaveType.setDescription(request.description());

        LeaveType updated = leaveTypeRepository.save(leaveType);
        log.info("Đã cập nhật loại phép: {} - {}", updated.getCode(), updated.getName());
        leaveTypePrototypeRegistry.refreshCache(); // Làm mới registry sau khi cập nhật
        return updated;
    }
}
