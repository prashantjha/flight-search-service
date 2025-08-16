package com.example.flight.search.service;

import com.example.flight.search.dto.FlightSearchRequest;
import com.example.flight.search.dto.FlightSearchResult;
import com.example.flight.search.dto.ScheduleDto;
import com.example.flight.search.entity.Schedule;
import com.example.flight.search.graph.Airport;
import com.example.flight.search.repository.AirportRepository;
import com.example.flight.search.repository.ScheduleRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PathFindingService {

  private static final Logger logger = LoggerFactory.getLogger(PathFindingService.class);

  @Autowired private AirportRepository airportRepository;

  @Autowired private ScheduleRepository scheduleRepository;

  public List<FlightSearchResult> findConnectingFlights(FlightSearchRequest request) {
    logger.info(
        "Finding connecting flights from {} to {} using Neo4j",
        request.getSource(),
        request.getDestination());

    List<FlightSearchResult> connectingFlights = new ArrayList<>();

    // Find one-stop connections using Neo4j
    List<List<Airport>> paths =
        airportRepository.findOneStopConnections(request.getSource(), request.getDestination(), 50);

    for (List<Airport> path : paths) {
      if (path.size() == 3) { // Source -> Intermediate -> Destination
        Airport source = path.get(0);
        Airport intermediate = path.get(1);
        Airport destination = path.get(2);

        List<FlightSearchResult> pathFlights =
            buildConnectingFlights(
                source.getCode(), intermediate.getCode(), destination.getCode(), request);
        connectingFlights.addAll(pathFlights);
      }
    }

    // Also try shortest paths with up to 3 hops
    List<List<Airport>> shortestPaths =
        airportRepository.findShortestPaths(request.getSource(), request.getDestination(), 30);

    for (List<Airport> path : shortestPaths) {
      if (path.size() > 2) {
        List<FlightSearchResult> pathFlights = buildMultiHopFlights(path, request);
        connectingFlights.addAll(pathFlights);
      }
    }

    return connectingFlights.stream()
        .distinct()
        .limit(100) // Limit results for performance
        .collect(Collectors.toList());
  }

  private List<FlightSearchResult> findConnectingFlightsWithoutGraph(FlightSearchRequest request) {
    List<FlightSearchResult> connectingFlights = new ArrayList<>();

    // Find common intermediate airports (simplified approach)
    List<String> commonIntermediates = List.of("BOM", "DEL", "BLR", "MAA", "CCU");

    for (String intermediate : commonIntermediates) {
      if (!intermediate.equals(request.getSource())
          && !intermediate.equals(request.getDestination())) {
        List<FlightSearchResult> pathFlights =
            buildConnectingFlights(
                request.getSource(), intermediate, request.getDestination(), request);
        connectingFlights.addAll(pathFlights);
      }
    }

    return connectingFlights.stream()
        .distinct()
        .limit(50) // Limit results for performance
        .collect(Collectors.toList());
  }

  private List<FlightSearchResult> buildConnectingFlights(
      String source, String intermediate, String destination, FlightSearchRequest request) {
    List<FlightSearchResult> results = new ArrayList<>();

    // Find first leg flights
    List<Schedule> firstLegSchedules =
        scheduleRepository.findDepartureSchedules(
            source, request.getTime(), request.getTime().plusDays(1), request.getNoOfSeats());

    for (Schedule firstLeg : firstLegSchedules) {
      if (!firstLeg.getDestination().equals(intermediate)) continue;

      // Find second leg flights (with minimum layover of 1 hour)
      LocalDateTime minConnectTime = firstLeg.getArrivalTime().plusHours(1);
      LocalDateTime maxConnectTime = firstLeg.getArrivalTime().plusHours(6);

      List<Schedule> secondLegSchedules =
          scheduleRepository.findDepartureSchedules(
              intermediate, minConnectTime, maxConnectTime, request.getNoOfSeats());

      for (Schedule secondLeg : secondLegSchedules) {
        if (!secondLeg.getDestination().equals(destination)) continue;

        // Create connecting flight result
        FlightSearchResult connectingFlight =
            createConnectingFlightResult(firstLeg, secondLeg, request.getNoOfSeats());
        results.add(connectingFlight);
      }
    }

    return results;
  }

  private List<FlightSearchResult> buildMultiHopFlights(
      List<Airport> path, FlightSearchRequest request) {
    List<FlightSearchResult> results = new ArrayList<>();

    if (path.size() < 3) return results;

    // Build flights for the entire path
    List<Schedule> pathSchedules = new ArrayList<>();
    LocalDateTime currentTime = request.getTime();

    for (int i = 0; i < path.size() - 1; i++) {
      String currentSource = path.get(i).getCode();
      String currentDestination = path.get(i + 1).getCode();

      List<Schedule> legSchedules =
          scheduleRepository.findDepartureSchedules(
              currentSource, currentTime, currentTime.plusDays(1), request.getNoOfSeats());

      Schedule bestLeg =
          legSchedules.stream()
              .filter(s -> s.getDestination().equals(currentDestination))
              .findFirst()
              .orElse(null);

      if (bestLeg == null) {
        return results; // Can't complete the path
      }

      pathSchedules.add(bestLeg);
      currentTime = bestLeg.getArrivalTime().plusHours(1); // Minimum layover
    }

    if (pathSchedules.size() == path.size() - 1) {
      FlightSearchResult multiHopFlight =
          createMultiHopFlightResult(pathSchedules, request.getNoOfSeats());
      results.add(multiHopFlight);
    }

    return results;
  }

  private FlightSearchResult createConnectingFlightResult(
      Schedule firstLeg, Schedule secondLeg, Integer seats) {
    List<ScheduleDto> schedules = new ArrayList<>();
    schedules.add(
        new ScheduleDto(
            firstLeg.getId(),
            firstLeg.getSource(),
            firstLeg.getDestination(),
            firstLeg.getDepartureTime(),
            firstLeg.getArrivalTime(),
            firstLeg.getAvailableSeats()));
    schedules.add(
        new ScheduleDto(
            secondLeg.getId(),
            secondLeg.getSource(),
            secondLeg.getDestination(),
            secondLeg.getDepartureTime(),
            secondLeg.getArrivalTime(),
            secondLeg.getAvailableSeats()));

    // Calculate total price (simplified - should consider seat class multipliers)
    Double totalPrice =
        firstLeg.getBaseFare().doubleValue() + secondLeg.getBaseFare().doubleValue();

    return new FlightSearchResult(
        firstLeg.getFlight().getFlightNumber() + "+" + secondLeg.getFlight().getFlightNumber(),
        firstLeg.getFlight().getAirline() + " / " + secondLeg.getFlight().getAirline(),
        firstLeg.getDepartureTime(),
        secondLeg.getArrivalTime(),
        java.math.BigDecimal.valueOf(totalPrice),
        1, // One hop
        schedules);
  }

  private FlightSearchResult createMultiHopFlightResult(List<Schedule> schedules, Integer seats) {
    List<ScheduleDto> scheduleDtos =
        schedules.stream()
            .map(
                s ->
                    new ScheduleDto(
                        s.getId(),
                        s.getSource(),
                        s.getDestination(),
                        s.getDepartureTime(),
                        s.getArrivalTime(),
                        s.getAvailableSeats()))
            .collect(Collectors.toList());

    // Calculate total price
    Double totalPrice = schedules.stream().mapToDouble(s -> s.getBaseFare().doubleValue()).sum();

    // Create flight number combination
    String combinedFlightNumber =
        schedules.stream()
            .map(s -> s.getFlight().getFlightNumber())
            .collect(Collectors.joining("+"));

    // Create airline combination
    String combinedAirline =
        schedules.stream()
            .map(s -> s.getFlight().getAirline())
            .distinct()
            .collect(Collectors.joining(" / "));

    return new FlightSearchResult(
        combinedFlightNumber,
        combinedAirline,
        schedules.get(0).getDepartureTime(),
        schedules.get(schedules.size() - 1).getArrivalTime(),
        java.math.BigDecimal.valueOf(totalPrice),
        schedules.size() - 1, // Number of hops
        scheduleDtos);
  }

  /** Find all possible non-circular routes with exactly the specified number of hops */
  public List<List<String>> findRoutesWithHops(String source, String destination, int hops) {
    logger.info("Finding routes from {} to {} with exactly {} hops", source, destination, hops);

    List<List<String>> routes = new ArrayList<>();

    try {
      if (airportRepository != null) {
        // Use Neo4j to find routes with specific hop count
        List<List<Airport>> neo4jRoutes =
            airportRepository.findPathsWithExactHops(
                source, destination, hops, 100 // maxResults
                );

        // Convert Airport paths to String airport codes
        for (List<Airport> airportPath : neo4jRoutes) {
          List<String> route =
              airportPath.stream().map(Airport::getCode).collect(Collectors.toList());
          routes.add(route);
        }

        logger.info("Found {} routes with {} hops using Neo4j", routes.size(), hops);
      } else {
        // Fallback: Generate routes algorithmically
        routes = findRoutesAlgorithmically(source, destination, hops);
        logger.info("Found {} routes with {} hops using algorithmic approach", routes.size(), hops);
      }
    } catch (Exception e) {
      logger.warn(
          "Error finding routes with Neo4j, falling back to algorithmic approach: {}",
          e.getMessage());
      routes = findRoutesAlgorithmically(source, destination, hops);
    }

    return routes;
  }

  /** Find all possible routes up to the specified maximum hops */
  public List<List<String>> findAllRoutesUpToMaxHops(
      String source, String destination, int maxHops) {
    logger.info("Finding all routes from {} to {} up to {} hops", source, destination, maxHops);

    List<List<String>> allRoutes = new ArrayList<>();

    // Find routes for each hop count from 0 to maxHops
    for (int hopCount = 0; hopCount <= maxHops; hopCount++) {
      List<List<String>> routesWithHops = findRoutesWithHops(source, destination, hopCount);
      allRoutes.addAll(routesWithHops);
    }

    logger.info("Found total {} routes up to {} hops", allRoutes.size(), maxHops);
    return allRoutes;
  }

  /** Algorithmic fallback for route finding when Neo4j is not available */
  private List<List<String>> findRoutesAlgorithmically(
      String source, String destination, int hops) {
    List<List<String>> routes = new ArrayList<>();

    if (hops == 0) {
      // Direct route
      if (hasDirectConnection(source, destination)) {
        routes.add(Arrays.asList(source, destination));
      }
    } else {
      // Multi-hop routes
      Set<String> visited = new HashSet<>();
      List<String> currentPath = new ArrayList<>();
      currentPath.add(source);
      visited.add(source);

      findRoutesRecursive(source, destination, hops, currentPath, visited, routes);
    }

    return routes;
  }

  private void findRoutesRecursive(
      String current,
      String destination,
      int remainingHops,
      List<String> currentPath,
      Set<String> visited,
      List<List<String>> routes) {

    if (remainingHops == 0) {
      if (hasDirectConnection(current, destination)) {
        List<String> completePath = new ArrayList<>(currentPath);
        completePath.add(destination);
        routes.add(completePath);
      }
      return;
    }

    // Get possible intermediate airports
    List<String> intermediates = getPossibleIntermediates(current);

    for (String intermediate : intermediates) {
      if (!visited.contains(intermediate) && !intermediate.equals(destination)) {
        visited.add(intermediate);
        currentPath.add(intermediate);

        findRoutesRecursive(
            intermediate, destination, remainingHops - 1, currentPath, visited, routes);

        // Backtrack
        currentPath.remove(currentPath.size() - 1);
        visited.remove(intermediate);
      }
    }
  }

  private boolean hasDirectConnection(String source, String destination) {
    // Check if there are any schedules between these airports
    try {
      List<Schedule> schedules = scheduleRepository.findBySourceAndDestination(source, destination);
      return !schedules.isEmpty();
    } catch (Exception e) {
      logger.debug("Error checking direct connection: {}", e.getMessage());
      return false;
    }
  }

  private List<String> getPossibleIntermediates(String source) {
    // Get airports that have connections from the source
    try {
      return scheduleRepository.findDestinationsBySource(source);
    } catch (Exception e) {
      logger.debug("Error getting intermediates: {}", e.getMessage());
      // Fallback to common hub airports
      return Arrays.asList("BOM", "DEL", "BLR", "MAA", "CCU", "HYD", "AMD", "COK", "GAU", "PNQ");
    }
  }
}
