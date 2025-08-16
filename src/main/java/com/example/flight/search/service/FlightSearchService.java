package com.example.flight.search.service;

import com.example.flight.search.document.FlightSearchDocument;
import com.example.flight.search.dto.FlightSearchRequest;
import com.example.flight.search.dto.FlightSearchResult;
import com.example.flight.search.dto.ScheduleDto;
import com.example.flight.search.entity.Schedule;
import com.example.flight.search.repository.FlightSearchRepository;
import com.example.flight.search.repository.ScheduleRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class FlightSearchService {

  private static final Logger logger = LoggerFactory.getLogger(FlightSearchService.class);
  private static final int MIN_LAYOVER_MINUTES = 60; // Minimum 1 hour layover
  private static final int MAX_LAYOVER_HOURS = 6; // Maximum 6 hours layover

  @Autowired private FlightSearchRepository flightSearchRepository;

  @Autowired private PathFindingService pathFindingService;

  @Autowired(required = false)
  private CacheService cacheService;

  @Autowired private ScheduleRepository scheduleRepository;

  @Cacheable(
      value = "flightSearch",
      key =
          "#request.source + '_' + #request.destination + '_' + #request.getDepartureDateTime() + '_' + #request.noOfSeats + '_' + #request.maxHops")
  public Page<FlightSearchResult> searchFlights(FlightSearchRequest request) {
    logger.info(
        "Searching flights from {} to {} for {} seats with maxHops: {}",
        request.getSource(),
        request.getDestination(),
        request.getNoOfSeats(),
        request.getMaxHops());

    List<FlightSearchResult> allResults = new ArrayList<>();
    LocalDateTime searchTime = request.getDepartureDateTime();

    // Determine maximum hops to search
    int maxHopsToSearch =
        request.getMaxHops() != null ? request.getMaxHops() : 3; // Default max 3 hops

    // Search for flights with 0 to maxHops - explicit hop calculation
    for (int currentHops = 0; currentHops <= maxHopsToSearch; currentHops++) {
      logger.info("Searching for flights with exactly {} hops", currentHops);

      if (currentHops == 0) {
        // Direct flights - search in Elasticsearch first, then database
        List<FlightSearchResult> directFlights = searchDirectFlights(request);
        allResults.addAll(directFlights);
      } else {
        // Multi-hop flights - use Neo4j to find routes, then validate with Elasticsearch/Database
        List<FlightSearchResult> multiHopFlights =
            searchMultiHopFlightsWithExactHops(request, currentHops);
        allResults.addAll(multiHopFlights);
      }
    }

    // Remove duplicates and apply additional filters
    allResults =
        allResults.stream()
            .distinct()
            .filter(flight -> isValidFlight(flight, request))
            .collect(Collectors.toList());

    // Sort results
    sortResults(allResults, request);

    // Apply pagination
    Pageable pageable = createPageable(request);
    return paginateResults(allResults, pageable);
  }

  /**
   * Search for multi-hop flights with exactly the specified number of hops Uses Neo4j to find
   * routes and Elasticsearch/Database to validate flights
   */
  private List<FlightSearchResult> searchMultiHopFlightsWithExactHops(
      FlightSearchRequest request, int exactHops) {
    logger.info("Searching multi-hop flights with exactly {} hops", exactHops);

    // Step 1: Get all possible non-circular routes from Neo4j with exact hop count
    List<List<String>> routes =
        pathFindingService.findRoutesWithHops(
            request.getSource(), request.getDestination(), exactHops);

    List<FlightSearchResult> results = new ArrayList<>();

    // Step 2: For each route, check if flights exist in correct sequence with timing validation
    for (List<String> route : routes) {
      logger.debug("Validating route with {} hops: {}", exactHops, route);

      // Find actual flights for this route with timing validation
      List<FlightSearchResult> routeFlights =
          findAndValidateFlightsForRoute(route, request, exactHops);
      results.addAll(routeFlights);
    }

    logger.info("Found {} valid multi-hop flights with exactly {} hops", results.size(), exactHops);
    return results;
  }

  /** Find and validate flights for a specific route with timing constraints */
  private List<FlightSearchResult> findAndValidateFlightsForRoute(
      List<String> route, FlightSearchRequest request, int expectedHops) {
    if (route.size() != expectedHops + 1) {
      logger.warn("Route size {} doesn't match expected hops {}", route.size(), expectedHops);
      return new ArrayList<>();
    }

    List<FlightSearchResult> routeFlights = new ArrayList<>();
    LocalDateTime currentSearchTime = request.getDepartureDateTime();

    // Try to find flights for each segment of the route
    List<List<Schedule>> segmentOptions = new ArrayList<>();

    // Step 1: Find all flight options for each segment
    for (int i = 0; i < route.size() - 1; i++) {
      String fromAirport = route.get(i);
      String toAirport = route.get(i + 1);

      // Define search window for this segment
      LocalDateTime segmentStartTime = currentSearchTime;
      LocalDateTime segmentEndTime = currentSearchTime.plusDays(1); // Search within 24 hours

      // Find flights for this segment using Elasticsearch first, then database
      List<Schedule> segmentSchedules =
          findFlightsForSegment(
              fromAirport, toAirport, segmentStartTime, segmentEndTime, request.getNoOfSeats());

      if (segmentSchedules.isEmpty()) {
        logger.debug("No flights found for segment {} -> {}", fromAirport, toAirport);
        return new ArrayList<>(); // Cannot complete this route
      }

      segmentOptions.add(segmentSchedules);

      // Update search time for next segment (minimum layover time)
      if (i < route.size() - 2) { // Not the last segment
        LocalDateTime earliestArrival =
            segmentSchedules.stream()
                .map(Schedule::getArrivalTime)
                .min(LocalDateTime::compareTo)
                .orElse(currentSearchTime);
        currentSearchTime = earliestArrival.plusMinutes(MIN_LAYOVER_MINUTES);
      }
    }

    // Step 2: Generate all valid flight combinations with timing validation
    routeFlights.addAll(generateValidFlightCombinations(segmentOptions, route, expectedHops));

    return routeFlights;
  }

  /** Find flights for a specific segment using Elasticsearch first, then database fallback */
  private List<Schedule> findFlightsForSegment(
      String source,
      String destination,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Integer requiredSeats) {
    List<Schedule> schedules = new ArrayList<>();

    try {
      // Try Elasticsearch first if available
      if (flightSearchRepository != null) {
        schedules =
            findSchedulesFromElasticsearch(source, destination, startTime, endTime, requiredSeats);
      }

      // If no results from Elasticsearch, try database
      if (schedules.isEmpty()) {
        schedules =
            scheduleRepository
                .findDepartureSchedules(source, startTime, endTime, requiredSeats)
                .stream()
                .filter(schedule -> schedule.getDestination().equals(destination))
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      logger.warn(
          "Error finding flights for segment {} -> {}: {}", source, destination, e.getMessage());
      // Fallback to database only
      schedules =
          scheduleRepository
              .findDepartureSchedules(source, startTime, endTime, requiredSeats)
              .stream()
              .filter(schedule -> schedule.getDestination().equals(destination))
              .collect(Collectors.toList());
    }

    logger.debug("Found {} flights for segment {} -> {}", schedules.size(), source, destination);
    return schedules;
  }

  /** Convert Elasticsearch results to Schedule entities for segment validation */
  private List<Schedule> findSchedulesFromElasticsearch(
      String source,
      String destination,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Integer requiredSeats) {
    // This is a simplified conversion - in practice, you'd need to fetch full Schedule entities
    // based on the Elasticsearch document IDs
    List<Schedule> schedules = new ArrayList<>();

    try {
      Pageable pageable = PageRequest.of(0, 100);
      Page<FlightSearchDocument> docs =
          flightSearchRepository
              .findBySourceAndDestinationAndDepartureTimeBetweenAndAvailableSeatsGreaterThanEqual(
                  source, destination, startTime, endTime, requiredSeats, pageable);

      // Convert documents to schedules (you'd need to implement this conversion)
      for (FlightSearchDocument doc : docs.getContent()) {
        // Fetch the actual schedule entity using the schedule ID from the document
        scheduleRepository.findById(doc.getScheduleId()).ifPresent(schedules::add);
      }
    } catch (Exception e) {
      logger.debug("Error querying Elasticsearch: {}", e.getMessage());
    }

    return schedules;
  }

  private List<FlightSearchResult> searchDirectFlights(FlightSearchRequest request) {
    logger.info(
        "Searching direct flights from {} to {}", request.getSource(), request.getDestination());

    List<FlightSearchResult> results = new ArrayList<>();

    if (flightSearchRepository != null) {
      // Search in Elasticsearch
      Pageable pageable = PageRequest.of(0, 100); // Get up to 100 direct flights
      Page<FlightSearchDocument> directFlights =
          flightSearchRepository
              .findBySourceAndDestinationAndDepartureTimeAfterAndAvailableSeatsGreaterThanEqual(
                  request.getSource(),
                  request.getDestination(),
                  request.getDepartureDateTime(),
                  request.getNoOfSeats(),
                  pageable);

      results =
          directFlights.stream()
              .map(doc -> convertToSearchResult(doc, 0)) // 0 hops for direct flights
              .collect(Collectors.toList());
    } else {
      // Fallback to database search
      results = searchDirectFlightsFromDatabase(request);
    }

    logger.info("Found {} direct flights", results.size());
    return results;
  }

  private List<FlightSearchResult> searchDirectFlightsFromDatabase(FlightSearchRequest request) {
    List<Schedule> schedules =
        scheduleRepository.findDirectFlightSchedules(
            request.getSource(),
            request.getDestination(),
            request.getDepartureDateTime(),
            request.getNoOfSeats());

    return schedules.stream()
        .map(schedule -> convertScheduleToSearchResult(schedule, 0))
        .collect(Collectors.toList());
  }

  private List<FlightSearchResult> searchMultiHopFlights(
      FlightSearchRequest request, int exactHops) {
    logger.info("Searching multi-hop flights with exactly {} hops", exactHops);

    // Get all possible routes from Neo4j with the specified number of hops
    List<List<String>> routes =
        pathFindingService.findRoutesWithHops(
            request.getSource(), request.getDestination(), exactHops);

    List<FlightSearchResult> results = new ArrayList<>();

    for (List<String> route : routes) {
      logger.debug("Checking route: {}", route);

      // Find actual flights for this route
      List<FlightSearchResult> routeFlights = findFlightsForRoute(route, request);
      results.addAll(routeFlights);
    }

    logger.info("Found {} multi-hop flights with {} hops", results.size(), exactHops);
    return results;
  }

  private List<FlightSearchResult> findFlightsForRoute(
      List<String> route, FlightSearchRequest request) {
    if (route.size() < 2) return new ArrayList<>();

    List<FlightSearchResult> routeFlights = new ArrayList<>();
    LocalDateTime currentTime = request.getDepartureDateTime();

    // Try to find flights for each segment of the route
    List<List<Schedule>> segmentFlights = new ArrayList<>();

    for (int i = 0; i < route.size() - 1; i++) {
      String fromAirport = route.get(i);
      String toAirport = route.get(i + 1);

      LocalDateTime segmentStartTime = currentTime;
      LocalDateTime segmentEndTime = currentTime.plusDays(1); // Search within 24 hours

      // Find flights for this segment
      List<Schedule> segmentSchedules =
          scheduleRepository
              .findDepartureSchedules(
                  fromAirport, segmentStartTime, segmentEndTime, request.getNoOfSeats())
              .stream()
              .filter(schedule -> schedule.getDestination().equals(toAirport))
              .collect(Collectors.toList());

      if (segmentSchedules.isEmpty()) {
        logger.debug("No flights found for segment {} -> {}", fromAirport, toAirport);
        return new ArrayList<>(); // Cannot complete this route
      }

      segmentFlights.add(segmentSchedules);

      // Update current time for next segment (add minimum layover)
      if (i < route.size() - 2) { // Not the last segment
        LocalDateTime earliestArrival =
            segmentSchedules.stream()
                .map(Schedule::getArrivalTime)
                .min(LocalDateTime::compareTo)
                .orElse(currentTime);
        currentTime = earliestArrival.plusMinutes(MIN_LAYOVER_MINUTES);
      }
    }

    // Generate all valid combinations of flights for this route
    routeFlights.addAll(generateFlightCombinations(segmentFlights, route, request));

    return routeFlights;
  }

  private List<FlightSearchResult> generateFlightCombinations(
      List<List<Schedule>> segmentFlights, List<String> route, FlightSearchRequest request) {

    List<FlightSearchResult> combinations = new ArrayList<>();

    // Use recursive approach to generate all valid combinations
    generateCombinationsRecursive(
        segmentFlights, new ArrayList<>(), 0, route, request, combinations);

    return combinations;
  }

  private void generateCombinationsRecursive(
      List<List<Schedule>> segmentFlights,
      List<Schedule> currentCombination,
      int segmentIndex,
      List<String> route,
      FlightSearchRequest request,
      List<FlightSearchResult> results) {

    if (segmentIndex >= segmentFlights.size()) {
      // We have a complete combination - validate timing
      if (isValidFlightCombination(currentCombination)) {
        FlightSearchResult result =
            createMultiHopFlightResult(currentCombination, route.size() - 1);
        results.add(result);
      }
      return;
    }

    // Try each flight option for the current segment
    List<Schedule> currentSegmentFlights = segmentFlights.get(segmentIndex);
    for (Schedule flight : currentSegmentFlights) {
      // Check if this flight is compatible with the previous flight (timing)
      if (segmentIndex == 0
          || isValidConnection(currentCombination.get(segmentIndex - 1), flight)) {
        currentCombination.add(flight);
        generateCombinationsRecursive(
            segmentFlights, currentCombination, segmentIndex + 1, route, request, results);
        currentCombination.remove(currentCombination.size() - 1); // Backtrack
      }
    }
  }

  /** Generate all valid flight combinations with strict timing validation */
  private List<FlightSearchResult> generateValidFlightCombinations(
      List<List<Schedule>> segmentOptions, List<String> route, int expectedHops) {

    List<FlightSearchResult> validCombinations = new ArrayList<>();

    // Use recursive approach to generate all valid timing combinations
    generateTimingValidatedCombinations(
        segmentOptions, new ArrayList<>(), 0, route, expectedHops, validCombinations);

    return validCombinations;
  }

  /** Recursive method to generate flight combinations with timing validation */
  private void generateTimingValidatedCombinations(
      List<List<Schedule>> segmentOptions,
      List<Schedule> currentCombination,
      int segmentIndex,
      List<String> route,
      int expectedHops,
      List<FlightSearchResult> results) {

    if (segmentIndex >= segmentOptions.size()) {
      // Complete combination found - create result
      if (isValidFlightCombination(currentCombination)) {
        FlightSearchResult result = createMultiHopFlightResult(currentCombination, expectedHops);
        if (result != null) {
          results.add(result);
        }
      }
      return;
    }

    // Try each flight option for current segment
    List<Schedule> currentSegmentOptions = segmentOptions.get(segmentIndex);
    for (Schedule flightOption : currentSegmentOptions) {
      // Check timing compatibility with previous flight
      if (segmentIndex == 0
          || isValidTimingConnection(currentCombination.get(segmentIndex - 1), flightOption)) {
        currentCombination.add(flightOption);
        generateTimingValidatedCombinations(
            segmentOptions, currentCombination, segmentIndex + 1, route, expectedHops, results);
        currentCombination.remove(currentCombination.size() - 1); // Backtrack
      }
    }
  }

  /** Enhanced timing validation for connections */
  private boolean isValidTimingConnection(Schedule prevFlight, Schedule nextFlight) {
    // Verify airport connection
    if (!prevFlight.getDestination().equals(nextFlight.getSource())) {
      return false;
    }

    // Check timing constraints: arrival must be before departure
    LocalDateTime arrivalTime = prevFlight.getArrivalTime();
    LocalDateTime departureTime = nextFlight.getDepartureTime();

    if (!arrivalTime.isBefore(departureTime)) {
      return false;
    }

    // Calculate layover duration
    long layoverMinutes = java.time.Duration.between(arrivalTime, departureTime).toMinutes();

    // Validate layover window: minimum 1 hour, maximum 6 hours
    boolean validLayover =
        layoverMinutes >= MIN_LAYOVER_MINUTES && layoverMinutes <= (MAX_LAYOVER_HOURS * 60);

    if (!validLayover) {
      logger.debug(
          "Invalid layover duration: {} minutes (valid range: {}-{} minutes)",
          layoverMinutes,
          MIN_LAYOVER_MINUTES,
          MAX_LAYOVER_HOURS * 60);
    }

    return validLayover;
  }

  private boolean isValidConnection(Schedule prevFlight, Schedule nextFlight) {
    // Check if arrival airport matches departure airport
    if (!prevFlight.getDestination().equals(nextFlight.getSource())) {
      return false;
    }

    // Check layover time
    LocalDateTime arrivalTime = prevFlight.getArrivalTime();
    LocalDateTime departureTime = nextFlight.getDepartureTime();

    long layoverMinutes = java.time.Duration.between(arrivalTime, departureTime).toMinutes();

    // Minimum layover: 1 hour, Maximum layover: 6 hours
    return layoverMinutes >= MIN_LAYOVER_MINUTES && layoverMinutes <= (MAX_LAYOVER_HOURS * 60);
  }

  private boolean isValidFlightCombination(List<Schedule> flights) {
    if (flights.isEmpty()) return false;

    // Check each connection
    for (int i = 0; i < flights.size() - 1; i++) {
      if (!isValidConnection(flights.get(i), flights.get(i + 1))) {
        return false;
      }
    }

    return true;
  }

  private boolean isValidFlight(FlightSearchResult flight, FlightSearchRequest request) {
    // Apply additional filters
    if (request.getMaxPrice() != null && flight.getPrice().doubleValue() > request.getMaxPrice()) {
      return false;
    }

    if (request.getAirline() != null
        && !flight.getAirline().toLowerCase().contains(request.getAirline().toLowerCase())) {
      return false;
    }

    // Note: We don't filter by maxHops here as we already generated flights with appropriate hops
    return true;
  }

  private Page<FlightSearchResult> paginateResults(
      List<FlightSearchResult> results, Pageable pageable) {
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), results.size());

    return new PageImpl<>(results.subList(start, end), pageable, results.size());
  }

  private void sortResults(List<FlightSearchResult> results, FlightSearchRequest request) {
    if (request.getSortByPrice()) {
      results.sort(Comparator.comparing(FlightSearchResult::getPrice));
    } else if (request.getSortByHops()) {
      results.sort(Comparator.comparing(FlightSearchResult::getNumberOfHops));
    } else {
      results.sort(Comparator.comparing(FlightSearchResult::getDepartureTime));
    }
  }

  private Pageable createPageable(FlightSearchRequest request) {
    Sort sort = Sort.unsorted();

    if (request.getSortByPrice() && request.getSortByHops()) {
      sort = Sort.by("price").and(Sort.by("numberOfHops"));
    } else if (request.getSortByPrice()) {
      sort = Sort.by("price");
    } else if (request.getSortByHops()) {
      sort = Sort.by("numberOfHops");
    } else {
      sort = Sort.by("departureTime");
    }

    return PageRequest.of(request.getPage(), request.getSize(), sort);
  }

  private FlightSearchResult convertToSearchResult(FlightSearchDocument document, int hops) {
    List<ScheduleDto> schedules = new ArrayList<>();
    schedules.add(
        new ScheduleDto(
            document.getScheduleId(),
            document.getSource(),
            document.getDestination(),
            document.getDepartureTime(),
            document.getArrivalTime(),
            document.getAvailableSeats()));

    return new FlightSearchResult(
        document.getFlightNumber(),
        document.getAirline(),
        document.getDepartureTime(),
        document.getArrivalTime(),
        document.getPrice(),
        hops,
        schedules);
  }

  private FlightSearchResult convertScheduleToSearchResult(Schedule schedule, int hops) {
    List<ScheduleDto> schedules = new ArrayList<>();
    schedules.add(
        new ScheduleDto(
            schedule.getId(),
            schedule.getSource(),
            schedule.getDestination(),
            schedule.getDepartureTime(),
            schedule.getArrivalTime(),
            schedule.getAvailableSeats()));

    return new FlightSearchResult(
        schedule.getFlight().getFlightNumber(),
        schedule.getFlight().getAirline(),
        schedule.getDepartureTime(),
        schedule.getArrivalTime(),
        schedule.getBaseFare(),
        hops,
        schedules);
  }

  private FlightSearchResult createMultiHopFlightResult(List<Schedule> schedules, int totalHops) {
    if (schedules.isEmpty()) return null;

    Schedule first = schedules.get(0);
    Schedule last = schedules.get(schedules.size() - 1);

    List<ScheduleDto> scheduleDtos =
        schedules.stream()
            .map(
                schedule ->
                    new ScheduleDto(
                        schedule.getId(),
                        schedule.getSource(),
                        schedule.getDestination(),
                        schedule.getDepartureTime(),
                        schedule.getArrivalTime(),
                        schedule.getAvailableSeats()))
            .collect(Collectors.toList());

    // Calculate total price
    double totalPrice = schedules.stream().mapToDouble(s -> s.getBaseFare().doubleValue()).sum();

    // Create combined flight number and airline
    String combinedFlightNumber =
        schedules.stream()
            .map(s -> s.getFlight().getFlightNumber())
            .collect(Collectors.joining("+"));

    String combinedAirline =
        schedules.stream()
            .map(s -> s.getFlight().getAirline())
            .distinct()
            .collect(Collectors.joining(" / "));

    return new FlightSearchResult(
        combinedFlightNumber,
        combinedAirline,
        first.getDepartureTime(),
        last.getArrivalTime(),
        java.math.BigDecimal.valueOf(totalPrice),
        totalHops,
        scheduleDtos);
  }
}
