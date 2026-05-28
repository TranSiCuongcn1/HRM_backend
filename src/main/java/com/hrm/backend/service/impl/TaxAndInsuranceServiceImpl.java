package com.hrm.backend.service.impl;

import com.hrm.backend.service.TaxAndInsuranceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TaxAndInsuranceServiceImpl implements TaxAndInsuranceService {

    // BHXH 8%, BHYT 1.5% => 9.5%
    private static final BigDecimal BHXH_BHYT_RATE = new BigDecimal("0.095");

    // BHTN 1%
    private static final BigDecimal BHTN_RATE = new BigDecimal("0.01");

    // Lương cơ sở (áp dụng từ 01/07/2024: 2.340.000 VNĐ)
    private static final BigDecimal BASE_SALARY = new BigDecimal("2340000");

    // Trần đóng BHXH, BHYT (20 lần lương cơ sở = 46.800.000 VNĐ)
    private static final BigDecimal MAX_INSURANCE_SALARY = BASE_SALARY.multiply(new BigDecimal("20"));

    // Lương tối thiểu vùng I (áp dụng từ 01/07/2024: 4.960.000 VNĐ)
    private static final BigDecimal REGIONAL_MIN_WAGE = new BigDecimal("4960000");

    // Trần đóng BHTN (20 lần lương tối thiểu vùng I = 99.200.000 VNĐ)
    private static final BigDecimal MAX_BHTN_SALARY = REGIONAL_MIN_WAGE.multiply(new BigDecimal("20"));

    // Giảm trừ gia cảnh bản thân (11 triệu)
    private static final BigDecimal PERSONAL_DEDUCTION = new BigDecimal("11000000");

    // Giảm trừ người phụ thuộc (4.4 triệu/người)
    private static final BigDecimal DEPENDENT_DEDUCTION = new BigDecimal("4400000");

    @Override
    public BigDecimal calculateInsurance(BigDecimal basicSalary) {
        if (basicSalary == null || basicSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 1. BHXH + BHYT (Trần 20 lần Lương cơ sở = 46.800.000 VNĐ)
        BigDecimal cappedSocialSalary = basicSalary.compareTo(MAX_INSURANCE_SALARY) > 0 
                ? MAX_INSURANCE_SALARY 
                : basicSalary;
        BigDecimal socialInsurance = cappedSocialSalary.multiply(BHXH_BHYT_RATE);

        // 2. BHTN (Trần 20 lần Lương tối thiểu vùng I = 99.200.000 VNĐ)
        BigDecimal cappedUnemploymentSalary = basicSalary.compareTo(MAX_BHTN_SALARY) > 0 
                ? MAX_BHTN_SALARY 
                : basicSalary;
        BigDecimal unemploymentInsurance = cappedUnemploymentSalary.multiply(BHTN_RATE);

        return socialInsurance.add(unemploymentInsurance).setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculatePIT(BigDecimal grossSalary, BigDecimal insuranceAmount, int dependentCount) {
        if (grossSalary == null || grossSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Tổng giảm trừ
        BigDecimal totalDeduction = PERSONAL_DEDUCTION
                .add(DEPENDENT_DEDUCTION.multiply(BigDecimal.valueOf(dependentCount)))
                .add(insuranceAmount != null ? insuranceAmount : BigDecimal.ZERO);

        // Thu nhập tính thuế (TNTT)
        BigDecimal taxableIncome = grossSalary.subtract(totalDeduction);

        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return calculateProgressiveTax(taxableIncome).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateProgressiveTax(BigDecimal taxableIncome) {
        double income = taxableIncome.doubleValue();
        double tax = 0;

        if (income <= 5_000_000) {
            tax = income * 0.05;
        } else if (income <= 10_000_000) {
            tax = income * 0.10 - 250_000;
        } else if (income <= 18_000_000) {
            tax = income * 0.15 - 750_000;
        } else if (income <= 32_000_000) {
            tax = income * 0.20 - 1_650_000;
        } else if (income <= 52_000_000) {
            tax = income * 0.25 - 3_250_000;
        } else if (income <= 80_000_000) {
            tax = income * 0.30 - 5_850_000;
        } else {
            tax = income * 0.35 - 9_850_000;
        }

        return BigDecimal.valueOf(tax);
    }
}
