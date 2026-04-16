package com.hrm.backend.repository;

import com.hrm.backend.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Integer> {

    Optional<Payroll> findByEmployeeIdAndMonth(Integer employeeId, String month);

    boolean existsByEmployeeIdAndMonth(Integer employeeId, String month);

    List<Payroll> findByMonthOrderByEmployeeCodeAsc(String month);

    List<Payroll> findByMonthAndStatus(String month, String status);

    List<Payroll> findByEmployeeIdOrderByMonthDesc(Integer employeeId);
}
