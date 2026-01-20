package com.gooddeal.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
    name = "user_favorites",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"})
)
public class UserFavorites {

    @Id
    @Column(name = "favorite_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer favoriteId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
