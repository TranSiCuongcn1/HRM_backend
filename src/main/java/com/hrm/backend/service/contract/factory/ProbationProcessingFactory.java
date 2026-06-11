package com.hrm.backend.service.contract.factory;

import com.hrm.backend.service.contract.ContractProcessingFactory;
import com.hrm.backend.service.contract.calculator.ContractSalaryCalculator;
import com.hrm.backend.service.contract.calculator.impl.ProbationSalaryCalculator;
import com.hrm.backend.service.contract.document.ContractDocumentGenerator;
import com.hrm.backend.service.contract.document.impl.ProbationDocumentGenerator;
import com.hrm.backend.service.contract.validator.ContractValidator;
import com.hrm.backend.service.contract.validator.impl.ProbationContractValidator;
import org.springframework.stereotype.Component;

@Component
public class ProbationProcessingFactory implements ContractProcessingFactory {

    private final ProbationContractValidator validator;
    private final ProbationSalaryCalculator salaryCalculator;
    private final ProbationDocumentGenerator documentGenerator;

    public ProbationProcessingFactory(
            ProbationContractValidator validator,
            ProbationSalaryCalculator salaryCalculator,
            ProbationDocumentGenerator documentGenerator) {
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
        return "PROBATION";
    }
}
