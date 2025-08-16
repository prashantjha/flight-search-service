package com.example.flight.search.service;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnClass(RedisTemplate.class)
public class CacheService {

  private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
  private static final String CACHE_PREFIX = "flight_search:";

  @Autowired(required = false)
  private RedisTemplate<String, Object> redisTemplate;

  public void cacheSearchResult(String key, Object result, long timeoutMinutes) {
    if (redisTemplate == null) {
      logger.debug("Redis not available - skipping cache operation");
      return;
    }

    try {
      String cacheKey = CACHE_PREFIX + key;
      redisTemplate.opsForValue().set(cacheKey, result, timeoutMinutes, TimeUnit.MINUTES);
      logger.debug("Cached search result with key: {}", cacheKey);
    } catch (Exception e) {
      logger.error("Error caching search result: {}", e.getMessage());
    }
  }

  public Object getCachedResult(String key) {
    if (redisTemplate == null) {
      logger.debug("Redis not available - cache miss");
      return null;
    }

    try {
      String cacheKey = CACHE_PREFIX + key;
      Object result = redisTemplate.opsForValue().get(cacheKey);
      if (result != null) {
        logger.debug("Cache hit for key: {}", cacheKey);
      }
      return result;
    } catch (Exception e) {
      logger.error("Error retrieving cached result: {}", e.getMessage());
      return null;
    }
  }

  public void evictCache(String key) {
    try {
      String cacheKey = CACHE_PREFIX + key;
      redisTemplate.delete(cacheKey);
      logger.debug("Evicted cache for key: {}", cacheKey);
    } catch (Exception e) {
      logger.error("Error evicting cache: {}", e.getMessage());
    }
  }

  public void evictAllFlightSearchCache() {
    try {
      redisTemplate.delete(redisTemplate.keys(CACHE_PREFIX + "*"));
      logger.info("Evicted all flight search cache");
    } catch (Exception e) {
      logger.error("Error evicting all cache: {}", e.getMessage());
    }
  }
}
