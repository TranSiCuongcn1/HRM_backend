package com.hrm.backend.config.prototype;

import com.hrm.backend.entity.Department;
import com.hrm.backend.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepartmentPrototypeRegistry {

    private final DepartmentRepository departmentRepository;
    
    // Cache các root departments (cấu trúc cây)
    private final List<Department> rootDepartmentsCache = new CopyOnWriteArrayList<>();
    
    // Cache tất cả departments theo ID để tìm nhanh
    private final Map<Integer, Department> allDepartmentsMap = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void initCache() {
        refreshCache();
    }

    @Transactional(readOnly = true)
    public synchronized void refreshCache() {
        log.info("Đang nạp dữ liệu phòng ban vào Department Prototype Registry Cache...");
        
        List<Department> roots = departmentRepository.findAllByParentIsNull();
        
        rootDepartmentsCache.clear();
        allDepartmentsMap.clear();
        
        for (Department root : roots) {
            // Clone đệ quy để ngắt phiên làm việc của EntityManager (detach)
            Department clonedRoot = root.clonePrototype();
            rootDepartmentsCache.add(clonedRoot);
            addToMapRecursive(clonedRoot);
        }
        
        log.info("Đã nạp xong {} phòng ban gốc vào Department Prototype Registry Cache (Tổng số node: {})", 
                rootDepartmentsCache.size(), allDepartmentsMap.size());
    }

    private void addToMapRecursive(Department dept) {
        if (dept == null) return;
        allDepartmentsMap.put(dept.getId(), dept);
        if (dept.getChildren() != null) {
            for (Department child : dept.getChildren()) {
                addToMapRecursive(child);
            }
        }
    }

    public List<Department> getRootDepartmentsCloned() {
        return rootDepartmentsCache.stream()
                .map(Department::clonePrototype)
                .collect(Collectors.toList());
    }

    public Optional<Department> getDepartmentByIdCloned(Integer id) {
        Department dept = allDepartmentsMap.get(id);
        return dept != null ? Optional.of(dept.clonePrototype()) : Optional.empty();
    }
}
