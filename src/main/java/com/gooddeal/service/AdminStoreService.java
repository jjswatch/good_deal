package com.gooddeal.service;

import com.gooddeal.model.Stores;
import java.util.List;

public interface AdminStoreService {
    List<Stores> getAllStores();
    Stores getStoreById(Integer id);
    Stores createStore(Stores store);
    Stores updateStore(Integer id, Stores store);
    void deleteStore(Integer id);
}
