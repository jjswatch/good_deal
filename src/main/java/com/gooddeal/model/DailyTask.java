package com.gooddeal.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "daily_tasks", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "task_date"}))
@Data
public class DailyTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private LocalDate taskDate;

    private boolean firstReportDone;

    private int distinctProductCount;

    private int totalReports;
}
