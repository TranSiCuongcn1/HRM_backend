package com.hrm.backend.service.impl;

import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createUserAccount(Employee employee, String defaultPassword) {
        String username = employee.getCode();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập '" + username + "' đã tồn tại");
        }

        User user = User.builder()
                .employee(employee)
                .username(username)
                .email(employee.getEmail())
                .passwordHash(passwordEncoder.encode(defaultPassword))
                .role("EMPLOYEE")
                .isActive(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Đã tạo tài khoản cho nhân viên: {} (username: {})", employee.getName(), username);
        return saved;
    }

    @Override
    @Transactional
    public void updateEmail(String oldEmail, String newEmail) {
        userRepository.findByEmail(oldEmail).ifPresent(user -> {
            user.setEmail(newEmail);
            userRepository.save(user);
            log.info("Đã đồng bộ email mới '{}' cho tài khoản: {}", newEmail, user.getUsername());
        });
    }

    @Override
    @Transactional
    public void deactivateAccount(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setIsActive(false);
            userRepository.save(user);
            log.info("Đã khóa tài khoản: {}", username);
        });
    }

    @Override
    @Transactional
    public void deleteAccountByEmployeeId(Integer employeeId) {
        userRepository.findByEmployee_Id(employeeId).ifPresent(userRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
