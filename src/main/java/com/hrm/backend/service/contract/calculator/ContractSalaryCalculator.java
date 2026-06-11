package com.hrm.backend.service.contract.calculator;

import java.math.BigDecimal;

public interface ContractSalaryCalculator {
    BigDecimal calculateEffectiveSalary(BigDecimal basicSalary);
    BigDecimal getSalaryCoefficient();
    String getDescription();
}
