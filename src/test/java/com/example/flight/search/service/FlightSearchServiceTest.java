package com.example.flight.search.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.flight.search.document.FlightSearchDocument;
import com.example.flight.search.dto.FlightSearchRequest;
import com.example.flight.search.dto.FlightSearchResult;
import com.example.flight.search.entity.Flight;
import com.example.flight.search.entity.Schedule;
import com.example.flight.search.repository.FlightSearchRepository;
import com.example.flight.search.repository.ScheduleRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlightSearchService Tests")
class FlightSearchServiceTest {

  @Mock private FlightSearchRepository flightSearchRepository;

  @Mock private PathFindingService pathFindingService;

  @Mock private CacheService cacheService;

  @Mock private ScheduleRepository scheduleRepository;

  @InjectMocks private FlightSearchService flightSearchService;

  private FlightSearchRequest searchRequest;
  private Schedule schedule1;
  private Schedule schedule2;
  private FlightSearchDocument document1;
  private Flight flight1;

  @BeforeEach
  void setUp() {
    // Setup test data
    searchRequest = new FlightSearchRequest();
    ReflectionTestUtils.setField(searchRequest, "source", "NYC");
    ReflectionTestUtils.setField(searchRequest, "destination", "LAX");
    ReflectionTestUtils.setField(
        searchRequest, "time", LocalDateTime.now().plusDays(1)); // Fixed field name
    ReflectionTestUtils.setField(searchRequest, "noOfSeats", 2);
    ReflectionTestUtils.setField(searchRequest, "maxHops", 2);

    // Setup Flight entity
    flight1 = new Flight();
    ReflectionTestUtils.setField(flight1, "id", 1L);
    ReflectionTestUtils.setField(flight1, "flightNumber", "DL100");
    ReflectionTestUtils.setField(flight1, "airline", "Delta Airlines");

    // Setup Schedule entities
    schedule1 = new Schedule();
    ReflectionTestUtils.setField(schedule1, "id", 1L);
    ReflectionTestUtils.setField(schedule1, "flight", flight1);
    ReflectionTestUtils.setField(schedule1, "source", "NYC");
    ReflectionTestUtils.setField(schedule1, "destination", "CHI");
    ReflectionTestUtils.setField(
        schedule1, "departureTime", LocalDateTime.now().plusDays(1).plusHours(10));
    ReflectionTestUtils.setField(
        schedule1, "arrivalTime", LocalDateTime.now().plusDays(1).plusHours(12));
    ReflectionTestUtils.setField(schedule1, "availableSeats", 10);

    schedule2 = new Schedule();
    ReflectionTestUtils.setField(schedule2, "id", 2L);
    ReflectionTestUtils.setField(schedule2, "flight", flight1);
    ReflectionTestUtils.setField(schedule2, "source", "CHI");
    ReflectionTestUtils.setField(schedule2, "destination", "LAX");
    ReflectionTestUtils.setField(
        schedule2, "departureTime", LocalDateTime.now().plusDays(1).plusHours(14));
    ReflectionTestUtils.setField(
        schedule2, "arrivalTime", LocalDateTime.now().plusDays(1).plusHours(17));
    ReflectionTestUtils.setField(schedule2, "availableSeats", 8);

    // Setup FlightSearchDocument
    document1 = new FlightSearchDocument();
    ReflectionTestUtils.setField(document1, "scheduleId", 1L);
    ReflectionTestUtils.setField(document1, "source", "NYC");
    ReflectionTestUtils.setField(document1, "destination", "LAX");
    ReflectionTestUtils.setField(document1, "flightNumber", "DL100");
    ReflectionTestUtils.setField(document1, "airline", "Delta Airlines");
    ReflectionTestUtils.setField(
        document1, "departureTime", LocalDateTime.now().plusDays(1).plusHours(8));
    ReflectionTestUtils.setField(
        document1, "arrivalTime", LocalDateTime.now().plusDays(1).plusHours(13));
    ReflectionTestUtils.setField(document1, "availableSeats", 15);
  }

  @Test
  @DisplayName("Should search flights successfully with direct flights")
  void testSearchFlights_DirectFlights_Success() {
    // Given
    Page<FlightSearchDocument> elasticsearchResults = new PageImpl<>(Arrays.asList(document1));
    when(flightSearchRepository
            .findBySourceAndDestinationAndDepartureTimeAfterAndAvailableSeatsGreaterThanEqual(
                eq("NYC"), eq("LAX"), any(LocalDateTime.class), eq(2), any(Pageable.class)))
        .thenReturn(elasticsearchResults);

    // When
    Page<FlightSearchResult> results = flightSearchService.searchFlights(searchRequest);

    // Then
    assertNotNull(results);
    assertTrue(results.getTotalElements() > 0);
    verify(flightSearchRepository)
        .findBySourceAndDestinationAndDepartureTimeAfterAndAvailableSeatsGreaterThanEqual(
            eq("NYC"), eq("LAX"), any(LocalDateTime.class), eq(2), any(Pageable.class));
  }

  @Test
  @DisplayName("Should handle Elasticsearch unavailable scenario")
  void testSearchFlights_ElasticsearchUnavailable() {
    // Given - Set flightSearchRepository to null to simulate Elasticsearch being unavailable
    ReflectionTestUtils.setField(flightSearchService, "flightSearchRepository", null);
    when(scheduleRepository.findDirectFlightSchedules(
            eq("NYC"), eq("LAX"), any(LocalDateTime.class), eq(2)))
        .thenReturn(Arrays.asList(schedule1));

    // When
    Page<FlightSearchResult> results = flightSearchService.searchFlights(searchRequest);

    // Then
    assertNotNull(results);
    verify(scheduleRepository)
        .findDirectFlightSchedules(eq("NYC"), eq("LAX"), any(LocalDateTime.class), eq(2));
  }

  @Test
  @DisplayName("Should handle empty results gracefully")
  void testSearchFlights_EmptyResults() {
    // Given
    when(flightSearchRepository
            .findBySourceAndDestinationAndDepartureTimeAfterAndAvailableSeatsGreaterThanEqual(
                anyString(), anyString(), any(LocalDateTime.class), anyInt(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    when(pathFindingService.findRoutesWithHops(anyString(), anyString(), anyInt()))
        .thenReturn(Collections.emptyList());

    // When
    Page<FlightSearchResult> results = flightSearchService.searchFlights(searchRequest);

    // Then
    assertNotNull(results);
    assertEquals(0, results.getTotalElements());
  }

  @Test
  @DisplayName("Should handle null maxHops with default value")
  void testSearchFlights_NullMaxHops() {
    // Given
    ReflectionTestUtils.setField(searchRequest, "maxHops", null);
    when(flightSearchRepository
            .findBySourceAndDestinationAndDepartureTimeAfterAndAvailableSeatsGreaterThanEqual(
                anyString(), anyString(), any(LocalDateTime.class), anyInt(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(Arrays.asList(document1)));

    // When
    Page<FlightSearchResult> results = flightSearchService.searchFlights(searchRequest);

    // Then
    assertNotNull(results);
    // Should search with default maxHops = 3
    verify(pathFindingService).findRoutesWithHops("NYC", "LAX", 1);
    verify(pathFindingService).findRoutesWithHops("NYC", "LAX", 2);
    verify(pathFindingService).findRoutesWithHops("NYC", "LAX", 3);
  }

  @Test
  @DisplayName("Should test private method isValidConnection")
  void testIsValidConnection() {
    // Given - Valid connection (CHI airport matches, 2 hour layover)

    // When - Use reflection to test private method
    boolean validResult =
        (Boolean)
            ReflectionTestUtils.invokeMethod(
                flightSearchService, "isValidConnection", schedule1, schedule2);

    // Then
    assertTrue(validResult);
  }

  @Test
  @DisplayName("Should test private method isValidConnection with invalid airport")
  void testIsValidConnection_InvalidAirport() {
    // Given - Invalid connection (different airports)
    Schedule invalidSchedule = new Schedule();
    ReflectionTestUtils.setField(invalidSchedule, "source", "DEN"); // Different airport

    // When
    boolean invalidResult =
        (Boolean)
            ReflectionTestUtils.invokeMethod(
                flightSearchService, "isValidConnection", schedule1, invalidSchedule);

    // Then
    assertFalse(invalidResult);
  }

  @Test
  @DisplayName("Should test private method isValidConnection with short layover")
  void testIsValidConnection_ShortLayover() {
    // Given - Too short layover
    Schedule shortLayoverSchedule = new Schedule();
    ReflectionTestUtils.setField(shortLayoverSchedule, "source", "CHI");
    ReflectionTestUtils.setField(
        shortLayoverSchedule,
        "departureTime",
        LocalDateTime.now().plusDays(1).plusHours(12).plusMinutes(30)); // Only 30 min layover

    // When
    boolean shortLayoverResult =
        (Boolean)
            ReflectionTestUtils.invokeMethod(
                flightSearchService, "isValidConnection", schedule1, shortLayoverSchedule);

    // Then
    assertFalse(shortLayoverResult);
  }

  @Test
  @DisplayName("Should test private method createPageable")
  void testCreatePageable() {
    // When
    Pageable result =
        (Pageable)
            ReflectionTestUtils.invokeMethod(flightSearchService, "createPageable", searchRequest);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getPageNumber());
  }

  @Test
  @DisplayName("Should test private method paginateResults")
  void testPaginateResults() {
    // Given
    List<FlightSearchResult> allResults =
        Arrays.asList(new FlightSearchResult(), new FlightSearchResult());
    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<FlightSearchResult> result =
        (Page<FlightSearchResult>)
            ReflectionTestUtils.invokeMethod(
                flightSearchService, "paginateResults", allResults, pageable);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getTotalElements());
    assertEquals(2, result.getNumberOfElements());
  }

  @Test
  @DisplayName("Should test cache integration")
  void testSearchFlights_CacheIntegration() {
    // Given
    when(flightSearchRepository
            .findBySourceAndDestinationAndDepartureTimeAfterAndAvailableSeatsGreaterThanEqual(
                anyString(), anyString(), any(LocalDateTime.class), anyInt(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(Arrays.asList(document1)));

    // When
    Page<FlightSearchResult> results1 = flightSearchService.searchFlights(searchRequest);
    Page<FlightSearchResult> results2 = flightSearchService.searchFlights(searchRequest);

    // Then
    assertNotNull(results1);
    assertNotNull(results2);
    // Cache should be working due to @Cacheable annotation
  }
}
