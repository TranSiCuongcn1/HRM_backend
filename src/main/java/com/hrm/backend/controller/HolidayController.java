package com.hrm.backend.controller;

import com.hrm.backend.dto.HolidayDto;
import com.hrm.backend.service.HolidayService;
import lombok.RequiredArgsConstructor;
import com.hrm.backend.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HolidayDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ngày lễ", holidayService.getAllHolidays()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HolidayDto>> create(@RequestBody HolidayDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Thêm mới ngày lễ thành công", holidayService.createHoliday(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HolidayDto>> update(@PathVariable Integer id, @RequestBody HolidayDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ngày lễ thành công", holidayService.updateHoliday(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa ngày lễ thành công", null));
    }
}
