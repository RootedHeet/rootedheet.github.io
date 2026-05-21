package com.webcraft.service;

import com.webcraft.model.Website;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage service (replace with JPA Repository + real DB in production).
 * Wire in a WebsiteRepository (extends JpaRepository<Website, Long>) for persistence.
 */
@Service
public class WebsiteStorageService {

    // Replace this map with JPA repository in production
    private final Map<String, Website> store = new ConcurrentHashMap<>();

    public Website save(Website website) {
        store.put(website.getWebsiteId(), website);
        return website;
    }

    public Optional<Website> findByWebsiteId(String websiteId) {
        return Optional.ofNullable(store.get(websiteId));
    }

    public void incrementViewCount(String websiteId) {
        Website w = store.get(websiteId);
        if (w != null) {
            w.setViewCount(w.getViewCount() + 1);
        }
    }

    public long totalCount() {
        return store.size();
    }
}
