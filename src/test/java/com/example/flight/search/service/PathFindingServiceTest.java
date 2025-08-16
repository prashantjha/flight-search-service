package com.example.flight.search.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.flight.search.dto.FlightSearchRequest;
import com.example.flight.search.dto.FlightSearchResult;
import com.example.flight.search.graph.Airport;
import com.example.flight.search.repository.AirportRepository;
import com.example.flight.search.repository.ScheduleRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("PathFindingService Tests")
class PathFindingServiceTest {

  @Mock private AirportRepository airportRepository;

  @Mock private ScheduleRepository scheduleRepository;

  @InjectMocks private PathFindingService pathFindingService;

  private FlightSearchRequest searchRequest;
  private Airport nycAirport;
  private Airport chiAirport;
  private Airport laxAirport;

  @BeforeEach
  void setUp() {
    // Setup test data
    searchRequest = new FlightSearchRequest();
    searchRequest.setSource("NYC");
    searchRequest.setDestination("LAX");
    searchRequest.setNoOfSeats(2);

    // Setup Airport entities
    nycAirport = new Airport();
    nycAirport.setCode("NYC");
    nycAirport.setName("New York City");

    chiAirport = new Airport();
    chiAirport.setCode("CHI");
    chiAirport.setName("Chicago");

    laxAirport = new Airport();
    laxAirport.setCode("LAX");
    laxAirport.setName("Los Angeles");
  }

  @Test
  @DisplayName("Should return empty list when no connecting flights found")
  void testFindConnectingFlights_NoResults() {
    // Given
    when(airportRepository.findOneStopConnections("NYC", "LAX", 50))
        .thenReturn(Collections.emptyList());

    // When
    List<FlightSearchResult> results = pathFindingService.findConnectingFlights(searchRequest);

    // Then
    assertNotNull(results);
    assertTrue(results.isEmpty());
    verify(airportRepository).findOneStopConnections("NYC", "LAX", 50);
  }

  @Test
  @DisplayName("Should find routes with specified hops using Neo4j")
  void testFindRoutesWithHops_WithNeo4j_Success() {
    // Given
    List<List<Airport>> neo4jPaths =
        Arrays.asList(Arrays.asList(nycAirport, chiAirport, laxAirport));
    when(airportRepository.findPathsWithExactHops("NYC", "LAX", 1, 100)).thenReturn(neo4jPaths);

    // When
    List<List<String>> routes = pathFindingService.findRoutesWithHops("NYC", "LAX", 1);

    // Then
    assertNotNull(routes);
    assertEquals(1, routes.size());
    assertEquals(Arrays.asList("NYC", "CHI", "LAX"), routes.get(0));
    verify(airportRepository).findPathsWithExactHops("NYC", "LAX", 1, 100);
  }

  @Test
  @DisplayName("Should return empty list when no routes found with Neo4j")
  void testFindRoutesWithHops_WithNeo4j_NoRoutes() {
    // Given
    when(airportRepository.findPathsWithExactHops("NYC", "NOWHERE", 1, 100))
        .thenReturn(Collections.emptyList());

    // When
    List<List<String>> routes = pathFindingService.findRoutesWithHops("NYC", "NOWHERE", 1);

    // Then
    assertNotNull(routes);
    assertTrue(routes.isEmpty());
    verify(airportRepository).findPathsWithExactHops("NYC", "NOWHERE", 1, 100);
  }

  @Test
  @DisplayName("Should handle Neo4j connection errors gracefully")
  void testFindRoutesWithHops_Neo4jException() {
    // Given
    when(airportRepository.findPathsWithExactHops("NYC", "LAX", 1, 100))
        .thenThrow(new RuntimeException("Neo4j connection failed"));

    // When
    List<List<String>> routes = pathFindingService.findRoutesWithHops("NYC", "LAX", 1);

    // Then
    assertNotNull(routes);
    // Should fallback to algorithmic approach
    verify(airportRepository).findPathsWithExactHops("NYC", "LAX", 1, 100);
  }

  @Test
  @DisplayName("Should find all routes up to max hops")
  void testFindAllRoutesUpToMaxHops_Success() {
    // Given
    List<List<Airport>> directRoute = Arrays.asList(Arrays.asList(nycAirport, laxAirport));
    List<List<Airport>> oneHopRoute =
        Arrays.asList(Arrays.asList(nycAirport, chiAirport, laxAirport));

    when(airportRepository.findPathsWithExactHops("NYC", "LAX", 0, 100)).thenReturn(directRoute);
    when(airportRepository.findPathsWithExactHops("NYC", "LAX", 1, 100)).thenReturn(oneHopRoute);

    // When
    List<List<String>> allRoutes = pathFindingService.findAllRoutesUpToMaxHops("NYC", "LAX", 1);

    // Then
    assertNotNull(allRoutes);
    assertEquals(2, allRoutes.size());
    assertTrue(allRoutes.contains(Arrays.asList("NYC", "LAX")));
    assertTrue(allRoutes.contains(Arrays.asList("NYC", "CHI", "LAX")));

    verify(airportRepository).findPathsWithExactHops("NYC", "LAX", 0, 100);
    verify(airportRepository).findPathsWithExactHops("NYC", "LAX", 1, 100);
  }

  @Test
  @DisplayName("Should handle zero hops (direct routes)")
  void testFindRoutesWithHops_ZeroHops() {
    // Given
    List<List<Airport>> directRoute = Arrays.asList(Arrays.asList(nycAirport, laxAirport));
    when(airportRepository.findPathsWithExactHops("NYC", "LAX", 0, 100)).thenReturn(directRoute);

    // When
    List<List<String>> routes = pathFindingService.findRoutesWithHops("NYC", "LAX", 0);

    // Then
    assertNotNull(routes);
    assertEquals(1, routes.size());
    assertEquals(Arrays.asList("NYC", "LAX"), routes.get(0));
  }

  @Test
  @DisplayName("Should handle high hop counts")
  void testFindRoutesWithHops_HighHopCount() {
    // Given
    Airport denAirport = new Airport();
    denAirport.setCode("DEN");

    Airport phxAirport = new Airport();
    phxAirport.setCode("PHX");

    List<List<Airport>> longRoute =
        Arrays.asList(Arrays.asList(nycAirport, chiAirport, denAirport, phxAirport, laxAirport));
    when(airportRepository.findPathsWithExactHops("NYC", "LAX", 3, 100)).thenReturn(longRoute);

    // When
    List<List<String>> routes = pathFindingService.findRoutesWithHops("NYC", "LAX", 3);

    // Then
    assertNotNull(routes);
    assertEquals(1, routes.size());
    assertEquals(5, routes.get(0).size()); // 4 hops = 5 airports
    assertEquals(Arrays.asList("NYC", "CHI", "DEN", "PHX", "LAX"), routes.get(0));
  }

  @Test
  @DisplayName("Should handle null airport repository gracefully")
  void testFindRoutesWithHops_NullRepository() {
    // Given
    pathFindingService = new PathFindingService(); // No repository injected

    // When
    List<List<String>> routes = pathFindingService.findRoutesWithHops("NYC", "LAX", 1);

    // Then
    assertNotNull(routes);
    // Should fallback to algorithmic approach
  }

  @Test
  @DisplayName("Should find multiple routes with same hop count")
  void testFindRoutesWithHops_MultipleRoutes() {
    // Given
    Airport denAirport = new Airport();
    denAirport.setCode("DEN");

    List<List<Airport>> multipleRoutes =
        Arrays.asList(
            Arrays.asList(nycAirport, chiAirport, laxAirport),
            Arrays.asList(nycAirport, denAirport, laxAirport));
    when(airportRepository.findPathsWithExactHops("NYC", "LAX", 1, 100)).thenReturn(multipleRoutes);

    // When
    List<List<String>> routes = pathFindingService.findRoutesWithHops("NYC", "LAX", 1);

    // Then
    assertNotNull(routes);
    assertEquals(2, routes.size());
    assertTrue(routes.contains(Arrays.asList("NYC", "CHI", "LAX")));
    assertTrue(routes.contains(Arrays.asList("NYC", "DEN", "LAX")));
  }

  @Test
  @DisplayName("Should handle connecting flights with invalid path size")
  void testFindConnectingFlights_InvalidPathSize() {
    // Given - Path with only 2 airports (should have 3 for one-stop)
    List<List<Airport>> invalidPaths =
        Arrays.asList(
            Arrays.asList(nycAirport, laxAirport) // Only 2 airports, not 3
            );
    when(airportRepository.findOneStopConnections("NYC", "LAX", 50)).thenReturn(invalidPaths);

    // When
    List<FlightSearchResult> results = pathFindingService.findConnectingFlights(searchRequest);

    // Then
    assertNotNull(results);
    assertTrue(results.isEmpty()); // Should skip invalid paths
  }

  @Test
  @DisplayName("Should handle repository exception in connecting flights")
  void testFindConnectingFlights_RepositoryException() {
    // Given
    when(airportRepository.findOneStopConnections("NYC", "LAX", 50))
        .thenThrow(new RuntimeException("Repository connection failed"));

    // When & Then
    assertThrows(
        RuntimeException.class,
        () -> {
          pathFindingService.findConnectingFlights(searchRequest);
        });
  }
}
