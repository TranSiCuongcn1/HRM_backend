package com.hrm.backend.service.contract.validator.creator;

import com.hrm.backend.service.contract.validator.ContractValidator;
import com.hrm.backend.service.contract.validator.impl.ProbationContractValidator;
import org.springframework.stereotype.Component;

@Component
public class ProbationValidatorCreator implements ContractValidatorCreator {

    private final ProbationContractValidator probationContractValidator;

    public ProbationValidatorCreator(ProbationContractValidator probationContractValidator) {
        this.probationContractValidator = probationContractValidator;
    }

    @Override
    public ContractValidator createValidator() {
        return probationContractValidator;
    }

    @Override
    public String getSupportedType() {
        return "PROBATION";
    }
}
