package com.hrm.backend.service.contract.factory;

import com.hrm.backend.service.contract.ContractProcessingFactory;
import com.hrm.backend.service.contract.calculator.ContractSalaryCalculator;
import com.hrm.backend.service.contract.calculator.impl.IndefiniteSalaryCalculator;
import com.hrm.backend.service.contract.document.ContractDocumentGenerator;
import com.hrm.backend.service.contract.document.impl.IndefiniteDocumentGenerator;
import com.hrm.backend.service.contract.validator.ContractValidator;
import com.hrm.backend.service.contract.validator.impl.IndefiniteContractValidator;
import org.springframework.stereotype.Component;

@Component
public class IndefiniteProcessingFactory implements ContractProcessingFactory {

    private final IndefiniteContractValidator validator;
    private final IndefiniteSalaryCalculator salaryCalculator;
    private final IndefiniteDocumentGenerator documentGenerator;

    public IndefiniteProcessingFactory(
            IndefiniteContractValidator validator,
            IndefiniteSalaryCalculator salaryCalculator,
            IndefiniteDocumentGenerator documentGenerator) {
        this.validator = validator;
        this.salaryCalculator = salaryCalculator;
        this.documentGenerator = documentGenerator;
    }

    @Override
    public ContractValidator createValidator() {
        return validator;
    }

    @Override
    public ContractSalaryCalculator createSalaryCalculator() {
        return salaryCalculator;
    }

    @Override
    public ContractDocumentGenerator createDocumentGenerator() {
        return documentGenerator;
    }

    @Override
    public String getSupportedType() {
        return "INDEFINITE";
    }
}
