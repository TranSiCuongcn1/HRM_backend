package com.hrm.backend.dto;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;

@Builder
public record LoginRequest(
    @NotBlank(message = "Tên đăng nhập không được để trống")
    String username,

    @NotBlank(message = "Mật khẩu không được để trống")
    String password
) {}
