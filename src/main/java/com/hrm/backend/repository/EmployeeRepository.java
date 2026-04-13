package com.hrm.backend.repository;

import com.hrm.backend.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Optional<Employee> findByCode(String code);

    Optional<Employee> findByEmail(String email);

    boolean existsByCode(String code);

    boolean existsByEmail(String email);

    /**
     * Tìm kiếm nhân viên theo tên, mã nhân viên hoặc email (không phân biệt hoa/thường).
     * Hỗ trợ phân trang.
     */
    @Query("SELECT e FROM Employee e WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            " AND (:status IS NULL OR :status = '' OR e.status = :status)")
    Page<Employee> searchEmployees(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );

    /**
     * Đếm số nhân viên thuộc một phòng ban
     */
    long countByDepartmentId(Integer departmentId);
}
