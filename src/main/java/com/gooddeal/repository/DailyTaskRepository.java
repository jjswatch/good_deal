package com.gooddeal.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gooddeal.model.DailyTask;

public interface DailyTaskRepository extends JpaRepository<DailyTask, Integer> {
    Optional<DailyTask> findByUserIdAndTaskDate(Integer userId, LocalDate date);
}
