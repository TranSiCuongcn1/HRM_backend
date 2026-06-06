package com.hrm.backend.service.impl;

import com.hrm.backend.dto.ShiftDto;
import com.hrm.backend.entity.Shift;
import com.hrm.backend.repository.ShiftRepository;
import com.hrm.backend.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final com.hrm.backend.config.prototype.ShiftPrototypeRegistry shiftPrototypeRegistry;

    @Override
    @Transactional(readOnly = true)
    public List<ShiftDto> getAllShifts() {
        return shiftPrototypeRegistry.getAllCloned().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftDto getShiftById(Integer id) {
        Shift shift = shiftPrototypeRegistry.getByIdCloned(id).orElseThrow(() -> new RuntimeException("Shift not found"));
        return mapToDto(shift);
    }

    @Override
    @Transactional
    public ShiftDto createShift(ShiftDto dto) {
        if (shiftRepository.existsByCode(dto.code())) {
            throw new RuntimeException("Code already exists");
        }
        
        if (Boolean.TRUE.equals(dto.isDefault())) {
            shiftRepository.findByIsDefaultTrue().ifPresent(s -> {
                s.setIsDefault(false);
                shiftRepository.save(s);
            });
        }
        
        Shift shift = Shift.builder()
                .code(dto.code())
                .name(dto.name())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .breakStartTime(dto.breakStartTime())
                .breakEndTime(dto.breakEndTime())
                .isDefault(dto.isDefault() != null ? dto.isDefault() : false)
                .isActive(dto.isActive() != null ? dto.isActive() : true)
                .build();
        Shift saved = shiftRepository.save(shift);
        shiftPrototypeRegistry.refreshCache(); // Làm mới registry sau khi thêm
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ShiftDto updateShift(Integer id, ShiftDto dto) {
        Shift shift = shiftRepository.findById(id).orElseThrow(() -> new RuntimeException("Shift not found"));
        
        if (Boolean.TRUE.equals(dto.isDefault()) && !Boolean.TRUE.equals(shift.getIsDefault())) {
            shiftRepository.findByIsDefaultTrue().ifPresent(s -> {
                s.setIsDefault(false);
                shiftRepository.save(s);
            });
        }
        
        shift.setCode(dto.code());
        shift.setName(dto.name());
        shift.setStartTime(dto.startTime());
        shift.setEndTime(dto.endTime());
        shift.setBreakStartTime(dto.breakStartTime());
        shift.setBreakEndTime(dto.breakEndTime());
        shift.setIsDefault(dto.isDefault() != null ? dto.isDefault() : false);
        shift.setIsActive(dto.isActive() != null ? dto.isActive() : true);
        
        Shift saved = shiftRepository.save(shift);
        shiftPrototypeRegistry.refreshCache(); // Làm mới registry sau khi cập nhật
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public void deleteShift(Integer id) {
        shiftRepository.deleteById(id);
        shiftPrototypeRegistry.refreshCache(); // Làm mới registry sau khi xóa
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftDto getDefaultShift() {
        return shiftPrototypeRegistry.getDefaultShiftCloned()
                .map(this::mapToDto)
                .orElse(null);
    }

    private ShiftDto mapToDto(Shift shift) {
        return ShiftDto.builder()
                .id(shift.getId())
                .code(shift.getCode())
                .name(shift.getName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .breakStartTime(shift.getBreakStartTime())
                .breakEndTime(shift.getBreakEndTime())
                .isDefault(shift.getIsDefault())
                .isActive(shift.getIsActive())
                .build();
    }
}
