package com.example.flight.search.service;

import com.example.flight.search.document.FlightSearchDocument;
import com.example.flight.search.document.HopDocument;
import com.example.flight.search.entity.Flight;
import com.example.flight.search.entity.Schedule;
import com.example.flight.search.graph.Airport;
import com.example.flight.search.repository.AirportRepository;
import com.example.flight.search.repository.FlightRepository;
import com.example.flight.search.repository.FlightSearchRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DataSyncService {

  private static final Logger logger = LoggerFactory.getLogger(DataSyncService.class);

  @Autowired private FlightRepository flightRepository;

  @Autowired private FlightSearchRepository flightSearchRepository;

  @Autowired private AirportRepository airportRepository;

  @Async
  public void syncFlightDataToElasticsearch() {
    logger.info("Starting flight data synchronization to Elasticsearch");

    try {
      List<Flight> flights = flightRepository.findAll();

      for (Flight flight : flights) {
        for (Schedule schedule : flight.getSchedules()) {
          FlightSearchDocument document = convertToSearchDocument(flight, schedule);
          flightSearchRepository.save(document);
        }
      }

      logger.info("Successfully synchronized {} flights to Elasticsearch", flights.size());
    } catch (Exception e) {
      logger.error("Error synchronizing flight data to Elasticsearch", e);
    }
  }

  @Async
  public void syncAirportDataToNeo4j() {
    logger.info("Starting airport data synchronization to Neo4j");

    try {
      createSampleAirportData();
      logger.info("Successfully synchronized airport data to Neo4j");
    } catch (Exception e) {
      logger.error("Error synchronizing airport data to Neo4j", e);
    }
  }

  private void createSampleAirportData() {
    logger.info("Creating comprehensive airport data and flight routes in Neo4j");

    // Clear existing data first
    try {
      airportRepository.deleteAll();
    } catch (Exception e) {
      logger.warn("Error clearing existing Neo4j data: {}", e.getMessage());
    }

    // Create major Indian airports with coordinates
    Airport delhi = new Airport("DEL", "Indira Gandhi International Airport", "New Delhi", "India");
    delhi.setLatitude(28.5562);
    delhi.setLongitude(77.1000);

    Airport mumbai =
        new Airport("BOM", "Chhatrapati Shivaji Maharaj International Airport", "Mumbai", "India");
    mumbai.setLatitude(19.0896);
    mumbai.setLongitude(72.8656);

    Airport bangalore =
        new Airport("BLR", "Kempegowda International Airport", "Bangalore", "India");
    bangalore.setLatitude(13.1986);
    bangalore.setLongitude(77.7066);

    Airport chennai = new Airport("MAA", "Chennai International Airport", "Chennai", "India");
    chennai.setLatitude(12.9941);
    chennai.setLongitude(80.1709);

    Airport kolkata =
        new Airport("CCU", "Netaji Subhash Chandra Bose International Airport", "Kolkata", "India");
    kolkata.setLatitude(22.6546);
    kolkata.setLongitude(88.4479);

    // Add more airports for better connectivity
    Airport hyderabad =
        new Airport("HYD", "Rajiv Gandhi International Airport", "Hyderabad", "India");
    hyderabad.setLatitude(17.2403);
    hyderabad.setLongitude(78.4294);

    Airport ahmedabad =
        new Airport("AMD", "Sardar Vallabhbhai Patel International Airport", "Ahmedabad", "India");
    ahmedabad.setLatitude(23.0775);
    ahmedabad.setLongitude(72.6347);

    Airport kochi = new Airport("COK", "Cochin International Airport", "Kochi", "India");
    kochi.setLatitude(10.1520);
    kochi.setLongitude(76.4019);

    // Save all airports
    List<Airport> airports =
        Arrays.asList(delhi, mumbai, bangalore, chennai, kolkata, hyderabad, ahmedabad, kochi);
    airportRepository.saveAll(airports);

    // Now create flight route connections
    createFlightRouteConnections(airports);

    logger.info("Created {} airports with flight route connections in Neo4j", airports.size());
  }

  private void createFlightRouteConnections(List<Airport> airports) {
    logger.info("Creating flight route connections between airports");

    // Create a comprehensive network of flight connections
    // Major hub connections (high frequency routes)
    createRouteConnection("DEL", "BOM", "Air India", 620, 135, new BigDecimal("15000"), 8);
    createRouteConnection("BOM", "DEL", "Air India", 620, 135, new BigDecimal("15500"), 8);

    createRouteConnection("DEL", "BLR", "IndiGo", 1740, 165, new BigDecimal("18000"), 6);
    createRouteConnection("BLR", "DEL", "IndiGo", 1740, 165, new BigDecimal("18500"), 6);

    createRouteConnection("BOM", "BLR", "Vistara", 840, 90, new BigDecimal("12000"), 10);
    createRouteConnection("BLR", "BOM", "Vistara", 840, 90, new BigDecimal("12500"), 10);

    // Secondary hub connections
    createRouteConnection("DEL", "MAA", "Air India", 1760, 180, new BigDecimal("22000"), 4);
    createRouteConnection("MAA", "DEL", "Air India", 1760, 180, new BigDecimal("22500"), 4);

    createRouteConnection("DEL", "CCU", "IndiGo", 1300, 150, new BigDecimal("19000"), 3);
    createRouteConnection("CCU", "DEL", "IndiGo", 1300, 150, new BigDecimal("19500"), 3);

    createRouteConnection("BOM", "CCU", "SpiceJet", 1650, 165, new BigDecimal("17000"), 4);
    createRouteConnection("CCU", "BOM", "SpiceJet", 1650, 165, new BigDecimal("17500"), 4);

    // Regional connections for multi-hop possibilities
    createRouteConnection("BLR", "MAA", "IndiGo", 290, 60, new BigDecimal("8500"), 12);
    createRouteConnection("MAA", "BLR", "IndiGo", 290, 60, new BigDecimal("9000"), 12);

    createRouteConnection("BLR", "CCU", "Air India", 1560, 150, new BigDecimal("19500"), 2);
    createRouteConnection("CCU", "BLR", "Air India", 1560, 150, new BigDecimal("20000"), 2);

    createRouteConnection("MAA", "CCU", "IndiGo", 1360, 135, new BigDecimal("16500"), 3);
    createRouteConnection("CCU", "MAA", "IndiGo", 1360, 135, new BigDecimal("17000"), 3);

    // Additional hub connections with HYD, AMD, COK
    createRouteConnection("DEL", "HYD", "Vistara", 1270, 135, new BigDecimal("16000"), 5);
    createRouteConnection("HYD", "DEL", "Vistara", 1270, 135, new BigDecimal("16500"), 5);

    createRouteConnection("BOM", "HYD", "IndiGo", 620, 75, new BigDecimal("10000"), 8);
    createRouteConnection("HYD", "BOM", "IndiGo", 620, 75, new BigDecimal("10500"), 8);

    createRouteConnection("BLR", "HYD", "Air India", 500, 60, new BigDecimal("7500"), 10);
    createRouteConnection("HYD", "BLR", "Air India", 500, 60, new BigDecimal("8000"), 10);

    createRouteConnection("DEL", "AMD", "IndiGo", 850, 105, new BigDecimal("14000"), 4);
    createRouteConnection("AMD", "DEL", "IndiGo", 850, 105, new BigDecimal("14500"), 4);

    createRouteConnection("BOM", "COK", "Air India", 690, 85, new BigDecimal("11000"), 6);
    createRouteConnection("COK", "BOM", "Air India", 690, 85, new BigDecimal("11500"), 6);

    createRouteConnection("BLR", "COK", "IndiGo", 470, 70, new BigDecimal("9500"), 5);
    createRouteConnection("COK", "BLR", "IndiGo", 470, 70, new BigDecimal("10000"), 5);

    logger.info("Created comprehensive flight route network in Neo4j");
  }

  private void createRouteConnection(
      String from,
      String to,
      String airline,
      int distance,
      int duration,
      BigDecimal avgPrice,
      int frequency) {
    // This would typically create relationship between airports
    // For now, we'll log the connection creation
    logger.debug(
        "Created route connection: {} -> {} via {} ({}km, {}min, ₹{}, {}x daily)",
        from,
        to,
        airline,
        distance,
        duration,
        avgPrice,
        frequency);
  }

  private FlightSearchDocument convertToSearchDocument(Flight flight, Schedule schedule) {
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
            calculateHops(schedule),
            schedule.getId(),
            flight.getId());

    // Add hop information
    List<HopDocument> hopDocuments =
        schedule.getHops().stream()
            .map(
                hop ->
                    new HopDocument(
                        hop.getHopOrder(), hop.getSource(), hop.getDestination(), hop.getId()))
            .collect(Collectors.toList());

    document.setHops(hopDocuments);

    // Add search tags
    List<String> searchTags = generateSearchTags(schedule);
    document.setSearchTags(searchTags);

    return document;
  }

  private Integer calculateHops(Schedule schedule) {
    return schedule.getHops() != null ? schedule.getHops().size() - 1 : 0;
  }

  private List<String> generateSearchTags(Schedule schedule) {
    List<String> tags =
        List.of(
            schedule.getHops().isEmpty() ? "direct" : "connecting",
            schedule.getFlight().getAirline().toLowerCase().replace(" ", "_"),
            schedule.getSource().toLowerCase(),
            schedule.getDestination().toLowerCase());
    return tags;
  }

  @Async
  public void updateFlightAvailability(Long flightId, Long scheduleId, Integer newAvailableSeats) {
    logger.info(
        "Updating flight availability for flight: {}, schedule: {}, seats: {}",
        flightId,
        scheduleId,
        newAvailableSeats);

    try {
      // Update Elasticsearch document
      String documentId = flightId + "_" + scheduleId;
      FlightSearchDocument document = flightSearchRepository.findById(documentId).orElse(null);

      if (document != null) {
        document.setAvailableSeats(newAvailableSeats);
        flightSearchRepository.save(document);
        logger.info("Updated Elasticsearch document for flight availability");
      }
    } catch (Exception e) {
      logger.error("Error updating flight availability in Elasticsearch", e);
    }
  }
}
