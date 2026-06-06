package com.hrm.backend.service;

import com.hrm.backend.entity.User;
import com.hrm.backend.entity.Employee;

import java.util.Optional;

public interface UserService {
    User createUserAccount(Employee employee, String defaultPassword);
    void updateEmail(String oldEmail, String newEmail);
    void deactivateAccount(String username);
    void deleteAccountByEmployeeId(Integer employeeId);
    Optional<User> findByUsername(String username);
}
