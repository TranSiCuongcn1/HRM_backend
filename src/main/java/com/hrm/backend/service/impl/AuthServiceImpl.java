package com.hrm.backend.service.impl;

import com.hrm.backend.dto.ChangePasswordRequest;
import com.hrm.backend.dto.LoginRequest;
import com.hrm.backend.dto.LoginResponse;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.security.JwtTokenProvider;
import com.hrm.backend.service.AuthService;
import com.hrm.backend.dto.ForgotPasswordRequest;
import com.hrm.backend.dto.VerifyForgotPasswordRequest;
import com.hrm.backend.service.EmailService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final Map<String, TempResetData> resetCache = new ConcurrentHashMap<>();

    @Getter
    @Setter
    @AllArgsConstructor
    private static class TempResetData {
        private String email;
        private String newPasswordHash;
        private String otpCode;
        private LocalDateTime expiryTime;
    }

    /**
     * Xác thực người dùng bằng username/password.
     * Nếu thành công, sinh JWT token và trả về thông tin người dùng.
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // Spring Security xác thực qua AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Sinh JWT Token
        String token = jwtTokenProvider.generateToken(authentication);

        // Lấy thông tin User từ DB để trả về response
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Người dùng không tồn tại"));

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .employeeName(user.getEmployee() != null ? user.getEmployee().getName() : null)
                .build();
    }

    /**
     * Đổi mật khẩu cho người dùng đang đăng nhập.
     * Kiểm tra mật khẩu hiện tại, kiểm tra xác nhận, sau đó cập nhật.
     */
    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        // Lấy user hiện tại từ DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra mật khẩu hiện tại có đúng không
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Mật khẩu hiện tại không đúng");
        }

        // Kiểm tra mật khẩu mới và xác nhận có trùng khớp không
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        // Kiểm tra mật khẩu mới không trùng mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }

        // Cập nhật mật khẩu mới (được mã hoá BCrypt)
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public void initiateForgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại trong hệ thống"));

        // Sinh mã OTP 6 chữ số ngẫu nhiên
        String otpCode = String.format("%06d", new Random().nextInt(1000000));
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        // Lưu vào cache (sử dụng email đã được chuyển thành chữ thường làm key)
        resetCache.put(email, new TempResetData(email, newPasswordHash, otpCode, expiryTime));

        // Gửi email bất đồng bộ (sử dụng email chính thức của user từ DB để gửi)
        String employeeName = user.getEmployee() != null ? user.getEmployee().getName() : user.getUsername();
        emailService.sendForgotPasswordOtpEmail(user.getEmail(), employeeName, otpCode);
    }

    @Override
    @Transactional
    public void verifyForgotPassword(VerifyForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        TempResetData resetData = resetCache.get(email);

        if (resetData == null) {
            throw new IllegalArgumentException("Không tìm thấy yêu cầu khôi phục mật khẩu hoặc đã bị hủy");
        }

        if (resetData.getExpiryTime().isBefore(LocalDateTime.now())) {
            resetCache.remove(email);
            throw new IllegalArgumentException("Mã OTP đã hết hạn. Vui lòng gửi lại yêu cầu.");
        }

        if (!resetData.getOtpCode().equals(request.getOtpCode().trim())) {
            throw new IllegalArgumentException("Mã OTP xác thực không chính xác");
        }

        // OTP hợp lệ -> Cập nhật mật khẩu mới vào DB
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        user.setPasswordHash(resetData.getNewPasswordHash());
        userRepository.save(user);

        // Xóa khỏi cache
        resetCache.remove(email);
    }
}
