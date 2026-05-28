# HRM Backend Test Execution Report

> **Thời gian báo cáo:** `2026-05-28 17:30:43`

## Thống Kê Tổng Quan

| Chỉ Số | Giá Trị |
| :--- | :--- |
| **Tổng số Test Cases** | `205` |
| **Thành công (Passed)** | `205` |
| **Thất bại (Failed)** | `0` |
| **Bỏ qua (Skipped)** | `0` |
| **Tỷ lệ thành công** | **100.0%** |
| **Tổng thời gian chạy** | `7.49 s` |

## Chi Tiết Kết Quả Kiểm Thử

| Trạng Thái | Bộ Kiểm Thử (Suite) | Tên Test Case (Method) | Thời Gian Chạy |
| :---: | :--- | :--- | :---: |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/daily - Admin should get company daily records | `31 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/employee/{employeeId} - Admin should get employee records | `64 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/my-records - Missing date parameter should return fallback 500 | `24 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/my-records - Should return current employee paged records | `23 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/range - Admin should get company records in date range | `34 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/stats/{employeeId} - Admin should get monthly stats | `2364 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/today - Should return current employee today record | `17 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box POST /api/v1/attendance/check-in - Business rule violation should return 400 Bad Request | `22 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box POST /api/v1/attendance/check-in - Should pass GPS and forwarded IP to service | `92 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box POST /api/v1/attendance/check-out - Should return updated attendance record | `16 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box POST /api/v1/attendance/mark-absent - Admin should mark employee absent | `21 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box PUT /api/v1/attendance/{id} - Admin should update attendance record | `28 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit adminUpdateRecord - Should recalculate work hours when check-in and check-out are provided | `2 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkIn - Duplicate check-in should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkIn - Holiday should not mark LATE even when after shift start | `3 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkIn - Invalid office IP should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkIn - Late compared with default shift should mark LATE | `2 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkIn - Missing GPS should throw IllegalArgumentException | `7 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkIn - Valid GPS/IP should create attendance record | `4 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkOut - Early checkout should mark EARLY_LEAVE | `340 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkOut - Overtime should be capped by approved OT request | `2 ms` |
| SUCCESS | `AttendanceStressTest` | Stress Test - Race Condition protection: Double check-in concurrently returns only 1 success | `18 ms` |
| SUCCESS | `AttendanceStressTest` | Stress Test - Simulating 200 employees check-in at peak start of shift concurrently | `108 ms` |
| SUCCESS | `AuthControllerBlackBoxTest` | Black-box POST /api/v1/auth/login - Bad credentials should return 401 Unauthorized | `19 ms` |
| SUCCESS | `AuthControllerBlackBoxTest` | Black-box POST /api/v1/auth/login - Empty password should return 400 Bad Request due to validation error | `57 ms` |
| SUCCESS | `AuthControllerBlackBoxTest` | Black-box POST /api/v1/auth/login - Valid credentials should return 200 OK and access token | `218 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box GET /api/v1/contracts/employee/{employeeId} - Returns employee contract history | `13 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box GET /api/v1/contracts/employee/{employeeId}/active - Returns active contract | `13 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box GET /api/v1/contracts/expiring - Returns contracts expiring in requested days | `14 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box GET /api/v1/contracts/{id} - Returns contract detail | `10 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box POST /api/v1/contracts - Business rule violation should return 400 Bad Request | `18 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box POST /api/v1/contracts - Missing required fields should return validation errors | `25 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box POST /api/v1/contracts - Non-positive salary should return validation error | `16 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box POST /api/v1/contracts - Valid payload should create DRAFT contract | `18 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box PUT /api/v1/contracts/{id} - Valid payload should update contract | `18 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box PUT /api/v1/contracts/{id}/activate - Should activate contract | `50 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box PUT /api/v1/contracts/{id}/terminate - Should terminate contract | `11 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit activateContract - Draft contract should become ACTIVE and expire old active contract | `2 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit activateContract - Non-draft contract should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit activateContract - Overlapping old active contract should shorten its endDate | `2 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - Definite contract without end date should throw IllegalArgumentException | `1 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - Exceeding 2 definite contracts should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - INDEFINITE contract may omit end date | `78 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - Overlapping dates should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - Probation > 180 days should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - Resigned employee should throw IllegalArgumentException | `1 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - Valid definite contract should create DRAFT contract | `3 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit findActiveContract - Active contract should return Optional response | `3 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit getContractsByEmployee - Existing employee should return contract history | `11 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit terminateContract - Active contract should become TERMINATED | `3 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit updateContract - Draft contract should update core fields | `2 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit updateContract - Non-draft contract should throw IllegalArgumentException | `0 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box DELETE /api/v1/departments/{id} - Runtime business error currently maps to 500 fallback | `10 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box DELETE /api/v1/departments/{id} - Should call delete service | `15 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box GET /api/v1/departments - Returns departments list | `12 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box GET /api/v1/departments/{id} - Returns department detail | `15 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box POST /api/v1/departments - Duplicate code should return 400 Bad Request | `12 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box POST /api/v1/departments - Missing code and name should return validation errors | `49 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box POST /api/v1/departments - Valid payload should create department | `13 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box PUT /api/v1/departments/{id} - Cyclic parent should return 400 Bad Request | `14 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box PUT /api/v1/departments/{id} - Valid payload should update department | `13 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Create Department - Duplicate department code should throw IllegalArgumentException | `88 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Create Department - Valid request should successfully create department | `4 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Delete Department - Deleting department with active employees should throw RuntimeException | `2 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Delete Department - Deleting department with sub-departments should throw RuntimeException | `2 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Delete Department - Empty department should be successfully deleted | `1 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Update Department - Cyclic parent relationship should throw IllegalArgumentException | `1 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Update Department - Cyclical indirect parent relationship should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box DELETE /api/v1/employees/{id} - Business conflict currently maps to 500 fallback | `8 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box DELETE /api/v1/employees/{id} - Should call delete service | `8 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box GET /api/v1/employees - Returns paged employees in ApiResponse | `49 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box GET /api/v1/employees/{id} - Returns employee detail | `11 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box POST /api/v1/employees - Duplicate email should return 400 Bad Request | `21 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box POST /api/v1/employees - Invalid email should return validation errors | `14 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box POST /api/v1/employees - Missing required fields should return validation errors | `13 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box POST /api/v1/employees - Valid payload should create employee | `38 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box PUT /api/v1/employees/{id} - Valid payload should update employee | `13 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box PUT /api/v1/employees/{id}/resign - Should call resign service | `12 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box DELETE /api/v1/holidays/{id} - Runtime error currently maps to 500 fallback | `36 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box DELETE /api/v1/holidays/{id} - Should call delete service | `9 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box GET /api/v1/holidays - Returns holiday list | `8 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box POST /api/v1/holidays - Duplicate date runtime error currently maps to 500 fallback | `11 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box POST /api/v1/holidays - Valid payload should create holiday | `9 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box POST /api/v1/holidays/batch - Valid payload should create holiday batch | `17 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box PUT /api/v1/holidays/{id} - Valid payload should update holiday | `10 ms` |
| SUCCESS | `HrmBackendApplicationTests` | contextLoads() | `8 ms` |
| SUCCESS | `HrmEndToEndIntegrationTest` | E2E - Employee attendance, leave, overtime and payroll flow should pass through security, services and DB | `1246 ms` |
| SUCCESS | `LeaveBalanceControllerBlackBoxTest` | Black-box GET /api/v1/leave-balances/employee/{employeeId} - Returns employee balances | `8 ms` |
| SUCCESS | `LeaveBalanceControllerBlackBoxTest` | Black-box GET /api/v1/leave-balances/employee/{employeeId} - Service runtime error currently maps to 500 fallback | `8 ms` |
| SUCCESS | `LeaveBalanceControllerBlackBoxTest` | Black-box GET /api/v1/leave-balances/my - Missing user currently maps to 500 fallback | `10 ms` |
| SUCCESS | `LeaveBalanceControllerBlackBoxTest` | Black-box GET /api/v1/leave-balances/my - Returns current employee balances | `42 ms` |
| SUCCESS | `LeaveBalanceControllerBlackBoxTest` | Black-box POST /api/v1/leave-balances/init - Should initialize balances | `9 ms` |
| SUCCESS | `LeaveBalanceControllerBlackBoxTest` | Black-box PUT /api/v1/leave-balances/{id} - Should update total and carry-over days | `9 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit deductBalance - Missing balance should throw RuntimeException | `3 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit deductBalance - Should add used days | `3 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit getBalancesByEmployee - Existing employee should return balances with remaining days | `4 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit initBalanceForEmployee - Existing balance should be skipped | `3 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit initBalanceForEmployee - Should create default balances for missing leave types | `8 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit refundBalance - Should subtract used days | `156 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit refundBalance - Used days should not go below zero | `2 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit updateBalance - Should update total and carry-over days | `5 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box GET /api/v1/leave-requests - Returns all requests with filters | `15 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box GET /api/v1/leave-requests/my - Returns current employee leave requests | `17 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box GET /api/v1/leave-requests/pending - Returns pending requests | `15 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box POST /api/v1/leave-requests - Business rule violation should return 400 Bad Request | `13 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box POST /api/v1/leave-requests - Missing required fields should return validation errors | `16 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box POST /api/v1/leave-requests - Non-positive days should return validation error | `49 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box POST /api/v1/leave-requests - Valid payload should submit leave request | `13 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box PUT /api/v1/leave-requests/{id}/approve - Should approve request | `11 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box PUT /api/v1/leave-requests/{id}/cancel - Should cancel own request | `12 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box PUT /api/v1/leave-requests/{id}/reject - Should reject request with reason | `9 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit approveRequest - Non-pending request should throw IllegalArgumentException | `84 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit approveRequest - Pending request should approve and deduct leave balance | `6 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit cancelRequest - Non-owner cannot cancel request | `2 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit cancelRequest - Owner can cancel pending request | `3 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit rejectRequest - Pending request should reject without deducting balance | `3 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - End date before start date should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - Insufficient balance should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - Leave request overlapping with holiday should exclude holiday days | `9 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - Overlapping request should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - Single-day holiday leave should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - UNPAID leave should skip balance check | `8 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - Valid annual leave should create PENDING request | `3 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - Weekend single-day leave should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `LeaveTypeControllerBlackBoxTest` | Black-box GET /api/v1/leave-types - Returns leave type list | `9 ms` |
| SUCCESS | `LeaveTypeControllerBlackBoxTest` | Black-box POST /api/v1/leave-types - Duplicate code should return 400 Bad Request | `11 ms` |
| SUCCESS | `LeaveTypeControllerBlackBoxTest` | Black-box POST /api/v1/leave-types - Missing code and name should return validation errors | `10 ms` |
| SUCCESS | `LeaveTypeControllerBlackBoxTest` | Black-box POST /api/v1/leave-types - Valid payload should create leave type | `12 ms` |
| SUCCESS | `LeaveTypeControllerBlackBoxTest` | Black-box PUT /api/v1/leave-types/{id} - Missing name should return validation error | `48 ms` |
| SUCCESS | `LeaveTypeControllerBlackBoxTest` | Black-box PUT /api/v1/leave-types/{id} - Valid payload should update leave type | `20 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box DELETE /api/v1/overtime-requests/{id} - Runtime exception should use fallback 500 | `10 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box DELETE /api/v1/overtime-requests/{id} - Should cancel own overtime request | `8 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box GET /api/v1/overtime-requests - Admin should get all overtime requests | `14 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box GET /api/v1/overtime-requests/my - Returns current employee overtime requests | `10 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box POST /api/v1/overtime-requests - Business rule violation should return 400 Bad Request | `10 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box POST /api/v1/overtime-requests - Valid payload should create overtime request | `53 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box PUT /api/v1/overtime-requests/{id}/approve - Should approve overtime request | `10 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box PUT /api/v1/overtime-requests/{id}/reject - Should reject overtime request with reason | `9 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit approveRequest - Non-pending request should throw RuntimeException | `3 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit approveRequest - Pending request should become APPROVED | `3 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit cancelRequest - Non-owner cannot cancel request | `3 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit cancelRequest - Non-pending request cannot be cancelled | `2 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit cancelRequest - Owner can cancel pending request | `3 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit createRequest - End time before start time should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit createRequest - Overlapping time range should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit createRequest - Valid time range should create PENDING request with calculated hours | `3 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit rejectRequest - Pending request should become REJECTED with reason | `3 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box GET /api/v1/payrolls - Admin should get monthly payroll page with filters | `11 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box GET /api/v1/payrolls/employee/{employeeId} - Admin should get any employee payrolls | `11 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box GET /api/v1/payrolls/employee/{employeeId} - Employee cannot view other employee payrolls | `9 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box GET /api/v1/payrolls/employee/{employeeId} - Employee should get own payrolls | `15 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box GET /api/v1/payrolls/{id} - Should get payroll detail | `9 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box POST /api/v1/payrolls/generate - Admin should generate monthly draft payrolls | `64 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box POST /api/v1/payrolls/generate - Business rule violation should return 400 Bad Request | `10 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box PUT /api/v1/payrolls/bulk-update - Admin should update multiple draft payrolls | `19 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box PUT /api/v1/payrolls/{id} - Admin should update draft payroll | `10 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box PUT /api/v1/payrolls/{id}/approve - Admin should approve payroll with username | `9 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box PUT /api/v1/payrolls/{id}/pay - Admin should mark payroll as paid | `10 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box PUT /api/v1/payrolls/{id}/submit - Admin should submit payroll | `9 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit approvePayroll - Calculated payroll should become APPROVED | `6 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit approvePayroll - Non-calculated payroll should throw IllegalArgumentException | `5 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit bulkUpdatePayroll - Draft payroll should merge allowances and deductions | `47 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit generatePayroll - Active employee should get DRAFT payroll with tax and insurance deductions | `12 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit generatePayroll - Employee without active contract should be skipped | `4 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit generatePayroll - Existing payroll should skip employee | `6 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit generatePayroll - Paid holiday without attendance should count as paid day | `10 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit generatePayroll - Working on paid holiday should calculate holiday work pay at x3.0 | `7 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit markAsPaid - Approved payroll should become PAID | `6 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit markAsPaid - Non-approved payroll should throw IllegalArgumentException | `6 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit submitPayroll - Draft payroll should become CALCULATED | `383 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit submitPayroll - Non-draft payroll should throw IllegalArgumentException | `4 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit updatePayroll - Draft payroll should merge request values and recalculate gross/net | `14 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit updatePayroll - Non-draft payroll should throw IllegalArgumentException | `5 ms` |
| SUCCESS | `SecurityPenTest` | Pen-Test JWT - Blank or malformed JWT string should return false | `12 ms` |
| SUCCESS | `SecurityPenTest` | Pen-Test JWT - Expired token should return false during validation | `9 ms` |
| SUCCESS | `SecurityPenTest` | Pen-Test JWT - Generate valid token and verify it passes validation | `159 ms` |
| SUCCESS | `SecurityPenTest` | Pen-Test JWT - Tampered token signature should return false during validation | `8 ms` |
| SUCCESS | `SecurityPenTest` | SQL Injection - Passing SQL payload in login username rejects cleanly with 401 Unauthorized | `10 ms` |
| SUCCESS | `SecurityPenTest` | SQL Injection - Passing SQL payload in search query is securely bound by JPA without error | `16 ms` |
| SUCCESS | `SecurityPenTest` | XSS Mitigation - Input containing HTML/JS script tags is bound safely as a literal string | `12 ms` |
| SUCCESS | `ShiftControllerBlackBoxTest` | Black-box DELETE /api/v1/shifts/{id} - Runtime error currently maps to 500 fallback | `38 ms` |
| SUCCESS | `ShiftControllerBlackBoxTest` | Black-box DELETE /api/v1/shifts/{id} - Should call delete service | `7 ms` |
| SUCCESS | `ShiftControllerBlackBoxTest` | Black-box GET /api/v1/shifts - Returns shift list | `6 ms` |
| SUCCESS | `ShiftControllerBlackBoxTest` | Black-box POST /api/v1/shifts - Duplicate code runtime error currently maps to 500 fallback | `8 ms` |
| SUCCESS | `ShiftControllerBlackBoxTest` | Black-box POST /api/v1/shifts - Valid payload should create shift | `8 ms` |
| SUCCESS | `ShiftControllerBlackBoxTest` | Black-box PUT /api/v1/shifts/{id} - Valid payload should update shift | `10 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Basic salary exactly at cap should calculate 10.5% of cap | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Basic salary over both BHXH/BHYT and BHTN caps should cap both | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Basic salary over cap of BHXH/BHYT but under BHTN cap | `0 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Negative basic salary should return zero | `2 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Normal basic salary under cap should calculate 10.5% exactly | `3 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Null basic salary should return zero | `0 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Zero basic salary should return zero | `0 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 1: Taxable income <= 5M (5% tax) | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 2: Taxable income between 5M and 10M (10% tax - 250k) | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 3: Taxable income between 10M and 18M (15% tax - 750k) | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 4: Taxable income between 18M and 32M (20% tax - 1.65M) | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 5: Taxable income between 32M and 52M (25% tax - 3.25M) | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 6: Taxable income between 52M and 80M (30% tax - 5.85M) | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 7: Taxable income > 80M (35% tax - 9.85M) | `3 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Gross salary under total deductions should return zero tax | `0 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Negative gross salary should return zero | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Null gross salary should return zero | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Null insurance should be treated as zero | `0 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Progressive tax boundary values should stay in correct brackets | `1 ms` |
