package com.hrm.backend.config.prototype;

import com.hrm.backend.entity.Shift;
import com.hrm.backend.repository.ShiftRepository;
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
public class ShiftPrototypeRegistry {

    private final ShiftRepository shiftRepository;
    private final Map<Integer, Shift> cacheById = new ConcurrentHashMap<>();
    private final Map<String, Shift> cacheByCode = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void initCache() {
        refreshCache();
    }

    @Transactional(readOnly = true)
    public synchronized void refreshCache() {
        log.info("Đang nạp danh sách ca làm việc vào Shift Prototype Registry Cache...");
        List<Shift> list = shiftRepository.findAll();
        cacheById.clear();
        cacheByCode.clear();
        for (Shift s : list) {
            Shift cloned = s.clonePrototype();
            cacheById.put(cloned.getId(), cloned);
            cacheByCode.put(cloned.getCode(), cloned);
        }
        log.info("Đã nạp {} ca làm việc vào Shift Prototype Registry Cache.", cacheById.size());
    }

    public List<Shift> getAllCloned() {
        return cacheById.values().stream()
                .map(Shift::clonePrototype)
                .collect(Collectors.toList());
    }

    public Optional<Shift> getByIdCloned(Integer id) {
        Shift s = cacheById.get(id);
        return s != null ? Optional.of(s.clonePrototype()) : Optional.empty();
    }

    public Optional<Shift> getByCodeCloned(String code) {
        Shift s = cacheByCode.get(code);
        return s != null ? Optional.of(s.clonePrototype()) : Optional.empty();
    }

    public Optional<Shift> getDefaultShiftCloned() {
        return cacheById.values().stream()
                .filter(Shift::getIsDefault)
                .findFirst()
                .map(Shift::clonePrototype);
    }
}
