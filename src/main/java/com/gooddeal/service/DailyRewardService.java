package com.gooddeal.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gooddeal.model.DailyTask;
import com.gooddeal.model.UserPoints;
import com.gooddeal.repository.DailyTaskRepository;
import com.gooddeal.repository.UserPointsRepository;

import jakarta.transaction.Transactional;

@Service
public class DailyRewardService {

    @Autowired
    private DailyTaskRepository taskRepo;

    @Autowired
    private UserPointsRepository pointRepo;

    @Transactional
    public void handleApprovedReport(
            Integer userId,
            Integer productId,
            boolean isFirstProductToday
    ) {

        LocalDate today = LocalDate.now();

        DailyTask task = taskRepo
            .findByUserIdAndTaskDate(userId, today)
            .orElseGet(() -> {
                DailyTask t = new DailyTask();
                t.setUserId(userId);
                t.setTaskDate(today);
                return t;
            });

        int reward = 0;

        // 1️⃣ 第一筆回報
        if (!task.isFirstReportDone()) {
            reward += 10;
            task.setFirstReportDone(true);
        }

        // 2️⃣ 不同商品（最多 2 次）
        if (isFirstProductToday && task.getDistinctProductCount() < 2) {
            reward += 5;
            task.setDistinctProductCount(task.getDistinctProductCount() + 1);
        }

        // 3️⃣ 每筆核准回報
        reward += 10;

        task.setTotalReports(task.getTotalReports() + 1);
        taskRepo.save(task);

        // 發放點數
        addPoints(userId, reward);
    }

    private void addPoints(Integer userId, int reward) {

        UserPoints up = pointRepo.findById(userId)
            .orElseGet(() -> {
                UserPoints p = new UserPoints();
                p.setUserId(userId);
                p.setPoints(0);
                p.setLevel(1);
                p.setTrustScore(BigDecimal.ZERO);
                return p;
            });

        up.setPoints(up.getPoints() + reward);
        pointRepo.save(up);
    }
}

