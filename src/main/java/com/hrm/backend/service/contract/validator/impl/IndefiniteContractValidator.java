package com.hrm.backend.service.contract.validator.impl;

import com.hrm.backend.dto.ContractRequest;
import com.hrm.backend.repository.ContractRepository;
import com.hrm.backend.service.contract.validator.ContractValidator;
import org.springframework.stereotype.Component;

@Component
public class IndefiniteContractValidator extends BaseContractValidator implements ContractValidator {

    public IndefiniteContractValidator(ContractRepository contractRepository) {
        super(contractRepository);
    }

    @Override
    public void validate(ContractRequest request, Integer contractId) {
        // Hợp đồng không xác định thời hạn (INDEFINITE) không yêu cầu endDate
        checkOverlapping(request.employeeId(), request.startDate(), null, contractId);
    }
}
