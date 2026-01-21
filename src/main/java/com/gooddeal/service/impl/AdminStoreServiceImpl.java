package com.gooddeal.service.impl;

import com.gooddeal.model.Stores;
import com.gooddeal.repository.StoresRepository;
import com.gooddeal.service.AdminStoreService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminStoreServiceImpl implements AdminStoreService {

    private final StoresRepository storesRepository;

    public AdminStoreServiceImpl(StoresRepository storesRepository) {
        this.storesRepository = storesRepository;
    }

    @Override
    public List<Stores> getAllStores() {
        return storesRepository.findAll();
    }

    @Override
    public Stores getStoreById(Integer id) {
        return storesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store Not Found"));
    }

    @Override
    public Stores createStore(Stores store) {
        return storesRepository.save(store);
    }

    @Override
    public Stores updateStore(Integer id, Stores store) {
        Stores db = getStoreById(id);
        db.setStoreName(store.getStoreName());
        db.setLocation(store.getLocation());
        db.setWebsite(store.getWebsite());
        db.setStoreGroup(store.getStoreGroup());
        return storesRepository.save(db);
    }

    @Override
    public void deleteStore(Integer id) {
        storesRepository.deleteById(id);
    }
}
