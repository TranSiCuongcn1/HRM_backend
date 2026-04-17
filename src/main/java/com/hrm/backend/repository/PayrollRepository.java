package com.hrm.backend.repository;

import com.hrm.backend.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

        @Query("SELECT p FROM Payroll p WHERE p.month = :month " +
            "AND (:status IS NULL OR :status = '' OR p.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.employee.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.employee.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:departmentId IS NULL OR p.employee.department.id = :departmentId)")
        Page<Payroll> searchPayrollsByMonth(
            @Param("month") String month,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("departmentId") Integer departmentId,
            Pageable pageable
        );

        Page<Payroll> findByEmployeeId(Integer employeeId, Pageable pageable);

        Page<Payroll> findByEmployeeIdAndStatus(Integer employeeId, String status, Pageable pageable);
}
