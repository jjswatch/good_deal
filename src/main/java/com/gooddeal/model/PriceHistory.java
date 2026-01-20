package com.gooddeal.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "price_history")
public class PriceHistory {

    @Id
    @Column(name = "history_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historyId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Stores store;
    
    @Column(name = "old_price")
    private BigDecimal oldPrice;
    
    @Column(name = "new_price", nullable = false)
    private BigDecimal newPrice;

    @CreationTimestamp
    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}
