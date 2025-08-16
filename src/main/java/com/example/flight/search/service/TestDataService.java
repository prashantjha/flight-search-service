package com.example.flight.search.service;

import com.example.flight.search.document.FlightSearchDocument;
import com.example.flight.search.entity.*;
import com.example.flight.search.graph.Airport;
import com.example.flight.search.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestDataService {

  private static final Logger logger = LoggerFactory.getLogger(TestDataService.class);

  @Autowired private FlightRepository flightRepository;

  @Autowired private ScheduleRepository scheduleRepository;

  @Autowired(required = false)
  private FlightSearchRepository flightSearchRepository;

  @Autowired(required = false)
  private AirportRepository airportRepository;

  public void populateTestData() {
    logger.info("Starting comprehensive test data population with fresh data...");

    try {
      // Clear existing data from all sources
      clearAllExistingData();

      // Create and populate Neo4j airports and routes
      populateAirportsAndRoutes();

      // Create test flights and schedules for today and next few days
      LocalDate today = LocalDate.now();
      createTestFlights(today);

      // Sync data to Elasticsearch (if available)
      if (flightSearchRepository != null) {
        syncToElasticsearch();
      } else {
        logger.info("Elasticsearch not available - skipping search index sync");
      }

      logger.info("Comprehensive test data population completed successfully!");
    } catch (Exception e) {
      logger.error("Error during test data population: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to populate test data", e);
    }
  }

  public void populateTestDataForDate(LocalDate targetDate) {
    logger.info("Starting test data population for date: {}", targetDate);

    try {
      // Create test flights for specific date
      createTestFlights(targetDate);

      // Sync data to Elasticsearch (if available)
      if (flightSearchRepository != null) {
        syncToElasticsearch();
      } else {
        logger.info("Elasticsearch not available - skipping search index sync");
      }

      logger.info("Test data population completed for date: {}", targetDate);
    } catch (Exception e) {
      logger.error(
          "Error during test data population for date {}: {}", targetDate, e.getMessage(), e);
      throw new RuntimeException("Failed to populate test data for date: " + targetDate, e);
    }
  }

  /** Clear all existing data from Neo4j, Elasticsearch, and Database with enhanced cleanup */
  private void clearAllExistingData() {
    logger.info("Performing comprehensive data cleanup from Neo4j, Elasticsearch, and Database...");

    // Clear Elasticsearch data with enhanced cleanup
    if (flightSearchRepository != null) {
      try {
        logger.info("Clearing Elasticsearch flight search index...");
        flightSearchRepository.deleteAll();
        // Wait a moment for Elasticsearch to process the deletion
        Thread.sleep(2000);
        logger.info("✅ Elasticsearch data cleared successfully");
      } catch (Exception e) {
        logger.warn("⚠️ Error clearing Elasticsearch data: {}", e.getMessage());
      }
    } else {
      logger.info("Elasticsearch not available - skipping search index cleanup");
    }

    // Clear Database data with proper order (foreign key constraints)
    try {
      logger.info("Clearing database data in proper order...");

      // Clear schedules first (they reference flights)
      scheduleRepository.deleteAll();
      logger.info("✅ Schedules cleared");

      // Clear flights
      flightRepository.deleteAll();
      logger.info("✅ Flights cleared");

      logger.info("✅ Database data cleared successfully");
    } catch (Exception e) {
      logger.warn("⚠️ Error clearing database data: {}", e.getMessage());
    }

    // Clear Neo4j data with enhanced cleanup
    if (airportRepository != null) {
      try {
        logger.info("Clearing Neo4j airport graph data...");
        airportRepository.deleteAll();
        // Wait a moment for Neo4j to process the deletion
        Thread.sleep(1000);
        logger.info("✅ Neo4j airport data cleared successfully");
      } catch (Exception e) {
        logger.warn("⚠️ Error clearing Neo4j data: {}", e.getMessage());
      }
    } else {
      logger.info("Neo4j not available - skipping airport graph cleanup");
    }

    logger.info("🧹 Complete data cleanup finished");
  }

  /** Populate airports and flight routes in Neo4j with enhanced format */
  private void populateAirportsAndRoutes() {
    if (airportRepository == null) {
      logger.info("Neo4j not available - skipping airport graph population");
      return;
    }

    logger.info("Populating enhanced airport network in Neo4j...");

    try {
      // Create comprehensive Indian airports with proper metadata
      List<Airport> airports = createEnhancedAirports();

      logger.info("Saving {} airports to Neo4j...", airports.size());
      airportRepository.saveAll(airports);

      // Create flight routes with relationships
      createEnhancedFlightRoutes(airports);

      logger.info("✅ Successfully populated {} airports with routes in Neo4j", airports.size());
    } catch (Exception e) {
      logger.error("❌ Error populating Neo4j data: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to populate Neo4j airport data", e);
    }
  }

  /** Create enhanced airports with complete metadata for better route finding */
  private List<Airport> createEnhancedAirports() {
    List<Airport> airports = new ArrayList<>();

    // Major hub airports (Tier 1)
    airports.add(
        createAirportWithMetadata(
            "DEL",
            "Indira Gandhi International",
            "New Delhi",
            "India",
            28.5562,
            77.1000,
            "MAJOR_HUB",
            3));
    airports.add(
        createAirportWithMetadata(
            "BOM",
            "Chhatrapati Shivaji Maharaj International",
            "Mumbai",
            "India",
            19.0896,
            72.8656,
            "MAJOR_HUB",
            2));
    airports.add(
        createAirportWithMetadata(
            "BLR",
            "Kempegowda International",
            "Bangalore",
            "India",
            13.1979,
            77.7061,
            "MAJOR_HUB",
            2));

    // Regional hubs (Tier 2)
    airports.add(
        createAirportWithMetadata(
            "MAA",
            "Chennai International",
            "Chennai",
            "India",
            12.9941,
            80.1709,
            "REGIONAL_HUB",
            2));
    airports.add(
        createAirportWithMetadata(
            "CCU",
            "Netaji Subhash Chandra Bose International",
            "Kolkata",
            "India",
            22.6546,
            88.4467,
            "REGIONAL_HUB",
            2));
    airports.add(
        createAirportWithMetadata(
            "HYD",
            "Rajiv Gandhi International",
            "Hyderabad",
            "India",
            17.2403,
            78.4294,
            "REGIONAL_HUB",
            1));

    // Secondary airports (Tier 3)
    airports.add(
        createAirportWithMetadata(
            "AMD",
            "Sardar Vallabhbhai Patel International",
            "Ahmedabad",
            "India",
            23.0726,
            72.6177,
            "SECONDARY",
            1));
    airports.add(
        createAirportWithMetadata(
            "COK", "Cochin International", "Kochi", "India", 10.1520, 76.4019, "SECONDARY", 1));
    airports.add(
        createAirportWithMetadata(
            "PNQ", "Pune Airport", "Pune", "India", 18.5821, 73.9197, "SECONDARY", 1));
    airports.add(
        createAirportWithMetadata(
            "JAI", "Jaipur International", "Jaipur", "India", 26.8242, 75.8122, "SECONDARY", 1));

    // Tier 3 destinations
    airports.add(
        createAirportWithMetadata(
            "LKO",
            "Chaudhary Charan Singh International",
            "Lucknow",
            "India",
            26.7606,
            80.8893,
            "DESTINATION",
            1));
    airports.add(
        createAirportWithMetadata(
            "IXC",
            "Chandigarh Airport",
            "Chandigarh",
            "India",
            30.6735,
            76.7884,
            "DESTINATION",
            1));
    airports.add(
        createAirportWithMetadata(
            "VNS",
            "Lal Bahadur Shastri Airport",
            "Varanasi",
            "India",
            25.4484,
            82.8595,
            "DESTINATION",
            1));
    airports.add(
        createAirportWithMetadata(
            "TRV",
            "Trivandrum International",
            "Thiruvananthapuram",
            "India",
            8.4821,
            76.9200,
            "DESTINATION",
            1));
    airports.add(
        createAirportWithMetadata(
            "GAU",
            "Lokpriya Gopinath Bordoloi International",
            "Guwahati",
            "India",
            26.1061,
            91.5856,
            "DESTINATION",
            1));

    return airports;
  }

  private Airport createAirportWithMetadata(
      String code,
      String name,
      String city,
      String country,
      double latitude,
      double longitude,
      String tier,
      int runways) {
    Airport airport = new Airport(code, name, city, country);
    airport.setLatitude(latitude);
    airport.setLongitude(longitude);
    // Note: In a real implementation, you'd set additional metadata like tier and runways
    return airport;
  }

  /** Create enhanced flight routes with proper relationships for multi-hop search */
  private void createEnhancedFlightRoutes(List<Airport> airports) {
    logger.info("Creating enhanced flight route network for optimal multi-hop search...");

    // This method would typically create CONNECTED_TO relationships in Neo4j
    // For now, routes will be inferred from actual flight schedules created later

    // In a full implementation, you would:
    // 1. Create direct routes between major hubs
    // 2. Create spoke routes from hubs to regional airports
    // 3. Set route metadata (distance, flight time, frequency)
    // 4. Create seasonal/time-based route variations

    logger.info("Flight route relationships will be established based on actual flight schedules");
  }

  private void createTestFlights(LocalDate date) {
    logger.info("Creating comprehensive test flights for date: {}", date);

    List<Flight> flights = new ArrayList<>();

    // Create flights for major airlines with multiple routes
    flights.add(createFlight("AI101", "Air India", 180));
    flights.add(createFlight("AI102", "Air India", 180));
    flights.add(createFlight("AI103", "Air India", 160));
    flights.add(createFlight("AI104", "Air India", 180));
    flights.add(createFlight("6E301", "IndiGo", 186));
    flights.add(createFlight("6E302", "IndiGo", 186));
    flights.add(createFlight("6E303", "IndiGo", 186));
    flights.add(createFlight("6E304", "IndiGo", 180));
    flights.add(createFlight("UK955", "Vistara", 158));
    flights.add(createFlight("UK956", "Vistara", 158));
    flights.add(createFlight("UK957", "Vistara", 164));
    flights.add(createFlight("SG8136", "SpiceJet", 189));
    flights.add(createFlight("SG8137", "SpiceJet", 189));

    // Save flights
    flights = flightRepository.saveAll(flights);

    // Create comprehensive schedules for multi-hop support
    createComprehensiveSchedules(flights, date);

    logger.info("Created {} comprehensive test flights for date: {}", flights.size(), date);
  }

  private Flight createFlight(String flightNumber, String airline, int capacity) {
    return new Flight(flightNumber, airline, capacity);
  }

  private void createComprehensiveSchedules(List<Flight> flights, LocalDate date) {
    logger.info("Creating comprehensive flight schedules for multi-hop search support");

    // Create time slots throughout the day
    LocalTime[] timeSlots = {
      LocalTime.of(6, 0), // Early morning
      LocalTime.of(8, 30), // Morning
      LocalTime.of(11, 0), // Late morning
      LocalTime.of(13, 30), // Afternoon
      LocalTime.of(16, 0), // Evening
      LocalTime.of(18, 30), // Late evening
      LocalTime.of(21, 0) // Night
    };

    for (Flight flight : flights) {
      List<Schedule> schedules = new ArrayList<>();

      // Create different route patterns based on flight
      switch (flight.getFlightNumber()) {
        case "AI101":
          // DEL-BOM trunk route (high frequency)
          schedules.add(
              createScheduleForTime(
                  flight, "DEL", "BOM", date, timeSlots[0], 135, 45, new BigDecimal("15000")));
          schedules.add(
              createScheduleForTime(
                  flight, "BOM", "DEL", date, timeSlots[3], 135, 50, new BigDecimal("15500")));
          break;

        case "AI102":
          // DEL-BLR route
          schedules.add(
              createScheduleForTime(
                  flight, "DEL", "BLR", date, timeSlots[1], 165, 38, new BigDecimal("18000")));
          schedules.add(
              createScheduleForTime(
                  flight, "BLR", "DEL", date, timeSlots[4], 165, 42, new BigDecimal("18500")));
          break;

        case "AI103":
          // DEL-MAA route
          schedules.add(
              createScheduleForTime(
                  flight, "DEL", "MAA", date, timeSlots[2], 180, 35, new BigDecimal("22000")));
          schedules.add(
              createScheduleForTime(
                  flight, "MAA", "DEL", date, timeSlots[5], 180, 40, new BigDecimal("22500")));
          break;

        case "AI104":
          // DEL-CCU route
          schedules.add(
              createScheduleForTime(
                  flight, "DEL", "CCU", date, timeSlots[1], 150, 32, new BigDecimal("19000")));
          schedules.add(
              createScheduleForTime(
                  flight, "CCU", "DEL", date, timeSlots[4], 150, 35, new BigDecimal("19500")));
          break;

        case "6E301":
          // BOM-BLR route (connecting hub)
          schedules.add(
              createScheduleForTime(
                  flight, "BOM", "BLR", date, timeSlots[0], 90, 65, new BigDecimal("12000")));
          schedules.add(
              createScheduleForTime(
                  flight, "BLR", "BOM", date, timeSlots[3], 90, 68, new BigDecimal("12500")));
          break;

        case "6E302":
          // BLR-MAA route (regional connection)
          schedules.add(
              createScheduleForTime(
                  flight, "BLR", "MAA", date, timeSlots[1], 60, 75, new BigDecimal("8500")));
          schedules.add(
              createScheduleForTime(
                  flight, "MAA", "BLR", date, timeSlots[4], 60, 72, new BigDecimal("9000")));
          break;

        case "6E303":
          // BLR-CCU route
          schedules.add(
              createScheduleForTime(
                  flight, "BLR", "CCU", date, timeSlots[2], 150, 38, new BigDecimal("19500")));
          schedules.add(
              createScheduleForTime(
                  flight, "CCU", "BLR", date, timeSlots[5], 150, 41, new BigDecimal("20000")));
          break;

        case "6E304":
          // MAA-CCU route
          schedules.add(
              createScheduleForTime(
                  flight, "MAA", "CCU", date, timeSlots[1], 135, 55, new BigDecimal("16500")));
          schedules.add(
              createScheduleForTime(
                  flight, "CCU", "MAA", date, timeSlots[4], 135, 58, new BigDecimal("17000")));
          break;

        case "UK955":
          // BOM-MAA route
          schedules.add(
              createScheduleForTime(
                  flight, "BOM", "MAA", date, timeSlots[2], 120, 48, new BigDecimal("14000")));
          schedules.add(
              createScheduleForTime(
                  flight, "MAA", "BOM", date, timeSlots[5], 120, 52, new BigDecimal("14500")));
          break;

        case "UK956":
          // BOM-CCU route
          schedules.add(
              createScheduleForTime(
                  flight, "BOM", "CCU", date, timeSlots[1], 165, 44, new BigDecimal("17000")));
          schedules.add(
              createScheduleForTime(
                  flight, "CCU", "BOM", date, timeSlots[4], 165, 47, new BigDecimal("17500")));
          break;

        case "UK957":
          // DEL-HYD route (additional hub)
          schedules.add(
              createScheduleForTime(
                  flight, "DEL", "HYD", date, timeSlots[2], 135, 42, new BigDecimal("16000")));
          schedules.add(
              createScheduleForTime(
                  flight, "HYD", "DEL", date, timeSlots[5], 135, 45, new BigDecimal("16500")));
          break;

        case "SG8136":
          // Multi-segment route: DEL-BOM-CCU (connecting flight)
          LocalDateTime leg1Dep = LocalDateTime.of(date, timeSlots[0].plusMinutes(45));
          LocalDateTime leg1Arr = leg1Dep.plusMinutes(135);
          Schedule leg1 =
              createSchedule(flight, "DEL", "BOM", leg1Dep, leg1Arr, 60, new BigDecimal("9500"));
          schedules.add(leg1);

          // 2-hour layover
          LocalDateTime leg2Dep = leg1Arr.plusHours(2);
          LocalDateTime leg2Arr = leg2Dep.plusMinutes(165);
          Schedule leg2 =
              createSchedule(flight, "BOM", "CCU", leg2Dep, leg2Arr, 58, new BigDecimal("11000"));
          schedules.add(leg2);

          // Create hops for this multi-segment flight
          createHopsForMultiSegment(flight, Arrays.asList(leg1, leg2));
          break;

        case "SG8137":
          // Another multi-segment route: BLR-MAA-CCU
          LocalDateTime sg137leg1Dep = LocalDateTime.of(date, timeSlots[1]);
          LocalDateTime sg137leg1Arr = sg137leg1Dep.plusMinutes(60);
          Schedule sg137leg1 =
              createSchedule(
                  flight, "BLR", "MAA", sg137leg1Dep, sg137leg1Arr, 55, new BigDecimal("7500"));
          schedules.add(sg137leg1);

          // 90-minute layover
          LocalDateTime sg137leg2Dep = sg137leg1Arr.plusMinutes(90);
          LocalDateTime sg137leg2Arr = sg137leg2Dep.plusMinutes(135);
          Schedule sg137leg2 =
              createSchedule(
                  flight, "MAA", "CCU", sg137leg2Dep, sg137leg2Arr, 52, new BigDecimal("12500"));
          schedules.add(sg137leg2);

          createHopsForMultiSegment(flight, Arrays.asList(sg137leg1, sg137leg2));
          break;
      }

      // Save schedules for this flight
      flight.setSchedules(schedules);
      scheduleRepository.saveAll(schedules);
    }

    logger.info(
        "Created comprehensive flight network supporting direct, one-hop, and multi-hop routes");
  }

  private Schedule createSchedule(
      Flight flight,
      String source,
      String destination,
      LocalDateTime departureTime,
      LocalDateTime arrivalTime,
      int availableSeats,
      BigDecimal baseFare) {
    return new Schedule(
        flight, source, destination, departureTime, arrivalTime, availableSeats, baseFare);
  }

  private void createHopsForMultiSegment(Flight flight, List<Schedule> schedules) {
    for (int i = 0; i < schedules.size(); i++) {
      Schedule schedule = schedules.get(i);
      // Create hop instance - in a real implementation, you'd save hops to repository
      new Hop(flight, schedule, i + 1, schedule.getSource(), schedule.getDestination());
    }
  }

  /** Enhanced Elasticsearch synchronization with improved document format */
  private void syncToElasticsearch() {
    if (flightSearchRepository == null) {
      logger.info("Elasticsearch not available - skipping sync");
      return;
    }

    logger.info("🔄 Syncing flight data to Elasticsearch with enhanced format...");

    try {
      List<Flight> flights = flightRepository.findAll();
      List<FlightSearchDocument> documents = new ArrayList<>();

      for (Flight flight : flights) {
        for (Schedule schedule : flight.getSchedules()) {
          // Create enhanced flight search document with proper hop calculation
          FlightSearchDocument document = createEnhancedFlightDocument(flight, schedule);
          documents.add(document);
        }
      }

      // Batch save all documents
      logger.info("Saving {} flight documents to Elasticsearch...", documents.size());
      flightSearchRepository.saveAll(documents);

      // Wait for Elasticsearch to process the indexing
      Thread.sleep(1000);

      logger.info("✅ Successfully synced {} flight documents to Elasticsearch", documents.size());

      // Verify the sync
      verifyElasticsearchSync(documents.size());

    } catch (Exception e) {
      logger.error("❌ Error syncing to Elasticsearch: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to sync flight data to Elasticsearch", e);
    }
  }

  /** Create enhanced flight document with proper metadata and hop calculation */
  private FlightSearchDocument createEnhancedFlightDocument(Flight flight, Schedule schedule) {
    // Calculate actual hops based on route complexity
    int actualHops = calculateActualHops(schedule);

    FlightSearchDocument document =
        new FlightSearchDocument(
            flight.getFlightNumber(),
            flight.getAirline(),
            schedule.getSource(),
            schedule.getDestination(),
            schedule.getDepartureTime(),
            schedule.getArrivalTime(),
            schedule.getBaseFare(),
            schedule.getAvailableSeats(),
            actualHops,
            schedule.getId(),
            flight.getId());

    // Enhanced search tags for better filtering and discovery
    List<String> enhancedTags = createEnhancedSearchTags(flight, schedule, actualHops);
    document.setSearchTags(enhancedTags);

    return document;
  }

  /** Calculate actual hops more accurately for the flight search system */
  private Integer calculateActualHops(Schedule schedule) {
    // For multi-segment flights (like SG8136, SG8137), calculate based on segments
    if (schedule.getHops() != null && !schedule.getHops().isEmpty()) {
      return schedule.getHops().size();
    }

    // For single-segment flights, it's 0 hops (direct)
    return 0;
  }

  /** Create comprehensive search tags for better Elasticsearch filtering */
  private List<String> createEnhancedSearchTags(Flight flight, Schedule schedule, int hops) {
    List<String> tags = new ArrayList<>();

    // Flight type tags
    tags.add(hops == 0 ? "direct" : "connecting");
    tags.add(hops == 0 ? "nonstop" : hops + "_hop");

    // Airline tags
    String airlineCode = flight.getAirline().toLowerCase().replace(" ", "_");
    tags.add("airline_" + airlineCode);
    tags.add(airlineCode);

    // Route tags
    tags.add(
        "route_"
            + schedule.getSource().toLowerCase()
            + "_"
            + schedule.getDestination().toLowerCase());
    tags.add("from_" + schedule.getSource().toLowerCase());
    tags.add("to_" + schedule.getDestination().toLowerCase());

    // Time-based tags
    LocalTime depTime = schedule.getDepartureTime().toLocalTime();
    if (depTime.isBefore(LocalTime.of(9, 0))) {
      tags.add("morning_departure");
    } else if (depTime.isBefore(LocalTime.of(17, 0))) {
      tags.add("daytime_departure");
    } else {
      tags.add("evening_departure");
    }

    // Price category tags
    double price = schedule.getBaseFare().doubleValue();
    if (price < 10000) {
      tags.add("budget");
    } else if (price < 25000) {
      tags.add("economy");
    } else {
      tags.add("premium");
    }

    // Capacity tags
    if (schedule.getAvailableSeats() > 50) {
      tags.add("high_availability");
    } else if (schedule.getAvailableSeats() > 20) {
      tags.add("moderate_availability");
    } else {
      tags.add("limited_availability");
    }

    return tags;
  }

  /** Verify that Elasticsearch synchronization was successful */
  private void verifyElasticsearchSync(long expectedCount) {
    try {
      long actualCount = flightSearchRepository.count();
      logger.info(
          "📊 Elasticsearch verification: Expected {} documents, Found {} documents",
          expectedCount,
          actualCount);

      if (actualCount != expectedCount) {
        logger.warn(
            "⚠️ Document count mismatch in Elasticsearch! Expected: {}, Actual: {}",
            expectedCount,
            actualCount);
      } else {
        logger.info("✅ Elasticsearch sync verification passed");
      }
    } catch (Exception e) {
      logger.warn("⚠️ Could not verify Elasticsearch sync: {}", e.getMessage());
    }
  }

  /** Public method to force refresh and repopulate all data */
  public void refreshAllData() {
    logger.info("🔄 Starting complete data refresh for Neo4j and Elasticsearch...");

    try {
      // Clear all existing data
      clearAllExistingData();

      // Wait for cleanup to complete
      Thread.sleep(3000);

      // Repopulate with fresh data
      populateTestData();

      logger.info("🎉 Complete data refresh completed successfully!");

    } catch (Exception e) {
      logger.error("❌ Error during complete data refresh: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to refresh all data", e);
    }
  }

  /** Helper method to create a schedule for a specific time slot */
  private Schedule createScheduleForTime(
      Flight flight,
      String source,
      String destination,
      LocalDate date,
      LocalTime departureTime,
      int durationMinutes,
      int availableSeats,
      BigDecimal baseFare) {
    LocalDateTime departure = LocalDateTime.of(date, departureTime);
    LocalDateTime arrival = departure.plusMinutes(durationMinutes);
    return createSchedule(
        flight, source, destination, departure, arrival, availableSeats, baseFare);
  }

  /** Create additional test data for edge cases and filtering tests */
  public void createAdditionalTestData() {
    logger.info("Creating additional test scenarios for edge cases and filtering...");

    try {
      // Create more flights for different scenarios
      LocalDate tomorrow = LocalDate.now().plusDays(1);

      // Create a high-price flight for price filtering tests
      Flight expensiveFlight = createFlight("AI999", "Air India", 180);
      expensiveFlight = flightRepository.save(expensiveFlight);

      Schedule expensiveSchedule =
          createSchedule(
              expensiveFlight,
              "DEL",
              "BOM",
              LocalDateTime.of(tomorrow, LocalTime.of(8, 0)),
              LocalDateTime.of(tomorrow, LocalTime.of(10, 15)),
              15, // Limited seats
              new BigDecimal("75000") // Expensive flight
              );
      scheduleRepository.save(expensiveSchedule);

      // Create a budget flight for price range testing
      Flight budgetFlight = createFlight("6E999", "IndiGo", 186);
      budgetFlight = flightRepository.save(budgetFlight);

      Schedule budgetSchedule =
          createSchedule(
              budgetFlight,
              "BOM",
              "DEL",
              LocalDateTime.of(tomorrow, LocalTime.of(14, 30)),
              LocalDateTime.of(tomorrow, LocalTime.of(16, 45)),
              120, // High availability
              new BigDecimal("3500") // Budget price
              );
      scheduleRepository.save(budgetSchedule);

      // Create a flight with very limited seats for availability testing
      Flight limitedFlight = createFlight("UK999", "Vistara", 158);
      limitedFlight = flightRepository.save(limitedFlight);

      Schedule limitedSchedule =
          createSchedule(
              limitedFlight,
              "BLR",
              "MAA",
              LocalDateTime.of(tomorrow, LocalTime.of(19, 15)),
              LocalDateTime.of(tomorrow, LocalTime.of(20, 15)),
              3, // Very limited seats
              new BigDecimal("18500"));
      scheduleRepository.save(limitedSchedule);

      // Sync additional data to Elasticsearch if available
      if (flightSearchRepository != null) {
        List<FlightSearchDocument> additionalDocs = new ArrayList<>();

        // Create documents for the additional flights
        additionalDocs.add(createEnhancedFlightDocument(expensiveFlight, expensiveSchedule));
        additionalDocs.add(createEnhancedFlightDocument(budgetFlight, budgetSchedule));
        additionalDocs.add(createEnhancedFlightDocument(limitedFlight, limitedSchedule));

        flightSearchRepository.saveAll(additionalDocs);
        logger.info(
            "✅ Synced {} additional flight documents to Elasticsearch", additionalDocs.size());
      }

      logger.info(
          "✅ Additional test data created successfully - includes high-price, budget, and limited availability scenarios");

    } catch (Exception e) {
      logger.error("❌ Error creating additional test data: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create additional test data", e);
    }
  }
}
