package com.hrm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.backend.dto.LoginRequest;
import com.hrm.backend.dto.LoginResponse;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerBlackBoxTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new com.hrm.backend.exception.GlobalExceptionHandler()) // Registers exception handler to verify proper error status mapping
                .build();
    }

    @Test
    @DisplayName("Black-box POST /api/v1/auth/login - Valid credentials should return 200 OK and access token")
    void login_ValidCredentials_ReturnsOkAndToken() throws Exception {
        LoginRequest request = new LoginRequest("admin", "password123");
        LoginResponse response = LoginResponse.builder()
                .accessToken("mock-jwt-token")
                .tokenType("Bearer")
                .username("admin")
                .role("ADMIN")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/auth/login - Empty password should return 400 Bad Request due to validation error")
    void login_EmptyPassword_ReturnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest("admin", ""); // Empty password triggers validation

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Dữ liệu đầu vào không hợp lệ"))
                .andExpect(jsonPath("$.data.password").value("Mật khẩu không được để trống"));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/auth/login - Bad credentials should return 401 Unauthorized")
    void login_BadCredentials_ReturnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest("admin", "wrong_password");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không đúng"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tên đăng nhập hoặc mật khẩu không đúng"));
    }
}
