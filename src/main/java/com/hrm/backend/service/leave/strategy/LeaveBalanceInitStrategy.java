package com.hrm.backend.service.leave.strategy;

import java.math.BigDecimal;

public interface LeaveBalanceInitStrategy {
    String getLeaveTypeCode();
    BigDecimal getDefaultDays();
}
