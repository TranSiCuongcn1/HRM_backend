package com.hrm.backend.controller;

import com.hrm.backend.dto.ApiResponse;
import com.hrm.backend.dto.ChangePasswordRequest;
import com.hrm.backend.dto.LoginRequest;
import com.hrm.backend.dto.LoginResponse;
import com.hrm.backend.dto.ForgotPasswordRequest;
import com.hrm.backend.dto.VerifyForgotPasswordRequest;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API xác thực và bảo mật người dùng")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * API Đăng nhập – trả về JWT Token
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Xác thực bằng username/password và trả về JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Đăng nhập thành công", loginResponse)
        );
    }

    /**
     * API Xem thông tin người dùng đang đăng nhập
     * GET /api/v1/auth/me
     */
    @GetMapping("/me")
    @Operation(summary = "Thông tin cá nhân", description = "Lấy thông tin người dùng đang đăng nhập dựa trên JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> getCurrentUser(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        LoginResponse response = LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .employeeName(user.getEmployee() != null ? user.getEmployee().getName() : null)
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("Lấy thông tin thành công", response)
        );
    }

    /**
     * API Đổi mật khẩu
     * PUT /api/v1/auth/change-password
     */
    @PutMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu cho người dùng đang đăng nhập")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {

        authService.changePassword(authentication.getName(), changePasswordRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Đổi mật khẩu thành công")
        );
    }

    /**
     * API Yêu cầu khôi phục mật khẩu (Quên mật khẩu)
     * POST /api/v1/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Yêu cầu khôi phục mật khẩu", description = "Nhận Email và Mật khẩu mới, sau đó gửi mã OTP xác minh qua Email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.initiateForgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Mã OTP xác thực đã được gửi về email của bạn. Vui lòng kiểm tra hộp thư.")
        );
    }

    /**
     * API Xác thực OTP khôi phục mật khẩu
     * POST /api/v1/auth/verify-forgot-password
     */
    @PostMapping("/verify-forgot-password")
    @Operation(summary = "Xác nhận OTP khôi phục mật khẩu", description = "Xác nhận OTP và chính thức cập nhật mật khẩu mới")
    public ResponseEntity<ApiResponse<Void>> verifyForgotPassword(
            @Valid @RequestBody VerifyForgotPasswordRequest request) {
        authService.verifyForgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Mật khẩu đã được khôi phục thành công. Vui lòng đăng nhập với mật khẩu mới.")
        );
    }
}
