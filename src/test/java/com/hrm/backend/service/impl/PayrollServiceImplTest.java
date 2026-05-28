package com.hrm.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.backend.dto.AttendanceResponse;
import com.hrm.backend.dto.ContractResponse;
import com.hrm.backend.dto.PayrollResponse;
import com.hrm.backend.dto.PayrollUpdateRequest;
import com.hrm.backend.entity.AttendanceRecord;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.Holiday;
import com.hrm.backend.entity.Payroll;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.AttendanceRepository;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.repository.HolidayRepository;
import com.hrm.backend.repository.PayrollRepository;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.service.AttendanceService;
import com.hrm.backend.service.ContractService;
import com.hrm.backend.service.EmailService;
import com.hrm.backend.service.LeaveRequestService;
import com.hrm.backend.service.TaxAndInsuranceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTest {

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContractService contractService;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private LeaveRequestService leaveRequestService;

    @Mock
    private TaxAndInsuranceService taxAndInsuranceService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EmailService emailService;

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private Employee employee;
    private Employee approver;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1)
                .code("EMP0001")
                .name("Nguyen Van A")
                .email("employee@example.com")
                .dependentCount(0)
                .build();
        approver = Employee.builder()
                .id(2)
                .code("ADM0001")
                .name("Admin User")
                .email("admin@example.com")
                .build();
    }

    @Test
    @DisplayName("Unit generatePayroll - Active employee should get DRAFT payroll with tax and insurance deductions")
    void generatePayroll_ActiveEmployee_CreatesDraftPayroll() {
        when(employeeRepository.findByStatus("ACTIVE")).thenReturn(List.of(employee));
        when(payrollRepository.existsByEmployeeIdAndMonth(1, "2026-06")).thenReturn(false);
        when(contractService.findActiveContract(1)).thenReturn(Optional.of(activeContract()));
        when(attendanceService.getMonthlyStats(1, 6, 2026)).thenReturn(monthlyStats(22, new BigDecimal("2.0")));
        when(leaveRequestService.getPaidLeaveDaysInMonth(1, 6, 2026)).thenReturn(new BigDecimal("1.0"));
        when(holidayRepository.findByDateBetween(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of());
        when(attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(
                1, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of(attendanceWithOvertime(new BigDecimal("2.0"))));
        when(taxAndInsuranceService.calculateInsurance(new BigDecimal("22000000")))
                .thenReturn(new BigDecimal("2310000"));
        when(taxAndInsuranceService.calculatePIT(any(), any(), any(Integer.class)))
                .thenReturn(new BigDecimal("500000"));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> {
            Payroll payroll = invocation.getArgument(0);
            payroll.setId(1);
            return payroll;
        });

        List<PayrollResponse> responses = payrollService.generatePayroll(
                6,
                2026,
                new BigDecimal("22"),
                Map.of("Meal", new BigDecimal("500000")),
                Map.of("Advance", new BigDecimal("100000"))
        );

        assertThat(responses).hasSize(1);
        PayrollResponse response = responses.get(0);
        assertThat(response.getStatus()).isEqualTo("DRAFT");
        assertThat(response.getActualDays()).isEqualByComparingTo(new BigDecimal("23"));
        assertThat(response.getDeductions()).containsEntry("Advance", new BigDecimal("100000"));
        assertThat(response.getDeductions()).containsEntry("BHXH_BHYT_BHTN (10.5%)", new BigDecimal("2310000"));
        assertThat(response.getDeductions()).containsEntry("Thuế TNCN (PIT)", new BigDecimal("500000"));

        ArgumentCaptor<Payroll> captor = ArgumentCaptor.forClass(Payroll.class);
        verify(payrollRepository).save(captor.capture());
        assertThat(captor.getValue().getMonth()).isEqualTo("2026-06");
        assertThat(captor.getValue().getGrossSalary()).isGreaterThan(new BigDecimal("22000000"));
        assertThat(captor.getValue().getNetSalary()).isEqualByComparingTo(
                captor.getValue().getGrossSalary().subtract(captor.getValue().getTotalDeductions()));
    }

    @Test
    @DisplayName("Unit generatePayroll - Existing payroll should skip employee")
    void generatePayroll_ExistingPayroll_SkipsEmployee() {
        when(employeeRepository.findByStatus("ACTIVE")).thenReturn(List.of(employee));
        when(payrollRepository.existsByEmployeeIdAndMonth(1, "2026-06")).thenReturn(true);
        when(holidayRepository.findByDateBetween(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of());

        List<PayrollResponse> responses = payrollService.generatePayroll(6, 2026, new BigDecimal("22"), null, null);

        assertThat(responses).isEmpty();
        verify(contractService, never()).findActiveContract(any());
        verify(payrollRepository, never()).save(any(Payroll.class));
    }

    @Test
    @DisplayName("Unit generatePayroll - Employee without active contract should be skipped")
    void generatePayroll_NoActiveContract_SkipsEmployee() {
        when(employeeRepository.findByStatus("ACTIVE")).thenReturn(List.of(employee));
        when(payrollRepository.existsByEmployeeIdAndMonth(1, "2026-06")).thenReturn(false);
        when(contractService.findActiveContract(1)).thenReturn(Optional.empty());
        when(holidayRepository.findByDateBetween(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of());

        List<PayrollResponse> responses = payrollService.generatePayroll(6, 2026, new BigDecimal("22"), null, null);

        assertThat(responses).isEmpty();
        verify(payrollRepository, never()).save(any(Payroll.class));
    }

    @Test
    @DisplayName("Unit updatePayroll - Draft payroll should merge request values and recalculate gross/net")
    void updatePayroll_DraftPayroll_RecalculatesSalary() {
        Payroll payroll = draftPayroll();
        when(payrollRepository.findById(1)).thenReturn(Optional.of(payroll));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollUpdateRequest request = new PayrollUpdateRequest(
                new BigDecimal("22"),
                new BigDecimal("20"),
                Map.of("Meal", new BigDecimal("500000")),
                Map.of("Advance", new BigDecimal("100000")),
                new BigDecimal("300000")
        );

        PayrollResponse response = payrollService.updatePayroll(1, request);

        assertThat(response.getGrossSalary()).isEqualByComparingTo(new BigDecimal("20800000.00"));
        assertThat(response.getNetSalary()).isEqualByComparingTo(new BigDecimal("20700000.00"));
        assertThat(response.getAllowances()).containsEntry("Meal", new BigDecimal("500000"));
        assertThat(response.getDeductions()).containsEntry("Advance", new BigDecimal("100000"));
    }

    @Test
    @DisplayName("Unit updatePayroll - Non-draft payroll should throw IllegalArgumentException")
    void updatePayroll_NonDraftPayroll_ThrowsException() {
        Payroll payroll = draftPayroll();
        payroll.setStatus("CALCULATED");
        when(payrollRepository.findById(1)).thenReturn(Optional.of(payroll));

        assertThatThrownBy(() -> payrollService.updatePayroll(1, new PayrollUpdateRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DRAFT");

        verify(payrollRepository, never()).save(any(Payroll.class));
    }

    @Test
    @DisplayName("Unit bulkUpdatePayroll - Draft payroll should merge allowances and deductions")
    void bulkUpdatePayroll_DraftPayroll_MergesAllowancesAndDeductions() {
        Payroll payroll = draftPayroll();
        payroll.setAllowances("{\"Meal\":500000}");
        payroll.setTotalAllowances(new BigDecimal("500000"));
        payroll.setDeductions("{\"Advance\":100000}");
        payroll.setTotalDeductions(new BigDecimal("100000"));
        when(payrollRepository.findById(1)).thenReturn(Optional.of(payroll));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollUpdateRequest.BulkUpdateRequest request = new PayrollUpdateRequest.BulkUpdateRequest(
                List.of(1),
                Map.of("Phone", new BigDecimal("300000")),
                Map.of("Union", new BigDecimal("50000"))
        );

        List<PayrollResponse> responses = payrollService.bulkUpdatePayroll(request);

        assertThat(responses).hasSize(1);
        PayrollResponse response = responses.get(0);
        assertThat(response.getAllowances()).containsEntry("Meal", new BigDecimal("500000"));
        assertThat(response.getAllowances()).containsEntry("Phone", new BigDecimal("300000"));
        assertThat(response.getDeductions()).containsEntry("Advance", new BigDecimal("100000"));
        assertThat(response.getDeductions()).containsEntry("Union", new BigDecimal("50000"));
        assertThat(response.getGrossSalary()).isEqualByComparingTo(new BigDecimal("22800000.00"));
        assertThat(response.getNetSalary()).isEqualByComparingTo(new BigDecimal("22650000.00"));
    }

    @Test
    @DisplayName("Unit generatePayroll - Paid holiday without attendance should count as paid day")
    void generatePayroll_PaidHolidayWithoutAttendance_AddsPaidHolidayDay() {
        when(employeeRepository.findByStatus("ACTIVE")).thenReturn(List.of(employee));
        when(payrollRepository.existsByEmployeeIdAndMonth(1, "2026-06")).thenReturn(false);
        when(contractService.findActiveContract(1)).thenReturn(Optional.of(activeContract()));
        when(attendanceService.getMonthlyStats(1, 6, 2026)).thenReturn(monthlyStats(21, BigDecimal.ZERO));
        when(leaveRequestService.getPaidLeaveDaysInMonth(1, 6, 2026)).thenReturn(BigDecimal.ZERO);
        when(holidayRepository.findByDateBetween(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of(paidHoliday(LocalDate.of(2026, 6, 10))));
        when(attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(
                1, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of());
        when(taxAndInsuranceService.calculateInsurance(new BigDecimal("22000000"))).thenReturn(BigDecimal.ZERO);
        when(taxAndInsuranceService.calculatePIT(any(), any(), any(Integer.class))).thenReturn(BigDecimal.ZERO);
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<PayrollResponse> responses = payrollService.generatePayroll(6, 2026, new BigDecimal("22"), null, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getActualDays()).isEqualByComparingTo(new BigDecimal("22"));
        assertThat(responses.get(0).getGrossSalary()).isEqualByComparingTo(new BigDecimal("22000000.00"));
        assertThat(responses.get(0).getNetSalary()).isEqualByComparingTo(new BigDecimal("22000000.00"));
    }

    @Test
    @DisplayName("Unit generatePayroll - Working on paid holiday should calculate holiday work pay at x3.0")
    void generatePayroll_WorkingOnPaidHoliday_CalculatesTriplePay() {
        when(employeeRepository.findByStatus("ACTIVE")).thenReturn(List.of(employee));
        when(payrollRepository.existsByEmployeeIdAndMonth(1, "2026-06")).thenReturn(false);
        when(contractService.findActiveContract(1)).thenReturn(Optional.of(activeContract()));
        when(attendanceService.getMonthlyStats(1, 6, 2026)).thenReturn(monthlyStats(22, BigDecimal.ZERO));
        when(leaveRequestService.getPaidLeaveDaysInMonth(1, 6, 2026)).thenReturn(BigDecimal.ZERO);
        when(holidayRepository.findByDateBetween(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of(paidHoliday(LocalDate.of(2026, 6, 10))));
        when(attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(
                1, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of(attendanceOnHoliday(new BigDecimal("8.0"))));
        when(taxAndInsuranceService.calculateInsurance(new BigDecimal("22000000"))).thenReturn(BigDecimal.ZERO);
        when(taxAndInsuranceService.calculatePIT(any(), any(), any(Integer.class))).thenReturn(BigDecimal.ZERO);
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<PayrollResponse> responses = payrollService.generatePayroll(6, 2026, new BigDecimal("22"), null, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getActualDays()).isEqualByComparingTo(new BigDecimal("22"));
        assertThat(responses.get(0).getOvertimePay()).isEqualByComparingTo(new BigDecimal("3000000.00"));
        assertThat(responses.get(0).getGrossSalary()).isEqualByComparingTo(new BigDecimal("25000000.00"));
    }

    @Test
    @DisplayName("Unit submitPayroll - Draft payroll should become CALCULATED")
    void submitPayroll_DraftPayroll_ChangesStatus() {
        Payroll payroll = draftPayroll();
        when(payrollRepository.findById(1)).thenReturn(Optional.of(payroll));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollResponse response = payrollService.submitPayroll(1);

        assertThat(response.getStatus()).isEqualTo("CALCULATED");
    }

    @Test
    @DisplayName("Unit submitPayroll - Non-draft payroll should throw IllegalArgumentException")
    void submitPayroll_NonDraftPayroll_ThrowsException() {
        Payroll payroll = draftPayroll();
        payroll.setStatus("APPROVED");
        when(payrollRepository.findById(1)).thenReturn(Optional.of(payroll));

        assertThatThrownBy(() -> payrollService.submitPayroll(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DRAFT");

        verify(payrollRepository, never()).save(any(Payroll.class));
    }

    @Test
    @DisplayName("Unit approvePayroll - Calculated payroll should become APPROVED")
    void approvePayroll_CalculatedPayroll_ChangesStatusAndApprover() {
        Payroll payroll = draftPayroll();
        payroll.setStatus("CALCULATED");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder()
                .username("admin")
                .employee(approver)
                .build()));
        when(payrollRepository.findById(1)).thenReturn(Optional.of(payroll));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollResponse response = payrollService.approvePayroll("admin", 1);

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(response.getApprovedByName()).isEqualTo("Admin User");
        assertThat(response.getApprovedAt()).isNotNull();
    }

    @Test
    @DisplayName("Unit approvePayroll - Non-calculated payroll should throw IllegalArgumentException")
    void approvePayroll_NonCalculatedPayroll_ThrowsException() {
        Payroll payroll = draftPayroll();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder()
                .username("admin")
                .employee(approver)
                .build()));
        when(payrollRepository.findById(1)).thenReturn(Optional.of(payroll));

        assertThatThrownBy(() -> payrollService.approvePayroll("admin", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CALCULATED");

        verify(payrollRepository, never()).save(any(Payroll.class));
    }

    @Test
    @DisplayName("Unit markAsPaid - Approved payroll should become PAID")
    void markAsPaid_ApprovedPayroll_ChangesStatus() {
        Payroll payroll = draftPayroll();
        payroll.setStatus("APPROVED");
        when(payrollRepository.findById(1)).thenReturn(Optional.of(payroll));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollResponse response = payrollService.markAsPaid(1);

        assertThat(response.getStatus()).isEqualTo("PAID");
        assertThat(response.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("Unit markAsPaid - Non-approved payroll should throw IllegalArgumentException")
    void markAsPaid_NonApprovedPayroll_ThrowsException() {
        Payroll payroll = draftPayroll();
        payroll.setStatus("CALCULATED");
        when(payrollRepository.findById(1)).thenReturn(Optional.of(payroll));

        assertThatThrownBy(() -> payrollService.markAsPaid(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("APPROVED");

        verify(payrollRepository, never()).save(any(Payroll.class));
    }

    private ContractResponse activeContract() {
        return ContractResponse.builder()
                .id(1)
                .employeeId(1)
                .basicSalary(new BigDecimal("22000000"))
                .status("ACTIVE")
                .build();
    }

    private AttendanceResponse.MonthlyStats monthlyStats(long totalWorkDays, BigDecimal totalOvertimeHours) {
        return AttendanceResponse.MonthlyStats.builder()
                .employeeId(1)
                .employeeCode("EMP0001")
                .employeeName("Nguyen Van A")
                .month(6)
                .year(2026)
                .totalWorkDays(totalWorkDays)
                .lateCount(0)
                .totalOvertimeHours(totalOvertimeHours)
                .build();
    }

    private AttendanceRecord attendanceWithOvertime(BigDecimal overtimeHours) {
        return AttendanceRecord.builder()
                .id(1)
                .employee(employee)
                .date(LocalDate.of(2026, 6, 2))
                .status("ON_TIME")
                .workHours(new BigDecimal("10.0"))
                .overtimeHours(overtimeHours)
                .build();
    }

    private AttendanceRecord attendanceOnHoliday(BigDecimal workHours) {
        return AttendanceRecord.builder()
                .id(2)
                .employee(employee)
                .date(LocalDate.of(2026, 6, 10))
                .status("ON_TIME")
                .workHours(workHours)
                .overtimeHours(BigDecimal.ZERO)
                .build();
    }

    private Holiday paidHoliday(LocalDate date) {
        return Holiday.builder()
                .id(1)
                .name("Paid Holiday")
                .date(date)
                .isPaid(true)
                .build();
    }

    private Payroll draftPayroll() {
        return Payroll.builder()
                .id(1)
                .employee(employee)
                .month("2026-06")
                .basicSalary(new BigDecimal("22000000"))
                .workDays(new BigDecimal("22"))
                .actualDays(new BigDecimal("22"))
                .allowances(null)
                .totalAllowances(BigDecimal.ZERO)
                .overtimePay(BigDecimal.ZERO)
                .grossSalary(new BigDecimal("22000000"))
                .deductions(null)
                .totalDeductions(BigDecimal.ZERO)
                .netSalary(new BigDecimal("22000000"))
                .status("DRAFT")
                .build();
    }
}
