package com.hrm.backend.service.contract.validator.creator;

import com.hrm.backend.service.contract.validator.ContractValidator;
import com.hrm.backend.service.contract.validator.impl.IndefiniteContractValidator;
import org.springframework.stereotype.Component;

@Component
public class IndefiniteValidatorCreator implements ContractValidatorCreator {

    private final IndefiniteContractValidator indefiniteContractValidator;

    public IndefiniteValidatorCreator(IndefiniteContractValidator indefiniteContractValidator) {
        this.indefiniteContractValidator = indefiniteContractValidator;
    }

    @Override
    public ContractValidator createValidator() {
        return indefiniteContractValidator;
    }

    @Override
    public String getSupportedType() {
        return "INDEFINITE";
    }
}
