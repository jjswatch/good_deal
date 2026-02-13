package com.gooddeal.controller;

import com.gooddeal.model.UserPreferredStore;
import com.gooddeal.service.UserPreferredStoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/preferred-stores")
public class UserPreferredStoreController {

    private final UserPreferredStoreService service;

    public UserPreferredStoreController(UserPreferredStoreService service) {
        this.service = service;
    }

    // 取得使用者偏好賣場
    @GetMapping("/{userId}")
    public List<UserPreferredStore> getUserPreferredStores(
            @PathVariable Integer userId
    ) {
        return service.getUserPreferredStores(userId);
    }
    
    @PostMapping("/sync")
    public void syncStores(@RequestBody Map<String, Object> body) {
    	Object userIdObj = body.get("userId");
        Object storeIdsObj = body.get("storeIds");

        if (userIdObj == null) throw new IllegalArgumentException("錯誤：userId 遺失");
        if (storeIdsObj == null) throw new IllegalArgumentException("錯誤：storeIds 遺失");
        Integer userId = Integer.valueOf(userIdObj.toString());
        @SuppressWarnings("unchecked")
        List<Integer> storeIds = (List<Integer>) storeIdsObj;

        service.syncPreferredStores(userId, storeIds);
    }

    // 新增偏好賣場（最多 3）
    @PostMapping
    public UserPreferredStore addPreferredStore(
            @RequestBody Map<String, Integer> body
    ) {
        return service.addPreferredStore(
                body.get("userId"),
                body.get("storeId")
        );
    }

    // 移除偏好賣場
    @DeleteMapping
    public void removePreferredStore(@RequestBody Map<String, Object> body) {
        Integer userId = Integer.valueOf(body.get("userId").toString());
        Integer storeId = Integer.valueOf(body.get("storeId").toString());
        
        service.removePreferredStore(userId, storeId);
    }

    // 更新排序（拖拉用）
    @PutMapping("/reorder")
    public void reorder(
            @RequestBody Map<String, Object> body
    ) {
        Integer userId = (Integer) body.get("userId");
        @SuppressWarnings("unchecked")
		List<Integer> storeIds =
                (List<Integer>) body.get("storeIds");

        service.updatePriorities(userId, storeIds);
    }
    
}

