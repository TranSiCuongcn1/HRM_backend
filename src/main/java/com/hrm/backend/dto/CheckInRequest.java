package com.hrm.backend.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record CheckInRequest(
    BigDecimal latitude,
    BigDecimal longitude
) {}
