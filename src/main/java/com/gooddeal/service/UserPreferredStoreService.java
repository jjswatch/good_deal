package com.gooddeal.service;

import com.gooddeal.model.Stores;
import com.gooddeal.model.UserPreferredStore;
import com.gooddeal.model.Users;
import com.gooddeal.repository.StoresRepository;
import com.gooddeal.repository.UserPreferredStoreRepository;
import com.gooddeal.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserPreferredStoreService {

    private final UserPreferredStoreRepository repo;
    private final UsersRepository usersRepo;
    private final StoresRepository storesRepo;

    public UserPreferredStoreService(
            UserPreferredStoreRepository repo,
            UsersRepository usersRepo,
            StoresRepository storesRepo
    ) {
        this.repo = repo;
        this.usersRepo = usersRepo;
        this.storesRepo = storesRepo;
    }

    /* ================= 查詢 ================= */
    public List<UserPreferredStore> getUserPreferredStores(Integer userId) {
        return repo.findByUserUserIdOrderByPriorityAsc(userId);
    }

    /* ================= 新增 ================= */

    @Transactional
    public UserPreferredStore addPreferredStore(
            Integer userId,
            Integer storeId
    ) {
        if (repo.countByUserUserId(userId) >= 3) {
            throw new IllegalStateException("最多只能設定 3 間常去賣場");
        }

        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Stores store = storesRepo.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        int nextPriority = (int) (repo.countByUserUserId(userId) + 1);

        UserPreferredStore pref = new UserPreferredStore();
        pref.setUser(user);
        pref.setStore(store);
        pref.setPriority(nextPriority);

        return repo.save(pref);
    }

    /* ================= 移除 ================= */
    @Transactional
    public void removePreferredStore(Integer userId, Integer storeId) {
        // 修正：確保參數與 Controller 一致
        repo.deleteByUserUserIdAndStoreStoreId(userId, storeId);
        reOrderPriorities(userId);
    }

    /* ================= 重新排序 ================= */

    @Transactional
    public void updatePriorities(
            Integer userId,
            List<Integer> storeIdsInOrder
    ) {
        List<UserPreferredStore> prefs =
                repo.findByUserUserIdOrderByPriorityAsc(userId);

        for (int i = 0; i < storeIdsInOrder.size(); i++) {
            Integer storeId = storeIdsInOrder.get(i);
            int currentPriority = i + 1;
            prefs.stream()
                .filter(p -> p.getStore().getStoreId().equals(storeId))
                .findFirst()
                .ifPresent(p -> p.setPriority(currentPriority));
        }

        repo.saveAll(prefs);
    }

    private void reOrderPriorities(Integer userId) {
        List<UserPreferredStore> prefs = repo.findByUserUserIdOrderByPriorityAsc(userId);
        for (int i = 0; i < prefs.size(); i++) {
            prefs.get(i).setPriority(i + 1);
        }
        repo.saveAll(prefs);
    }
    
    @Transactional
    public void syncPreferredStores(Integer userId, List<Integer> storeIds) {
        // 1. 清空舊資料
        repo.deleteByUserUserId(userId);

        // 2. 取得使用者
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("找不到使用者"));

        // 3. 依序存入 (最多 3 筆)
        int limit = Math.min(storeIds.size(), 3);
        for (int i = 0; i < limit; i++) {
            Integer sId = storeIds.get(i);
            Stores store = storesRepo.findById(sId)
                    .orElseThrow(() -> new IllegalArgumentException("找不到賣場 ID: " + sId));

            UserPreferredStore pref = new UserPreferredStore();
            pref.setUser(user);
            pref.setStore(store);
            pref.setPriority(i + 1);

            repo.save(pref);
        }
    }
}

