# 🍃 Spring Boot Backend - Human Resource Management (HRM) Engine

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Security](https://img.shields.io/badge/Spring%20Security-6.x-blue.svg?style=for-the-badge&logo=springsecurity)](https://spring.io/projects/spring-security)
[![Database](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![Migration](https://img.shields.io/badge/Flyway-Enabled-red?style=for-the-badge&logo=flyway)](https://flywaydb.org/)
[![API Doc](https://img.shields.io/badge/OpenAPI-Swagger--UI-lightgreen?style=for-the-badge&logo=swagger)](https://swagger.io/)

Tài liệu kỹ thuật chuyên sâu dành riêng cho mã nguồn **Spring Boot Backend** (`HRM_backend`). Hệ thống cung cấp toàn bộ RESTful APIs phục vụ quản trị nhân sự, chấm công nâng cao (GPS/IP verification), cấu hình ca làm việc, tự động hóa đơn xin nghỉ phép và động cơ tính lương lũy tiến theo quy chuẩn luật lao động Việt Nam.

---

## 🏗️ Kiến trúc Hệ thống Backend (Clean Architecture Pattern)

Mã nguồn Backend tuân thủ nghiêm ngặt mô hình kiến trúc phân lớp (Layered Architecture) kết hợp với các nguyên lý thiết kế SOLID:

```mermaid
graph TD
    %% Styling
    classDef layer fill:#1e1e2e,stroke:#89b4fa,stroke-width:2px,color:#cdd6f4;
    classDef component fill:#313244,stroke:#a6e3a1,stroke-width:1.5px,color:#a6e3a1;
    classDef security fill:#313244,stroke:#f9e2af,stroke-width:1.5px,color:#f9e2af;
    classDef exception fill:#313244,stroke:#f38ba8,stroke-width:1.5px,color:#f38ba8;
    classDef db fill:#181825,stroke:#cba6f7,stroke-width:2px,color:#cba6f7;

    %% Presentation Layer
    subgraph Presentation ["Presentation Layer (Controllers)"]
        AC[AuthController]:::component
        DC[DepartmentController]:::component
        EC[EmployeeController]:::component
        PC[PayrollController]:::component
        GEH[GlobalExceptionHandler]:::exception
    end

    %% Security Layer
    subgraph Security ["Security & Middleware"]
        SF[JwtAuthenticationFilter]:::security
        SC[SecurityConfig]:::security
        UD[UserDetailsServiceImpl]:::security
    end

    %% Business Layer
    subgraph Business ["Business Logic Layer (Services)"]
        AS[AuthService]:::component
        DS[DepartmentService]:::component
        ES[EmployeeService]:::component
        PYS[PayrollService / TaxAndInsuranceService]:::component
    end

    %% Persistence Layer
    subgraph Persistence ["Persistence Layer (Repositories & Entities)"]
        RPS[JPA Repositories]:::component
        ENT[Database Entities]:::component
        FLY[Flyway Schema Migrations]:::component
    end

    %% DB
    Postgres[(PostgreSQL 16 Database)]:::db

    %% Interconnections
    Client([React FE / HTTP Request]) --> SF
    SF --> SC
    SC --> AC & DC & EC & PC
    
    AC --> AS
    DC --> DS
    EC --> ES
    PC --> PYS
    
    AS & DS & ES & PYS --> RPS
    RPS --> ENT
    ENT --> Postgres
    FLY -->|Auto Migration| Postgres
    GEH -->|Bắt lỗi toàn cục| Client
```

---

## 📊 Sơ đồ Quan hệ Thực thể Database (ER Diagram)

Dưới đây là thiết kế Schema cơ sở dữ liệu chi tiết của HRM được đồng bộ tự động thông qua **Flyway Migrations**:

```mermaid
erDiagram
    USERS {
        Integer id PK
        Integer employee_id FK
        String username UK
        String email UK
        String password_hash
        String role
        Boolean is_active
    }
    
    EMPLOYEES {
        Integer id PK
        String code UK
        String name
        String avatar
        String email UK
        String phone
        LocalDate birthday
        String address
        LocalDate join_date
        Integer department_id FK
        String status
        LocalDate resignation_date
        Integer dependent_count
    }

    DEPARTMENTS {
        Integer id PK
        String code UK
        String name
        String description
        Integer manager_id FK
        Integer parent_id FK
    }

    CONTRACTS {
        Integer id PK
        Integer employee_id FK
        String contract_type
        LocalDate start_date
        LocalDate end_date
        BigDecimal basic_salary
        String status
    }

    ATTENDANCE_RECORDS {
        Integer id PK
        Integer employee_id FK
        LocalDate date
        LocalTime check_in
        LocalTime check_out
        String status
        BigDecimal overtime_hours
        BigDecimal work_hours
        String note
        Integer late_minutes
        Integer early_leave_minutes
        String check_in_ip
        BigDecimal check_in_lat
        BigDecimal check_in_lng
        String check_out_ip
        BigDecimal check_out_lat
        BigDecimal check_out_lng
        Boolean check_in_gps_valid
        Boolean check_in_ip_valid
        Boolean check_out_gps_valid
        Boolean check_out_ip_valid
    }

    OVERTIME_REQUESTS {
        Integer id PK
        Integer employee_id FK
        LocalDate date
        LocalTime start_time
        LocalTime end_time
        BigDecimal hours
        String reason
        String status
        Integer approved_by FK
        LocalDateTime approved_at
        String rejection_reason
    }

    SHIFTS {
        Integer id PK
        String code UK
        String name
        LocalTime start_time
        LocalTime end_time
        LocalTime break_start_time
        LocalTime break_end_time
        Boolean is_default
        Boolean is_active
    }

    LEAVE_TYPES {
        Integer id PK
        String code UK
        String name
        Boolean is_paid
        String description
    }

    LEAVE_BALANCES {
        Integer id PK
        Integer employee_id FK
        Integer leave_type_id FK
        Integer year
        BigDecimal total_days
        BigDecimal used_days
        BigDecimal carry_over_days
    }

    LEAVE_REQUESTS {
        Integer id PK
        Integer employee_id FK
        Integer leave_type_id FK
        LocalDate start_date
        LocalDate end_date
        BigDecimal days
        String reason
        String attachment_url
        String status
        Integer approved_by FK
        LocalDateTime approved_at
        String rejection_reason
    }

    PAYROLL {
        Integer id PK
        Integer employee_id FK
        String month UK
        BigDecimal basic_salary
        BigDecimal work_days
        BigDecimal actual_days
        String allowances
        BigDecimal total_allowances
        BigDecimal overtime_pay
        BigDecimal gross_salary
        String deductions
        BigDecimal total_deductions
        BigDecimal net_salary
        String status
        Integer approved_by FK
        LocalDateTime approved_at
        LocalDateTime paid_at
    }

    HOLIDAYS {
        Integer id PK
        String name
        LocalDate date UK
        BigDecimal multiplier
    }

    EMPLOYEES ||--|| USERS : "has_user"
    DEPARTMENTS ||--o{ EMPLOYEES : "has_employees"
    EMPLOYEES ||--o{ DEPARTMENTS : "manages"
    DEPARTMENTS ||--o{ DEPARTMENTS : "parent_department"
    EMPLOYEES ||--o{ CONTRACTS : "signs_contracts"
    EMPLOYEES ||--o{ ATTENDANCE_RECORDS : "records_attendance"
    EMPLOYEES ||--o{ OVERTIME_REQUESTS : "requests_overtime"
    EMPLOYEES ||--o{ OVERTIME_REQUESTS : "approves_overtime"
    EMPLOYEES ||--o{ LEAVE_BALANCES : "has_leave_balances"
    LEAVE_TYPES ||--o{ LEAVE_BALANCES : "balances_type"
    EMPLOYEES ||--o{ LEAVE_REQUESTS : "requests_leaves"
    LEAVE_TYPES ||--o{ LEAVE_REQUESTS : "leave_request_type"
    EMPLOYEES ||--o{ LEAVE_REQUESTS : "approves_leaves"
    EMPLOYEES ||--o{ PAYROLL : "receives_payrolls"
    EMPLOYEES ||--o{ PAYROLL : "approves_payrolls"
```

---

## 🧪 Hệ thống Kiểm thử Toàn diện (Testing Architecture)

Mã nguồn backend áp dụng hai mô hình kiểm thử chính để bảo đảm tính an toàn tối đa cho dữ liệu doanh nghiệp và logic nghiệp vụ.

```
                    ┌───────────────────────────────┐
                    │       MÔ HÌNH KIỂM THỬ        │
                    └───────────────┬───────────────┘
                                    │
            ┌───────────────────────┴───────────────────────┐
            ▼                                               ▼
┌───────────────────────┐                       ┌───────────────────────┐
│   WHITE BOX TESTING   │                       │   BLACK BOX TESTING   │
│ (Unit & Mock Testing) │                       │  (Integration / API)  │
└───────────┬───────────┘                       └───────────┬───────────┘
            │                                               │
 ┌──────────┴──────────┐                         ┌──────────┴──────────┐
 ▼                     ▼                         ▼                     ▼
Thuật toán Thuế/BH     Tránh lặp phòng ban      MockMvc Standalone    Biên lỗi Validation
(Tax & Insurance)      (Cyclic Verification)    (API Endpoint Rules)  (400, 401, 200 states)
```

---

### 🔍 1. White Box Testing - Logic nội bộ & Thuật toán lõi

#### **Luồng Kiểm thử Thuật toán Thuế & BHXH bắt buộc Việt Nam (`TaxAndInsuranceServiceImplTest.java`)**

Nghiệp vụ tính toán bảo hiểm được quy định dựa trên mức lương cơ sở trần (Capped Basic Salary) và lũy tiến từng phần của Biểu thuế Thu nhập cá nhân (PIT) 7 bậc:

```mermaid
flowchart TD
    %% Styling
    classDef default fill:#1e1e2e,stroke:#cdd6f4,stroke-width:2px,color:#cdd6f4;
    classDef process fill:#313244,stroke:#89b4fa,stroke-width:2px,color:#89b4fa;
    classDef decision fill:#45475a,stroke:#f9e2af,stroke-width:2px,color:#f9e2af;
    classDef terminal fill:#181825,stroke:#a6e3a1,stroke-width:2px,color:#a6e3a1;

    A([Bắt đầu tính toán]) --> B{Basic Salary <= 0 hoặc Null?}:::decision
    B -- Đúng --> C[BHXH = 0]:::process
    B -- Sai --> D{Basic Salary > Trần 46.8M?}:::decision
    
    D -- Đúng --> E[BHXH = Trần 46.8M * 10.5% = 4.914.000]:::process
    D -- Sai --> F[BHXH = Basic Salary * 10.5%]:::process
    
    C & E & F --> G[Thu nhập tính thuế = Gross - BHXH - Giảm trừ bản thân 11M - Giảm trừ gia cảnh 4.4M * N]:::process
    
    G --> H{Thu nhập tính thuế <= 0?}:::decision
    H -- Đúng --> I[Thuế PIT = 0]:::process
    H -- Sai --> J{Phân vùng lũy tiến theo bậc thu nhập}:::decision
    
    J --> B1["Bậc 1 (<= 5M): 5%"]:::process
    J --> B2["Bậc 2 (5M - 10M): 10% - 250k"]:::process
    J --> B3["Bậc 3 (10M - 18M): 15% - 750k"]:::process
    J --> B4["Bậc 4 (18M - 32M): 20% - 1.65M"]:::process
    J --> B5["Bậc 5 (32M - 52M): 25% - 3.25M"]:::process
    J --> B6["Bậc 6 (52M - 80M): 30% - 5.85M"]:::process
    J --> B7["Bậc 7 (> 80M): 35% - 9.85M"]:::process
    
    B1 & B2 & B3 & B4 & B5 & B6 & B7 --> K[Tổng hợp lũy tiến từng bậc thu nhập]:::process
    K & I --> L([Trả về kết quả Thuế PIT & BHXH]):::terminal
```

#### **Luồng Kiểm thử Chống lặp vòng cây Phòng ban (`DepartmentServiceImplTest.java`)**

Sử dụng Mockito để cô lập và kiểm chứng cấu trúc cây phân cấp phòng ban, chống đệ quy lặp vòng làm tràn bộ nhớ (Stack Overflow):

```mermaid
flowchart TD
    classDef error fill:#181825,stroke:#f38ba8,stroke-width:2px,color:#f38ba8;
    classDef success fill:#181825,stroke:#a6e3a1,stroke-width:2px,color:#a6e3a1;

    A([Yêu cầu cập nhật Phòng ban A]) --> B{Lựa chọn Cha mới trùng ID của A?}
    B -- Đúng --> C[Ném IllegalArgumentException: Phòng ban cha không thể là chính nó]:::error
    B -- Sai --> D[Mockito giả lập tìm kiếm Cha mới trong DB]
    
    D --> E{Duyệt cây Cha: Phát hiện ID của A nằm trong nhánh tổ tiên của Cha?}
    E -- Đúng --> F[Ném IllegalArgumentException: Phòng ban cha không thể là con của nó]:::error
    E -- Sai --> G[Lưu cập nhật phòng ban thành công]:::success
```

---

### 📥 2. Black Box Testing - Kiểm thử biên cổng API

Black Box Testing tập trung kiểm tra chất lượng của API đăng nhập `/api/v1/auth/login` thông qua MockMvc Standalone kết hợp đăng ký `GlobalExceptionHandler` để đảm bảo định dạng lỗi trả về thống nhất:

```mermaid
flowchart LR
    classDef pipeline fill:#313244,stroke:#89b4fa,stroke-width:1.5px,color:#89b4fa;
    classDef action fill:#45475a,stroke:#f9e2af,stroke-width:1.5px,color:#f9e2af;
    classDef err fill:#181825,stroke:#f38ba8,stroke-width:2px,color:#f38ba8;
    classDef success fill:#181825,stroke:#a6e3a1,stroke-width:2px,color:#a6e3a1;

    Request([MockMvc Gửi HTTP POST /api/v1/auth/login]) --> ValCheck{Spring Validation: Mật khẩu rỗng?}:::pipeline
    
    ValCheck -- Đúng --> ExNotValid[Ném MethodArgumentNotValidException]:::action
    ExNotValid --> ExHandler[GlobalExceptionHandler biên dịch Map lỗi]:::action
    ExHandler --> Resp400[HTTP 400 Bad Request]:::err
    
    ValCheck -- Sai --> AuthMock{Giao AuthService giả lập xác thực}:::pipeline
    AuthMock -- Đăng nhập sai --> BadCred[Ném BadCredentialsException]:::action
    BadCred --> ExHandler2[GlobalExceptionHandler biên dịch thông điệp lỗi]:::action
    ExHandler2 --> Resp401[HTTP 401 Unauthorized]:::err
    
    AuthMock -- Thành công --> Resp200[HTTP 200 OK + Bearer Access Token]:::success
```

---

## ⚡ Các thông số & Kịch bản Test chi tiết (Test Specifications)

### **1. Phân vùng tương đương & Biên của Thuế & BHXH (White Box)**

Quy đổi: Mức lương cơ sở hiện hành của Việt Nam áp dụng trong hệ thống là **2.340.000 VND**.
*   **Trần BHXH bắt buộc:** 20 lần lương cơ sở = $20 \times 2.340.000 = 46.800.000\text{ VND}$.
*   **Mức đóng bảo hiểm xã hội bắt buộc:** 10.5% (8% BHXH, 1.5% BHYT, 1% BHTN).

| Mã Test | Mô tả Ca kiểm thử | Giá trị Đầu vào (Salary) | Phụ thuộc | Công thức Áp dụng | Đầu ra Kỳ vọng (Expected Output) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **WB-T-01** | Lương cơ bản Null | `null` | 0 | Không tính bảo hiểm | `0 VND` (Không lỗi hệ thống) |
| **WB-T-02** | Lương cơ bản âm | `-5,000,000 VND` | 0 | Giới hạn dưới | `0 VND` |
| **WB-T-03** | Lương dưới trần BHXH | `10,000,000 VND` | 0 | $10.000.000 \times 10.5\%$ | **`1,050,000 VND`** |
| **WB-T-04** | Lương chạm đúng trần | `46,800,000 VND` | 0 | $46.800.000 \times 10.5\%$ | **`4,914,000 VND`** |
| **WB-T-05** | Lương vượt trần BHXH | `60,000,000 VND` | 0 | Khống chế ở mức trần | **`4,914,000 VND`** (Capped) |
| **WB-T-06** | Thu nhập miễn thuế PIT | `10,000,000 VND` | 0 | Lương < Giảm trừ bản thân (11M)| **`0 VND`** |
| **WB-T-07** | Lương Chịu thuế Bậc 1 | Gross: `20,000,000 VND` | 1 | Chịu thuế: $20M - 11M(BT) - 4.4M(PT) - 2.1M(BH) = 2.5M \le 5M$ | **`125,000 VND`** ($2.5M \times 5\%$) |
| **WB-T-08** | Lương Chịu thuế Bậc 3 | Gross: `30,000,000 VND` | 0 | Chịu thuế: $30M - 11M - 3.15M = 15.85M$ (Bậc 3) | **`1,627,500 VND`** ($15.85M \times 15\% - 750k$) |
| **WB-T-09** | Lương Chịu thuế Bậc 7 | Gross: `120,000,000 VND` | 2 | Chịu thuế: $120M - 11M - 8.8M - 4.914M = 95.286M > 80M$ (Bậc 7) | **`23,500,100 VND`** ($95.286M \times 35\% - 9.85M$) |

---

### **2. Biên đầu vào & Phản hồi HTTP của API Login (Black Box)**

| Mã Test | Mô tả Kiểm thử | Payload REST Gửi đi | HTTP Code | Cấu trúc JSON Phản hồi (Expected Response) |
| :--- | :--- | :--- | :--- | :--- |
| **BB-T-01** | Thông tin đăng nhập hợp lệ | `{"username": "admin", "password": "password123"}` | **200 OK** | `{"success": true, "message": "Đăng nhập thành công", "data": {"accessToken": "mock-jwt...", "role": "ADMIN"}}` |
| **BB-T-02** | Mật khẩu để trống (Biên Validation) | `{"username": "admin", "password": ""}` | **400 Bad Request** | `{"success": false, "message": "Dữ liệu đầu vào không hợp lệ", "data": {"password": "Mật khẩu không được để trống"}}` |
| **BB-T-03** | Sai mật khẩu (Bad Credentials) | `{"username": "admin", "password": "wrong_password"}` | **401 Unauthorized**| `{"success": false, "message": "Tên đăng nhập hoặc mật khẩu không đúng"}` |

---

## 🛠️ Hướng dẫn Setup & Khởi chạy Backend

### **1. Biến môi trường cấu hình (Environment Variables)**

Backend có thể cấu hình linh hoạt qua tệp [application.yml](file:///c:/Users/Lenovo/Desktop/HRM/HRM_backend/src/main/resources/application.yml) hoặc thiết lập các biến môi trường trực tiếp trên OS hoặc Docker:

```properties
DB_HOST=localhost         # Host kết nối PostgreSQL database
DB_PORT=5432              # Cổng kết nối PostgreSQL
DB_NAME=HRM               # Tên Database tạo sẵn
DB_USERNAME=postgres      # Tài khoản Database
DB_PASSWORD=123456        # Mật khẩu Database
SMTP_USERNAME=your_gmail  # Tài khoản SMTP gửi mail
SMTP_PASSWORD=your_app_pw # Mật khẩu ứng dụng SMTP Gmail
```

### **2. Chạy thủ công trên máy thật**

Yêu cầu máy cài đặt **Java 21** và **Maven 3.9+**.

1. Cài đặt thư viện và biên dịch mã nguồn:
   ```bash
   mvn clean install
   ```
2. Khởi động ứng dụng Spring Boot Backend:
   ```bash
   mvn spring-boot:run
   ```
3. Truy cập tài liệu mô tả RESTful APIs thông qua Swagger UI:
   [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

### **3. Thực thi Bộ Kiểm thử tự động (Running Tests)**

Thực thi kịch bản kiểm thử toàn diện (White Box & Black Box) để đảm bảo chất lượng vận hành:
```bash
mvn test
```

Màn hình hiển thị kết quả kiểm thử hoàn thành thành công:
```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.hrm.backend.service.impl.TaxAndInsuranceServiceImplTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.112 s
[INFO] Running com.hrm.backend.service.impl.DepartmentServiceImplTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.720 s
[INFO] Running com.hrm.backend.controller.AuthControllerBlackBoxTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.050 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---
**Lead Backend Architect** - *Tran Si Cuong (TranSiCuongcn1)*
