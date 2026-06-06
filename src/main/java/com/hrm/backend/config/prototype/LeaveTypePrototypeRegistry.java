package com.hrm.backend.config.prototype;

import com.hrm.backend.entity.LeaveType;
import com.hrm.backend.repository.LeaveTypeRepository;
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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveTypePrototypeRegistry {

    private final LeaveTypeRepository leaveTypeRepository;
    private final Map<String, LeaveType> cacheByCode = new ConcurrentHashMap<>();
    private final Map<Integer, LeaveType> cacheById = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void initCache() {
        refreshCache();
    }

    @Transactional(readOnly = true)
    public synchronized void refreshCache() {
        log.info("Đang nạp danh mục loại phép vào LeaveType Prototype Registry Cache...");
        List<LeaveType> list = leaveTypeRepository.findAll();
        cacheByCode.clear();
        cacheById.clear();
        for (LeaveType lt : list) {
            LeaveType cloned = lt.clonePrototype();
            cacheByCode.put(cloned.getCode(), cloned);
            cacheById.put(cloned.getId(), cloned);
        }
        log.info("Đã nạp {} loại phép vào LeaveType Prototype Registry Cache.", cacheById.size());
    }

    public List<LeaveType> getAllCloned() {
        return cacheById.values().stream()
                .map(LeaveType::clonePrototype)
                .collect(Collectors.toList());
    }

    public Optional<LeaveType> getByIdCloned(Integer id) {
        LeaveType lt = cacheById.get(id);
        return lt != null ? Optional.of(lt.clonePrototype()) : Optional.empty();
    }

    public Optional<LeaveType> getByCodeCloned(String code) {
        LeaveType lt = cacheByCode.get(code);
        return lt != null ? Optional.of(lt.clonePrototype()) : Optional.empty();
    }
}
