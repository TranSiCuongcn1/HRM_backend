-- =======================================================
-- ADD COLUMNS FOR GPS AND IP ATTENDANCE VERIFICATION
-- =======================================================

ALTER TABLE attendance_records
ADD COLUMN check_in_ip VARCHAR(50),
ADD COLUMN check_in_lat DECIMAL(10, 8),
ADD COLUMN check_in_lng DECIMAL(11, 8),
ADD COLUMN check_in_gps_valid BOOLEAN,
ADD COLUMN check_in_ip_valid BOOLEAN,
ADD COLUMN check_out_ip VARCHAR(50),
ADD COLUMN check_out_lat DECIMAL(10, 8),
ADD COLUMN check_out_lng DECIMAL(11, 8),
ADD COLUMN check_out_gps_valid BOOLEAN,
ADD COLUMN check_out_ip_valid BOOLEAN;
