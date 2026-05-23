package com.hrm.backend.service;

import com.hrm.backend.dto.ChangePasswordRequest;
import com.hrm.backend.dto.LoginRequest;
import com.hrm.backend.dto.LoginResponse;
import com.hrm.backend.dto.ForgotPasswordRequest;
import com.hrm.backend.dto.VerifyForgotPasswordRequest;

public interface AuthService {

    /**
     * Xác thực người dùng và trả về JWT token
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * Đổi mật khẩu cho người dùng đang đăng nhập
     */
    void changePassword(String username, ChangePasswordRequest changePasswordRequest);

    /**
     * Khởi động quy trình quên mật khẩu (gửi mã OTP qua email)
     */
    void initiateForgotPassword(ForgotPasswordRequest request);

    /**
     * Xác minh OTP và hoàn tất đổi mật khẩu mới
     */
    void verifyForgotPassword(VerifyForgotPasswordRequest request);
}
