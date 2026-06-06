package com.hrm.backend.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder(toBuilder = true)
public record CheckInRequest(
    BigDecimal latitude,
    BigDecimal longitude
) {}
