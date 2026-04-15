package com.hrm.backend.repository;

import com.hrm.backend.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer> {

    /**
     * Tìm hợp đồng theo nhân viên và trạng thái.
     * Dùng chính cho Payroll: lấy hợp đồng ACTIVE để bóc basic_salary.
     */
    Optional<Contract> findByEmployeeIdAndStatus(Integer employeeId, String status);

    /**
     * Lịch sử toàn bộ hợp đồng của nhân viên (mới nhất trước).
     */
    List<Contract> findByEmployeeIdOrderByStartDateDesc(Integer employeeId);

    /**
     * Kiểm tra nhân viên đã có hợp đồng ở trạng thái nào chưa (tránh trùng ACTIVE).
     */
    boolean existsByEmployeeIdAndStatus(Integer employeeId, String status);

    /**
     * Danh sách hợp đồng sắp hết hạn trong khoảng thời gian (cảnh báo cho HR).
     */
    List<Contract> findByStatusAndEndDateBetween(String status, LocalDate from, LocalDate to);

    /**
     * Tìm tất cả hợp đồng theo trạng thái (phục vụ danh sách / lọc).
     */
    List<Contract> findByStatus(String status);

    /**
     * Tìm tất cả hợp đồng của 1 nhân viên theo trạng thái.
     */
    List<Contract> findByEmployeeIdAndStatusOrderByStartDateDesc(Integer employeeId, String status);
}
