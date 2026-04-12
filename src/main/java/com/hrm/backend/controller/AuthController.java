package com.hrm.backend.controller;

import com.hrm.backend.dto.ApiResponse;
import com.hrm.backend.dto.ChangePasswordRequest;
import com.hrm.backend.dto.LoginRequest;
import com.hrm.backend.dto.LoginResponse;
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
}
