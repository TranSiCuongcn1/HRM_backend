package com.hrm.backend.dto;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
public record DepartmentResponse(
    Integer id,
    String code,
    String name,
    String description,
    ManagerInfo manager,
    DepartmentSummary parent,
    List<DepartmentResponse> children,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    @Builder
    public record ManagerInfo(
        Integer id,
        String code,
        String name
    ) {}

    @Builder
    public record DepartmentSummary(
        Integer id,
        String code,
        String name
    ) {}
}
