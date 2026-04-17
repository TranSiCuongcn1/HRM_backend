package com.hrm.backend.repository;

import com.hrm.backend.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Integer> {

        boolean existsByEmployeeId(Integer employeeId);

    /**
     * Lấy số dư phép của NV theo loại phép và năm
     */
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(
            Integer employeeId, Integer leaveTypeId, int year);

    /**
     * Xem tất cả số dư phép của NV trong 1 năm
     */
    List<LeaveBalance> findByEmployeeIdAndYear(Integer employeeId, int year);

    /**
     * Kiểm tra đã tồn tại balance chưa (tránh khởi tạo trùng)
     */
    boolean existsByEmployeeIdAndLeaveTypeIdAndYear(
            Integer employeeId, Integer leaveTypeId, int year);
}
