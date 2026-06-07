package com.hrm.backend.service.leave.strategy.impl;

import com.hrm.backend.service.leave.strategy.LeaveBalanceInitStrategy;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class AnnualLeaveInitStrategy implements LeaveBalanceInitStrategy {
    private static final BigDecimal DEFAULT_ANNUAL_DAYS = new BigDecimal("12.0");

    @Override
    public String getLeaveTypeCode() {
        return "ANNUAL";
    }

    @Override
    public BigDecimal getDefaultDays() {
        return DEFAULT_ANNUAL_DAYS;
    }
}
