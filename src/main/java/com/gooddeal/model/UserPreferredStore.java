package com.gooddeal.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
    name = "user_preferred_stores",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "store_id"}),
        @UniqueConstraint(columnNames = {"user_id", "priority"})
    }
)
public class UserPreferredStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Stores store;

    // 1 = 最常去, 2, 3
    @Column(nullable = false)
    private Integer priority;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

