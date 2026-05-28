-- =========================================
-- 4. TẠO FUNCTIONS & TRIGGERS
-- =========================================

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
