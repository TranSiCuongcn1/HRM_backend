package com.hrm.backend.controller;

import com.hrm.backend.dto.ShiftDto;
import com.hrm.backend.service.ShiftService;
import lombok.RequiredArgsConstructor;
import com.hrm.backend.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShiftDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ca làm việc", shiftService.getAllShifts()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShiftDto>> create(@RequestBody ShiftDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Thêm mới ca làm việc thành công", shiftService.createShift(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShiftDto>> update(@PathVariable Integer id, @RequestBody ShiftDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ca làm việc thành công", shiftService.updateShift(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        shiftService.deleteShift(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa ca làm việc thành công", null));
    }
}
