package com.hrm.backend.service.impl;

import com.hrm.backend.dto.AttendanceRequest;
import com.hrm.backend.dto.AttendanceResponse;
import com.hrm.backend.entity.AttendanceRecord;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.Holiday;
import com.hrm.backend.entity.OvertimeRequest;
import com.hrm.backend.entity.Shift;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.AttendanceRepository;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.repository.HolidayRepository;
import com.hrm.backend.repository.OvertimeRequestRepository;
import com.hrm.backend.repository.ShiftRepository;
import com.hrm.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private OvertimeRequestRepository overtimeRequestRepository;

    @Mock
    private HolidayRepository holidayRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Employee employee;
    private User user;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1)
                .code("EMP0001")
                .name("Nguyen Van A")
                .build();
        user = User.builder()
                .id(1)
                .username("employee")
                .employee(employee)
                .build();

        ReflectionTestUtils.setField(attendanceService, "officeLatitude", 10.848031);
        ReflectionTestUtils.setField(attendanceService, "officeLongitude", 106.784944);
        ReflectionTestUtils.setField(attendanceService, "allowedRadiusMeters", 200.0);
        ReflectionTestUtils.setField(attendanceService, "allowedIpsConfig", "127.0.0.1,203.0.113.10");
    }

    @Test
    @DisplayName("Unit checkIn - Valid GPS/IP should create attendance record")
    void checkIn_ValidGpsAndIp_CreatesRecord() {
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(user));
        when(attendanceRepository.existsByEmployeeIdAndDate(1, LocalDate.now())).thenReturn(false);
        when(shiftRepository.findByIsDefaultTrue()).thenReturn(Optional.of(defaultShiftStartingAfterNow()));
        when(holidayRepository.findByDate(LocalDate.now())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> {
            AttendanceRecord record = invocation.getArgument(0);
            record.setId(1);
            return record;
        });

        AttendanceResponse response = attendanceService.checkIn(
                "employee",
                new BigDecimal("10.848031"),
                new BigDecimal("106.784944"),
                "127.0.0.1");

        assertThat(response.status()).isEqualTo("ON_TIME");
        assertThat(response.checkInGpsValid()).isTrue();
        assertThat(response.checkInIpValid()).isTrue();
        assertThat(response.employeeCode()).isEqualTo("EMP0001");

        ArgumentCaptor<AttendanceRecord> captor = ArgumentCaptor.forClass(AttendanceRecord.class);
        verify(attendanceRepository).save(captor.capture());
        assertThat(captor.getValue().getDate()).isEqualTo(LocalDate.now());
        assertThat(captor.getValue().getLateMinutes()).isZero();
    }

    @Test
    @DisplayName("Unit checkIn - Duplicate check-in should throw IllegalArgumentException")
    void checkIn_DuplicateToday_ThrowsException() {
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(user));
        when(attendanceRepository.existsByEmployeeIdAndDate(1, LocalDate.now())).thenReturn(true);

        assertThatThrownBy(() -> attendanceService.checkIn(
                "employee",
                new BigDecimal("10.848031"),
                new BigDecimal("106.784944"),
                "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("check-in");

        verify(attendanceRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Unit checkIn - Missing GPS should throw IllegalArgumentException")
    void checkIn_MissingGps_ThrowsException() {
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(user));
        when(attendanceRepository.existsByEmployeeIdAndDate(1, LocalDate.now())).thenReturn(false);
        when(shiftRepository.findByIsDefaultTrue()).thenReturn(Optional.of(defaultShiftStartingAfterNow()));
        when(holidayRepository.findByDate(LocalDate.now())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkIn("employee", null, null, "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GPS");

        verify(attendanceRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Unit checkIn - Invalid office IP should throw IllegalArgumentException")
    void checkIn_InvalidIp_ThrowsException() {
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(user));
        when(attendanceRepository.existsByEmployeeIdAndDate(1, LocalDate.now())).thenReturn(false);
        when(shiftRepository.findByIsDefaultTrue()).thenReturn(Optional.of(defaultShiftStartingAfterNow()));
        when(holidayRepository.findByDate(LocalDate.now())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkIn(
                "employee",
                new BigDecimal("10.848031"),
                new BigDecimal("106.784944"),
                "198.51.100.20"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("IP");

        verify(attendanceRepository, never()).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("Unit checkIn - Late compared with default shift should mark LATE")
    void checkIn_AfterShiftStart_MarksLate() {
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(user));
        when(attendanceRepository.existsByEmployeeIdAndDate(1, LocalDate.now())).thenReturn(false);
        when(shiftRepository.findByIsDefaultTrue()).thenReturn(Optional.of(defaultShiftStartingBeforeNow()));
        when(holidayRepository.findByDate(LocalDate.now())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.checkIn(
                "employee",
                new BigDecimal("10.848031"),
                new BigDecimal("106.784944"),
                "127.0.0.1");

        assertThat(response.status()).isEqualTo("LATE");
        assertThat(response.lateMinutes()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Unit checkIn - Holiday should not mark LATE even when after shift start")
    void checkIn_Holiday_DoesNotMarkLate() {
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(user));
        when(attendanceRepository.existsByEmployeeIdAndDate(1, LocalDate.now())).thenReturn(false);
        when(shiftRepository.findByIsDefaultTrue()).thenReturn(Optional.of(defaultShiftStartingBeforeNow()));
        when(holidayRepository.findByDate(LocalDate.now())).thenReturn(Optional.of(Holiday.builder()
                .id(1)
                .name("National Day")
                .date(LocalDate.now())
                .build()));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.checkIn(
                "employee",
                new BigDecimal("10.848031"),
                new BigDecimal("106.784944"),
                "127.0.0.1");

        assertThat(response.status()).isEqualTo("ON_TIME");
        assertThat(response.lateMinutes()).isZero();
        assertThat(response.note()).contains("National Day");
    }

    @Test
    @DisplayName("Unit checkOut - Early checkout should mark EARLY_LEAVE")
    void checkOut_BeforeShiftEnd_MarksEarlyLeave() {
        AttendanceRecord record = checkedInRecord(LocalDate.now(), LocalTime.now().minusHours(4), "ON_TIME");
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(user));
        when(attendanceRepository.findByEmployeeIdAndDate(1, LocalDate.now())).thenReturn(Optional.of(record));
        when(shiftRepository.findByIsDefaultTrue()).thenReturn(Optional.of(defaultShiftEndingAfterNow()));
        when(overtimeRequestRepository.findByEmployeeIdAndDateAndStatus(1, LocalDate.now(), "APPROVED"))
                .thenReturn(List.of());
        when(holidayRepository.findByDate(LocalDate.now())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.checkOut(
                "employee",
                new BigDecimal("10.848031"),
                new BigDecimal("106.784944"),
                "127.0.0.1");

        assertThat(response.status()).isEqualTo("EARLY_LEAVE");
        assertThat(response.earlyLeaveMinutes()).isGreaterThan(0);
        assertThat(response.checkOutGpsValid()).isTrue();
        assertThat(response.checkOutIpValid()).isTrue();
    }

    @Test
    @DisplayName("Unit checkOut - Overtime should be capped by approved OT request")
    void checkOut_ApprovedOvertime_CapsOvertimeHours() {
        AttendanceRecord record = checkedInRecord(LocalDate.now(), LocalTime.now().minusHours(10), "ON_TIME");
        Shift shift = Shift.builder()
                .id(1)
                .code("STD")
                .name("Standard")
                .startTime(LocalTime.now().minusHours(10))
                .endTime(LocalTime.now().minusHours(1))
                .build();
        when(userRepository.findByUsername("employee")).thenReturn(Optional.of(user));
        when(attendanceRepository.findByEmployeeIdAndDate(1, LocalDate.now())).thenReturn(Optional.of(record));
        when(shiftRepository.findByIsDefaultTrue()).thenReturn(Optional.of(shift));
        when(overtimeRequestRepository.findByEmployeeIdAndDateAndStatus(1, LocalDate.now(), "APPROVED"))
                .thenReturn(List.of(OvertimeRequest.builder().hours(new BigDecimal("1.50")).build()));
        when(holidayRepository.findByDate(LocalDate.now())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.checkOut(
                "employee",
                new BigDecimal("10.848031"),
                new BigDecimal("106.784944"),
                "127.0.0.1");

        assertThat(response.workHours()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(response.overtimeHours()).isEqualByComparingTo(new BigDecimal("1.50"));
    }

    @Test
    @DisplayName("Unit adminUpdateRecord - Should recalculate work hours when check-in and check-out are provided")
    void adminUpdateRecord_WithTimes_RecalculatesWorkHours() {
        AttendanceRecord record = checkedInRecord(LocalDate.of(2026, 6, 2), LocalTime.of(8, 0), "ON_TIME");
        record.setCheckOut(LocalTime.of(12, 0));
        when(attendanceRepository.findById(1)).thenReturn(Optional.of(record));
        when(shiftRepository.findByIsDefaultTrue()).thenReturn(Optional.of(defaultOfficeShift()));
        when(overtimeRequestRepository.findByEmployeeIdAndDateAndStatus(1, LocalDate.of(2026, 6, 2), "APPROVED"))
                .thenReturn(List.of());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceRequest request = new AttendanceRequest(
                1,
                LocalDate.of(2026, 6, 2),
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                "ON_TIME",
                "Manual fix");

        AttendanceResponse response = attendanceService.adminUpdateRecord(1, request);

        assertThat(response.workHours()).isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat(response.overtimeHours()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.note()).isEqualTo("Manual fix");
    }

    private AttendanceRecord checkedInRecord(LocalDate date, LocalTime checkIn, String status) {
        return AttendanceRecord.builder()
                .id(1)
                .employee(employee)
                .date(date)
                .checkIn(checkIn)
                .status(status)
                .lateMinutes(0)
                .earlyLeaveMinutes(0)
                .workHours(BigDecimal.ZERO)
                .overtimeHours(BigDecimal.ZERO)
                .build();
    }

    private Shift defaultOfficeShift() {
        return Shift.builder()
                .id(1)
                .code("STD")
                .name("Standard")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .breakStartTime(LocalTime.of(12, 0))
                .breakEndTime(LocalTime.of(13, 0))
                .isDefault(true)
                .build();
    }

    private Shift defaultShiftStartingAfterNow() {
        return Shift.builder()
                .id(1)
                .code("FUTURE")
                .name("Future")
                .startTime(LocalTime.now().plusHours(1))
                .endTime(LocalTime.now().plusHours(9))
                .isDefault(true)
                .build();
    }

    private Shift defaultShiftStartingBeforeNow() {
        return Shift.builder()
                .id(1)
                .code("PAST")
                .name("Past")
                .startTime(LocalTime.now().minusHours(1))
                .endTime(LocalTime.now().plusHours(7))
                .isDefault(true)
                .build();
    }

    private Shift defaultShiftEndingAfterNow() {
        return Shift.builder()
                .id(1)
                .code("EARLY")
                .name("Early leave check")
                .startTime(LocalTime.now().minusHours(6))
                .endTime(LocalTime.now().plusHours(1))
                .isDefault(true)
                .build();
    }
}
