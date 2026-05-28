-- ==========================================
-- 7. SEED MOCK DATA LARGE
-- ==========================================
-- 6.2 Seed departments
INSERT INTO departments (code, name, description)
SELECT
  'PB' || LPAD(gs::text, 3, '0'),
  (ARRAY['Phòng Nhân sự', 'Phòng Kế toán', 'Phòng IT', 'Phòng Marketing', 'Phòng Kinh doanh', 'Phòng Hành chính', 'Phòng Kỹ thuật', 'Phòng Vận hành', 'Phòng Chăm sóc khách hàng', 'Phòng Pháp chế', 'Phòng R&D', 'Phòng Sản xuất'])[gs],
  (ARRAY['Tuyển dụng, đào tạo và phát triển nhân sự', 'Quản lý tài chính, thu chi, lương thưởng', 'Phát triển và bảo trì hệ thống phần mềm', 'Nghiên cứu thị trường và quảng bá thương hiệu', 'Mở rộng đối tác và phát triển doanh thu', 'Quản trị văn phòng và hỗ trợ các phòng ban', 'Bảo trì trang thiết bị và hạ tầng mạng', 'Đảm bảo hoạt động kinh doanh hàng ngày', 'Tư vấn và hỗ trợ người dùng', 'Cố vấn luật và kiểm soát rủi ro pháp lý', 'Nghiên cứu công nghệ mới, sản phẩm mới', 'Quản lý quy trình vận hành sản phẩm'])[gs]
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
  n.ho || ' ' || n.dem || ' ' || n.ten,
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
FROM generate_series(1, 1200) AS gs
CROSS JOIN LATERAL (
  SELECT
    (ARRAY['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Huỳnh', 'Phan', 'Vũ', 'Võ', 'Đặng'])[floor(random() * 10 + 1 + (gs * 0))::int] as ho,
    (ARRAY['Thị', 'Văn', 'Hữu', 'Đức', 'Ngọc', 'Minh', 'Xuân', 'Thu', 'Thanh', 'Tuấn', 'Hải', 'Thùy', 'Hồng', 'Thái'])[floor(random() * 14 + 1 + (gs * 0))::int] as dem,
    (ARRAY['Anh', 'Hùng', 'Cường', 'Lan', 'Hoa', 'Mai', 'Linh', 'Sơn', 'Tùng', 'Nam', 'Hiếu', 'Thảo', 'Trang', 'Hải', 'Bình', 'Phương', 'Nhung', 'Quỳnh', 'Dương', 'Hà'])[floor(random() * 20 + 1 + (gs * 0))::int] as ten
) n;

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
  '$2a$10$3.vanjSR/276bNj6IQpwCOPnZcJ1rdUR4CYDhQ28nalAHLEGqzM3a',
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
    WHEN lt.is_paid THEN ROUND(((random() + (e.id * 0)) * 6)::numeric, 1)
    ELSE 0.0
  END,
  CASE
    WHEN y.y = EXTRACT(YEAR FROM CURRENT_DATE)::int AND lt.is_paid THEN ROUND(((random() + (e.id * 0)) * 3)::numeric, 1)
    ELSE 0.0
  END
FROM employees e
CROSS JOIN leave_types lt
CROSS JOIN years y;

-- 6.8 Seed attendance records (du lieu 4 thang lam viec gan day)
WITH work_dates AS (
  SELECT d::date AS work_date
  FROM generate_series(CURRENT_DATE - INTERVAL '120 day', CURRENT_DATE, INTERVAL '1 day') d
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
CROSS JOIN LATERAL (SELECT random() + (e.id * 0) AS r1, random() + (e.id * 0) AS r2, random() + (e.id * 0) AS r3) r
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
    (CURRENT_DATE + (((e.id * 7) + (n.seq * 13)) % 60 - 30))::date AS start_date,
    (CURRENT_DATE + (((e.id * 7) + (n.seq * 13)) % 60 - 30) + ((e.id + n.seq) % 3))::date AS end_date,
    ((e.id + n.seq) % 3 + 1)::DECIMAL(4, 1) AS days,
    CASE
      WHEN (e.id + n.seq) % 5 = 0 THEN 'PENDING'
      WHEN (e.id + n.seq) % 6 = 0 THEN 'REJECTED'
      ELSE 'APPROVED'
    END AS status
) req;

-- 6.9.1 Update session cho các đơn có ngày lẻ
UPDATE leave_requests
SET half_day_session = 'MORNING'
WHERE days % 1 != 0 AND id % 2 = 0;

UPDATE leave_requests
SET half_day_session = 'AFTERNOON'
WHERE days % 1 != 0 AND id % 2 != 0;

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
  (CURRENT_DATE + (((e.id * 5) + (n.seq * 11)) % 30 - 15))::date,
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
