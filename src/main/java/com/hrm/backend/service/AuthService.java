package com.hrm.backend.service;

import com.hrm.backend.dto.ChangePasswordRequest;
import com.hrm.backend.dto.LoginRequest;
import com.hrm.backend.dto.LoginResponse;

public interface AuthService {

    /**
     * Xác thực người dùng và trả về JWT token
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * Đổi mật khẩu cho người dùng đang đăng nhập
     */
    void changePassword(String username, ChangePasswordRequest changePasswordRequest);
}
