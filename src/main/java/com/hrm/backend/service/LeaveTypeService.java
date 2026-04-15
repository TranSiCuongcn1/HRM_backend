package com.hrm.backend.service;

import com.hrm.backend.dto.LeaveTypeRequest;
import com.hrm.backend.entity.LeaveType;

import java.util.List;

public interface LeaveTypeService {

    LeaveType createLeaveType(LeaveTypeRequest request);

    List<LeaveType> getAllLeaveTypes();

    LeaveType updateLeaveType(Integer id, LeaveTypeRequest request);
}
