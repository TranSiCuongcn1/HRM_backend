-- ==========================================
-- 1. TẠO BẢNG (TABLES)
-- ==========================================

CREATE TABLE departments (
  id SERIAL PRIMARY KEY,
  code VARCHAR(50) UNIQUE NOT NULL,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  parent_id INTEGER,
  manager_id INTEGER,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employees (
  id SERIAL PRIMARY KEY,
  department_id INTEGER,
  code VARCHAR(20) UNIQUE NOT NULL,
  name VARCHAR(100) NOT NULL,
  avatar VARCHAR(255),
  email VARCHAR(100) UNIQUE NOT NULL,
  phone VARCHAR(20),
  birthday DATE,
  address TEXT,
  join_date DATE NOT NULL,
  status VARCHAR DEFAULT 'ACTIVE', -- Note: 'ACTIVE, INACTIVE, RESIGNED'
  resignation_date DATE,
  dependent_count INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE contracts (
  id SERIAL PRIMARY KEY,
  employee_id INTEGER NOT NULL,
  contract_type VARCHAR(50) NOT NULL, -- Note: 'PROBATION, DEFINITE_1YR, INDEFINITE'
  start_date DATE NOT NULL,
  end_date DATE, -- Null cho hợp đồng vô thời hạn
  basic_salary DECIMAL(12, 2) NOT NULL,
  status VARCHAR(50) DEFAULT 'ACTIVE', -- Note: 'DRAFT, ACTIVE, EXPIRED, TERMINATED'
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  employee_id INTEGER UNIQUE,
  username VARCHAR(50) UNIQUE NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR DEFAULT 'EMPLOYEE', -- Note: 'ADMIN, EMPLOYEE'
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE overtime_requests (
  id SERIAL PRIMARY KEY,
  employee_id INTEGER NOT NULL,
  date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  hours DECIMAL(4, 2) NOT NULL,
  reason TEXT NOT NULL,
  status VARCHAR(20) DEFAULT 'PENDING', -- Note: 'PENDING, APPROVED, REJECTED, CANCELLED'
  approved_by INTEGER,
  approved_at TIMESTAMP,
  rejection_reason TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_records (
  id SERIAL PRIMARY KEY,
  employee_id INTEGER NOT NULL,
  date DATE NOT NULL,
  check_in TIME,
  check_out TIME,
  status VARCHAR, -- Note: 'ON_TIME, LATE, EARLY_LEAVE, ABSENT, HALF_DAY'
  overtime_hours DECIMAL(4, 2) DEFAULT 0,
  work_hours DECIMAL(4, 2) DEFAULT 0,
  note TEXT,
  late_minutes INTEGER DEFAULT 0,
  early_leave_minutes INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE holidays (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  date DATE UNIQUE NOT NULL,
  is_paid BOOLEAN DEFAULT true
);

CREATE TABLE shifts (
  id SERIAL PRIMARY KEY,
  code VARCHAR(20) UNIQUE NOT NULL,
  name VARCHAR(100) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  break_start_time TIME,
  break_end_time TIME,
  is_default BOOLEAN DEFAULT false,
  is_active BOOLEAN DEFAULT true
);

CREATE TABLE leave_types (
  id SERIAL PRIMARY KEY,
  code VARCHAR(20) UNIQUE NOT NULL,
  name VARCHAR(100) NOT NULL,
  is_paid BOOLEAN DEFAULT true,
  description TEXT
);

CREATE TABLE leave_balances (
  id SERIAL PRIMARY KEY,
  employee_id INTEGER NOT NULL,
  leave_type_id INTEGER NOT NULL,
  year INTEGER NOT NULL,
  total_days DECIMAL(4, 1) DEFAULT 0,
  used_days DECIMAL(4, 1) DEFAULT 0,
  carry_over_days DECIMAL(4, 1) DEFAULT 0
);

CREATE TABLE leave_requests (
  id SERIAL PRIMARY KEY,
  employee_id INTEGER NOT NULL,
  leave_type_id INTEGER NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  days DECIMAL(4, 1) NOT NULL,
  reason TEXT NOT NULL,
  attachment_url VARCHAR(255),
  status VARCHAR DEFAULT 'PENDING', -- Note: 'PENDING, APPROVED, REJECTED, CANCELLED'
  approved_by INTEGER,
  approved_at TIMESTAMP,
  rejection_reason TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payroll (
  id SERIAL PRIMARY KEY,
  employee_id INTEGER NOT NULL,
  month VARCHAR(7) NOT NULL, -- Note: 'YYYY-MM'
  basic_salary DECIMAL(12, 2) NOT NULL,
  allowances TEXT,
  total_allowances DECIMAL(12, 2) DEFAULT 0,
  overtime_pay DECIMAL(12, 2) DEFAULT 0,
  gross_salary DECIMAL(12, 2) NOT NULL,
  deductions TEXT,
  total_deductions DECIMAL(12, 2) DEFAULT 0,
  net_salary DECIMAL(12, 2) NOT NULL,
  work_days DECIMAL(4, 1),
  actual_days DECIMAL(4, 1),
  status VARCHAR DEFAULT 'DRAFT', -- Note: 'DRAFT, CALCULATED, APPROVED, PAID'
  approved_by INTEGER,
  approved_at TIMESTAMP,
  paid_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 2. TẠO INDEXES & UNIQUE CONSTRAINTS
-- ==========================================

ALTER TABLE attendance_records ADD CONSTRAINT unique_employee_date UNIQUE (employee_id, date);
ALTER TABLE leave_balances ADD CONSTRAINT unique_employee_leave_year UNIQUE (employee_id, leave_type_id, year);
ALTER TABLE payroll ADD CONSTRAINT unique_employee_month UNIQUE (employee_id, month);

-- ==========================================
-- 3. TẠO FOREIGN KEYS (RELATIONSHIPS)
-- ==========================================

ALTER TABLE departments ADD CONSTRAINT fk_departments_manager FOREIGN KEY (manager_id) REFERENCES employees(id);
ALTER TABLE departments ADD CONSTRAINT fk_departments_parent FOREIGN KEY (parent_id) REFERENCES departments(id) ON DELETE SET NULL;
ALTER TABLE employees ADD CONSTRAINT fk_employees_department FOREIGN KEY (department_id) REFERENCES departments(id);

ALTER TABLE contracts ADD CONSTRAINT fk_contracts_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE users ADD CONSTRAINT fk_users_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE attendance_records ADD CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;

ALTER TABLE overtime_requests ADD CONSTRAINT fk_overtime_requests_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;
ALTER TABLE overtime_requests ADD CONSTRAINT fk_overtime_requests_approver FOREIGN KEY (approved_by) REFERENCES employees(id) ON DELETE SET NULL;

ALTER TABLE leave_balances ADD CONSTRAINT fk_leave_balances_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;
ALTER TABLE leave_balances ADD CONSTRAINT fk_leave_balances_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id);

ALTER TABLE leave_requests ADD CONSTRAINT fk_leave_requests_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;
ALTER TABLE leave_requests ADD CONSTRAINT fk_leave_requests_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id);
ALTER TABLE leave_requests ADD CONSTRAINT fk_leave_requests_approver FOREIGN KEY (approved_by) REFERENCES employees(id) ON DELETE SET NULL;

ALTER TABLE payroll ADD CONSTRAINT fk_payroll_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE;
ALTER TABLE payroll ADD CONSTRAINT fk_payroll_approver FOREIGN KEY (approved_by) REFERENCES employees(id) ON DELETE SET NULL;

-- ==========================================
-- 4. TẠO FUNCTIONS & TRIGGERS
-- ==========================================

-- Function & Trigger đồng bộ Email từ employees sang users
CREATE OR REPLACE FUNCTION fn_SyncEmployeeEmail()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE users 
    SET email = NEW.email, updated_at = CURRENT_TIMESTAMP
    WHERE employee_id = NEW.id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_SyncEmployeeEmail
AFTER UPDATE OF email ON employees
FOR EACH ROW
EXECUTE FUNCTION fn_SyncEmployeeEmail();

-- Function & Trigger kiểm tra số dư phép trước khi xin nghỉ
CREATE OR REPLACE FUNCTION fn_CheckLeaveBalance()
RETURNS TRIGGER AS $$
DECLARE
    v_remaining DECIMAL(4,1);
    v_is_paid BOOLEAN;
BEGIN
    -- Lấy thông tin loại phép
    SELECT is_paid INTO v_is_paid FROM leave_types WHERE id = NEW.leave_type_id;

    -- Chỉ kiểm tra cho các loại phép có tính lương
    IF (v_is_paid = true) THEN
        SELECT (total_days + carry_over_days - used_days) INTO v_remaining
        FROM leave_balances
        WHERE employee_id = NEW.employee_id 
          AND leave_type_id = NEW.leave_type_id
          AND year = EXTRACT(YEAR FROM NEW.start_date);

        IF v_remaining IS NULL OR v_remaining < NEW.days THEN
            RAISE EXCEPTION 'Số dư phép không đủ. Còn lại: %, Yêu cầu: %', COALESCE(v_remaining, 0), NEW.days;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_CheckLeaveBalance
BEFORE INSERT OR UPDATE OF days, status ON leave_requests
FOR EACH ROW
EXECUTE FUNCTION fn_CheckLeaveBalance();

-- ==========================================
-- 5. TẠO STORED PROCEDURES
-- ==========================================

-- Thủ tục tạo Nhân viên kèm Tài khoản
CREATE OR REPLACE PROCEDURE sp_CreateEmployeeWithUser(
    p_department_id INT,
    p_code VARCHAR,
    p_name VARCHAR,
    p_email VARCHAR,
    p_phone VARCHAR,
    p_join_date DATE,
    p_password_hash VARCHAR,
    p_role VARCHAR,
    OUT r_employee_id INT
)
AS $$
BEGIN
    -- 1. Thêm vào bảng employees
    INSERT INTO employees (department_id, code, name, email, phone, join_date, status)
    VALUES (p_department_id, p_code, p_name, p_email, p_phone, p_join_date, 'ACTIVE')
    RETURNING id INTO r_employee_id;

    -- 2. Thêm vào bảng users
    INSERT INTO users (employee_id, username, email, password_hash, role)
    VALUES (r_employee_id, p_code, p_email, p_password_hash, p_role);
END;
$$ LANGUAGE plpgsql;

-- Thủ tục duyệt nghỉ phép và trừ số dư tự động
CREATE OR REPLACE PROCEDURE sp_ApproveLeaveRequest(
    p_request_id INT,
    p_approver_id INT,
    p_status VARCHAR
)
AS $$
DECLARE
    v_employee_id INT;
    v_leave_type_id INT;
    v_days DECIMAL(4,1);
    v_year INT;
BEGIN
    -- Lấy thông tin đơn
    SELECT employee_id, leave_type_id, days, EXTRACT(YEAR FROM start_date)
    INTO v_employee_id, v_leave_type_id, v_days, v_year
    FROM leave_requests WHERE id = p_request_id;

    -- 1. Cập nhật trạng thái đơn
    UPDATE leave_requests 
    SET status = p_status, approved_by = p_approver_id, approved_at = CURRENT_TIMESTAMP
    WHERE id = p_request_id;

    -- 2. Nếu status là APPROVED -> cập nhật dùng số ngày phép trong leave_balances
    IF p_status = 'APPROVED' THEN
        UPDATE leave_balances 
        SET used_days = used_days + v_days
        WHERE employee_id = v_employee_id 
          AND leave_type_id = v_leave_type_id 
          AND year = v_year;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Thủ tục tính lương hàng tháng
CREATE OR REPLACE PROCEDURE sp_CalculateMonthlyPayroll(
    p_month_year VARCHAR -- Định dạng 'YYYY-MM'
)
AS $$
DECLARE
    emp_record RECORD;
    v_basic_salary DECIMAL(12,2);
    v_total_work_days DECIMAL(4,1);
    v_total_ot_hours DECIMAL(6,2);
    v_paid_leave_days DECIMAL(4,1);
    v_daily_rate DECIMAL(12,2);
    v_ot_pay DECIMAL(12,2);
    v_net_salary DECIMAL(12,2);
    v_standard_days DECIMAL(4,1) := 22.0;
BEGIN
    FOR emp_record IN SELECT id FROM employees WHERE status = 'ACTIVE' LOOP
        -- 1. Lấy lương cơ bản từ hợp đồng ACTIVE
        SELECT basic_salary INTO v_basic_salary 
        FROM contracts WHERE employee_id = emp_record.id AND status = 'ACTIVE' LIMIT 1;

        IF v_basic_salary IS NOT NULL THEN
            -- 2. Tính ngày công thực tế (không tính vắng mặt)
            SELECT COUNT(*), COALESCE(SUM(overtime_hours), 0) INTO v_total_work_days, v_total_ot_hours
            FROM attendance_records 
            WHERE employee_id = emp_record.id 
              AND TO_CHAR(date, 'YYYY-MM') = p_month_year 
              AND status != 'ABSENT';

            -- 3. Tính ngày nghỉ có lương
            SELECT COALESCE(SUM(days), 0) INTO v_paid_leave_days
            FROM leave_requests
            WHERE employee_id = emp_record.id
              AND status = 'APPROVED'
              AND TO_CHAR(start_date, 'YYYY-MM') = p_month_year;

            -- 4. Tính toán các chỉ số lương
            v_daily_rate := v_basic_salary / v_standard_days;
            v_ot_pay := v_total_ot_hours * (v_daily_rate / 8) * 1.5;
            v_net_salary := (v_daily_rate * (v_total_work_days + v_paid_leave_days)) + v_ot_pay;

            -- 5. Upsert vào bảng payroll
            INSERT INTO payroll (employee_id, month, basic_salary, work_days, actual_days, overtime_pay, gross_salary, net_salary, status)
            VALUES (emp_record.id, p_month_year, v_basic_salary, v_standard_days, (v_total_work_days + v_paid_leave_days), v_ot_pay, v_net_salary, v_net_salary, 'DRAFT')
            ON CONFLICT (employee_id, month) DO UPDATE SET
                actual_days = EXCLUDED.actual_days,
                overtime_pay = EXCLUDED.overtime_pay,
                gross_salary = EXCLUDED.gross_salary,
                net_salary = EXCLUDED.net_salary,
                updated_at = CURRENT_TIMESTAMP;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ==========================================
-- 6. SEED MOCK DATA SO LUONG LON (FOR TEST)
-- ==========================================

-- 6.1 Seed leave types
INSERT INTO leave_types (code, name, is_paid, description)
VALUES
  ('ANNUAL', 'Nghi phep nam', true, 'Nghi phep huong luong theo nam'),
  ('SICK', 'Nghi om', true, 'Nghi om co giay xac nhan y te'),
  ('PERSONAL', 'Nghi viec rieng', false, 'Nghi viec rieng khong huong luong'),
  ('MATERNITY', 'Nghi thai san', true, 'Nghi thai san theo quy dinh')
ON CONFLICT (code) DO NOTHING;

-- 6.2 Seed departments
INSERT INTO departments (code, name, description)
SELECT
  'PB' || LPAD(gs::text, 3, '0'),
  'Phong ban ' || gs,
  'Du lieu phong ban mock #' || gs
FROM generate_series(1, 12) AS gs;

-- 6.3 Seed employees (1200 records)
INSERT INTO employees (
  department_id,
  code,
  name,
  email,
  phone,
  birthday,
  address,
  join_date,
  status,
  resignation_date
)
SELECT
  ((gs - 1) % 12) + 1,
  'EMP' || LPAD(gs::text, 5, '0'),
  'Nhan vien ' || LPAD(gs::text, 5, '0'),
  'employee' || LPAD(gs::text, 5, '0') || '@hrm.local',
  '09' || LPAD((10000000 + gs)::text, 8, '0'),
  CURRENT_DATE - ((22 * 365) + (gs % 2000)),
  'Dia chi mock so ' || gs,
  CURRENT_DATE - ((gs % 1460) + 30),
  CASE
    WHEN gs % 20 = 0 THEN 'RESIGNED'
    WHEN gs % 9 = 0 THEN 'INACTIVE'
    ELSE 'ACTIVE'
  END,
  CASE
    WHEN gs % 20 = 0 THEN CURRENT_DATE - ((gs % 300) + 15)
    ELSE NULL
  END
FROM generate_series(1, 1200) AS gs;

-- 6.4 Gan manager cho tung phong ban
UPDATE departments d
SET manager_id = x.min_emp_id
FROM (
  SELECT department_id, MIN(id) AS min_emp_id
  FROM employees
  GROUP BY department_id
) x
WHERE d.id = x.department_id;

-- 6.5 Seed users cho toan bo employees
-- Password hash bcrypt duoi day la tai khoan mau cho mat khau: password
INSERT INTO users (employee_id, username, email, password_hash, role)
SELECT
  e.id,
  LOWER(e.code),
  e.email,
  '$2a$10$7EqJtq98hPqEX7fNZaFWoO5o9Pqj8VY5A1NVuMcZV8K4D4ew3Efr2',
  CASE WHEN e.id <= 10 THEN 'ADMIN' ELSE 'EMPLOYEE' END
FROM employees e;

-- 6.6 Seed contracts
INSERT INTO contracts (
  employee_id,
  contract_type,
  start_date,
  end_date,
  basic_salary,
  status
)
SELECT
  e.id,
  CASE
    WHEN e.id % 5 = 0 THEN 'PROBATION'
    WHEN e.id % 3 = 0 THEN 'DEFINITE_1YR'
    ELSE 'INDEFINITE'
  END,
  e.join_date,
  CASE
    WHEN e.id % 3 = 0 THEN (e.join_date + INTERVAL '1 year')::date
    ELSE NULL
  END,
  (8500000 + ((e.id % 45) * 250000))::DECIMAL(12, 2),
  CASE WHEN e.status = 'RESIGNED' THEN 'TERMINATED' ELSE 'ACTIVE' END
FROM employees e;

-- 6.7 Seed leave balances cho 2 nam (nam hien tai va nam truoc)
WITH years AS (
  SELECT generate_series(EXTRACT(YEAR FROM CURRENT_DATE)::int - 1, EXTRACT(YEAR FROM CURRENT_DATE)::int) AS y
)
INSERT INTO leave_balances (
  employee_id,
  leave_type_id,
  year,
  total_days,
  used_days,
  carry_over_days
)
SELECT
  e.id,
  lt.id,
  y.y,
  CASE WHEN lt.is_paid THEN 12.0 ELSE 0.0 END,
  CASE
    WHEN lt.is_paid THEN ROUND((random() * 6)::numeric, 1)
    ELSE 0.0
  END,
  CASE
    WHEN y.y = EXTRACT(YEAR FROM CURRENT_DATE)::int AND lt.is_paid THEN ROUND((random() * 3)::numeric, 1)
    ELSE 0.0
  END
FROM employees e
CROSS JOIN leave_types lt
CROSS JOIN years y;

-- 6.8 Seed attendance records (du lieu 4 thang lam viec gan day)
WITH work_dates AS (
  SELECT d::date AS work_date
  FROM generate_series(CURRENT_DATE - INTERVAL '120 day', CURRENT_DATE - INTERVAL '1 day', INTERVAL '1 day') d
  WHERE EXTRACT(ISODOW FROM d) BETWEEN 1 AND 5
)
INSERT INTO attendance_records (
  employee_id,
  date,
  check_in,
  check_out,
  status,
  overtime_hours,
  work_hours,
  note
)
SELECT
  e.id,
  wd.work_date,
  CASE
    WHEN r.r1 < 0.04 THEN NULL
    ELSE (TIME '08:00' + ((FLOOR(r.r2 * 31) - 10)::int || ' minutes')::interval)::time
  END,
  CASE
    WHEN r.r1 < 0.04 THEN NULL
    ELSE (TIME '17:30' + ((FLOOR(r.r3 * 121) - 30)::int || ' minutes')::interval)::time
  END,
  CASE
    WHEN r.r1 < 0.04 THEN 'ABSENT'
    WHEN r.r1 < 0.12 THEN 'LATE'
    WHEN r.r1 < 0.18 THEN 'EARLY_LEAVE'
    WHEN r.r1 < 0.25 THEN 'HALF_DAY'
    ELSE 'ON_TIME'
  END,
  CASE
    WHEN r.r1 < 0.70 THEN 0
    ELSE ROUND((r.r3 * 4)::numeric, 2)
  END,
  CASE
    WHEN r.r1 < 0.04 THEN 0
    WHEN r.r1 < 0.25 THEN ROUND((4 + r.r2 * 3)::numeric, 2)
    ELSE ROUND((8 + r.r3 * 2)::numeric, 2)
  END,
  'Attendance mock data'
FROM employees e
JOIN work_dates wd ON wd.work_date >= e.join_date
CROSS JOIN LATERAL (SELECT random() AS r1, random() AS r2, random() AS r3) r
WHERE e.status <> 'RESIGNED'
   OR wd.work_date <= COALESCE(e.resignation_date, CURRENT_DATE);

-- 6.9 Seed leave requests
INSERT INTO leave_requests (
  employee_id,
  leave_type_id,
  start_date,
  end_date,
  days,
  reason,
  status,
  approved_by,
  approved_at,
  rejection_reason
)
SELECT
  e.id,
  lt.id,
  req.start_date,
  req.end_date,
  req.days,
  'Xin nghi mock data #' || n.seq,
  req.status,
  CASE WHEN req.status IN ('APPROVED', 'REJECTED') THEN COALESCE(d.manager_id, 1) ELSE NULL END,
  CASE WHEN req.status = 'APPROVED' THEN (req.start_date - INTERVAL '2 day')::timestamp ELSE NULL END,
  CASE WHEN req.status = 'REJECTED' THEN 'Khong du dieu kien duyet' ELSE NULL END
FROM employees e
JOIN departments d ON d.id = e.department_id
CROSS JOIN generate_series(1, 3) AS n(seq)
CROSS JOIN LATERAL (
  SELECT id, is_paid
  FROM leave_types
  ORDER BY id
  LIMIT 1
  OFFSET ((e.id + n.seq) % 4)
) lt
CROSS JOIN LATERAL (
  SELECT
    (CURRENT_DATE - (((e.id * 7) + (n.seq * 13)) % 320 + 5))::date AS start_date,
    (CURRENT_DATE - (((e.id * 7) + (n.seq * 13)) % 320 + 5) + ((e.id + n.seq) % 3))::date AS end_date,
    ((e.id + n.seq) % 3 + 1)::DECIMAL(4, 1) AS days,
    CASE
      WHEN (e.id + n.seq) % 5 = 0 THEN 'PENDING'
      WHEN (e.id + n.seq) % 6 = 0 THEN 'REJECTED'
      ELSE 'APPROVED'
    END AS status
) req;

-- 6.10 Seed overtime requests
INSERT INTO overtime_requests (
  employee_id,
  date,
  start_time,
  end_time,
  hours,
  reason,
  status,
  approved_by,
  approved_at,
  rejection_reason
)
SELECT
  e.id,
  (CURRENT_DATE - (((e.id * 5) + (n.seq * 11)) % 120 + 2))::date,
  (TIME '18:00' + ((n.seq % 3) || ' hours')::interval)::time,
  (TIME '20:00' + ((n.seq % 3) || ' hours')::interval)::time,
  (2 + (n.seq % 3))::DECIMAL(4,2),
  'Tang ca mock data #' || n.seq,
  CASE
    WHEN (e.id + n.seq) % 7 = 0 THEN 'PENDING'
    WHEN (e.id + n.seq) % 8 = 0 THEN 'REJECTED'
    ELSE 'APPROVED'
  END,
  CASE
    WHEN (e.id + n.seq) % 7 = 0 THEN NULL
    ELSE COALESCE(d.manager_id, 1)
  END,
  CASE
    WHEN (e.id + n.seq) % 7 = 0 THEN NULL
    ELSE CURRENT_TIMESTAMP - ((n.seq + 1) || ' days')::interval
  END,
  CASE
    WHEN (e.id + n.seq) % 8 = 0 THEN 'Khong phu hop ke hoach cong viec'
    ELSE NULL
  END
FROM employees e
JOIN departments d ON d.id = e.department_id
CROSS JOIN generate_series(1, 2) AS n(seq)
WHERE e.status = 'ACTIVE';

-- 6.11 Seed payroll cho 6 thang gan nhat
WITH month_ref AS (
  SELECT
    TO_CHAR(date_trunc('month', CURRENT_DATE) - (m || ' month')::interval, 'YYYY-MM') AS month_key,
    (date_trunc('month', CURRENT_DATE) - (m || ' month')::interval)::date AS month_date,
    m AS m_idx
  FROM generate_series(0, 5) AS m
)
INSERT INTO payroll (
  employee_id,
  month,
  basic_salary,
  allowances,
  total_allowances,
  overtime_pay,
  gross_salary,
  deductions,
  total_deductions,
  net_salary,
  work_days,
  actual_days,
  status,
  approved_by,
  approved_at,
  paid_at
)
SELECT
  e.id,
  mr.month_key,
  c.basic_salary,
  jsonb_build_object('meal', 500000, 'phone', 300000, 'responsibility', 700000)::text,
  1500000::DECIMAL(12,2),
  ROUND((((e.id % 12) + mr.m_idx)::numeric * 85000), 2),
  ROUND((c.basic_salary + 1500000 + (((e.id % 12) + mr.m_idx)::numeric * 85000)), 2),
  jsonb_build_object('insurance', ROUND((c.basic_salary * 0.105)::numeric, 2), 'tax', ROUND((c.basic_salary * 0.03)::numeric, 2))::text,
  ROUND((c.basic_salary * 0.135)::numeric, 2),
  ROUND((c.basic_salary + 1500000 + (((e.id % 12) + mr.m_idx)::numeric * 85000) - (c.basic_salary * 0.135))::numeric, 2),
  22.0,
  LEAST(22.0, (18 + ((e.id + mr.m_idx) % 6))::numeric),
  CASE
    WHEN mr.m_idx = 0 THEN 'CALCULATED'
    WHEN mr.m_idx = 1 THEN 'APPROVED'
    ELSE 'PAID'
  END,
  1,
  CASE WHEN mr.m_idx >= 1 THEN (mr.month_date + INTERVAL '25 day')::timestamp ELSE NULL END,
  CASE WHEN mr.m_idx >= 2 THEN (mr.month_date + INTERVAL '28 day')::timestamp ELSE NULL END
FROM employees e
JOIN contracts c ON c.employee_id = e.id
JOIN month_ref mr ON mr.month_date >= date_trunc('month', e.join_date)
ON CONFLICT (employee_id, month) DO NOTHING;
