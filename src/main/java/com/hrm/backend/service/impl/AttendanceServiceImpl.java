package com.hrm.backend.service.impl;

import com.hrm.backend.dto.AttendanceRequest;
import com.hrm.backend.dto.AttendanceResponse;
import com.hrm.backend.entity.AttendanceRecord;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.Shift;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.AttendanceRepository;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.repository.ShiftRepository;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.repository.OvertimeRequestRepository;
import com.hrm.backend.entity.OvertimeRequest;
import com.hrm.backend.entity.Holiday;
import com.hrm.backend.repository.HolidayRepository;
import com.hrm.backend.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final OvertimeRequestRepository overtimeRequestRepository;
    private final HolidayRepository holidayRepository;

    @org.springframework.beans.factory.annotation.Value("${app.attendance.office-latitude:21.028511}")
    private double officeLatitude;

    @org.springframework.beans.factory.annotation.Value("${app.attendance.office-longitude:105.804817}")
    private double officeLongitude;

    @org.springframework.beans.factory.annotation.Value("${app.attendance.allowed-radius-meters:200.0}")
    private double allowedRadiusMeters;

    @org.springframework.beans.factory.annotation.Value("${app.attendance.allowed-ips:127.0.0.1,0:0:0:0:0:0:0:1}")
    private String allowedIpsConfig;

    private static final BigDecimal STANDARD_WORK_HOURS = new BigDecimal("8.00");

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000; // in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private boolean isValidIpAddress(String clientIp) {
        if (clientIp == null || clientIp.trim().isEmpty()) {
            return false;
        }

        if (allowedIpsConfig == null || allowedIpsConfig.trim().isEmpty() || "*".equals(allowedIpsConfig.trim())) {
            return true;
        }
        
        String[] allowedList = allowedIpsConfig.split(",");
        for (String allowed : allowedList) {
            String trimmedAllowed = allowed.trim();
            if (trimmedAllowed.isEmpty()) {
                continue;
            }
            if (trimmedAllowed.equalsIgnoreCase(clientIp)) {
                return true;
            }
            if (trimmedAllowed.contains("/")) {
                try {
                    if (matchIpSubnet(clientIp, trimmedAllowed)) {
                        return true;
                    }
                } catch (Exception e) {
                    log.warn("Lỗi phân tích CIDR subnet: {}", trimmedAllowed, e);
                }
            }
        }
        return false;
    }

    private VerificationResult verifyAttendanceLocation(
            java.math.BigDecimal latitude,
            java.math.BigDecimal longitude,
            String ipAddress) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Vui long bat dinh vi GPS de cham cong");
        }

        double distance = calculateDistance(
                latitude.doubleValue(),
                longitude.doubleValue(),
                officeLatitude,
                officeLongitude);
        boolean gpsValid = distance <= allowedRadiusMeters;
        if (!gpsValid) {
            throw new IllegalArgumentException(
                    "GPS nam ngoai pham vi van phong (" + Math.round(distance) + "m/" + Math.round(allowedRadiusMeters) + "m)");
        }

        boolean ipValid = isValidIpAddress(ipAddress);
        if (!ipValid) {
            throw new IllegalArgumentException("IP hien tai khong nam trong mang van phong");
        }

        return new VerificationResult(gpsValid, ipValid);
    }

    private record VerificationResult(Boolean gpsValid, Boolean ipValid) {
    }

    private boolean matchIpSubnet(String clientIp, String cidr) {
        String[] parts = cidr.split("/");
        String subnetIp = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        try {
            java.net.InetAddress clientAddr = java.net.InetAddress.getByName(clientIp);
            java.net.InetAddress subnetAddr = java.net.InetAddress.getByName(subnetIp);

            byte[] clientBytes = clientAddr.getAddress();
            byte[] subnetBytes = subnetAddr.getAddress();

            if (clientBytes.length != subnetBytes.length) {
                return false;
            }

            int bytesToCheck = prefixLength / 8;
            int remainingBits = prefixLength % 8;

            for (int i = 0; i < bytesToCheck; i++) {
                if (clientBytes[i] != subnetBytes[i]) {
                    return false;
                }
            }

            if (remainingBits > 0) {
                int mask = 0xFF00 >> remainingBits & 0xFF;
                if ((clientBytes[bytesToCheck] & mask) != (subnetBytes[bytesToCheck] & mask)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    // ========================================
    // 1. NHÂN VIÊN CHECK-IN
    // ========================================

    @Override
    @Transactional
    public AttendanceResponse checkIn(String username, java.math.BigDecimal latitude, java.math.BigDecimal longitude, String ipAddress) {
        Employee employee = getEmployeeByUsername(username);
        LocalDate today = LocalDate.now();

        // Kiểm tra đã check-in hôm nay chưa
        if (attendanceRepository.existsByEmployeeIdAndDate(employee.getId(), today)) {
            throw new IllegalArgumentException("Bạn đã check-in ngày hôm nay rồi");
        }

        LocalTime now = LocalTime.now();

        // Lấy ca làm việc mặc định
        Shift defaultShift = shiftRepository.findByIsDefaultTrue().orElse(null);
        String status = "ON_TIME";
        int lateMinutes = 0;
        String note = null;

        // Kiểm tra nếu là ngày lễ
        Holiday holiday = holidayRepository.findByDate(today).orElse(null);
        boolean isHoliday = holiday != null;

        if (isHoliday) {
            note = "[Ngày lễ: " + holiday.getName() + "]";
        } else if (defaultShift != null && now.isAfter(defaultShift.getStartTime())) {
            status = "LATE";
            lateMinutes = (int) Duration.between(defaultShift.getStartTime(), now).toMinutes();
        }

        // Kiểm tra định vị GPS
        VerificationResult verification = verifyAttendanceLocation(latitude, longitude, ipAddress);

        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .date(today)
                .checkIn(now)
                .status(status)
                .lateMinutes(lateMinutes)
                .checkInIp(ipAddress)
                .checkInLat(latitude)
                .checkInLng(longitude)
                .checkInGpsValid(verification.gpsValid())
                .checkInIpValid(verification.ipValid())
                .note(note)
                .build();

        AttendanceRecord saved = attendanceRepository.save(record);
        log.info("Check-in: {} lúc {} - Trạng thái: {}, GPS Valid: {}, IP Valid: {}", employee.getCode(), now, status, verification.gpsValid(), verification.ipValid());

        return mapToResponse(saved);
    }

    // ========================================
    // 2. NHÂN VIÊN CHECK-OUT
    // ========================================

    @Override
    @Transactional
    public AttendanceResponse checkOut(String username, java.math.BigDecimal latitude, java.math.BigDecimal longitude, String ipAddress) {
        Employee employee = getEmployeeByUsername(username);
        LocalDate today = LocalDate.now();

        // Hỗ trợ ca đêm: Nếu hôm nay không có check-in, tìm bản ghi hôm qua chưa có check-out
        AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today)
                .orElse(null);

        if (record == null || record.getCheckOut() != null) {
            AttendanceRecord yesterdayRecord = attendanceRepository
                    .findByEmployeeIdAndDateAndCheckOutIsNull(employee.getId(), today.minusDays(1))
                    .orElse(null);

            if (yesterdayRecord != null) {
                record = yesterdayRecord;
            }
        }

        if (record == null) {
            throw new IllegalArgumentException("Bạn chưa check-in. Vui lòng check-in trước.");
        }

        if (record.getCheckOut() != null) {
            throw new IllegalArgumentException("Bạn đã check-out ca làm việc này rồi");
        }

        LocalTime now = LocalTime.now();
        record.setCheckOut(now);

        Shift defaultShift = shiftRepository.findByIsDefaultTrue().orElse(null);
        int earlyLeaveMinutes = 0;

        // Tính workHours và overtimeHours
        calculateHours(record, defaultShift);

        // Kiểm tra nếu là ngày lễ
        Holiday holiday = holidayRepository.findByDate(today).orElse(null);
        boolean isHoliday = holiday != null;

        if (isHoliday) {
            String holidayNote = "[Ngày lễ: " + holiday.getName() + "]";
            if (record.getNote() == null || !record.getNote().contains(holidayNote)) {
                record.setNote(record.getNote() == null ? holidayNote : record.getNote() + " " + holidayNote);
            }
        }

        // Nếu về trước giờ chuẩn → đánh EARLY_LEAVE (chỉ khi không phải ngày lễ)
        if (!isHoliday && defaultShift != null && now.isBefore(defaultShift.getEndTime())) {
            earlyLeaveMinutes = (int) Duration.between(now, defaultShift.getEndTime()).toMinutes();
            record.setEarlyLeaveMinutes(earlyLeaveMinutes);
            if (!"LATE".equals(record.getStatus())) {
                record.setStatus("EARLY_LEAVE");
            }
        }

        // Kiểm tra định vị GPS
        VerificationResult verification = verifyAttendanceLocation(latitude, longitude, ipAddress);

        record.setCheckOutIp(ipAddress);
        record.setCheckOutLat(latitude);
        record.setCheckOutLng(longitude);
        record.setCheckOutGpsValid(verification.gpsValid());
        record.setCheckOutIpValid(verification.ipValid());

        AttendanceRecord saved = attendanceRepository.save(record);
        log.info("Check-out: {} lúc {} - Giờ làm: {}h, OT: {}h, GPS Valid: {}, IP Valid: {}",
                employee.getCode(), now, saved.getWorkHours(), saved.getOvertimeHours(), verification.gpsValid(), verification.ipValid());

        return mapToResponse(saved);
    }

    // ========================================
    // 3. XEM TRẠNG THÁI HÔM NAY
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getMyToday(String username) {
        Employee employee = getEmployeeByUsername(username);
        LocalDate today = LocalDate.now();

        AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today)
                .orElse(null);

        if (record == null) {
            // Chưa check-in → trả về response trống với thông tin NV
            return AttendanceResponse.builder()
                    .employeeId(employee.getId())
                    .employeeCode(employee.getCode())
                    .employeeName(employee.getName())
                    .date(today)
                    .build();
        }

        return mapToResponse(record);
    }

    // ========================================
    // 4. LỊCH SỬ CHẤM CÔNG CỦA MÌNH
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getMyRecords(String username, LocalDate from, LocalDate to, String status, Pageable pageable) {
        Employee employee = getEmployeeByUsername(username);
        return getRecordsByEmployee(employee.getId(), from, to, status, pageable);
    }

    // ========================================
    // 5. ADMIN SỬA BẢN GHI CHẤM CÔNG
    // ========================================

    @Override
    @Transactional
    public AttendanceResponse adminUpdateRecord(Integer recordId, AttendanceRequest request) {
        AttendanceRecord record = attendanceRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi chấm công ID: " + recordId));

        // Cập nhật các trường Admin gửi lên
        if (request.getCheckIn() != null) {
            record.setCheckIn(request.getCheckIn());
        }
        if (request.getCheckOut() != null) {
            record.setCheckOut(request.getCheckOut());
        }
        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
        }
        if (request.getNote() != null) {
            record.setNote(request.getNote());
        }

        // Tính lại workHours và overtimeHours nếu có đủ giờ vào/ra
        if (record.getCheckIn() != null && record.getCheckOut() != null) {
            Shift defaultShift = shiftRepository.findByIsDefaultTrue().orElse(null);
            calculateHours(record, defaultShift);
        }

        AttendanceRecord updated = attendanceRepository.save(record);
        log.info("Admin đã sửa bản ghi chấm công #{} - NV: {}, Ngày: {}, Note: {}",
                recordId, record.getEmployee().getCode(), record.getDate(), request.getNote());

        return mapToResponse(updated);
    }

    // ========================================
    // 6. ADMIN ĐÁNH VẮNG MẶT
    // ========================================

    @Override
    @Transactional
    public AttendanceResponse markAbsent(Integer employeeId, LocalDate date, String note) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên ID: " + employeeId));

        // Kiểm tra đã có bản ghi cho ngày này chưa
        AttendanceRecord record = attendanceRepository.findByEmployeeIdAndDate(employeeId, date)
                .orElse(null);

        if (record != null) {
            // Đã có bản ghi → cập nhật thành ABSENT
            record.setStatus("ABSENT");
            record.setWorkHours(BigDecimal.ZERO);
            record.setOvertimeHours(BigDecimal.ZERO);
            record.setNote(note);
        } else {
            // Chưa có → tạo bản ghi mới với status ABSENT
            record = AttendanceRecord.builder()
                    .employee(employee)
                    .date(date)
                    .status("ABSENT")
                    .workHours(BigDecimal.ZERO)
                    .overtimeHours(BigDecimal.ZERO)
                    .note(note)
                    .build();
        }

        AttendanceRecord saved = attendanceRepository.save(record);
        log.info("Admin đánh vắng mặt: NV {} ngày {} - Lý do: {}", employee.getCode(), date, note);

        return mapToResponse(saved);
    }

    // ========================================
    // 7. LỊCH SỬ CHẤM CÔNG THEO NHÂN VIÊN
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getRecordsByEmployee(
            Integer employeeId,
            LocalDate from,
            LocalDate to,
            String status,
            Pageable pageable) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new RuntimeException("Không tìm thấy nhân viên ID: " + employeeId);
        }
        return attendanceRepository
                .searchByEmployeeAndDateRange(employeeId, from, to, status, pageable)
                .map(this::mapToResponse);
    }

    // ========================================
    // 8. BẢNG CHẤM CÔNG TOÀN CÔNG TY THEO NGÀY
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getRecordsByDate(
            LocalDate date,
            String status,
            String keyword,
            Boolean hasOvertime,
            Pageable pageable) {
        return attendanceRepository.searchByDate(date, status, Boolean.TRUE.equals(hasOvertime), keyword, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getRecordsByDateRange(
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            String keyword,
            Boolean hasOvertime,
            Pageable pageable) {
        return attendanceRepository.searchByDateRange(fromDate, toDate, status, Boolean.TRUE.equals(hasOvertime), keyword, pageable)
                .map(this::mapToResponse);
    }

    // ========================================
    // 9. THỐNG KÊ THÁNG (CHO PAYROLL)
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse.MonthlyStats getMonthlyStats(Integer employeeId, int month, int year) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên ID: " + employeeId));

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        // Đếm số ngày đi làm (không tính ABSENT)
        long totalWorkDays = attendanceRepository
                .countByEmployeeIdAndDateBetweenAndStatusNot(employeeId, from, to, "ABSENT");

        // Đếm số lần đi trễ
        long lateCount = attendanceRepository
                .countByEmployeeIdAndDateBetweenAndStatus(employeeId, from, to, "LATE");

        // Tính tổng giờ OT
        List<AttendanceRecord> records = attendanceRepository
                .findByEmployeeIdAndDateBetweenOrderByDateDesc(employeeId, from, to);

        BigDecimal totalOT = records.stream()
                .map(r -> r.getOvertimeHours() != null ? r.getOvertimeHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AttendanceResponse.MonthlyStats.builder()
                .employeeId(employeeId)
                .employeeCode(employee.getCode())
                .employeeName(employee.getName())
                .month(month)
                .year(year)
                .totalWorkDays(totalWorkDays)
                .lateCount(lateCount)
                .totalOvertimeHours(totalOT)
                .build();
    }

    // ========================================
    // HELPER: Lấy Employee từ JWT username
    // ========================================

    private Employee getEmployeeByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));

        if (user.getEmployee() == null) {
            throw new RuntimeException("Tài khoản " + username + " chưa liên kết với nhân viên");
        }

        return user.getEmployee();
    }

    // ========================================
    // HELPER: Tính workHours và overtimeHours
    // ========================================

    private void calculateHours(AttendanceRecord record, Shift shift) {
        if (record.getCheckIn() == null || record.getCheckOut() == null) {
            return;
        }

        Duration duration = Duration.between(record.getCheckIn(), record.getCheckOut());
        long totalMinutes = duration.toMinutes();
        
        if (shift != null && shift.getBreakStartTime() != null && shift.getBreakEndTime() != null) {
            long breakMinutes = Duration.between(shift.getBreakStartTime(), shift.getBreakEndTime()).toMinutes();
            if (record.getCheckIn().isBefore(shift.getBreakStartTime()) && record.getCheckOut().isAfter(shift.getBreakEndTime())) {
                totalMinutes -= breakMinutes;
            }
        }

        // workHours = tổng thời gian làm việc (đơn vị: giờ, làm tròn 2 chữ số)
        BigDecimal workHours = BigDecimal.valueOf(totalMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        record.setWorkHours(workHours);

        // overtimeHours = max(0, workHours - 8)
        BigDecimal overtime = workHours.subtract(STANDARD_WORK_HOURS);
        BigDecimal actualOvertime = overtime.compareTo(BigDecimal.ZERO) > 0 ? overtime : BigDecimal.ZERO;

        // Ràng buộc với Đơn đăng ký tăng ca đã duyệt (APPROVED) theo Phương án B
        BigDecimal approvedOTHours = BigDecimal.ZERO;
        if (record.getEmployee() != null && record.getDate() != null) {
            List<OvertimeRequest> approvedRequests = overtimeRequestRepository
                    .findByEmployeeIdAndDateAndStatus(record.getEmployee().getId(), record.getDate(), "APPROVED");
            
            if (approvedRequests != null && !approvedRequests.isEmpty()) {
                approvedOTHours = approvedRequests.stream()
                        .map(OvertimeRequest::getHours)
                        .filter(h -> h != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        }

        // overtimeHours được duyệt thực tế = min(actualOvertime, approvedOTHours)
        BigDecimal finalOvertime = actualOvertime.min(approvedOTHours);
        record.setOvertimeHours(finalOvertime);
    }

    // ========================================
    // HELPER: Entity → Response DTO
    // ========================================

    private AttendanceResponse mapToResponse(AttendanceRecord record) {
        return AttendanceResponse.builder()
                .id(record.getId())
                .employeeId(record.getEmployee().getId())
                .employeeCode(record.getEmployee().getCode())
                .employeeName(record.getEmployee().getName())
                .date(record.getDate())
                .checkIn(record.getCheckIn())
                .checkOut(record.getCheckOut())
                .status(record.getStatus())
                .overtimeHours(record.getOvertimeHours())
                .workHours(record.getWorkHours())
                .lateMinutes(record.getLateMinutes())
                .earlyLeaveMinutes(record.getEarlyLeaveMinutes())
                .checkInIp(record.getCheckInIp())
                .checkInLat(record.getCheckInLat())
                .checkInLng(record.getCheckInLng())
                .checkOutIp(record.getCheckOutIp())
                .checkOutLat(record.getCheckOutLat())
                .checkOutLng(record.getCheckOutLng())
                .checkInGpsValid(record.getCheckInGpsValid())
                .checkInIpValid(record.getCheckInIpValid())
                .checkOutGpsValid(record.getCheckOutGpsValid())
                .checkOutIpValid(record.getCheckOutIpValid())
                .note(record.getNote())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
