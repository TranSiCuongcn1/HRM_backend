-- Large mock data seed for HRM project (PostgreSQL)
-- Target DB: HRM
-- Notes:
-- 1) This script truncates business tables and reseeds with large sample data.
-- 2) Default login password for seeded users: 123456

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

TRUNCATE TABLE
  payroll,
  leave_requests,
  leave_balances,
  attendance_records,
  users,
  contracts,
  employees,
  leave_types,
  departments
RESTART IDENTITY CASCADE;

-- Departments with a simple hierarchy
INSERT INTO departments (code, name, description, parent_id)
VALUES
  ('D-HQ', 'Ban dieu hanh', 'Ban dieu hanh cong ty', NULL),
  ('D-HR', 'Nhan su', 'Quan ly nhan su va dao tao', 1),
  ('D-FIN', 'Tai chinh - Ke toan', 'Ngan sach, quyet toan va bao cao', 1),
  ('D-IT', 'Cong nghe thong tin', 'Phat trien va van hanh he thong', 1),
  ('D-SALES', 'Kinh doanh', 'Ban hang va mo rong thi truong', 1),
  ('D-MKT', 'Marketing', 'Thuong hieu va truyen thong', 1),
  ('D-CS', 'Cham soc khach hang', 'Ho tro va tiep nhan phan hoi', 1),
  ('D-OPS', 'Van hanh', 'Dieu phoi hoat dong van hanh', 1),
  ('D-PRD', 'San pham', 'Nghien cuu va phat trien san pham', 1),
  ('D-QA', 'Dam bao chat luong', 'Kiem thu va chat luong', 4),
  ('D-SEC', 'An toan thong tin', 'Bao mat he thong va du lieu', 4),
  ('D-ADM', 'Hanh chinh', 'Quan tri hanh chinh noi bo', 1),
  ('D-LGL', 'Phap che', 'Tu van va kiem soat phap ly', 1),
  ('D-RND', 'Nghien cuu', 'Nghien cuu cong nghe moi', 4),
  ('D-DATA', 'Du lieu', 'Kho du lieu va phan tich', 4),
  ('D-BI', 'Business Intelligence', 'Phan tich kinh doanh', 15),
  ('D-WH', 'Kho van', 'Quan ly kho va giao nhan', 8),
  ('D-PROC', 'Mua sam', 'Mua sam va nha cung cap', 8);

-- Admin employee
INSERT INTO employees (
  department_id, code, name, avatar, email, phone, birthday, address, join_date, status, resignation_date
)
VALUES (
  1,
  'ADM001',
  'System Admin',
  NULL,
  'admin@hrm.local',
  '0900000000',
  DATE '1990-01-01',
  'Tru so chinh',
  CURRENT_DATE - INTERVAL '2000 days',
  'ACTIVE',
  NULL
);

-- Bulk employees: 800 records
INSERT INTO employees (
  department_id, code, name, avatar, email, phone, birthday, address, join_date, status, resignation_date
)
SELECT
  ((g - 1) % 18) + 1 AS department_id,
  'EMP' || LPAD(g::text, 4, '0') AS code,
  'Nhan vien ' || g AS name,
  NULL,
  'emp' || LPAD(g::text, 4, '0') || '@hrm.local' AS email,
  '09' || LPAD((10000000 + g)::text, 8, '0') AS phone,
  (DATE '1985-01-01' + ((random() * 9000)::int || ' days')::interval)::date AS birthday,
  'Dia chi so ' || g,
  (CURRENT_DATE - ((random() * 1800)::int || ' days')::interval)::date AS join_date,
  CASE WHEN g % 17 = 0 THEN 'RESIGNED' ELSE 'ACTIVE' END AS status,
  CASE WHEN g % 17 = 0 THEN (CURRENT_DATE - ((random() * 300)::int || ' days')::interval)::date ELSE NULL END AS resignation_date
FROM generate_series(1, 800) AS g;

-- Assign each department manager from its active employee pool
WITH ranked_manager AS (
  SELECT
    e.id,
    e.department_id,
    ROW_NUMBER() OVER (PARTITION BY e.department_id ORDER BY e.join_date ASC, e.id ASC) AS rn
  FROM employees e
  WHERE e.status = 'ACTIVE'
)
UPDATE departments d
SET manager_id = rm.id
FROM ranked_manager rm
WHERE d.id = rm.department_id AND rm.rn = 1;

-- Users: one admin + all employees
INSERT INTO users (employee_id, username, email, password_hash, role, is_active)
SELECT
  e.id,
  CASE WHEN e.code = 'ADM001' THEN 'admin' ELSE lower(e.code) END,
  e.email,
  crypt('123456', gen_salt('bf', 8)),
  CASE WHEN e.code = 'ADM001' THEN 'ADMIN' ELSE 'EMPLOYEE' END,
  CASE WHEN e.status = 'RESIGNED' THEN false ELSE true END
FROM employees e;

-- Leave types
INSERT INTO leave_types (code, name, is_paid, description)
VALUES
  ('ANNUAL', 'Phep nam', true, 'Phep nam theo quy dinh cong ty'),
  ('SICK', 'Nghi benh', true, 'Nghi benh co giay xac nhan'),
  ('WEDDING', 'Phep cuoi', true, 'Nghi cuoi theo che do'),
  ('BEREAVEMENT', 'Phep tang', true, 'Nghi tang theo che do'),
  ('MATERNITY', 'Phep sinh', true, 'Che do thai san'),
  ('UNPAID', 'Khong luong', false, 'Nghi khong huong luong');

-- Contracts: one for each employee
INSERT INTO contracts (employee_id, contract_type, start_date, end_date, basic_salary, status)
SELECT
  e.id,
  CASE
    WHEN e.id % 5 = 0 THEN 'PROBATION'
    WHEN e.id % 3 = 0 THEN 'DEFINITE_1YR'
    ELSE 'INDEFINITE'
  END AS contract_type,
  GREATEST(e.join_date, CURRENT_DATE - INTERVAL '720 days')::date AS start_date,
  CASE
    WHEN e.id % 5 = 0 THEN (GREATEST(e.join_date, CURRENT_DATE - INTERVAL '720 days') + INTERVAL '60 days')::date
    WHEN e.id % 3 = 0 THEN (GREATEST(e.join_date, CURRENT_DATE - INTERVAL '720 days') + INTERVAL '365 days')::date
    ELSE NULL
  END AS end_date,
  (
    9000000
    + (COALESCE(e.department_id, 1) * 350000)
    + ((e.id % 15) * 250000)
  )::numeric(12,2) AS basic_salary,
  CASE
    WHEN e.status = 'RESIGNED' THEN 'TERMINATED'
    WHEN e.id % 5 = 0 THEN 'ACTIVE'
    ELSE 'ACTIVE'
  END AS status
FROM employees e;

-- Leave balances for current and previous year
WITH years AS (
  SELECT EXTRACT(YEAR FROM CURRENT_DATE)::int AS y
  UNION ALL
  SELECT (EXTRACT(YEAR FROM CURRENT_DATE)::int - 1) AS y
)
INSERT INTO leave_balances (employee_id, leave_type_id, year, total_days, used_days, carry_over_days)
SELECT
  e.id,
  lt.id,
  y.y,
  CASE
    WHEN lt.code = 'ANNUAL' THEN 12.0
    WHEN lt.code = 'SICK' THEN 30.0
    WHEN lt.code = 'WEDDING' THEN 3.0
    WHEN lt.code = 'BEREAVEMENT' THEN 3.0
    WHEN lt.code = 'MATERNITY' THEN 0.0
    ELSE 0.0
  END::numeric(4,1) AS total_days,
  CASE
    WHEN lt.code = 'ANNUAL' THEN (random() * 8)::numeric(4,1)
    WHEN lt.code = 'SICK' THEN (random() * 5)::numeric(4,1)
    WHEN lt.code IN ('WEDDING', 'BEREAVEMENT') THEN (random() * 1)::numeric(4,1)
    ELSE 0.0
  END::numeric(4,1) AS used_days,
  CASE WHEN lt.code = 'ANNUAL' THEN (random() * 3)::numeric(4,1) ELSE 0.0 END::numeric(4,1) AS carry_over_days
FROM employees e
CROSS JOIN leave_types lt
CROSS JOIN years y;

-- Leave requests: random 1..4 per employee
WITH admin_emp AS (
  SELECT id FROM employees WHERE code = 'ADM001' LIMIT 1
), random_requests AS (
  SELECT
    e.id AS employee_id,
    (SELECT id FROM leave_types ORDER BY random() LIMIT 1) AS leave_type_id,
    ((CURRENT_DATE - INTERVAL '210 days') + ((random() * 210)::int || ' days')::interval)::date AS start_date,
    (CASE WHEN random() < 0.2 THEN 0.5 ELSE (1 + floor(random() * 4)) END)::numeric(4,1) AS days,
    CASE
      WHEN random() < 0.55 THEN 'APPROVED'
      WHEN random() < 0.75 THEN 'PENDING'
      WHEN random() < 0.92 THEN 'REJECTED'
      ELSE 'CANCELLED'
    END AS status
  FROM employees e
  JOIN LATERAL generate_series(1, (1 + floor(random() * 4))::int) g ON true
  WHERE e.code <> 'ADM001'
)
INSERT INTO leave_requests (
  employee_id, leave_type_id, start_date, end_date, days, reason, attachment_url, status,
  approved_by, approved_at, rejection_reason
)
SELECT
  rr.employee_id,
  rr.leave_type_id,
  rr.start_date,
  (rr.start_date + (CEIL(rr.days)::int - 1) * INTERVAL '1 day')::date AS end_date,
  rr.days,
  'Ly do nghi phep mock #' || rr.employee_id,
  NULL,
  rr.status,
  CASE WHEN rr.status IN ('APPROVED', 'REJECTED') THEN (SELECT id FROM admin_emp) ELSE NULL END,
  CASE WHEN rr.status IN ('APPROVED', 'REJECTED') THEN NOW() - ((random() * 180)::int || ' days')::interval ELSE NULL END,
  CASE WHEN rr.status = 'REJECTED' THEN 'Khong du dieu kien duyet (mock)' ELSE NULL END
FROM random_requests rr;

-- Attendance records: 90 days x active employees (weekdays only, random missing)
INSERT INTO attendance_records (
  employee_id, date, check_in, check_out, status, overtime_hours, work_hours, note
)
SELECT
  e.id,
  d.day::date,
  CASE WHEN status_calc = 'ABSENT' THEN NULL ELSE checkin_calc END,
  CASE WHEN status_calc = 'ABSENT' THEN NULL ELSE checkout_calc END,
  status_calc,
  CASE
    WHEN status_calc = 'ABSENT' THEN 0.00
    ELSE GREATEST(0::numeric, (work_calc - 8.0))::numeric(4,2)
  END AS overtime_hours,
  CASE
    WHEN status_calc = 'ABSENT' THEN 0.00
    ELSE work_calc::numeric(4,2)
  END AS work_hours,
  CASE WHEN status_calc = 'ABSENT' THEN 'Nghi khong bao truoc (mock)' ELSE NULL END
FROM (
  SELECT * FROM employees WHERE status = 'ACTIVE' AND code <> 'ADM001'
) e
CROSS JOIN LATERAL (
  SELECT gs::date AS day
  FROM generate_series(CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE - INTERVAL '1 day', INTERVAL '1 day') gs
  WHERE EXTRACT(ISODOW FROM gs) BETWEEN 1 AND 5
) d
CROSS JOIN LATERAL (
  SELECT
    CASE
      WHEN random() < 0.08 THEN 'ABSENT'
      WHEN random() < 0.22 THEN 'LATE'
      WHEN random() < 0.30 THEN 'EARLY_LEAVE'
      ELSE 'ON_TIME'
    END AS status_calc,
    (TIME '07:45' + ((random() * 80)::int || ' minutes')::interval)::time AS checkin_calc,
    (TIME '16:45' + ((random() * 120)::int || ' minutes')::interval)::time AS checkout_calc,
    (7.5 + random() * 3.5)::numeric(4,2) AS work_calc
) x
WHERE random() < 0.96;

-- Attendance records for today so Daily/Overtime/Absence pages always have visible data
INSERT INTO attendance_records (
  employee_id, date, check_in, check_out, status, overtime_hours, work_hours, note
)
SELECT
  e.id,
  CURRENT_DATE,
  CASE
    WHEN x.status_calc = 'ABSENT' THEN NULL
    WHEN x.status_calc = 'LATE' THEN TIME '08:20'
    ELSE TIME '07:55'
  END,
  CASE
    WHEN x.status_calc = 'ABSENT' THEN NULL
    WHEN x.has_overtime THEN TIME '18:45'
    WHEN x.status_calc = 'EARLY_LEAVE' THEN TIME '16:20'
    ELSE TIME '17:10'
  END,
  x.status_calc,
  CASE
    WHEN x.status_calc = 'ABSENT' THEN 0.00
    WHEN x.has_overtime THEN (1 + (e.id % 3))::numeric(4,2)
    ELSE 0.00
  END AS overtime_hours,
  CASE
    WHEN x.status_calc = 'ABSENT' THEN 0.00
    WHEN x.has_overtime THEN (9 + (e.id % 2))::numeric(4,2)
    WHEN x.status_calc = 'EARLY_LEAVE' THEN 7.20
    ELSE 8.10
  END AS work_hours,
  CASE
    WHEN x.status_calc = 'ABSENT' THEN 'Vang mat hom nay (mock)'
    WHEN x.has_overtime THEN 'Tang ca hom nay (mock)'
    ELSE 'Cham cong hom nay (mock)'
  END AS note
FROM (
  SELECT * FROM employees WHERE status = 'ACTIVE' AND code <> 'ADM001'
) e
CROSS JOIN LATERAL (
  SELECT
    CASE
      WHEN e.id % 10 = 0 THEN 'ABSENT'
      WHEN e.id % 6 = 0 THEN 'LATE'
      WHEN e.id % 8 = 0 THEN 'EARLY_LEAVE'
      ELSE 'ON_TIME'
    END AS status_calc,
    (e.id % 4 = 0) AS has_overtime
) x;

-- Payroll for last 6 months
WITH admin_emp AS (
  SELECT id FROM employees WHERE code = 'ADM001' LIMIT 1
), month_list AS (
  SELECT to_char((date_trunc('month', CURRENT_DATE) - (g.n || ' month')::interval), 'YYYY-MM') AS month_label
  FROM generate_series(0, 5) AS g(n)
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
  m.month_label,
  c.basic_salary,
  '{"an_trua":500000,"xang_xe":300000}'::text,
  800000.00::numeric(12,2),
  ((random() * 1500000)::numeric(12,2)) AS overtime_pay,
  (c.basic_salary + 800000.00 + (random() * 1500000)::numeric(12,2))::numeric(12,2) AS gross_salary,
  '{"bhxh":900000,"thue_tncn":450000}'::text,
  1350000.00::numeric(12,2),
  (c.basic_salary + 800000.00 - 1350000.00 + (random() * 1500000)::numeric(12,2))::numeric(12,2) AS net_salary,
  22.0::numeric(4,1),
  (18 + floor(random() * 5))::numeric(4,1),
  CASE
    WHEN random() < 0.35 THEN 'DRAFT'
    WHEN random() < 0.65 THEN 'CALCULATED'
    WHEN random() < 0.90 THEN 'APPROVED'
    ELSE 'PAID'
  END AS status,
  (SELECT id FROM admin_emp),
  NOW() - ((random() * 60)::int || ' days')::interval,
  CASE WHEN random() < 0.3 THEN NOW() - ((random() * 20)::int || ' days')::interval ELSE NULL END
FROM employees e
JOIN contracts c ON c.employee_id = e.id
CROSS JOIN month_list m
WHERE e.status = 'ACTIVE';

COMMIT;

-- Quick summary
SELECT 'departments' AS table_name, COUNT(*) AS total FROM departments
UNION ALL SELECT 'employees', COUNT(*) FROM employees
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'contracts', COUNT(*) FROM contracts
UNION ALL SELECT 'attendance_records', COUNT(*) FROM attendance_records
UNION ALL SELECT 'leave_types', COUNT(*) FROM leave_types
UNION ALL SELECT 'leave_balances', COUNT(*) FROM leave_balances
UNION ALL SELECT 'leave_requests', COUNT(*) FROM leave_requests
UNION ALL SELECT 'payroll', COUNT(*) FROM payroll
ORDER BY table_name;

-- Verify admin salary seed (contract + payroll rows)
SELECT
  e.code AS admin_code,
  c.basic_salary AS admin_basic_salary,
  COUNT(p.id) AS payroll_rows
FROM employees e
LEFT JOIN contracts c ON c.employee_id = e.id AND c.status = 'ACTIVE'
LEFT JOIN payroll p ON p.employee_id = e.id
WHERE e.code = 'ADM001'
GROUP BY e.code, c.basic_salary;
