package com.hrm.backend.repository;

import com.hrm.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmployee_Id(Integer employeeId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
