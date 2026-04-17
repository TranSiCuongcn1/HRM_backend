package com.hrm.backend.repository;

import com.hrm.backend.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
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
