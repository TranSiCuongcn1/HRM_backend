package com.hrm.backend.service;

import com.hrm.backend.dto.ContractRequest;
import com.hrm.backend.dto.ContractResponse;

import java.util.List;

public interface ContractService {

    /**
     * Tạo hợp đồng mới (status = DRAFT)
     */
    ContractResponse createContract(ContractRequest request);

    /**
     * Cập nhật hợp đồng (chỉ cho phép sửa khi status = DRAFT)
     */
    ContractResponse updateContract(Integer id, ContractRequest request);

    /**
     * Kích hoạt hợp đồng: DRAFT → ACTIVE
     * Tự động EXPIRED hợp đồng cũ nếu nhân viên đã có hợp đồng ACTIVE
     */
    ContractResponse activateContract(Integer id);

    /**
     * Chấm dứt hợp đồng trước hạn: ACTIVE → TERMINATED
     */
    ContractResponse terminateContract(Integer id);

    /**
     * Xem chi tiết 1 hợp đồng
     */
    ContractResponse getContractById(Integer id);

    /**
     * Lấy lịch sử hợp đồng của nhân viên (sắp xếp mới nhất trước)
     */
    List<ContractResponse> getContractsByEmployee(Integer employeeId);

    /**
     * Lấy hợp đồng ACTIVE hiện tại của nhân viên.
     * Module Payroll sẽ gọi method này để lấy basic_salary.
     */
    ContractResponse getActiveContract(Integer employeeId);

    /**
     * Danh sách hợp đồng sắp hết hạn trong N ngày tới (cảnh báo cho HR)
     */
    List<ContractResponse> getExpiringContracts(int days);
}
