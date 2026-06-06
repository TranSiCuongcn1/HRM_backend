package com.hrm.backend.service.leave.strategy.impl;

import com.hrm.backend.service.leave.strategy.LeaveBalanceInitStrategy;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class WeddingLeaveInitStrategy implements LeaveBalanceInitStrategy {
    private static final BigDecimal DEFAULT_WEDDING_DAYS = new BigDecimal("3.0");

    @Override
    public String getLeaveTypeCode() {
        return "WEDDING";
    }

    @Override
    public BigDecimal getDefaultDays() {
        return DEFAULT_WEDDING_DAYS;
    }
}
