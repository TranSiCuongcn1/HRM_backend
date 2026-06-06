package com.hrm.backend.entity;

import com.hrm.backend.config.prototype.Prototype;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department implements Prototype<Department> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, unique = true, nullable = false)
    private String code;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager; // Trưởng phòng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent; // Phòng ban cha (cấu trúc cây)

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Department> children;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public Department clonePrototype() {
        Department cloned = Department.builder()
                .id(this.id)
                .code(this.code)
                .name(this.name)
                .description(this.description)
                .manager(this.manager)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
        
        if (this.children != null) {
            List<Department> clonedChildren = this.children.stream()
                    .map(child -> {
                        Department clonedChild = child.clonePrototype();
                        clonedChild.setParent(cloned);
                        return clonedChild;
                    })
                    .collect(Collectors.toList());
            cloned.setChildren(clonedChildren);
        }
        return cloned;
    }
}
