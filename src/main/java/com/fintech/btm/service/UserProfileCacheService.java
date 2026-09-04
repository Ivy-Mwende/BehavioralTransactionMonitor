package com.fintech.btm.service;

import com.fintech.btm.model.UserProfile;
import com.fintech.btm.repository.UserProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserProfileCacheService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileCacheService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Cacheable(value = "userProfiles", key = "#userId", unless = "#result == null")
    public UserProfile getUserProfile(Long userId) {
        log.info("Fetching user profile from database: userId={}", userId);
        Optional<UserProfile> profile = userProfileRepository.findByUserId(userId);
        return profile.orElse(null);
    }

    @CacheEvict(value = "userProfiles", key = "#userId")
    public void evictUserProfileCache(Long userId) {
        log.info("Evicting user profile cache: userId={}", userId);
    }

    @CacheEvict(value = "userProfiles", allEntries = true)
    public void evictAllUserProfileCache() {
        log.info("Evicting all user profile cache");
    }
}
