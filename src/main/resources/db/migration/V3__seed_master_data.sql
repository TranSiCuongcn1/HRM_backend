-- ==========================================
-- 6. SEED MASTER DATA
-- ==========================================
-- ==========================================

-- 6.0a Seed shifts
INSERT INTO shifts (code, name, start_time, end_time, break_start_time, break_end_time, is_default)
VALUES 
  ('CA1', 'Ca Hành chính', '08:00', '17:30', '12:00', '13:00', true),
  ('CA2', 'Ca Sáng', '06:00', '14:00', '11:30', '12:00', false),
  ('CA3', 'Ca Chiều', '14:00', '22:00', '17:30', '18:00', false)
ON CONFLICT (code) DO NOTHING;

-- 6.0b Seed holidays
INSERT INTO holidays (name, date, is_paid)
VALUES 
  ('Tết Dương lịch', date_trunc('year', CURRENT_DATE)::date, true),
  ('Giỗ Tổ Hùng Vương', (date_trunc('year', CURRENT_DATE) + INTERVAL '3 months 9 days')::date, true),
  ('Giải phóng miền Nam', (date_trunc('year', CURRENT_DATE) + INTERVAL '3 months 29 days')::date, true),
  ('Quốc tế Lao động', (date_trunc('year', CURRENT_DATE) + INTERVAL '4 months')::date, true),
  ('Quốc khánh', (date_trunc('year', CURRENT_DATE) + INTERVAL '8 months 1 day')::date, true)
ON CONFLICT (date) DO NOTHING;

-- 6.1 Seed leave types
INSERT INTO leave_types (code, name, is_paid, description)
VALUES
  ('ANNUAL', 'Nghỉ phép năm', true, 'Nghỉ phép hưởng lương theo năm'),
  ('SICK', 'Nghỉ ốm', true, 'Nghỉ ốm có giấy xác nhận y tế'),
  ('PERSONAL', 'Nghỉ việc riêng', false, 'Nghỉ việc riêng không hưởng lương'),
  ('MATERNITY', 'Nghỉ thai sản', true, 'Nghỉ thai sản theo quy định')
ON CONFLICT (code) DO NOTHING;

-- 6.2 Seed departments
