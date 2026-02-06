package com.gooddeal.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "user_points")
@Data
public class UserPoints {

    @Id
    private Integer userId;

    private int points;

    private int level;
    
    @Column(name = "trust_score")
    private BigDecimal trustScore;
}
