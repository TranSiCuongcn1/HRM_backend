package com.hrm.backend.service.impl;

import com.hrm.backend.dto.ChangePasswordRequest;
import com.hrm.backend.dto.LoginRequest;
import com.hrm.backend.dto.LoginResponse;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.security.JwtTokenProvider;
import com.hrm.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
