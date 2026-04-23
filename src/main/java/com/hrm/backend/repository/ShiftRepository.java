package com.hrm.backend.repository;

import com.hrm.backend.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Integer> {
    Optional<Shift> findByIsDefaultTrue();
    boolean existsByCode(String code);
}
