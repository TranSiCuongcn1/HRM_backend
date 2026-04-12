// ==========================================
// 1. ĐỊNH NGHĨA CÁC BẢNG (TABLES)
// ==========================================

Table departments {
  id integer [pk, increment]
  code varchar(20) [unique, not null]
  name varchar(100) [not null]
  description text
  manager_id integer
  created_at timestamp [default: `now()`]
}

Table employees {
  id integer [pk, increment]
  department_id integer
  code varchar(20) [unique, not null]
  name varchar(100) [not null]
  avatar varchar(255)
  email varchar(100) [unique, not null]
  phone varchar(20)
  birthday date
  address text
  join_date date [not null]
  status varchar [default: 'ACTIVE', note: 'ACTIVE, INACTIVE, RESIGNED']
  resignation_date date
  created_at timestamp [default: `now()`]
  updated_at timestamp [default: `now()`]
}

Table contracts {
  id integer [pk, increment]
  employee_id integer [not null]
  contract_type varchar(50) [not null, note: 'PROBATION, DEFINITE_1YR, INDEFINITE']
  start_date date [not null]
  end_date date
  basic_salary decimal(12,2) [not null]
  status varchar(50) [default: 'ACTIVE', note: 'DRAFT, ACTIVE, EXPIRED, TERMINATED']
  created_at timestamp [default: `now()`]
  updated_at timestamp [default: `now()`]
}

Table users {
  id integer [pk, increment]
  employee_id integer [unique]
  username varchar(50) [unique, not null]
  email varchar(100) [unique, not null]
  password_hash varchar(255) [not null]
  role varchar [default: 'EMPLOYEE', note: 'ADMIN, EMPLOYEE']
  created_at timestamp [default: `now()`]
  updated_at timestamp [default: `now()`]
}

Table attendance_records {
  id integer [pk, increment]
  employee_id integer [not null]
  date date [not null]
  check_in time
  check_out time
  status varchar [note: 'ON_TIME, LATE, EARLY_LEAVE, ABSENT, HALF_DAY']
  overtime_hours decimal(4,2) [default: 0]
  work_hours decimal(4,2) [default: 0]
  note text
  created_at timestamp [default: `now()`]

  indexes {
    (employee_id, date) [unique]
  }
}

Table leave_types {
  id integer [pk, increment]
  code varchar(20) [unique, not null]
  name varchar(100) [not null]
  is_paid boolean [default: true]
  description text
}

Table leave_balances {
  id integer [pk, increment]
  employee_id integer [not null]
  leave_type_id integer [not null]
  year integer [not null]
  total_days decimal(4,1) [default: 0]
  used_days decimal(4,1) [default: 0]
  carry_over_days decimal(4,1) [default: 0]

  indexes {
    (employee_id, leave_type_id, year) [unique]
  }
}

Table leave_requests {
  id integer [pk, increment]
  employee_id integer [not null]
  leave_type_id integer [not null]
  start_date date [not null]
  end_date date [not null]
  days decimal(4,1) [not null]
  reason text [not null]
  attachment_url varchar(255)
  status varchar [default: 'PENDING', note: 'PENDING, APPROVED, REJECTED, CANCELLED']
  approved_by integer
  approved_at timestamp
  rejection_reason text
  created_at timestamp [default: `now()`]
  updated_at timestamp [default: `now()`]
}

Table payroll {
  id integer [pk, increment]
  employee_id integer [not null]
  month varchar(7) [not null, note: 'YYYY-MM']
  basic_salary decimal(12,2) [not null]
  allowances jsonb
  total_allowances decimal(12,2) [default: 0]
  overtime_pay decimal(12,2) [default: 0]
  gross_salary decimal(12,2) [not null]
  deductions jsonb
  total_deductions decimal(12,2) [default: 0]
  net_salary decimal(12,2) [not null]
  work_days decimal(4,1)
  actual_days decimal(4,1)
  status varchar [default: 'DRAFT', note: 'DRAFT, CALCULATED, APPROVED, PAID']
  approved_by integer
  approved_at timestamp
  paid_at timestamp
  created_at timestamp [default: `now()`]
  updated_at timestamp [default: `now()`]

  indexes {
    (employee_id, month) [unique]
  }
}

// ==========================================
// 2. THIẾT LẬP MỐI QUAN HỆ (RELATIONSHIPS)
// ==========================================

Ref: departments.manager_id > employees.id // Many-to-one (Một nhân viên quản lý nhiều phòng ban - thực tế thường là 1-1 nhưng để > cho linh hoạt)
Ref: employees.department_id > departments.id

Ref: contracts.employee_id > employees.id [delete: cascade]

Ref: users.employee_id - employees.id [delete: cascade] // Quan hệ 1-1

Ref: attendance_records.employee_id > employees.id [delete: cascade]

Ref: leave_balances.employee_id > employees.id [delete: cascade]
Ref: leave_balances.leave_type_id > leave_types.id

Ref: leave_requests.employee_id > employees.id [delete: cascade]
Ref: leave_requests.leave_type_id > leave_types.id
Ref: leave_requests.approved_by > employees.id [delete: set null]

Ref: payroll.employee_id > employees.id [delete: cascade]
Ref: payroll.approved_by > employees.id [delete: set null]