package com.hrm.backend.service.payroll.export;

import com.hrm.backend.dto.DepartmentResponse;
import com.hrm.backend.dto.PayrollResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrototypePatternAuditorTest {

    @Test
    @DisplayName("Chỉ ra lỗi Shallow Clone của Lombok @Builder(toBuilder=true) trên PayrollResponse")
    void testShallowCloneVulnerability_PayrollResponse() {
        // 1. Chuẩn bị Map allowances có thể thay đổi (Mutable Map)
        Map<String, BigDecimal> originalAllowances = new HashMap<>();
        originalAllowances.put("Phụ cấp ăn trưa", new BigDecimal("1000000"));

        Map<String, BigDecimal> originalDeductions = new HashMap<>();
        originalDeductions.put("BHXH", new BigDecimal("800000"));

        PayrollResponse originalPayroll = PayrollResponse.builder()
                .employeeCode("EMP001")
                .employeeName("Nguyễn Văn A")
                .basicSalary(new BigDecimal("15000000"))
                .allowances(originalAllowances)
                .deductions(originalDeductions)
                .build();

        // 2. Nhân bản đối tượng bằng toBuilder()
        PayrollResponse clonedPayroll = originalPayroll.toBuilder()
                .basicSalary(new BigDecimal("18000000")) // Giả sử tăng lương cơ bản
                .build();

        // 3. Thay đổi dữ liệu trong allowances của đối tượng nhân bản
        clonedPayroll.allowances().put("Phụ cấp độc hại", new BigDecimal("500000"));

        // 4. KIỂM TRA LỖI CHÍ MẠNG: Dữ liệu của đối tượng gốc CŨNG bị thay đổi theo
        // do toBuilder() chỉ thực hiện sao chép nông (Shallow Copy) tham chiếu của Map allowances
        assertThat(originalPayroll.allowances())
                .as("Lỗi vùng nhớ: Đối tượng gốc bị ảnh hưởng khi sửa đổi đối tượng nhân bản!")
                .containsKey("Phụ cấp độc hại");

        System.out.println(">>> THÀNH CÔNG: Đã chứng minh được lỗi dùng chung vùng nhớ (Shallow Clone) trên PayrollResponse!");
        System.out.println("Original Allowances: " + originalPayroll.allowances());
        System.out.println("Cloned Allowances: " + clonedPayroll.allowances());
    }

    @Test
    @DisplayName("Chỉ ra lỗi Shallow Clone của Lombok @Builder(toBuilder=true) trên DepartmentResponse")
    void testShallowCloneVulnerability_DepartmentResponse() {
        // 1. Chuẩn bị danh sách phòng ban con có thể thay đổi (Mutable List)
        List<DepartmentResponse> originalChildren = new ArrayList<>();
        originalChildren.add(DepartmentResponse.builder().id(2).name("Tổ Lập Trình Java").build());

        DepartmentResponse originalDept = DepartmentResponse.builder()
                .id(1)
                .name("Phòng Công Nghệ")
                .children(originalChildren)
                .build();

        // 2. Nhân bản đối tượng bằng toBuilder()
        DepartmentResponse clonedDept = originalDept.toBuilder()
                .name("Phòng Công Nghệ & Giải Pháp")
                .build();

        // 3. Sửa đổi danh sách phòng ban con của đối tượng nhân bản
        clonedDept.children().add(DepartmentResponse.builder().id(3).name("Tổ Mobile").build());

        // 4. KIỂM TRA LỖI CHÍ MẠNG: Danh sách phòng ban con của đối tượng gốc cũng bị thêm phần tử mới
        assertThat(originalDept.children())
                .as("Lỗi vùng nhớ: Danh sách con của đối tượng gốc bị ảnh hưởng!")
                .hasSize(2);

        System.out.println(">>> THÀNH CÔNG: Đã chứng minh được lỗi dùng chung vùng nhớ (Shallow Clone) trên DepartmentResponse!");
        System.out.println("Original Children Count: " + originalDept.children().size());
        System.out.println("Cloned Children Count: " + clonedDept.children().size());
    }

    @Test
    @DisplayName("Giải pháp khắc phục 1: Sử dụng cấu trúc Immutable Collections (Map.copyOf, List.copyOf)")
    void testMitigation_ImmutableCollections() {
        // Khi khởi tạo hoặc map sang DTO, ta ép kiểu dữ liệu sang Immutable (Bất biến)
        Map<String, BigDecimal> mutableAllowances = new HashMap<>();
        mutableAllowances.put("Phụ cấp ăn trưa", new BigDecimal("1000000"));

        // Sử dụng Map.copyOf để tạo Map bất biến
        PayrollResponse originalPayroll = PayrollResponse.builder()
                .employeeCode("EMP001")
                .employeeName("Nguyễn Văn A")
                .allowances(Map.copyOf(mutableAllowances)) // Khắc phục: Map.copyOf sinh ra UnmodifiableMap
                .build();

        PayrollResponse clonedPayroll = originalPayroll.toBuilder()
                .basicSalary(new BigDecimal("18000000"))
                .build();

        // Kiểm tra xem việc sửa đổi trên đối tượng gốc hoặc nhân bản có bị chặn đứng bằng ngoại lệ hay không
        assertThatThrownBy(() -> clonedPayroll.allowances().put("Phụ cấp mới", BigDecimal.ONE))
                .isInstanceOf(UnsupportedOperationException.class);

        System.out.println(">>> THÀNH CÔNG: Giải pháp dùng Bất biến (Immutable) ngăn chặn việc chỉnh sửa trực tiếp cấu trúc dữ liệu tham chiếu!");
    }

    @Test
    @DisplayName("Giải pháp khắc phục 2: Triển khai Deep Copy thủ công hoặc Builder tùy chỉnh")
    void testMitigation_DeepCopyBuilder() {
        // Ta có thể cấu hình Builder tùy chỉnh hoặc phương thức clone thủ công
        Map<String, BigDecimal> mutableAllowances = new HashMap<>();
        mutableAllowances.put("Phụ cấp ăn trưa", new BigDecimal("1000000"));

        PayrollResponse originalPayroll = PayrollResponse.builder()
                .employeeCode("EMP001")
                .employeeName("Nguyễn Văn A")
                .allowances(mutableAllowances)
                .build();

        // Thực hiện sao chép sâu thủ công (Deep Copy) qua một helper hoặc qua việc khởi tạo lại Map mới
        PayrollResponse clonedPayroll = originalPayroll.toBuilder()
                .basicSalary(new BigDecimal("18000000"))
                // Khắc phục: Chủ động tạo instance Map mới độc lập trong quá trình build
                .allowances(originalPayroll.allowances() != null ? new HashMap<>(originalPayroll.allowances()) : null)
                .build();

        // Thay đổi trên clone
        clonedPayroll.allowances().put("Phụ cấp độc hại", new BigDecimal("500000"));

        // Xác minh đối tượng gốc không bị thay đổi
        assertThat(originalPayroll.allowances())
                .doesNotContainKey("Phụ cấp độc hại")
                .hasSize(1);
        assertThat(clonedPayroll.allowances())
                .containsKey("Phụ cấp độc hại")
                .hasSize(2);

        System.out.println(">>> THÀNH CÔNG: Giải pháp Deep Copy thủ công qua Builder giúp hai đối tượng hoàn toàn tách biệt vùng nhớ!");
    }
}
