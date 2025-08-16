package com.example.flight.search.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheService Tests")
class CacheServiceTest {

  @Mock private RedisTemplate<String, Object> redisTemplate;

  @Mock private ValueOperations<String, Object> valueOperations;

  @InjectMocks private CacheService cacheService;

  private static final String TEST_KEY = "test_key";
  private static final String TEST_VALUE = "test_value";
  private static final String CACHE_PREFIX = "flight_search:";

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  @DisplayName("Should cache search result successfully")
  void testCacheSearchResult_Success() {
    // When
    cacheService.cacheSearchResult(TEST_KEY, TEST_VALUE, 30L);

    // Then
    verify(valueOperations).set(CACHE_PREFIX + TEST_KEY, TEST_VALUE, 30L, TimeUnit.MINUTES);
  }

  @Test
  @DisplayName("Should handle Redis exception when caching")
  void testCacheSearchResult_RedisException() {
    // Given
    doThrow(new RuntimeException("Redis connection failed"))
        .when(valueOperations)
        .set(anyString(), any(), anyLong(), any(TimeUnit.class));

    // When & Then
    assertDoesNotThrow(
        () -> {
          cacheService.cacheSearchResult(TEST_KEY, TEST_VALUE, 30L);
        });
  }

  @Test
  @DisplayName("Should get cached result successfully")
  void testGetCachedResult_Success() {
    // Given
    when(valueOperations.get(CACHE_PREFIX + TEST_KEY)).thenReturn(TEST_VALUE);

    // When
    Object result = cacheService.getCachedResult(TEST_KEY);

    // Then
    assertEquals(TEST_VALUE, result);
    verify(valueOperations).get(CACHE_PREFIX + TEST_KEY);
  }

  @Test
  @DisplayName("Should return null when cached result not found")
  void testGetCachedResult_NotFound() {
    // Given
    when(valueOperations.get(CACHE_PREFIX + TEST_KEY)).thenReturn(null);

    // When
    Object result = cacheService.getCachedResult(TEST_KEY);

    // Then
    assertNull(result);
    verify(valueOperations).get(CACHE_PREFIX + TEST_KEY);
  }

  @Test
  @DisplayName("Should handle Redis exception when getting cached result")
  void testGetCachedResult_RedisException() {
    // Given
    when(valueOperations.get(anyString()))
        .thenThrow(new RuntimeException("Redis connection failed"));

    // When
    Object result = cacheService.getCachedResult(TEST_KEY);

    // Then
    assertNull(result);
  }
}
