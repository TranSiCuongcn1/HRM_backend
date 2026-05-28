package com.hrm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hrm.backend.dto.DepartmentRequest;
import com.hrm.backend.dto.DepartmentResponse;
import com.hrm.backend.exception.GlobalExceptionHandler;
import com.hrm.backend.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerBlackBoxTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(departmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Black-box GET /api/v1/departments - Returns departments list")
    void getAllDepartments_ReturnsDepartments() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(List.of(standardResponse()));

        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].code").value("IT"));
    }

    @Test
    @DisplayName("Black-box GET /api/v1/departments/{id} - Returns department detail")
    void getDepartmentById_ReturnsDepartment() throws Exception {
        when(departmentService.getDepartmentById(1)).thenReturn(standardResponse());

        mockMvc.perform(get("/api/v1/departments/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Information Technology"));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/departments - Valid payload should create department")
    void createDepartment_ValidPayload_ReturnsOk() throws Exception {
        when(departmentService.createDepartment(any(DepartmentRequest.class))).thenReturn(standardResponse());

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("IT"));
    }

    @Test
    @DisplayName("Black-box POST /api/v1/departments - Missing code and name should return validation errors")
    void createDepartment_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        DepartmentRequest request = standardRequest();
        request.setCode("");
        request.setName("");

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").exists())
                .andExpect(jsonPath("$.data.name").exists());
    }

    @Test
    @DisplayName("Black-box POST /api/v1/departments - Duplicate code should return 400 Bad Request")
    void createDepartment_DuplicateCode_ReturnsBadRequest() throws Exception {
        when(departmentService.createDepartment(any(DepartmentRequest.class)))
                .thenThrow(new IllegalArgumentException("Department code already exists"));

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Department code already exists"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/departments/{id} - Valid payload should update department")
    void updateDepartment_ValidPayload_ReturnsOk() throws Exception {
        DepartmentResponse updated = standardResponse();
        updated.setName("Engineering");
        when(departmentService.updateDepartment(eq(1), any(DepartmentRequest.class))).thenReturn(updated);

        DepartmentRequest request = standardRequest();
        request.setName("Engineering");

        mockMvc.perform(put("/api/v1/departments/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Engineering"));
    }

    @Test
    @DisplayName("Black-box PUT /api/v1/departments/{id} - Cyclic parent should return 400 Bad Request")
    void updateDepartment_CyclicParent_ReturnsBadRequest() throws Exception {
        when(departmentService.updateDepartment(eq(1), any(DepartmentRequest.class)))
                .thenThrow(new IllegalArgumentException("Parent department cannot be itself"));

        mockMvc.perform(put("/api/v1/departments/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Parent department cannot be itself"));
    }

    @Test
    @DisplayName("Black-box DELETE /api/v1/departments/{id} - Should call delete service")
    void deleteDepartment_ReturnsOk() throws Exception {
        doNothing().when(departmentService).deleteDepartment(1);

        mockMvc.perform(delete("/api/v1/departments/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(departmentService).deleteDepartment(1);
    }

    @Test
    @DisplayName("Black-box DELETE /api/v1/departments/{id} - Runtime business error currently maps to 500 fallback")
    void deleteDepartment_BusinessRuleViolation_ReturnsInternalServerError() throws Exception {
        doThrow(new RuntimeException("Cannot delete department with employees"))
                .when(departmentService).deleteDepartment(1);

        mockMvc.perform(delete("/api/v1/departments/{id}", 1))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    private DepartmentRequest standardRequest() {
        DepartmentRequest request = new DepartmentRequest();
        request.setCode("IT");
        request.setName("Information Technology");
        request.setDescription("IT Department");
        return request;
    }

    private DepartmentResponse standardResponse() {
        return DepartmentResponse.builder()
                .id(1)
                .code("IT")
                .name("Information Technology")
                .description("IT Department")
                .build();
    }
}
