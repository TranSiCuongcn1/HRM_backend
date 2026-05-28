package com.hrm.backend.listener;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom JUnit 5 Test Execution Listener.
 * Intercepts test life cycles to print a beautiful console ASCII table 
 * and generate a high-fidelity Markdown test report (TEST_REPORT.md).
 */
public class UnitTestReportListener implements TestExecutionListener {

    private final Map<String, Long> startTimes = new ConcurrentHashMap<>();
    private final List<TestResult> results = Collections.synchronizedList(new ArrayList<>());
    
    // Statistics
    private final AtomicInteger totalCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger skippedCount = new AtomicInteger(0);

    private static class TestResult {
        String className;
        String methodName;
        String displayName;
        String status; // SUCCESS, FAILED, SKIPPED
        long durationMs;
        String errorMessage;
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            startTimes.put(testIdentifier.getUniqueId(), System.currentTimeMillis());
            totalCount.incrementAndGet();
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            long endTime = System.currentTimeMillis();
            Long startTime = startTimes.remove(testIdentifier.getUniqueId());
            long duration = (startTime != null) ? (endTime - startTime) : 0;

            TestResult result = new TestResult();
            result.displayName = testIdentifier.getDisplayName();
            result.durationMs = duration;

            testIdentifier.getSource().ifPresent(source -> {
                if (source instanceof MethodSource) {
                    MethodSource methodSource = (MethodSource) source;
                    result.className = methodSource.getClassName();
                    result.methodName = methodSource.getMethodName();
                }
            });

            if (result.className == null) {
                result.className = "UnknownSuite";
            } else {
                // Extract simple class name
                int lastDot = result.className.lastIndexOf('.');
                if (lastDot != -1) {
                    result.className = result.className.substring(lastDot + 1);
                }
            }

            switch (testExecutionResult.getStatus()) {
                case SUCCESSFUL:
                    result.status = "SUCCESS";
                    successCount.incrementAndGet();
                    break;
                case FAILED:
                    result.status = "FAILED";
                    failureCount.incrementAndGet();
                    result.errorMessage = testExecutionResult.getThrowable()
                            .map(this::getStackTraceAsString)
                            .orElse("Unknown error");
                    break;
                case ABORTED:
                    result.status = "ABORTED";
                    skippedCount.incrementAndGet();
                    result.errorMessage = testExecutionResult.getThrowable()
                            .map(Throwable::getMessage)
                            .orElse("Aborted");
                    break;
            }
            results.add(result);
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        if (testIdentifier.isTest()) {
            totalCount.incrementAndGet();
            skippedCount.incrementAndGet();

            TestResult result = new TestResult();
            result.displayName = testIdentifier.getDisplayName();
            result.status = "SKIPPED";
            result.durationMs = 0;
            result.errorMessage = reason != null ? reason : "Skipped";

            testIdentifier.getSource().ifPresent(source -> {
                if (source instanceof MethodSource) {
                    MethodSource methodSource = (MethodSource) source;
                    result.className = methodSource.getClassName();
                    result.methodName = methodSource.getMethodName();
                }
            });

            if (result.className == null) {
                result.className = "UnknownSuite";
            } else {
                int lastDot = result.className.lastIndexOf('.');
                if (lastDot != -1) {
                    result.className = result.className.substring(lastDot + 1);
                }
            }
            results.add(result);
        }
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        if (totalCount.get() == 0) {
            return;
        }

        // Sort results by ClassName then DisplayName
        List<TestResult> sortedResults;
        synchronized (results) {
            sortedResults = new ArrayList<>(results);
        }
        sortedResults.sort(Comparator.comparing((TestResult r) -> r.className)
                .thenComparing(r -> r.displayName));

        // Generate Console Output
        printConsoleReport(sortedResults);

        // Generate Markdown File
        generateMarkdownReport(sortedResults);
    }

    private void printConsoleReport(List<TestResult> sortedResults) {
        String reset = "\u001B[0m";
        String bold = "\u001B[1m";
        String green = "\u001B[32m";
        String red = "\u001B[31m";
        String yellow = "\u001B[33m";
        String cyan = "\u001B[36m";

        System.out.println("\n");
        System.out.println(bold + cyan + "==========================================================================================" + reset);
        System.out.println(bold + cyan + "                                HRM BACKEND TEST REPORT                                  " + reset);
        System.out.println(bold + cyan + "==========================================================================================" + reset);
        System.out.printf(bold + " | %-10s | %-32s | %-32s | %-7s |%n", "STATUS", "TEST SUITE", "TEST CASE / METHOD", "TIME");
        System.out.println(cyan + "------------------------------------------------------------------------------------------" + reset);

        for (TestResult result : sortedResults) {
            String statusColor = reset;
            String statusText = result.status;
            if ("SUCCESS".equals(result.status)) {
                statusColor = green;
                statusText = "SUCCESS";
            } else if ("FAILED".equals(result.status)) {
                statusColor = red;
                statusText = "FAILED";
            } else if ("SKIPPED".equals(result.status) || "ABORTED".equals(result.status)) {
                statusColor = yellow;
                statusText = result.status;
            }

            // Truncate to avoid table overflow in narrow terminals
            String suiteName = truncate(result.className, 32);
            String testCaseName = truncate(result.displayName, 32);
            String duration = result.durationMs + "ms";

            System.out.printf(" | " + statusColor + "%-10s" + reset + " | %-32s | %-32s | %-7s |%n",
                    statusText, suiteName, testCaseName, duration);
        }

        System.out.println(bold + cyan + "==========================================================================================" + reset);
        
        // Print Summary stats
        int total = totalCount.get();
        int passed = successCount.get();
        int failed = failureCount.get();
        int skipped = skippedCount.get();
        double successRate = total > 0 ? (passed * 100.0 / total) : 0;
        
        System.out.printf(bold + " SUMMARY: Total: %d | " + green + "Passed: %d" + reset + bold + " | " + red + "Failed: %d" + reset + bold + " | " + yellow + "Skipped: %d" + reset + bold + " | Success Rate: %.1f%%%n", 
                total, passed, failed, skipped, successRate);
        System.out.println(bold + cyan + "==========================================================================================" + reset);
        System.out.println("\n");
    }

    private void generateMarkdownReport(List<TestResult> sortedResults) {
        StringBuilder sb = new StringBuilder();
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        int total = totalCount.get();
        int passed = successCount.get();
        int failed = failureCount.get();
        int skipped = skippedCount.get();
        double successRate = total > 0 ? (passed * 100.0 / total) : 0;
        long totalDurationMs = sortedResults.stream().mapToLong(r -> r.durationMs).sum();

        sb.append("# HRM Backend Test Execution Report\n\n");
        sb.append("> **Thời gian báo cáo:** `").append(timestamp).append("`\n\n");

        sb.append("## Thống Kê Tổng Quan\n\n");
        sb.append("| Chỉ Số | Giá Trị |\n");
        sb.append("| :--- | :--- |\n");
        sb.append("| **Tổng số Test Cases** | `").append(total).append("` |\n");
        sb.append("| **Thành công (Passed)** | `").append(passed).append("` |\n");
        sb.append("| **Thất bại (Failed)** | `").append(failed).append("` |\n");
        sb.append("| **Bỏ qua (Skipped)** | `").append(skipped).append("` |\n");
        sb.append("| **Tỷ lệ thành công** | **").append(String.format("%.1f%%", successRate)).append("** |\n");
        sb.append("| **Tổng thời gian chạy** | `").append(String.format("%.2f s", totalDurationMs / 1000.0)).append("` |\n\n");

        sb.append("## Chi Tiết Kết Quả Kiểm Thử\n\n");
        sb.append("| Trạng Thái | Bộ Kiểm Thử (Suite) | Tên Test Case (Method) | Thời Gian Chạy |\n");
        sb.append("| :---: | :--- | :--- | :---: |\n");

        for (TestResult result : sortedResults) {
            String statusText = result.status;

            sb.append("| ").append(statusText)
              .append(" | `").append(result.className).append("`")
              .append(" | ").append(result.displayName)
              .append(" | `").append(result.durationMs).append(" ms` |\n");
        }

        if (failed > 0) {
            sb.append("\n## Chi Tiết Các Lỗi Phóng Phát (Failures Detail)\n\n");
            for (TestResult result : sortedResults) {
                if ("FAILED".equals(result.status)) {
                    sb.append("### FAILED: ").append(result.className).append(" ➔ ").append(result.displayName).append("\n");
                    sb.append("```\n").append(result.errorMessage).append("\n```\n\n");
                }
            }
        }

        try {
            // Write inside target directory of the backend module
            Path targetReportPath = Paths.get("target/TEST_RESULT.md");
            Files.createDirectories(targetReportPath.getParent());
            Files.writeString(targetReportPath, sb.toString(), StandardCharsets.UTF_8);
            
            // Write inside docs/test-reports under backend module root (HRM_backend/docs/test-reports)
            Path docsReportDir = Paths.get("docs/test-reports");
            Files.createDirectories(docsReportDir);
            Path docsReportPath = docsReportDir.resolve("TEST_RESULT.md");
            Files.writeString(docsReportPath, sb.toString(), StandardCharsets.UTF_8);
            System.out.println("[INFO] [TestReportListener] Successfully wrote clean test result to: " + docsReportPath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to write test result: " + e.getMessage());
        }
    }

    private String truncate(String text, int length) {
        if (text == null) {
            return "";
        }
        if (text.length() <= length) {
            return text;
        }
        return text.substring(0, length - 3) + "...";
    }
}
