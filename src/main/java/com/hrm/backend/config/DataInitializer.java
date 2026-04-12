package com.hrm.backend.config;

import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Tạo tài khoản Admin mặc định khi khởi động ứng dụng lần đầu.
     * Username: admin
     * Password: admin123
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                createAdminUser();
                log.info("=== Tài khoản Admin mặc định đã được tạo ===");
                log.info("Username: admin");
                log.info("Password: admin123");
                log.info("============================================");
            } else {
                log.info("Tài khoản Admin đã tồn tại, bỏ qua khởi tạo.");
            }
        };
    }

    @Transactional
    protected void createAdminUser() {
        // Tạo Employee trước (vì User liên kết 1-1 với Employee)
        Employee adminEmployee = Employee.builder()
                .code("EMP001")
                .name("System Administrator")
                .email("admin@hrm.com")
                .joinDate(java.time.LocalDate.now())
                .status("ACTIVE")
                .build();
        entityManager.persist(adminEmployee);
        entityManager.flush();

        // Tạo User Admin
        User adminUser = User.builder()
                .employee(adminEmployee)
                .username("admin")
                .email("admin@hrm.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role("ADMIN")
                .build();
        userRepository.save(adminUser);
    }
}
