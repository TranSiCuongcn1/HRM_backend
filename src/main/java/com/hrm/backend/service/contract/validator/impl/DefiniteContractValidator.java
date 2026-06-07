package com.hrm.backend.service.contract.validator.impl;

import com.hrm.backend.dto.ContractRequest;
import com.hrm.backend.entity.Contract;
import com.hrm.backend.repository.ContractRepository;
import com.hrm.backend.service.contract.validator.ContractValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefiniteContractValidator extends BaseContractValidator implements ContractValidator {

    public DefiniteContractValidator(ContractRepository contractRepository) {
        super(contractRepository);
    }

    @Override
    public void validate(ContractRequest request, Integer contractId) {
        if (request.endDate() == null) {
            throw new IllegalArgumentException(
                    "Hợp đồng loại " + request.contractType() + " bắt buộc phải có ngày kết thúc (endDate)");
        }
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        checkDefiniteContractLimit(request.employeeId(), contractId);
        checkOverlapping(request.employeeId(), request.startDate(), request.endDate(), contractId);
    }

    private void checkDefiniteContractLimit(Integer employeeId, Integer contractId) {
        List<Contract> contracts = contractRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
        long definiteCount = contracts.stream()
                .filter(c -> !c.getId().equals(contractId)) // Bỏ qua chính nó khi sửa
                .filter(c -> !"DRAFT".equals(c.getStatus()) && !"TERMINATED".equals(c.getStatus())) // Chỉ tính các hợp đồng đã ký/kích hoạt hiệu lực
                .filter(c -> !"PROBATION".equals(c.getContractType()) && !"INDEFINITE".equals(c.getContractType()))
                .count();
        if (definiteCount >= 2) {
            throw new IllegalArgumentException(
                    "Theo Bộ luật Lao động Việt Nam, người lao động không được ký quá 2 lần hợp đồng xác định thời hạn. Vui lòng ký hợp đồng vô thời hạn (INDEFINITE).");
        }
    }
}
