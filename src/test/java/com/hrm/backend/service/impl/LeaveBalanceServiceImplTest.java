package com.hrm.backend.service.impl;

import com.hrm.backend.dto.LeaveBalanceResponse;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.LeaveBalance;
import com.hrm.backend.entity.LeaveType;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.repository.LeaveBalanceRepository;
import com.hrm.backend.repository.LeaveTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveBalanceServiceImplTest {

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @InjectMocks
    private LeaveBalanceServiceImpl leaveBalanceService;

    private Employee employee;
    private LeaveType annual;
    private LeaveType sick;
    private LeaveType unpaid;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1)
                .code("EMP0001")
                .name("Nguyen Van A")
                .build();
        annual = leaveType(1, "ANNUAL", "Annual Leave");
        sick = leaveType(2, "SICK", "Sick Leave");
        unpaid = leaveType(3, "UNPAID", "Unpaid Leave");
    }

    @Test
    @DisplayName("Unit initBalanceForEmployee - Should create default balances for missing leave types")
    void initBalanceForEmployee_MissingTypes_CreatesDefaultBalances() {
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findAll()).thenReturn(List.of(annual, sick, unpaid));
        when(leaveBalanceRepository.existsByEmployeeIdAndLeaveTypeIdAndYear(1, 1, 2026)).thenReturn(false);
        when(leaveBalanceRepository.existsByEmployeeIdAndLeaveTypeIdAndYear(1, 2, 2026)).thenReturn(false);
        when(leaveBalanceRepository.existsByEmployeeIdAndLeaveTypeIdAndYear(1, 3, 2026)).thenReturn(false);
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leaveBalanceService.initBalanceForEmployee(1, 2026);

        ArgumentCaptor<LeaveBalance> captor = ArgumentCaptor.forClass(LeaveBalance.class);
        verify(leaveBalanceRepository, org.mockito.Mockito.times(3)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(balance -> balance.getLeaveType().getCode())
                .containsExactly("ANNUAL", "SICK", "UNPAID");
        assertThat(captor.getAllValues().get(0).getTotalDays()).isEqualByComparingTo(new BigDecimal("12.0"));
        assertThat(captor.getAllValues().get(1).getTotalDays()).isEqualByComparingTo(new BigDecimal("30.0"));
        assertThat(captor.getAllValues().get(2).getTotalDays()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Unit initBalanceForEmployee - Existing balance should be skipped")
    void initBalanceForEmployee_ExistingBalance_SkipsCreation() {
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findAll()).thenReturn(List.of(annual));
        when(leaveBalanceRepository.existsByEmployeeIdAndLeaveTypeIdAndYear(1, 1, 2026)).thenReturn(true);

        leaveBalanceService.initBalanceForEmployee(1, 2026);

        verify(leaveBalanceRepository, never()).save(any(LeaveBalance.class));
    }

    @Test
    @DisplayName("Unit getBalancesByEmployee - Existing employee should return balances with remaining days")
    void getBalancesByEmployee_ExistingEmployee_ReturnsRemainingDays() {
        when(employeeRepository.existsById(1)).thenReturn(true);
        when(leaveBalanceRepository.findByEmployeeIdAndYear(1, 2026))
                .thenReturn(List.of(balance(annual, new BigDecimal("12.0"), new BigDecimal("2.0"), new BigDecimal("1.0"))));

        List<LeaveBalanceResponse> responses = leaveBalanceService.getBalancesByEmployee(1, 2026);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).remainingDays()).isEqualByComparingTo(new BigDecimal("11.0"));
    }

    @Test
    @DisplayName("Unit updateBalance - Should update total and carry-over days")
    void updateBalance_ValidBalance_UpdatesTotals() {
        LeaveBalance balance = balance(annual, new BigDecimal("12.0"), BigDecimal.ZERO, BigDecimal.ZERO);
        when(leaveBalanceRepository.findById(1)).thenReturn(Optional.of(balance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveBalanceResponse response = leaveBalanceService.updateBalance(
                1,
                new BigDecimal("15.0"),
                new BigDecimal("2.0"));

        assertThat(response.totalDays()).isEqualByComparingTo(new BigDecimal("15.0"));
        assertThat(response.carryOverDays()).isEqualByComparingTo(new BigDecimal("2.0"));
        assertThat(response.remainingDays()).isEqualByComparingTo(new BigDecimal("17.0"));
    }

    @Test
    @DisplayName("Unit deductBalance - Should add used days")
    void deductBalance_ExistingBalance_AddsUsedDays() {
        LeaveBalance balance = balance(annual, new BigDecimal("12.0"), new BigDecimal("2.0"), BigDecimal.ZERO);
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1, 1, 2026))
                .thenReturn(Optional.of(balance));

        leaveBalanceService.deductBalance(1, 1, 2026, new BigDecimal("1.5"));

        assertThat(balance.getUsedDays()).isEqualByComparingTo(new BigDecimal("3.5"));
        verify(leaveBalanceRepository).save(balance);
    }

    @Test
    @DisplayName("Unit refundBalance - Should subtract used days")
    void refundBalance_ExistingBalance_SubtractsUsedDays() {
        LeaveBalance balance = balance(annual, new BigDecimal("12.0"), new BigDecimal("3.0"), BigDecimal.ZERO);
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1, 1, 2026))
                .thenReturn(Optional.of(balance));

        leaveBalanceService.refundBalance(1, 1, 2026, new BigDecimal("1.5"));

        assertThat(balance.getUsedDays()).isEqualByComparingTo(new BigDecimal("1.5"));
        verify(leaveBalanceRepository).save(balance);
    }

    @Test
    @DisplayName("Unit refundBalance - Used days should not go below zero")
    void refundBalance_MoreThanUsed_ClampsUsedDaysToZero() {
        LeaveBalance balance = balance(annual, new BigDecimal("12.0"), new BigDecimal("1.0"), BigDecimal.ZERO);
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1, 1, 2026))
                .thenReturn(Optional.of(balance));

        leaveBalanceService.refundBalance(1, 1, 2026, new BigDecimal("3.0"));

        assertThat(balance.getUsedDays()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(leaveBalanceRepository).save(balance);
    }

    @Test
    @DisplayName("Unit deductBalance - Missing balance should throw RuntimeException")
    void deductBalance_MissingBalance_ThrowsException() {
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1, 1, 2026))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveBalanceService.deductBalance(1, 1, 2026, BigDecimal.ONE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("2026");

        verify(leaveBalanceRepository, never()).save(any(LeaveBalance.class));
    }

    private LeaveBalance balance(LeaveType leaveType, BigDecimal totalDays, BigDecimal usedDays, BigDecimal carryOverDays) {
        return LeaveBalance.builder()
                .id(1)
                .employee(employee)
                .leaveType(leaveType)
                .year(2026)
                .totalDays(totalDays)
                .usedDays(usedDays)
                .carryOverDays(carryOverDays)
                .build();
    }

    private LeaveType leaveType(Integer id, String code, String name) {
        return LeaveType.builder()
                .id(id)
                .code(code)
                .name(name)
                .isPaid(!"UNPAID".equals(code))
                .build();
    }
}
