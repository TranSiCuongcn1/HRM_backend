package com.hrm.backend.service;

import com.hrm.backend.dto.OvertimeRequestRequest;
import com.hrm.backend.dto.OvertimeRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OvertimeRequestService {
    OvertimeRequestResponse createRequest(String username, OvertimeRequestRequest request);
    
    Page<OvertimeRequestResponse> getMyRequests(String username, String status, String keyword, Pageable pageable);
    
    Page<OvertimeRequestResponse> getAllRequests(String status, String keyword, Pageable pageable);
    
    OvertimeRequestResponse approveRequest(Integer requestId, String adminUsername);
    
    OvertimeRequestResponse rejectRequest(Integer requestId, String adminUsername, String reason);
    
    void cancelRequest(Integer requestId, String username);
}
