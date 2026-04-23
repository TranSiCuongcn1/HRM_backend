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

    @Override
    @Transactional(readOnly = true)
    public List<ShiftDto> getAllShifts() {
        return shiftRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftDto getShiftById(Integer id) {
        Shift shift = shiftRepository.findById(id).orElseThrow(() -> new RuntimeException("Shift not found"));
        return mapToDto(shift);
    }

    @Override
    @Transactional
    public ShiftDto createShift(ShiftDto dto) {
        if (shiftRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Code already exists");
        }
        
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            shiftRepository.findByIsDefaultTrue().ifPresent(s -> {
                s.setIsDefault(false);
                shiftRepository.save(s);
            });
        }
        
        Shift shift = Shift.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .breakStartTime(dto.getBreakStartTime())
                .breakEndTime(dto.getBreakEndTime())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
        return mapToDto(shiftRepository.save(shift));
    }

    @Override
    @Transactional
    public ShiftDto updateShift(Integer id, ShiftDto dto) {
        Shift shift = shiftRepository.findById(id).orElseThrow(() -> new RuntimeException("Shift not found"));
        
        if (Boolean.TRUE.equals(dto.getIsDefault()) && !Boolean.TRUE.equals(shift.getIsDefault())) {
            shiftRepository.findByIsDefaultTrue().ifPresent(s -> {
                s.setIsDefault(false);
                shiftRepository.save(s);
            });
        }
        
        shift.setCode(dto.getCode());
        shift.setName(dto.getName());
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());
        shift.setBreakStartTime(dto.getBreakStartTime());
        shift.setBreakEndTime(dto.getBreakEndTime());
        shift.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        shift.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        
        return mapToDto(shiftRepository.save(shift));
    }

    @Override
    @Transactional
    public void deleteShift(Integer id) {
        shiftRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftDto getDefaultShift() {
        return shiftRepository.findByIsDefaultTrue()
                .map(this::mapToDto)
                .orElse(null);
    }

    private ShiftDto mapToDto(Shift shift) {
        ShiftDto dto = new ShiftDto();
        dto.setId(shift.getId());
        dto.setCode(shift.getCode());
        dto.setName(shift.getName());
        dto.setStartTime(shift.getStartTime());
        dto.setEndTime(shift.getEndTime());
        dto.setBreakStartTime(shift.getBreakStartTime());
        dto.setBreakEndTime(shift.getBreakEndTime());
        dto.setIsDefault(shift.getIsDefault());
        dto.setIsActive(shift.getIsActive());
        return dto;
    }
}
