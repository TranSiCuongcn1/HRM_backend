package com.hrm.backend.service.impl;

import com.hrm.backend.dto.HolidayDto;
import com.hrm.backend.entity.Holiday;
import com.hrm.backend.repository.HolidayRepository;
import com.hrm.backend.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        if (holidayRepository.existsByDate(dto.date())) {
            throw new RuntimeException("Date already exists");
        }
        Holiday holiday = Holiday.builder()
                .name(dto.name())
                .date(dto.date())
                .isPaid(dto.isPaid() != null ? dto.isPaid() : true)
                .build();
        return mapToDto(holidayRepository.save(holiday));
    }

    @Override
    @Transactional
    public List<HolidayDto> createHolidays(List<HolidayDto> dtos) {
        List<Holiday> holidaysToSave = new ArrayList<>();
        for (HolidayDto dto : dtos) {
            if (!holidayRepository.existsByDate(dto.date())) {
                holidaysToSave.add(Holiday.builder()
                        .name(dto.name())
                        .date(dto.date())
                        .isPaid(dto.isPaid() != null ? dto.isPaid() : true)
                        .build());
            }
        }
        return holidayRepository.saveAll(holidaysToSave).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HolidayDto updateHoliday(Integer id, HolidayDto dto) {
        Holiday holiday = holidayRepository.findById(id).orElseThrow(() -> new RuntimeException("Holiday not found"));
        holiday.setName(dto.name());
        holiday.setDate(dto.date());
        holiday.setIsPaid(dto.isPaid() != null ? dto.isPaid() : true);
        return mapToDto(holidayRepository.save(holiday));
    }

    @Override
    @Transactional
    public void deleteHoliday(Integer id) {
        holidayRepository.deleteById(id);
    }

    private HolidayDto mapToDto(Holiday holiday) {
        return HolidayDto.builder()
                .id(holiday.getId())
                .name(holiday.getName())
                .date(holiday.getDate())
                .isPaid(holiday.getIsPaid())
                .build();
    }
}
