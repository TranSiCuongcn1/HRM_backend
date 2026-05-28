package com.hrm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.backend.dto.ApiResponse;
import com.hrm.backend.dto.CheckInRequest;
import com.hrm.backend.dto.AttendanceResponse;
import com.hrm.backend.exception.GlobalExceptionHandler;
import com.hrm.backend.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AttendanceStressTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceController attendanceController;

    private static final int CONCURRENT_REQUESTS = 200;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Stress Test - Simulating 200 employees check-in at peak start of shift concurrently")
    void stress_PeakCheckInConcurrency_AllProcessedSuccessfully() throws Exception {
        // Prepare mock behaviors for 200 distinct employee codes
        for (int i = 1; i <= CONCURRENT_REQUESTS; i++) {
            String empCode = "EMP" + String.format("%04d", i);
            AttendanceResponse mockResponse = AttendanceResponse.builder()
                    .id(i)
                    .employeeCode(empCode)
                    .employeeName("Employee " + i)
                    .date(LocalDate.now())
                    .checkIn(LocalTime.of(8, 0))
                    .status("ON_TIME")
                    .workHours(BigDecimal.ZERO)
                    .build();

            when(attendanceService.checkIn(eq(empCode), any(), any(), any()))
                    .thenReturn(mockResponse);
        }

        // Setup Thread Pool and synchronization Latches
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(CONCURRENT_REQUESTS);

        List<Future<Integer>> results = new ArrayList<>();
        
        for (int i = 1; i <= CONCURRENT_REQUESTS; i++) {
            final int id = i;
            final String empCode = "EMP" + String.format("%04d", id);
            
            Callable<Integer> task = () -> {
                try {
                    // Block until startLatch is released to ensure simultaneous launch
                    startLatch.await();
                    
                    Authentication auth = mock(Authentication.class);
                    when(auth.getName()).thenReturn(empCode);

                    CheckInRequest req = new CheckInRequest(new BigDecimal("10.848031"), new BigDecimal("106.784944"));

                    MvcResult result = mockMvc.perform(post("/api/v1/attendance/check-in")
                                    .principal(auth)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                            .andReturn();
                    
                    return result.getResponse().getStatus();
                } catch (Exception e) {
                    return 500;
                } finally {
                    finishLatch.countDown();
                }
            };
            
            results.add(executorService.submit(task));
        }

        // Release the barrier and start all 200 threads at the same millisecond
        startLatch.countDown();
        
        // Wait for all requests to finish processing (timeout after 10 seconds to avoid test hanging)
        boolean finished = finishLatch.await(10, TimeUnit.SECONDS);
        
        // Verify all threads completed
        executorService.shutdown();

        // Evaluate results
        int successCount = 0;
        int failureCount = 0;
        
        for (Future<Integer> future : results) {
            int statusCode = future.get();
            if (statusCode == 201) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        System.out.println("Stress Test Concurrency Output: " + successCount + " successful, " + failureCount + " failed.");
        assertEquals(CONCURRENT_REQUESTS, successCount, "All 200 concurrent check-in requests must succeed");
    }

    @Test
    @DisplayName("Stress Test - Race Condition protection: Double check-in concurrently returns only 1 success")
    void stress_RaceConditionDoubleCheckIn_OnlyOneSucceeds() throws Exception {
        int duplicateCount = 5;
        String empCode = "EMP0005";

        // Mock first request to succeed, subsequent requests to fail with IllegalArgumentException (business rule)
        AttendanceResponse mockSuccess = AttendanceResponse.builder()
                .id(99)
                .employeeCode(empCode)
                .employeeName("Test Employee")
                .date(LocalDate.now())
                .checkIn(LocalTime.of(8, 0))
                .status("ON_TIME")
                .build();

        AtomicInteger callCount = new AtomicInteger(0);
        when(attendanceService.checkIn(eq(empCode), any(), any(), any()))
                .thenAnswer(invocation -> {
                    int current = callCount.incrementAndGet();
                    if (current == 1) {
                        return mockSuccess;
                    } else {
                        throw new IllegalArgumentException("Already checked in today");
                    }
                });

        ExecutorService executorService = Executors.newFixedThreadPool(duplicateCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(duplicateCount);

        List<Future<Integer>> results = new ArrayList<>();

        for (int i = 0; i < duplicateCount; i++) {
            Callable<Integer> task = () -> {
                try {
                    startLatch.await();
                    
                    Authentication auth = mock(Authentication.class);
                    when(auth.getName()).thenReturn(empCode);

                    CheckInRequest req = new CheckInRequest();

                    MvcResult result = mockMvc.perform(post("/api/v1/attendance/check-in")
                                    .principal(auth)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req)))
                            .andReturn();
                    
                    return result.getResponse().getStatus();
                } catch (Exception e) {
                    return 500;
                } finally {
                    finishLatch.countDown();
                }
            };
            results.add(executorService.submit(task));
        }

        // Fire all 5 duplicate check-in requests simultaneously
        startLatch.countDown();
        finishLatch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        // Process results
        int successCount = 0;
        int badRequestCount = 0;

        for (Future<Integer> future : results) {
            int status = future.get();
            if (status == 201) {
                successCount++;
            } else if (status == 400) {
                badRequestCount++;
            }
        }

        System.out.println("Race Condition Concurrency: " + successCount + " success, " + badRequestCount + " blocked cleanly.");
        assertEquals(1, successCount, "Exactly 1 check-in must succeed");
        assertEquals(duplicateCount - 1, badRequestCount, "All other duplicate concurrent requests must be rejected with 400 Bad Request");
    }
}
