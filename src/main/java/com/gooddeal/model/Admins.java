package com.gooddeal.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "admins")
public class Admins {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Integer adminId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
