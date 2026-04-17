package com.hrm.backend.repository;

import com.hrm.backend.entity.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Integer> {

    /**
     * Tìm bản ghi chấm công của nhân viên trong 1 ngày cụ thể
     */
    Optional<AttendanceRecord> findByEmployeeIdAndDate(Integer employeeId, LocalDate date);

    /**
     * Kiểm tra nhân viên đã check-in ngày hôm nay chưa
     */
    boolean existsByEmployeeIdAndDate(Integer employeeId, LocalDate date);

        boolean existsByEmployeeId(Integer employeeId);

    /**
     * Lấy toàn bộ bản ghi chấm công trong khoảng ngày của 1 nhân viên
     */
    List<AttendanceRecord> findByEmployeeIdAndDateBetweenOrderByDateDesc(
            Integer employeeId, LocalDate from, LocalDate to);

    /**
     * Bảng chấm công toàn công ty theo 1 ngày
     */
    List<AttendanceRecord> findByDateOrderByEmployeeCodeAsc(LocalDate date);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.date = :date " +
            "AND (:status IS NULL OR :status = '' OR a.status = :status) " +
            "AND (:hasOvertime = false OR COALESCE(a.overtimeHours, 0) > 0) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(a.employee.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.employee.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AttendanceRecord> searchByDate(
            @Param("date") LocalDate date,
            @Param("status") String status,
            @Param("hasOvertime") boolean hasOvertime,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT a FROM AttendanceRecord a WHERE a.employee.id = :employeeId " +
            "AND a.date BETWEEN :from AND :to " +
            "AND (:status IS NULL OR :status = '' OR a.status = :status)")
    Page<AttendanceRecord> searchByEmployeeAndDateRange(
            @Param("employeeId") Integer employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") String status,
            Pageable pageable
    );

    /**
     * Đếm số ngày có đi làm (không phải ABSENT) trong khoảng thời gian
     */
    long countByEmployeeIdAndDateBetweenAndStatusNot(
            Integer employeeId, LocalDate from, LocalDate to, String status);

    /**
     * Đếm số lần đi trễ trong khoảng thời gian
     */
    long countByEmployeeIdAndDateBetweenAndStatus(
            Integer employeeId, LocalDate from, LocalDate to, String status);
}
