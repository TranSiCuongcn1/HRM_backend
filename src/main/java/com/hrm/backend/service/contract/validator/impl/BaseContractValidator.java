package com.hrm.backend.service.contract.validator.impl;

import com.hrm.backend.entity.Contract;
import com.hrm.backend.repository.ContractRepository;
import java.time.LocalDate;
import java.util.List;

public abstract class BaseContractValidator {
    protected final ContractRepository contractRepository;

    protected BaseContractValidator(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    protected void checkOverlapping(Integer employeeId, LocalDate newStart, LocalDate newEnd, Integer contractId) {
        List<Contract> contracts = contractRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
        for (Contract existing : contracts) {
            // Bỏ qua chính nó (khi sửa DRAFT)
            if (contractId != null && existing.getId().equals(contractId)) {
                continue;
            }
            // Bỏ qua hợp đồng DRAFT (chưa hiệu lực) và TERMINATED
            if ("DRAFT".equals(existing.getStatus()) || "TERMINATED".equals(existing.getStatus())) {
                continue;
            }

            LocalDate existStart = existing.getStartDate();
            LocalDate existEnd = existing.getEndDate();

            // Kiểm tra giao nhau: (StartA <= EndB) và (StartB <= EndA)
            boolean overlap = (existEnd == null || !newStart.isAfter(existEnd)) &&
                              (newEnd == null || !existStart.isAfter(newEnd));

            if (overlap) {
                throw new IllegalArgumentException(
                        String.format("Thời gian hợp đồng bị trùng lặp với hợp đồng số #%d (%s) từ %s đến %s",
                                existing.getId(),
                                existing.getContractType(),
                                existStart,
                                existEnd != null ? existEnd : "Vô thời hạn"));
            }
        }
    }
}
