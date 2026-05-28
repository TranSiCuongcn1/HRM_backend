package com.hrm.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.backend.entity.AttendanceRecord;
import com.hrm.backend.entity.Contract;
import com.hrm.backend.entity.Department;
import com.hrm.backend.entity.Employee;
import com.hrm.backend.entity.LeaveBalance;
import com.hrm.backend.entity.LeaveType;
import com.hrm.backend.entity.Shift;
import com.hrm.backend.entity.User;
import com.hrm.backend.repository.AttendanceRepository;
import com.hrm.backend.repository.ContractRepository;
import com.hrm.backend.repository.DepartmentRepository;
import com.hrm.backend.repository.EmployeeRepository;
import com.hrm.backend.repository.LeaveBalanceRepository;
import com.hrm.backend.repository.LeaveTypeRepository;
import com.hrm.backend.repository.ShiftRepository;
import com.hrm.backend.repository.UserRepository;
import com.hrm.backend.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HrmEndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @MockitoBean
    private EmailService emailService;

    private Employee employee;
    private LeaveType annualLeave;

    @BeforeEach
    void setUp() {
        Department department = departmentRepository.findByCode("IT")
                .orElseGet(() -> departmentRepository.save(Department.builder()
                        .code("IT-E2E")
                        .name("Integration Department")
                        .build()));

        employee = employeeRepository.findByCode("E2E001").orElseGet(() -> employeeRepository.save(Employee.builder()
                .code("E2E001")
                .name("Integration Employee")
                .email("integration.employee@hrm.test")
                .joinDate(LocalDate.of(2026, 1, 1))
                .department(department)
                .status("ACTIVE")
                .dependentCount(0)
                .build()));

        userRepository.findByUsername("e2e.employee").orElseGet(() -> userRepository.save(User.builder()
                .employee(employee)
                .username("e2e.employee")
                .email("integration.employee.user@hrm.test")
                .passwordHash(passwordEncoder.encode("employee123"))
                .role("EMPLOYEE")
                .isActive(true)
                .build()));

        annualLeave = leaveTypeRepository.findAll().stream()
                .filter(type -> "ANNUAL".equals(type.getCode()))
                .findFirst()
                .orElseThrow();

        if (leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employee.getId(), annualLeave.getId(), 2026).isEmpty()) {
            leaveBalanceRepository.save(LeaveBalance.builder()
                    .employee(employee)
                    .leaveType(annualLeave)
                    .year(2026)
                    .totalDays(new BigDecimal("12.0"))
                    .usedDays(BigDecimal.ZERO)
                    .carryOverDays(BigDecimal.ZERO)
                    .build());
        }

        if (contractRepository.findByEmployeeIdAndStatus(employee.getId(), "ACTIVE").isEmpty()) {
            contractRepository.save(Contract.builder()
                    .employee(employee)
                    .contractType("DEFINITE_1YR")
                    .startDate(LocalDate.of(2026, 1, 1))
                    .endDate(LocalDate.of(2026, 12, 31))
                    .basicSalary(new BigDecimal("22000000"))
                    .status("ACTIVE")
                    .build());
        }

        if (shiftRepository.findByIsDefaultTrue().isEmpty()) {
            shiftRepository.save(Shift.builder()
                    .code("E2E_STD")
                    .name("E2E Standard Shift")
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(17, 0))
                    .breakStartTime(LocalTime.of(12, 0))
                    .breakEndTime(LocalTime.of(13, 0))
                    .isDefault(true)
                    .isActive(true)
                    .build());
        }
    }

    @Test
    @DisplayName("E2E - Employee attendance, leave, overtime and payroll flow should pass through security, services and DB")
    void employeeMonthlyHrmFlow_WithRealSecurityAndDatabase_Succeeds() throws Exception {
        LocalDate today = LocalDate.of(2026, 5, 28);
        String adminToken = loginAndGetToken("admin", "admin123");
        String employeeToken = loginAndGetToken("e2e.employee", "employee123");

        mockMvc.perform(get("/api/v1/payrolls")
                        .header("Authorization", bearer(employeeToken))
                        .param("month", "2026-05"))
                .andExpect(status().isForbidden());

        String checkInJson = mockMvc.perform(post("/api/v1/attendance/check-in")
                        .header("Authorization", bearer(employeeToken))
                        .header("X-Forwarded-For", "127.0.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "latitude", new BigDecimal("10.848031"),
                                "longitude", new BigDecimal("106.784944")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        int attendanceId = objectMapper.readTree(checkInJson).path("data").path("id").asInt();

        mockMvc.perform(post("/api/v1/attendance/check-out")
                        .header("Authorization", bearer(employeeToken))
                        .header("X-Forwarded-For", "127.0.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "latitude", new BigDecimal("10.848031"),
                                "longitude", new BigDecimal("106.784944")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String overtimeJson = mockMvc.perform(post("/api/v1/overtime-requests")
                        .header("Authorization", bearer(employeeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "date", today.toString(),
                                "startTime", "17:00:00",
                                "endTime", "19:00:00",
                                "reason", "Release support"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        int overtimeId = objectMapper.readTree(overtimeJson).path("data").path("id").asInt();

        mockMvc.perform(put("/api/v1/overtime-requests/{id}/approve", overtimeId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(put("/api/v1/attendance/{id}", attendanceId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "employeeId", employee.getId(),
                                "date", today.toString(),
                                "checkIn", "08:00:00",
                                "checkOut", "18:00:00",
                                "status", "ON_TIME",
                                "note", "E2E normalized attendance"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workHours").value(9.0))
                .andExpect(jsonPath("$.data.overtimeHours").value(1.0));

        seedAdditionalAttendanceDays(today);

        String leaveJson = mockMvc.perform(post("/api/v1/leave-requests")
                        .header("Authorization", bearer(employeeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "leaveTypeId", annualLeave.getId(),
                                "startDate", today.toString(),
                                "endDate", today.toString(),
                                "days", new BigDecimal("1.0"),
                                "reason", "Family matter"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        int leaveRequestId = objectMapper.readTree(leaveJson).path("data").path("id").asInt();

        mockMvc.perform(put("/api/v1/leave-requests/{id}/approve", leaveRequestId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employee.getId(), annualLeave.getId(), 2026)
                .orElseThrow();
        assertThat(balance.getUsedDays()).isEqualByComparingTo(new BigDecimal("1.0"));

        String payrollJson = mockMvc.perform(post("/api/v1/payrolls/generate")
                        .header("Authorization", bearer(adminToken))
                        .param("month", "5")
                        .param("year", "2026")
                        .param("workDays", "22"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode payroll = objectMapper.readTree(payrollJson).path("data").findValuesAsText("employeeCode").stream()
                .anyMatch("E2E001"::equals)
                ? payrollForEmployee(payrollJson, "E2E001")
                : objectMapper.createObjectNode();

        assertThat(payroll.path("employeeCode").asText()).isEqualTo("E2E001");
        assertThat(payroll.path("status").asText()).isEqualTo("DRAFT");
        assertThat(payroll.path("actualDays").decimalValue()).isEqualByComparingTo(new BigDecimal("22.0"));
        assertThat(payroll.path("overtimePay").decimalValue()).isGreaterThan(BigDecimal.ZERO);
        assertThat(payroll.path("deductions").has("BHXH_BHYT_BHTN (10.5%)")).isTrue();

        int payrollId = payroll.path("id").asInt();
        mockMvc.perform(put("/api/v1/payrolls/{id}/submit", payrollId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));

        mockMvc.perform(put("/api/v1/payrolls/{id}/approve", payrollId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(put("/api/v1/payrolls/{id}/pay", payrollId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private JsonNode payrollForEmployee(String payrollJson, String employeeCode) throws Exception {
        for (JsonNode node : objectMapper.readTree(payrollJson).path("data")) {
            if (employeeCode.equals(node.path("employeeCode").asText())) {
                return node;
            }
        }
        return objectMapper.createObjectNode();
    }

    private void seedAdditionalAttendanceDays(LocalDate excludedDate) {
        List<LocalDate> dates = LocalDate.of(2026, 5, 1)
                .datesUntil(LocalDate.of(2026, 6, 1))
                .filter(date -> !date.equals(excludedDate))
                .filter(date -> date.getDayOfWeek().getValue() <= 5)
                .sorted(Comparator.naturalOrder())
                .limit(21)
                .toList();

        for (LocalDate date : dates) {
            if (attendanceRepository.findByEmployeeIdAndDate(employee.getId(), date).isPresent()) {
                continue;
            }
            attendanceRepository.save(AttendanceRecord.builder()
                    .employee(employee)
                    .date(date)
                    .checkIn(LocalTime.of(8, 0))
                    .checkOut(LocalTime.of(17, 0))
                    .status("ON_TIME")
                    .workHours(new BigDecimal("8.00"))
                    .overtimeHours(BigDecimal.ZERO)
                    .lateMinutes(0)
                    .earlyLeaveMinutes(0)
                    .build());
        }
    }
}
