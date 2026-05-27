package com.hrm.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TaxAndInsuranceServiceImplTest {

    private TaxAndInsuranceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TaxAndInsuranceServiceImpl();
    }

    @Test
    @DisplayName("Calculate Insurance - Null basic salary should return zero")
    void calculateInsurance_NullSalary_ReturnsZero() {
        BigDecimal result = service.calculateInsurance(null);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Calculate Insurance - Negative basic salary should return zero")
    void calculateInsurance_NegativeSalary_ReturnsZero() {
        BigDecimal result = service.calculateInsurance(new BigDecimal("-5000000"));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Calculate Insurance - Zero basic salary should return zero")
    void calculateInsurance_ZeroSalary_ReturnsZero() {
        BigDecimal result = service.calculateInsurance(BigDecimal.ZERO);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Calculate Insurance - Normal basic salary under cap should calculate 10.5% exactly")
    void calculateInsurance_NormalSalaryUnderCap_ReturnsCorrectAmount() {
        // 10M * 10.5% = 1,050,000
        BigDecimal basicSalary = new BigDecimal("10000000");
        BigDecimal expected = new BigDecimal("1050000");
        
        BigDecimal result = service.calculateInsurance(basicSalary);
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Calculate Insurance - Basic salary exactly at cap should calculate 10.5% of cap")
    void calculateInsurance_SalaryAtCap_ReturnsCappedAmount() {
        // Base Salary = 2.34M, Cap = 20 * 2.34M = 46.8M
        // 46.8M * 10.5% = 4,914,000
        BigDecimal basicSalary = new BigDecimal("46800000");
        BigDecimal expected = new BigDecimal("4914000");
        
        BigDecimal result = service.calculateInsurance(basicSalary);
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Calculate Insurance - Basic salary over cap should return capped amount")
    void calculateInsurance_SalaryOverCap_ReturnsCappedAmount() {
        // Salary = 60M (Over Cap of 46.8M)
        // Capped at 46.8M -> 4,914,000
        BigDecimal basicSalary = new BigDecimal("60000000");
        BigDecimal expected = new BigDecimal("4914000");
        
        BigDecimal result = service.calculateInsurance(basicSalary);
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Calculate PIT - Null gross salary should return zero")
    void calculatePIT_NullGrossSalary_ReturnsZero() {
        BigDecimal result = service.calculatePIT(null, new BigDecimal("1000000"), 0);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Calculate PIT - Negative gross salary should return zero")
    void calculatePIT_NegativeGrossSalary_ReturnsZero() {
        BigDecimal result = service.calculatePIT(new BigDecimal("-10000000"), new BigDecimal("1000000"), 0);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Calculate PIT - Gross salary under total deductions should return zero tax")
    void calculatePIT_SalaryUnderDeductions_ReturnsZero() {
        // Gross = 10M
        // Deductions: Personal = 11M, Dependents = 0, Insurance = 1.05M -> Total = 12.05M
        // Taxable Income = 10M - 12.05M <= 0 -> Tax = 0
        BigDecimal gross = new BigDecimal("10000000");
        BigDecimal insurance = new BigDecimal("1050000");
        
        BigDecimal result = service.calculatePIT(gross, insurance, 0);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Calculate PIT - Bracket 1: Taxable income <= 5M (5% tax)")
    void calculatePIT_Bracket1_CalculatesFivePercent() {
        // Gross = 20M, Insurance = 2.1M, Dependents = 1 (4.4M)
        // Total Deductions = Personal (11M) + Dependent (4.4M) + Insurance (2.1M) = 17.5M
        // Taxable Income = 20M - 17.5M = 2.5M
        // Tax = 2.5M * 5% = 125,000
        BigDecimal gross = new BigDecimal("20000000");
        BigDecimal insurance = new BigDecimal("2100000");
        BigDecimal expectedTax = new BigDecimal("125000");
        
        BigDecimal result = service.calculatePIT(gross, insurance, 1);
        assertThat(result).isEqualByComparingTo(expectedTax);
    }

    @Test
    @DisplayName("Calculate PIT - Bracket 3: Taxable income between 10M and 18M (15% tax - 750k)")
    void calculatePIT_Bracket3_CalculatesProgressiveTax() {
        // Gross = 30M, Insurance = 3.15M, Dependents = 0
        // Total Deductions = Personal (11M) + Insurance (3.15M) = 14.15M
        // Taxable Income = 30M - 14.15M = 15.85M (Bracket 3)
        // Tax = 15.85M * 15% - 750,000 = 2,377,500 - 750,000 = 1,627,500
        BigDecimal gross = new BigDecimal("30000000");
        BigDecimal insurance = new BigDecimal("3150000");
        BigDecimal expectedTax = new BigDecimal("1627500");
        
        BigDecimal result = service.calculatePIT(gross, insurance, 0);
        assertThat(result).isEqualByComparingTo(expectedTax);
    }

    @Test
    @DisplayName("Calculate PIT - Bracket 7: Taxable income > 80M (35% tax - 9.85M)")
    void calculatePIT_Bracket7_CalculatesMaxProgressiveTax() {
        // Gross = 120M, Insurance = 4.914M (capped), Dependents = 2 (8.8M)
        // Total Deductions = Personal (11M) + Dependents (8.8M) + Insurance (4.914M) = 24.714M
        // Taxable Income = 120M - 24.714M = 95.286M (Bracket 7)
        // Tax = 95.286M * 35% - 9,850,000 = 33,350,100 - 9,850,000 = 23,500,100
        BigDecimal gross = new BigDecimal("120000000");
        BigDecimal insurance = new BigDecimal("4914000");
        BigDecimal expectedTax = new BigDecimal("23500100");
        
        BigDecimal result = service.calculatePIT(gross, insurance, 2);
        assertThat(result).isEqualByComparingTo(expectedTax);
    }
}
