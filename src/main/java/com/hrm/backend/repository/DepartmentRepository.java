package com.hrm.backend.repository;

import com.hrm.backend.entity.Department;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    
    @EntityGraph(attributePaths = {"manager", "parent"})
    Optional<Department> findByCode(String code);
    
    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"manager", "parent"})
    List<Department> findAllByParentIsNull();

    @EntityGraph(attributePaths = {"manager", "parent", "children"})
    Optional<Department> findWithChildrenById(Integer id);

    List<Department> findByManager_Id(Integer managerId);
}
