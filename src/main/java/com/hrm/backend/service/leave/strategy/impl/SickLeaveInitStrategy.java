package com.hrm.backend.service.leave.strategy.impl;

import com.hrm.backend.service.leave.strategy.LeaveBalanceInitStrategy;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class SickLeaveInitStrategy implements LeaveBalanceInitStrategy {
    private static final BigDecimal DEFAULT_SICK_DAYS = new BigDecimal("30.0");

    @Override
    public String getLeaveTypeCode() {
        return "SICK";
    }

    @Override
    public BigDecimal getDefaultDays() {
        return DEFAULT_SICK_DAYS;
    }
}
