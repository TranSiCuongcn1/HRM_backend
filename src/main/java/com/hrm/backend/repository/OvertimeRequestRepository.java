package com.hrm.backend.repository;

import com.hrm.backend.entity.OvertimeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Integer> {

    @Query("SELECT orq FROM OvertimeRequest orq WHERE orq.employee.id = :employeeId " +
            "AND (:status IS NULL OR :status = '' OR orq.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(orq.reason) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<OvertimeRequest> searchByEmployee(
            @Param("employeeId") Integer employeeId,
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT orq FROM OvertimeRequest orq WHERE " +
            "(:status IS NULL OR :status = '' OR orq.status = :status) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(orq.employee.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(orq.employee.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(orq.reason) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<OvertimeRequest> searchAll(
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    boolean existsByEmployeeIdAndDate(Integer employeeId, LocalDate date);
}
