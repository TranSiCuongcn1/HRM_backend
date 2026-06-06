package com.hrm.backend.service.contract.validator;

import com.hrm.backend.dto.ContractRequest;

public interface ContractValidator {
    void validate(ContractRequest request, Integer contractId);
}
