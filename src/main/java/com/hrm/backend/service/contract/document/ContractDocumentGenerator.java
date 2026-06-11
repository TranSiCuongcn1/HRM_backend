package com.hrm.backend.service.contract.document;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ContractDocumentGenerator {
    String generateSummary(String employeeName, String contractType,
                           LocalDate startDate, LocalDate endDate,
                           BigDecimal basicSalary);
    String getContractTitle();
}
