package com.hrm.backend.service.payroll.export;

import com.hrm.backend.config.prototype.DepartmentPrototypeRegistry;
import com.hrm.backend.config.prototype.LeaveTypePrototypeRegistry;
import com.hrm.backend.config.prototype.ShiftPrototypeRegistry;
import com.hrm.backend.entity.Department;
import com.hrm.backend.entity.LeaveType;
import com.hrm.backend.entity.Shift;
import com.hrm.backend.repository.DepartmentRepository;
import com.hrm.backend.repository.LeaveTypeRepository;
import com.hrm.backend.repository.ShiftRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GofPrototypePatternTest {

    @Test
    @DisplayName("Xác thực Deep Copy của Department và cấu trúc cây con")
    void testDepartmentDeepCopy() {
        // 1. Tạo cây phòng ban mẫu
        Department child1 = Department.builder().id(2).code("SUB1").name("Tổ 1").build();
        Department child2 = Department.builder().id(3).code("SUB2").name("Tổ 2").build();
        List<Department> children = new ArrayList<>();
        children.add(child1);
        children.add(child2);

        Department parent = Department.builder()
                .id(1)
                .code("PARENT")
                .name("Phòng Lớn")
                .children(children)
                .build();
        child1.setParent(parent);
        child2.setParent(parent);

        // 2. Clone prototype
        Department cloned = parent.clonePrototype();

        // 3. Thay đổi thông tin trên bản clone
        cloned.setName("Phòng Lớn Đã Sửa");
        cloned.getChildren().get(0).setName("Tổ 1 Đã Sửa");

        // 4. Xác minh: Đối tượng gốc KHÔNG bị thay đổi (Deep Copy thành công)
        assertThat(parent.getName()).isEqualTo("Phòng Lớn");
        assertThat(parent.getChildren().get(0).getName()).isEqualTo("Tổ 1");

        assertThat(cloned.getName()).isEqualTo("Phòng Lớn Đã Sửa");
        assertThat(cloned.getChildren().get(0).getName()).isEqualTo("Tổ 1 Đã Sửa");

        // 5. Xác minh liên kết parent của các con ở bản clone trỏ đến chính cloned parent
        assertThat(cloned.getChildren().get(0).getParent()).isSameAs(cloned);
        assertThat(cloned.getChildren().get(0).getParent().getName()).isEqualTo("Phòng Lớn Đã Sửa");
    }

    @Test
    @DisplayName("Xác thực hoạt động của DepartmentPrototypeRegistry (Cache & Clone)")
    void testDepartmentPrototypeRegistry() {
        DepartmentRepository mockRepo = mock(DepartmentRepository.class);
        
        Department root = Department.builder().id(1).code("ROOT").name("Phòng Gốc").children(new ArrayList<>()).build();
        when(mockRepo.findAllByParentIsNull()).thenReturn(List.of(root));

        DepartmentPrototypeRegistry registry = new DepartmentPrototypeRegistry(mockRepo);
        
        // Nạp cache
        registry.refreshCache();
        
        // Lấy bản clone lần 1 và lần 2
        Optional<Department> clone1 = registry.getDepartmentByIdCloned(1);
        Optional<Department> clone2 = registry.getDepartmentByIdCloned(1);

        assertThat(clone1).isPresent();
        assertThat(clone2).isPresent();
        
        // Hai bản clone phải là 2 instance khác nhau trong bộ nhớ
        assertThat(clone1.get()).isNotSameAs(clone2.get());
        
        // Thay đổi clone 1 không ảnh hưởng đến clone 2
        clone1.get().setName("Tên Mới");
        assertThat(clone2.get().getName()).isEqualTo("Phòng Gốc");
        
        // Xác minh chỉ truy vấn DB 1 lần lúc nạp cache, các lần sau lấy từ cache
        verify(mockRepo, times(1)).findAllByParentIsNull();
    }

    @Test
    @DisplayName("Xác thực hoạt động của LeaveTypePrototypeRegistry (Cache & Clone)")
    void testLeaveTypePrototypeRegistry() {
        LeaveTypeRepository mockRepo = mock(LeaveTypeRepository.class);
        LeaveType lt = LeaveType.builder().id(1).code("ANNUAL").name("Phép năm").isPaid(true).build();
        when(mockRepo.findAll()).thenReturn(List.of(lt));

        LeaveTypePrototypeRegistry registry = new LeaveTypePrototypeRegistry(mockRepo);
        registry.refreshCache();

        Optional<LeaveType> clone1 = registry.getByCodeCloned("ANNUAL");
        Optional<LeaveType> clone2 = registry.getByCodeCloned("ANNUAL");

        assertThat(clone1).isPresent();
        assertThat(clone2).isPresent();
        assertThat(clone1.get()).isNotSameAs(clone2.get());
        
        clone1.get().setName("Phép năm sửa đổi");
        assertThat(clone2.get().getName()).isEqualTo("Phép năm");
    }

    @Test
    @DisplayName("Xác thực hoạt động của ShiftPrototypeRegistry (Cache & Clone)")
    void testShiftPrototypeRegistry() {
        ShiftRepository mockRepo = mock(ShiftRepository.class);
        Shift shift = Shift.builder()
                .id(1)
                .code("HC")
                .name("Hành chính")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .isDefault(true)
                .build();
        when(mockRepo.findAll()).thenReturn(List.of(shift));

        ShiftPrototypeRegistry registry = new ShiftPrototypeRegistry(mockRepo);
        registry.refreshCache();

        Optional<Shift> clone1 = registry.getDefaultShiftCloned();
        Optional<Shift> clone2 = registry.getDefaultShiftCloned();

        assertThat(clone1).isPresent();
        assertThat(clone2).isPresent();
        assertThat(clone1.get()).isNotSameAs(clone2.get());
        
        clone1.get().setName("Hành chính đổi tên");
        assertThat(clone2.get().getName()).isEqualTo("Hành chính");
    }
}
