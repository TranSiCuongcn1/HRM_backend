package com.hrm.backend.repository;

import com.hrm.backend.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {

        boolean existsByEmployeeId(Integer employeeId);

    /**
     * Danh sách đơn của nhân viên (mới nhất trước)
     */
    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Integer employeeId);

    /**
     * Lọc đơn theo trạng thái (cho Admin xem hàng chờ duyệt)
     */
    List<LeaveRequest> findByStatusOrderByCreatedAtAsc(String status);

    /**
     * Lấy tất cả đơn (không lọc)
     */
    List<LeaveRequest> findAllByOrderByCreatedAtDesc();

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
            "AND (:status IS NULL OR :status = '' OR lr.status = :status) " +
            "AND (:leaveTypeId IS NULL OR lr.leaveType.id = :leaveTypeId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(lr.reason) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(lr.leaveType.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<LeaveRequest> searchMyRequests(
            @Param("employeeId") Integer employeeId,
            @Param("status") String status,
            @Param("leaveTypeId") Integer leaveTypeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
            "(:status IS NULL OR :status = '' OR lr.status = :status) " +
            "AND (:leaveTypeId IS NULL OR lr.leaveType.id = :leaveTypeId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(lr.employee.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(lr.employee.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(lr.reason) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<LeaveRequest> searchAllRequests(
            @Param("status") String status,
            @Param("leaveTypeId") Integer leaveTypeId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * Đếm tổng ngày nghỉ có lương đã duyệt trong khoảng thời gian.
     * Module Payroll sẽ gọi method này để tính paidLeaveDays.
     *
     * Logic: Lấy tổng field `days` từ các đơn APPROVED
     * mà loại phép có isPaid = true và khoảng ngày nằm trong tháng.
     */
    @Query("SELECT COALESCE(SUM(lr.days), 0) FROM LeaveRequest lr " +
            "JOIN lr.leaveType lt " +
            "WHERE lr.employee.id = :empId " +
            "AND lr.status = 'APPROVED' " +
            "AND lt.isPaid = true " +
            "AND lr.startDate <= :endDate " +
            "AND lr.endDate >= :startDate")
    BigDecimal sumApprovedPaidLeaveDays(
            @Param("empId") Integer empId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
