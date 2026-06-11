package com.hrm.backend.service.contract.calculator.impl;

import com.hrm.backend.service.contract.calculator.ContractSalaryCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ProbationSalaryCalculator implements ContractSalaryCalculator {

    private static final BigDecimal PROBATION_RATE = new BigDecimal("0.85");

    @Override
    public BigDecimal calculateEffectiveSalary(BigDecimal basicSalary) {
        if (basicSalary == null) {
            return BigDecimal.ZERO;
        }
        return basicSalary.multiply(PROBATION_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getSalaryCoefficient() {
        return PROBATION_RATE;
    }

    @Override
    public String getDescription() {
        return "Lương thử việc nhận 85% lương cơ bản chính thức";
    }
}
