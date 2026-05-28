package com.hrm.backend.service.impl;

import com.hrm.backend.dto.ContractRequest;
import com.hrm.backend.dto.ContractResponse;
import com.hrm.backend.entity.Contract;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.repository.ContractRepository;
import com.hrm.backend.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractServiceImplTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ContractServiceImpl contractService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1)
                .code("EMP0001")
                .name("Nguyen Van A")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("Unit createContract - Valid definite contract should create DRAFT contract")
    void createContract_ValidRequest_CreatesDraftContract() {
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
            Contract contract = invocation.getArgument(0);
            contract.setId(1);
            return contract;
        });

        ContractResponse response = contractService.createContract(standardRequest());

        assertThat(response.getStatus()).isEqualTo("DRAFT");
        assertThat(response.getEmployeeCode()).isEqualTo("EMP0001");
        assertThat(response.getBasicSalary()).isEqualByComparingTo(new BigDecimal("22000000"));

        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("DRAFT");
        assertThat(captor.getValue().getEmployee()).isEqualTo(employee);
    }

    @Test
    @DisplayName("Unit createContract - Resigned employee should throw IllegalArgumentException")
    void createContract_ResignedEmployee_ThrowsException() {
        employee.setStatus("RESIGNED");
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> contractService.createContract(standardRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nghỉ việc");

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Unit createContract - Definite contract without end date should throw IllegalArgumentException")
    void createContract_DefiniteContractWithoutEndDate_ThrowsException() {
        ContractRequest request = standardRequest();
        request.setEndDate(null);
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> contractService.createContract(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endDate");

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Unit createContract - INDEFINITE contract may omit end date")
    void createContract_IndefiniteWithoutEndDate_CreatesDraftContract() {
        ContractRequest request = standardRequest();
        request.setContractType("INDEFINITE");
        request.setEndDate(null);
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContractResponse response = contractService.createContract(request);

        assertThat(response.getContractType()).isEqualTo("INDEFINITE");
        assertThat(response.getEndDate()).isNull();
        assertThat(response.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("Unit updateContract - Draft contract should update core fields")
    void updateContract_DraftContract_UpdatesFields() {
        Contract contract = draftContract();
        ContractRequest request = standardRequest();
        request.setBasicSalary(new BigDecimal("25000000"));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContractResponse response = contractService.updateContract(1, request);

        assertThat(response.getBasicSalary()).isEqualByComparingTo(new BigDecimal("25000000"));
        assertThat(response.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("Unit updateContract - Non-draft contract should throw IllegalArgumentException")
    void updateContract_NonDraftContract_ThrowsException() {
        Contract contract = draftContract();
        contract.setStatus("ACTIVE");
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> contractService.updateContract(1, standardRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DRAFT");

        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Unit activateContract - Draft contract should become ACTIVE and expire old active contract")
    void activateContract_DraftContract_ExpiresOldActiveContract() {
        Contract oldActive = activeContract(10);
        Contract draft = draftContract();
        when(contractRepository.findById(1)).thenReturn(Optional.of(draft));
        when(contractRepository.findByEmployeeIdAndStatus(1, "ACTIVE")).thenReturn(Optional.of(oldActive));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContractResponse response = contractService.activateContract(1);

        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(oldActive.getStatus()).isEqualTo("EXPIRED");
        verify(contractRepository).save(oldActive);
        verify(contractRepository).save(draft);
    }

    @Test
    @DisplayName("Unit activateContract - Non-draft contract should throw IllegalArgumentException")
    void activateContract_NonDraftContract_ThrowsException() {
        Contract contract = activeContract(1);
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> contractService.activateContract(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DRAFT");

        verify(contractRepository, never()).findByEmployeeIdAndStatus(any(), any());
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("Unit terminateContract - Active contract should become TERMINATED")
    void terminateContract_ActiveContract_ChangesStatus() {
        Contract contract = activeContract(1);
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContractResponse response = contractService.terminateContract(1);

        assertThat(response.getStatus()).isEqualTo("TERMINATED");
    }

    @Test
    @DisplayName("Unit getContractsByEmployee - Existing employee should return contract history")
    void getContractsByEmployee_ExistingEmployee_ReturnsHistory() {
        when(employeeRepository.existsById(1)).thenReturn(true);
        when(contractRepository.findByEmployeeIdOrderByStartDateDesc(1))
                .thenReturn(List.of(activeContract(2), draftContract()));

        List<ContractResponse> responses = contractService.getContractsByEmployee(1);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ContractResponse::getEmployeeCode).containsOnly("EMP0001");
    }

    @Test
    @DisplayName("Unit findActiveContract - Active contract should return Optional response")
    void findActiveContract_ActiveContract_ReturnsOptional() {
        when(contractRepository.findByEmployeeIdAndStatus(1, "ACTIVE")).thenReturn(Optional.of(activeContract(1)));

        Optional<ContractResponse> response = contractService.findActiveContract(1);

        assertThat(response).isPresent();
        assertThat(response.get().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Unit createContract - Overlapping dates should throw IllegalArgumentException")
    void createContract_OverlappingDates_ThrowsException() {
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        Contract active = activeContract(10);
        when(contractRepository.findByEmployeeIdOrderByStartDateDesc(1)).thenReturn(List.of(active));

        ContractRequest overlappingRequest = new ContractRequest(
                1,
                "DEFINITE_1YR",
                LocalDate.of(2025, 12, 1), // Trùng vào giữa hợp đồng active (2025-06-01 -> 2026-05-31)
                LocalDate.of(2026, 11, 30),
                new BigDecimal("22000000")
        );

        assertThatThrownBy(() -> contractService.createContract(overlappingRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("trùng lặp");
    }

    @Test
    @DisplayName("Unit createContract - Probation > 180 days should throw IllegalArgumentException")
    void createContract_ProbationOver180Days_ThrowsException() {
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

        ContractRequest longProbation = new ContractRequest(
                1,
                "PROBATION",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), // 364 ngày > 180
                new BigDecimal("15000000")
        );

        assertThatThrownBy(() -> contractService.createContract(longProbation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("thử việc tối đa");
    }

    @Test
    @DisplayName("Unit createContract - Exceeding 2 definite contracts should throw IllegalArgumentException")
    void createContract_DefiniteLimitExceeded_ThrowsException() {
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

        Contract past1 = activeContract(10);
        past1.setStatus("EXPIRED");
        Contract past2 = activeContract(11);
        past2.setStatus("EXPIRED");
        past2.setStartDate(LocalDate.of(2024, 6, 1));
        past2.setEndDate(LocalDate.of(2025, 5, 31));

        // Employee đã có 2 hợp đồng xác định thời hạn EXPIRED
        when(contractRepository.findByEmployeeIdOrderByStartDateDesc(1)).thenReturn(List.of(past1, past2));

        ContractRequest thirdDefinite = standardRequest();

        assertThatThrownBy(() -> contractService.createContract(thirdDefinite))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("không được ký quá 2 lần");
    }

    @Test
    @DisplayName("Unit activateContract - Overlapping old active contract should shorten its endDate")
    void activateContract_OverlappingOldActiveContract_ShortensEndDate() {
        Contract oldActive = activeContract(10); // 2025-06-01 -> 2026-05-31
        Contract draft = draftContract(); // 2026-06-01 -> 2027-05-31, but let's make it starts early, e.g. 2026-05-15
        draft.setStartDate(LocalDate.of(2026, 5, 15));

        when(contractRepository.findById(1)).thenReturn(Optional.of(draft));
        when(contractRepository.findByEmployeeIdAndStatus(1, "ACTIVE")).thenReturn(Optional.of(oldActive));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        contractService.activateContract(1);

        assertThat(oldActive.getStatus()).isEqualTo("EXPIRED");
        // endDate của oldActive phải bị rút ngắn thành 2026-05-14 (trước ngày 2026-05-15 một ngày)
        assertThat(oldActive.getEndDate()).isEqualTo(LocalDate.of(2026, 5, 14));
    }

    private ContractRequest standardRequest() {
        return new ContractRequest(
                1,
                "DEFINITE_1YR",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2027, 5, 31),
                new BigDecimal("22000000")
        );
    }

    private Contract draftContract() {
        return Contract.builder()
                .id(1)
                .employee(employee)
                .contractType("DEFINITE_1YR")
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2027, 5, 31))
                .basicSalary(new BigDecimal("22000000"))
                .status("DRAFT")
                .build();
    }

    private Contract activeContract(Integer id) {
        return Contract.builder()
                .id(id)
                .employee(employee)
                .contractType("DEFINITE_1YR")
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2026, 5, 31))
                .basicSalary(new BigDecimal("20000000"))
                .status("ACTIVE")
                .build();
    }
}
