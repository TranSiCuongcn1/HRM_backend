package com.hrm.backend.controller;

import com.hrm.backend.dto.ApiResponse;
import com.hrm.backend.dto.ContractRequest;
import com.hrm.backend.dto.ContractResponse;
import com.hrm.backend.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Tag(name = "Contract Management", description = "API quản lý hợp đồng lao động")
public class ContractController {

    private final ContractService contractService;

    // ========================================
    // 1. TẠO HỢP ĐỒNG MỚI
    // ========================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo hợp đồng",
            description = "Tạo hợp đồng lao động mới (status = DRAFT). Chỉ ADMIN mới có quyền thực hiện.")
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(
            @Valid @RequestBody ContractRequest request) {

        ContractResponse created = contractService.createContract(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Tạo hợp đồng thành công (DRAFT)", created)
        );
    }

    // ========================================
    // 2. CẬP NHẬT HỢP ĐỒNG (CHỈ DRAFT)
    // ========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sửa hợp đồng",
            description = "Cập nhật thông tin hợp đồng. Chỉ cho phép sửa khi đang ở trạng thái DRAFT.")
    public ResponseEntity<ApiResponse<ContractResponse>> updateContract(
            @PathVariable Integer id,
            @Valid @RequestBody ContractRequest request) {

        ContractResponse updated = contractService.updateContract(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật hợp đồng thành công", updated)
        );
    }

    // ========================================
    // 3. KÍCH HOẠT HỢP ĐỒNG
    // ========================================

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kích hoạt hợp đồng",
            description = "Chuyển trạng thái DRAFT → ACTIVE. Nếu nhân viên đã có hợp đồng ACTIVE, "
                    + "hợp đồng cũ sẽ tự động chuyển thành EXPIRED.")
    public ResponseEntity<ApiResponse<ContractResponse>> activateContract(@PathVariable Integer id) {

        ContractResponse activated = contractService.activateContract(id);
        return ResponseEntity.ok(
                ApiResponse.success("Kích hoạt hợp đồng thành công", activated)
        );
    }

    // ========================================
    // 4. CHẤM DỨT HỢP ĐỒNG TRƯỚC HẠN
    // ========================================

    @PutMapping("/{id}/terminate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Chấm dứt hợp đồng",
            description = "Chấm dứt hợp đồng trước hạn (ACTIVE → TERMINATED).")
    public ResponseEntity<ApiResponse<ContractResponse>> terminateContract(@PathVariable Integer id) {

        ContractResponse terminated = contractService.terminateContract(id);
        return ResponseEntity.ok(
                ApiResponse.success("Đã chấm dứt hợp đồng", terminated)
        );
    }

    // ========================================
    // 5. XEM CHI TIẾT HỢP ĐỒNG
    // ========================================

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết hợp đồng",
            description = "Xem thông tin chi tiết một hợp đồng theo ID.")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractById(@PathVariable Integer id) {

        ContractResponse contract = contractService.getContractById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy thông tin hợp đồng thành công", contract)
        );
    }

    // ========================================
    // 6. LỊCH SỬ HỢP ĐỒNG CỦA NHÂN VIÊN
    // ========================================

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Lịch sử hợp đồng",
            description = "Lấy toàn bộ lịch sử hợp đồng của một nhân viên (mới nhất trước).")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getContractsByEmployee(
            @PathVariable Integer employeeId) {

        List<ContractResponse> contracts = contractService.getContractsByEmployee(employeeId);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy lịch sử hợp đồng thành công", contracts)
        );
    }

    // ========================================
    // 7. HỢP ĐỒNG ACTIVE HIỆN TẠI
    // ========================================

    @GetMapping("/employee/{employeeId}/active")
    @Operation(summary = "Hợp đồng hiện tại",
            description = "Lấy hợp đồng ACTIVE hiện tại của nhân viên (dùng để xem lương cơ bản).")
    public ResponseEntity<ApiResponse<ContractResponse>> getActiveContract(
            @PathVariable Integer employeeId) {

        ContractResponse contract = contractService.getActiveContract(employeeId);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy hợp đồng hiện tại thành công", contract)
        );
    }

    // ========================================
    // 8. CẢNH BÁO HỢP ĐỒNG SẮP HẾT HẠN
    // ========================================

    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hợp đồng sắp hết hạn",
            description = "Lấy danh sách hợp đồng ACTIVE sẽ hết hạn trong N ngày tới. Mặc định 30 ngày.")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getExpiringContracts(
            @RequestParam(defaultValue = "30") int days) {

        List<ContractResponse> contracts = contractService.getExpiringContracts(days);
        return ResponseEntity.ok(
                ApiResponse.success("Danh sách hợp đồng sắp hết hạn trong " + days + " ngày tới", contracts)
        );
    }
}
