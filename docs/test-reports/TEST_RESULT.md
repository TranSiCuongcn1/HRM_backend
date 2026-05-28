# HRM Backend Test Execution Report

> **Thời gian báo cáo:** `2026-05-28 16:34:05`

## Thống Kê Tổng Quan

| Chỉ Số | Giá Trị |
| :--- | :--- |
| **Tổng số Test Cases** | `195` |
| **Thành công (Passed)** | `195` |
| **Thất bại (Failed)** | `0` |
| **Bỏ qua (Skipped)** | `0` |
| **Tỷ lệ thành công** | **100.0%** |
| **Tổng thời gian chạy** | `6.81 s` |

## Chi Tiết Kết Quả Kiểm Thử

| Trạng Thái | Bộ Kiểm Thử (Suite) | Tên Test Case (Method) | Thời Gian Chạy |
| :---: | :--- | :--- | :---: |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/daily - Admin should get company daily records | `30 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/employee/{employeeId} - Admin should get employee records | `54 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/my-records - Missing date parameter should return fallback 500 | `24 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/my-records - Should return current employee paged records | `26 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/range - Admin should get company records in date range | `30 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/stats/{employeeId} - Admin should get monthly stats | `2194 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box GET /api/v1/attendance/today - Should return current employee today record | `24 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box POST /api/v1/attendance/check-in - Business rule violation should return 400 Bad Request | `28 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box POST /api/v1/attendance/check-in - Should pass GPS and forwarded IP to service | `94 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box POST /api/v1/attendance/check-out - Should return updated attendance record | `16 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box POST /api/v1/attendance/mark-absent - Admin should mark employee absent | `19 ms` |
| SUCCESS | `AttendanceControllerBlackBoxTest` | Black-box PUT /api/v1/attendance/{id} - Admin should update attendance record | `27 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit adminUpdateRecord - Should recalculate work hours when check-in and check-out are provided | `2 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkIn - Duplicate check-in should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkIn - Holiday should not mark LATE even when after shift start | `3 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkIn - Invalid office IP should throw IllegalArgumentException | `1 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkIn - Late compared with default shift should mark LATE | `2 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkIn - Missing GPS should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkIn - Valid GPS/IP should create attendance record | `5 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkOut - Early checkout should mark EARLY_LEAVE | `329 ms` |
| SUCCESS | `AttendanceServiceImplTest` | Unit checkOut - Overtime should be capped by approved OT request | `2 ms` |
| SUCCESS | `AuthControllerBlackBoxTest` | Black-box POST /api/v1/auth/login - Bad credentials should return 401 Unauthorized | `14 ms` |
| SUCCESS | `AuthControllerBlackBoxTest` | Black-box POST /api/v1/auth/login - Empty password should return 400 Bad Request due to validation error | `47 ms` |
| SUCCESS | `AuthControllerBlackBoxTest` | Black-box POST /api/v1/auth/login - Valid credentials should return 200 OK and access token | `185 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box GET /api/v1/contracts/employee/{employeeId} - Returns employee contract history | `15 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box GET /api/v1/contracts/employee/{employeeId}/active - Returns active contract | `13 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box GET /api/v1/contracts/expiring - Returns contracts expiring in requested days | `16 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box GET /api/v1/contracts/{id} - Returns contract detail | `13 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box POST /api/v1/contracts - Business rule violation should return 400 Bad Request | `20 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box POST /api/v1/contracts - Missing required fields should return validation errors | `24 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box POST /api/v1/contracts - Non-positive salary should return validation error | `17 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box POST /api/v1/contracts - Valid payload should create DRAFT contract | `20 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box PUT /api/v1/contracts/{id} - Valid payload should update contract | `17 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box PUT /api/v1/contracts/{id}/activate - Should activate contract | `46 ms` |
| SUCCESS | `ContractControllerBlackBoxTest` | Black-box PUT /api/v1/contracts/{id}/terminate - Should terminate contract | `13 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit activateContract - Draft contract should become ACTIVE and expire old active contract | `3 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit activateContract - Non-draft contract should throw IllegalArgumentException | `1 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit activateContract - Overlapping old active contract should shorten its endDate | `2 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - Definite contract without end date should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - Exceeding 2 definite contracts should throw IllegalArgumentException | `1 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - INDEFINITE contract may omit end date | `63 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - Overlapping dates should throw IllegalArgumentException | `1 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - Probation > 180 days should throw IllegalArgumentException | `0 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - Resigned employee should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit createContract - Valid definite contract should create DRAFT contract | `2 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit findActiveContract - Active contract should return Optional response | `2 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit getContractsByEmployee - Existing employee should return contract history | `11 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit terminateContract - Active contract should become TERMINATED | `2 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit updateContract - Draft contract should update core fields | `2 ms` |
| SUCCESS | `ContractServiceImplTest` | Unit updateContract - Non-draft contract should throw IllegalArgumentException | `1 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box DELETE /api/v1/departments/{id} - Runtime business error currently maps to 500 fallback | `9 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box DELETE /api/v1/departments/{id} - Should call delete service | `18 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box GET /api/v1/departments - Returns departments list | `9 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box GET /api/v1/departments/{id} - Returns department detail | `12 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box POST /api/v1/departments - Duplicate code should return 400 Bad Request | `12 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box POST /api/v1/departments - Missing code and name should return validation errors | `45 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box POST /api/v1/departments - Valid payload should create department | `14 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box PUT /api/v1/departments/{id} - Cyclic parent should return 400 Bad Request | `13 ms` |
| SUCCESS | `DepartmentControllerBlackBoxTest` | Black-box PUT /api/v1/departments/{id} - Valid payload should update department | `14 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Create Department - Duplicate department code should throw IllegalArgumentException | `69 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Create Department - Valid request should successfully create department | `2 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Delete Department - Deleting department with active employees should throw RuntimeException | `2 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Delete Department - Deleting department with sub-departments should throw RuntimeException | `1 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Delete Department - Empty department should be successfully deleted | `2 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Update Department - Cyclic parent relationship should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `DepartmentServiceImplTest` | Update Department - Cyclical indirect parent relationship should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box DELETE /api/v1/employees/{id} - Business conflict currently maps to 500 fallback | `11 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box DELETE /api/v1/employees/{id} - Should call delete service | `11 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box GET /api/v1/employees - Returns paged employees in ApiResponse | `49 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box GET /api/v1/employees/{id} - Returns employee detail | `11 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box POST /api/v1/employees - Duplicate email should return 400 Bad Request | `14 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box POST /api/v1/employees - Invalid email should return validation errors | `14 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box POST /api/v1/employees - Missing required fields should return validation errors | `16 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box POST /api/v1/employees - Valid payload should create employee | `35 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box PUT /api/v1/employees/{id} - Valid payload should update employee | `14 ms` |
| SUCCESS | `EmployeeControllerBlackBoxTest` | Black-box PUT /api/v1/employees/{id}/resign - Should call resign service | `18 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box DELETE /api/v1/holidays/{id} - Runtime error currently maps to 500 fallback | `39 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box DELETE /api/v1/holidays/{id} - Should call delete service | `9 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box GET /api/v1/holidays - Returns holiday list | `10 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box POST /api/v1/holidays - Duplicate date runtime error currently maps to 500 fallback | `12 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box POST /api/v1/holidays - Valid payload should create holiday | `11 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box POST /api/v1/holidays/batch - Valid payload should create holiday batch | `17 ms` |
| SUCCESS | `HolidayControllerBlackBoxTest` | Black-box PUT /api/v1/holidays/{id} - Valid payload should update holiday | `11 ms` |
| SUCCESS | `HrmBackendApplicationTests` | contextLoads() | `7 ms` |
| SUCCESS | `HrmEndToEndIntegrationTest` | E2E - Employee attendance, leave, overtime and payroll flow should pass through security, services and DB | `1360 ms` |
| SUCCESS | `LeaveBalanceControllerBlackBoxTest` | Black-box GET /api/v1/leave-balances/employee/{employeeId} - Returns employee balances | `9 ms` |
| SUCCESS | `LeaveBalanceControllerBlackBoxTest` | Black-box GET /api/v1/leave-balances/employee/{employeeId} - Service runtime error currently maps to 500 fallback | `9 ms` |
| SUCCESS | `LeaveBalanceControllerBlackBoxTest` | Black-box GET /api/v1/leave-balances/my - Missing user currently maps to 500 fallback | `9 ms` |
| SUCCESS | `LeaveBalanceControllerBlackBoxTest` | Black-box GET /api/v1/leave-balances/my - Returns current employee balances | `48 ms` |
| SUCCESS | `LeaveBalanceControllerBlackBoxTest` | Black-box POST /api/v1/leave-balances/init - Should initialize balances | `9 ms` |
| SUCCESS | `LeaveBalanceControllerBlackBoxTest` | Black-box PUT /api/v1/leave-balances/{id} - Should update total and carry-over days | `9 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit deductBalance - Missing balance should throw RuntimeException | `2 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit deductBalance - Should add used days | `1 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit getBalancesByEmployee - Existing employee should return balances with remaining days | `2 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit initBalanceForEmployee - Existing balance should be skipped | `3 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit initBalanceForEmployee - Should create default balances for missing leave types | `4 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit refundBalance - Should subtract used days | `135 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit refundBalance - Used days should not go below zero | `1 ms` |
| SUCCESS | `LeaveBalanceServiceImplTest` | Unit updateBalance - Should update total and carry-over days | `2 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box GET /api/v1/leave-requests - Returns all requests with filters | `13 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box GET /api/v1/leave-requests/my - Returns current employee leave requests | `12 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box GET /api/v1/leave-requests/pending - Returns pending requests | `11 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box POST /api/v1/leave-requests - Business rule violation should return 400 Bad Request | `13 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box POST /api/v1/leave-requests - Missing required fields should return validation errors | `14 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box POST /api/v1/leave-requests - Non-positive days should return validation error | `47 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box POST /api/v1/leave-requests - Valid payload should submit leave request | `16 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box PUT /api/v1/leave-requests/{id}/approve - Should approve request | `11 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box PUT /api/v1/leave-requests/{id}/cancel - Should cancel own request | `11 ms` |
| SUCCESS | `LeaveRequestControllerBlackBoxTest` | Black-box PUT /api/v1/leave-requests/{id}/reject - Should reject request with reason | `10 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit approveRequest - Non-pending request should throw IllegalArgumentException | `70 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit approveRequest - Pending request should approve and deduct leave balance | `4 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit cancelRequest - Non-owner cannot cancel request | `3 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit cancelRequest - Owner can cancel pending request | `1 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit rejectRequest - Pending request should reject without deducting balance | `3 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - End date before start date should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - Insufficient balance should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - Leave request overlapping with holiday should exclude holiday days | `2 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - Overlapping request should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - Single-day holiday leave should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - UNPAID leave should skip balance check | `3 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - Valid annual leave should create PENDING request | `2 ms` |
| SUCCESS | `LeaveRequestServiceImplTest` | Unit submitRequest - Weekend single-day leave should throw IllegalArgumentException | `2 ms` |
| SUCCESS | `LeaveTypeControllerBlackBoxTest` | Black-box GET /api/v1/leave-types - Returns leave type list | `7 ms` |
| SUCCESS | `LeaveTypeControllerBlackBoxTest` | Black-box POST /api/v1/leave-types - Duplicate code should return 400 Bad Request | `10 ms` |
| SUCCESS | `LeaveTypeControllerBlackBoxTest` | Black-box POST /api/v1/leave-types - Missing code and name should return validation errors | `12 ms` |
| SUCCESS | `LeaveTypeControllerBlackBoxTest` | Black-box POST /api/v1/leave-types - Valid payload should create leave type | `10 ms` |
| SUCCESS | `LeaveTypeControllerBlackBoxTest` | Black-box PUT /api/v1/leave-types/{id} - Missing name should return validation error | `40 ms` |
| SUCCESS | `LeaveTypeControllerBlackBoxTest` | Black-box PUT /api/v1/leave-types/{id} - Valid payload should update leave type | `19 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box DELETE /api/v1/overtime-requests/{id} - Runtime exception should use fallback 500 | `11 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box DELETE /api/v1/overtime-requests/{id} - Should cancel own overtime request | `14 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box GET /api/v1/overtime-requests - Admin should get all overtime requests | `11 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box GET /api/v1/overtime-requests/my - Returns current employee overtime requests | `17 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box POST /api/v1/overtime-requests - Business rule violation should return 400 Bad Request | `12 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box POST /api/v1/overtime-requests - Valid payload should create overtime request | `54 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box PUT /api/v1/overtime-requests/{id}/approve - Should approve overtime request | `9 ms` |
| SUCCESS | `OvertimeRequestControllerBlackBoxTest` | Black-box PUT /api/v1/overtime-requests/{id}/reject - Should reject overtime request with reason | `9 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit approveRequest - Non-pending request should throw RuntimeException | `2 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit approveRequest - Pending request should become APPROVED | `2 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit cancelRequest - Non-owner cannot cancel request | `2 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit cancelRequest - Non-pending request cannot be cancelled | `2 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit cancelRequest - Owner can cancel pending request | `2 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit createRequest - End time before start time should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit createRequest - Valid time range should create PENDING request with calculated hours | `2 ms` |
| SUCCESS | `OvertimeRequestServiceImplTest` | Unit rejectRequest - Pending request should become REJECTED with reason | `3 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box GET /api/v1/payrolls - Admin should get monthly payroll page with filters | `12 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box GET /api/v1/payrolls/employee/{employeeId} - Admin should get any employee payrolls | `12 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box GET /api/v1/payrolls/employee/{employeeId} - Employee cannot view other employee payrolls | `8 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box GET /api/v1/payrolls/employee/{employeeId} - Employee should get own payrolls | `16 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box GET /api/v1/payrolls/{id} - Should get payroll detail | `8 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box POST /api/v1/payrolls/generate - Admin should generate monthly draft payrolls | `64 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box POST /api/v1/payrolls/generate - Business rule violation should return 400 Bad Request | `8 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box PUT /api/v1/payrolls/bulk-update - Admin should update multiple draft payrolls | `17 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box PUT /api/v1/payrolls/{id} - Admin should update draft payroll | `14 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box PUT /api/v1/payrolls/{id}/approve - Admin should approve payroll with username | `9 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box PUT /api/v1/payrolls/{id}/pay - Admin should mark payroll as paid | `9 ms` |
| SUCCESS | `PayrollControllerBlackBoxTest` | Black-box PUT /api/v1/payrolls/{id}/submit - Admin should submit payroll | `8 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit approvePayroll - Calculated payroll should become APPROVED | `5 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit approvePayroll - Non-calculated payroll should throw IllegalArgumentException | `3 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit bulkUpdatePayroll - Draft payroll should merge allowances and deductions | `36 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit generatePayroll - Active employee should get DRAFT payroll with tax and insurance deductions | `9 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit generatePayroll - Employee without active contract should be skipped | `4 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit generatePayroll - Existing payroll should skip employee | `5 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit generatePayroll - Paid holiday without attendance should count as paid day | `9 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit generatePayroll - Working on paid holiday should calculate holiday work pay at x3.0 | `6 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit markAsPaid - Approved payroll should become PAID | `5 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit markAsPaid - Non-approved payroll should throw IllegalArgumentException | `6 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit submitPayroll - Draft payroll should become CALCULATED | `303 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit submitPayroll - Non-draft payroll should throw IllegalArgumentException | `4 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit updatePayroll - Draft payroll should merge request values and recalculate gross/net | `9 ms` |
| SUCCESS | `PayrollServiceImplTest` | Unit updatePayroll - Non-draft payroll should throw IllegalArgumentException | `4 ms` |
| SUCCESS | `ShiftControllerBlackBoxTest` | Black-box DELETE /api/v1/shifts/{id} - Runtime error currently maps to 500 fallback | `36 ms` |
| SUCCESS | `ShiftControllerBlackBoxTest` | Black-box DELETE /api/v1/shifts/{id} - Should call delete service | `8 ms` |
| SUCCESS | `ShiftControllerBlackBoxTest` | Black-box GET /api/v1/shifts - Returns shift list | `8 ms` |
| SUCCESS | `ShiftControllerBlackBoxTest` | Black-box POST /api/v1/shifts - Duplicate code runtime error currently maps to 500 fallback | `9 ms` |
| SUCCESS | `ShiftControllerBlackBoxTest` | Black-box POST /api/v1/shifts - Valid payload should create shift | `9 ms` |
| SUCCESS | `ShiftControllerBlackBoxTest` | Black-box PUT /api/v1/shifts/{id} - Valid payload should update shift | `9 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Basic salary exactly at cap should calculate 10.5% of cap | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Basic salary over both BHXH/BHYT and BHTN caps should cap both | `0 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Basic salary over cap of BHXH/BHYT but under BHTN cap | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Negative basic salary should return zero | `3 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Normal basic salary under cap should calculate 10.5% exactly | `0 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Null basic salary should return zero | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate Insurance - Zero basic salary should return zero | `0 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 1: Taxable income <= 5M (5% tax) | `2 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 2: Taxable income between 5M and 10M (10% tax - 250k) | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 3: Taxable income between 10M and 18M (15% tax - 750k) | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 4: Taxable income between 18M and 32M (20% tax - 1.65M) | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 5: Taxable income between 32M and 52M (25% tax - 3.25M) | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 6: Taxable income between 52M and 80M (30% tax - 5.85M) | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Bracket 7: Taxable income > 80M (35% tax - 9.85M) | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Gross salary under total deductions should return zero tax | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Negative gross salary should return zero | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Null gross salary should return zero | `2 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Null insurance should be treated as zero | `1 ms` |
| SUCCESS | `TaxAndInsuranceServiceImplTest` | Calculate PIT - Progressive tax boundary values should stay in correct brackets | `2 ms` |
