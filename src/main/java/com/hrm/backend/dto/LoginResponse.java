package com.hrm.backend.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
    String accessToken,
    String tokenType,
    Integer userId,
    Integer employeeId,
    String username,
    String email,
    String role,
    String employeeName
) {}
