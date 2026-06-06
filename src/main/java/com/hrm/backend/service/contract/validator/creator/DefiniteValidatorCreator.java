package com.hrm.backend.service.contract.validator.creator;

import com.hrm.backend.service.contract.validator.ContractValidator;
import com.hrm.backend.service.contract.validator.impl.DefiniteContractValidator;
import org.springframework.stereotype.Component;

@Component
public class DefiniteValidatorCreator implements ContractValidatorCreator {

    private final DefiniteContractValidator definiteContractValidator;

    public DefiniteValidatorCreator(DefiniteContractValidator definiteContractValidator) {
        this.definiteContractValidator = definiteContractValidator;
    }

    @Override
    public ContractValidator createValidator() {
        return definiteContractValidator;
    }

    @Override
    public String getSupportedType() {
        return "DEFINITE_1YR";
    }
}
