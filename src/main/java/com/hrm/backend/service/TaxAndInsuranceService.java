package com.hrm.backend.service;

import java.math.BigDecimal;

public interface TaxAndInsuranceService {
    BigDecimal calculateInsurance(BigDecimal basicSalary);
    BigDecimal calculatePIT(BigDecimal grossSalary, BigDecimal insuranceAmount, int dependentCount);
}
