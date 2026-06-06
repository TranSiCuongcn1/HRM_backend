package com.hrm.backend.service.contract.validator.impl;

import com.hrm.backend.dto.ContractRequest;
import com.hrm.backend.repository.ContractRepository;
import com.hrm.backend.service.contract.validator.ContractValidator;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class ProbationContractValidator extends BaseContractValidator implements ContractValidator {

    public ProbationContractValidator(ContractRepository contractRepository) {
        super(contractRepository);
    }

    @Override
    public void validate(ContractRequest request, Integer contractId) {
        if (request.endDate() == null) {
            throw new IllegalArgumentException(
                    "Hợp đồng loại " + request.contractType() + " bắt buộc phải có ngày kết thúc (endDate)");
        }
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        long probationDays = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        if (probationDays > 180) {
            throw new IllegalArgumentException("Thời gian thử việc tối đa theo luật định là 180 ngày.");
        }

        checkOverlapping(request.employeeId(), request.startDate(), request.endDate(), contractId);
    }
}
