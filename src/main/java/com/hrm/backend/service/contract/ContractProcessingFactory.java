package com.hrm.backend.service.contract;

import com.hrm.backend.service.contract.calculator.ContractSalaryCalculator;
import com.hrm.backend.service.contract.document.ContractDocumentGenerator;
import com.hrm.backend.service.contract.validator.ContractValidator;

public interface ContractProcessingFactory {
    ContractValidator createValidator();
    ContractSalaryCalculator createSalaryCalculator();
    ContractDocumentGenerator createDocumentGenerator();
    String getSupportedType();
}
