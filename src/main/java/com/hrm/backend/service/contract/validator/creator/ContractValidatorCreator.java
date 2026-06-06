package com.hrm.backend.service.contract.validator.creator;

import com.hrm.backend.service.contract.validator.ContractValidator;

public interface ContractValidatorCreator {
    // Factory Method
    ContractValidator createValidator();
    
    // Support type mapping
    String getSupportedType();
}
