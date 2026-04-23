package com.hrm.backend.service.impl;

import com.hrm.backend.dto.HolidayDto;
import com.hrm.backend.entity.Holiday;
import com.hrm.backend.repository.HolidayRepository;
import com.hrm.backend.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HolidayDto> getAllHolidays() {
        return holidayRepository.findAllByOrderByDateDesc().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HolidayDto createHoliday(HolidayDto dto) {
        if (holidayRepository.existsByDate(dto.getDate())) {
            throw new RuntimeException("Date already exists");
        }
        Holiday holiday = Holiday.builder()
                .name(dto.getName())
                .date(dto.getDate())
                .isPaid(dto.getIsPaid() != null ? dto.getIsPaid() : true)
                .build();
        return mapToDto(holidayRepository.save(holiday));
    }

    @Override
    @Transactional
    public HolidayDto updateHoliday(Integer id, HolidayDto dto) {
        Holiday holiday = holidayRepository.findById(id).orElseThrow(() -> new RuntimeException("Holiday not found"));
        holiday.setName(dto.getName());
        holiday.setDate(dto.getDate());
        holiday.setIsPaid(dto.getIsPaid() != null ? dto.getIsPaid() : true);
        return mapToDto(holidayRepository.save(holiday));
    }

    @Override
    @Transactional
    public void deleteHoliday(Integer id) {
        holidayRepository.deleteById(id);
    }

    private HolidayDto mapToDto(Holiday holiday) {
        HolidayDto dto = new HolidayDto();
        dto.setId(holiday.getId());
        dto.setName(holiday.getName());
        dto.setDate(holiday.getDate());
        dto.setIsPaid(holiday.getIsPaid());
        return dto;
    }
}
