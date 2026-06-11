package com.hrm.backend.service.contract.calculator.impl;

import com.hrm.backend.service.contract.calculator.ContractSalaryCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class IndefiniteSalaryCalculator implements ContractSalaryCalculator {

    private static final BigDecimal COEFFICIENT = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);

    @Override
    public BigDecimal calculateEffectiveSalary(BigDecimal basicSalary) {
        if (basicSalary == null) {
            return BigDecimal.ZERO;
        }
        return basicSalary.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getSalaryCoefficient() {
        return COEFFICIENT;
    }

    @Override
    public String getDescription() {
        return "Lương hợp đồng không xác định thời hạn nhận 100% lương cơ bản";
    }
}
