package com.hrm.backend.service.contract.factory;

import com.hrm.backend.service.contract.ContractProcessingFactory;
import com.hrm.backend.service.contract.calculator.ContractSalaryCalculator;
import com.hrm.backend.service.contract.calculator.impl.DefiniteSalaryCalculator;
import com.hrm.backend.service.contract.document.ContractDocumentGenerator;
import com.hrm.backend.service.contract.document.impl.DefiniteDocumentGenerator;
import com.hrm.backend.service.contract.validator.ContractValidator;
import com.hrm.backend.service.contract.validator.impl.DefiniteContractValidator;
import org.springframework.stereotype.Component;

@Component
public class DefiniteProcessingFactory implements ContractProcessingFactory {

    private final DefiniteContractValidator validator;
    private final DefiniteSalaryCalculator salaryCalculator;
    private final DefiniteDocumentGenerator documentGenerator;

    public DefiniteProcessingFactory(
            DefiniteContractValidator validator,
            DefiniteSalaryCalculator salaryCalculator,
            DefiniteDocumentGenerator documentGenerator) {
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
        return "DEFINITE_1YR";
    }
}
