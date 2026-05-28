package com.hrm.backend.scheduler;

import com.hrm.backend.entity.AttendanceRecord;
import com.hrm.backend.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceScheduler {

    private final AttendanceRepository attendanceRepository;

    /**
     * Chạy vào 23:59 mỗi ngày để quét các bản ghi quên check-out
     */
    @Scheduled(cron = "0 59 23 * * ?")
    @Transactional
    public void markMissingCheckouts() {
        LocalDate today = LocalDate.now();
        log.info("Bắt đầu quét các bản ghi chấm công quên check-out cho ngày {}", today);

        // Tìm các bản ghi chưa có checkOut và không phải là ABSENT
        List<AttendanceRecord> missingRecords = attendanceRepository
                .findByDateAndCheckOutIsNullAndStatusNot(today, "ABSENT");

        int count = 0;
        for (AttendanceRecord record : missingRecords) {
            record.setStatus("MISSING_CHECKOUT");
            // Vẫn để checkOut = null để admin có thể nhập tay sau
            count++;
        }

        if (count > 0) {
            attendanceRepository.saveAll(missingRecords);
            log.info("Đã cập nhật {} bản ghi thành trạng thái MISSING_CHECKOUT", count);
        } else {
            log.info("Không có bản ghi nào bị quên check-out trong ngày.");
        }
    }
}
