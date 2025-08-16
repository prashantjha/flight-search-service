package com.example.flight.search.controller;

import com.example.flight.search.dto.FlightSearchRequest;
import com.example.flight.search.dto.FlightSearchResult;
import com.example.flight.search.service.FlightSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@Tag(name = "Flight Search API", description = "High-performance flight search operations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FlightSearchController {

  private static final Logger logger = LoggerFactory.getLogger(FlightSearchController.class);

  @Autowired private FlightSearchService flightSearchService;

  @Operation(
      summary = "Search for flights",
      description =
          "Search for available flights with enhanced multi-hop support. Supports both direct and connecting flights with proper timing validation.",
      tags = {"Flight Search"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Flights found successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<Page<FlightSearchResult>> searchFlights(
      @Parameter(
              description = "Source airport code (IATA 3-letter code)",
              example = "DEL",
              required = true)
          @RequestParam
          String source,
      @Parameter(
              description = "Destination airport code (IATA 3-letter code)",
              example = "BOM",
              required = true)
          @RequestParam
          String destination,
      @Parameter(
              description = "Departure date (YYYY-MM-DD format)",
              example = "2025-08-20",
              required = false,
              schema =
                  @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "date"))
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate departureDate,
      @Parameter(
              description =
                  "Legacy parameter: Departure date and time (ISO format: YYYY-MM-DDTHH:MM:SS). Use departureDate and preferredTime instead.",
              example = "2025-08-20T06:00:00",
              required = false,
              schema =
                  @io.swagger.v3.oas.annotations.media.Schema(
                      type = "string",
                      format = "date-time"))
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime time,
      @Parameter(
              description = "Number of seats required (must be positive)",
              example = "2",
              required = true,
              schema = @io.swagger.v3.oas.annotations.media.Schema(minimum = "1", maximum = "9"))
          @RequestParam
          Integer noOfSeats,
      @Parameter(description = "Sort results by price (ascending)", example = "false")
          @RequestParam(defaultValue = "false")
          Boolean sortByPrice,
      @Parameter(description = "Sort results by number of hops (ascending)", example = "false")
          @RequestParam(defaultValue = "false")
          Boolean sortByHops,
      @Parameter(description = "Page number for pagination (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          Integer page,
      @Parameter(
              description = "Number of results per page",
              example = "10",
              schema = @io.swagger.v3.oas.annotations.media.Schema(minimum = "1", maximum = "100"))
          @RequestParam(defaultValue = "10")
          Integer size,
      @Parameter(
              description =
                  "Maximum price filter (in INR). Only flights below this price will be returned",
              example = "25000")
          @RequestParam(required = false)
          Double maxPrice,
      @Parameter(
              description =
                  "Maximum number of hops/stops. 0=direct flights, 1=one stop, etc. If not specified, searches up to 3 hops",
              example = "1",
              schema = @io.swagger.v3.oas.annotations.media.Schema(minimum = "0", maximum = "3"))
          @RequestParam(required = false)
          Integer maxHops,
      @Parameter(description = "Preferred airline (partial match supported)", example = "Air India")
          @RequestParam(required = false)
          String airline) {
    // Determine the actual departure time to use
    LocalDateTime actualTime;
    try {
      actualTime = determineDateTime(departureDate, null, time);
      if (actualTime == null) {
        throw new IllegalArgumentException(
            "Either 'departureDate' or 'time' parameter is required");
      }
    } catch (IllegalArgumentException e) {
      logger.warn("Invalid request parameters: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }

    logger.info(
        "Flight search request: {} to {} on {} for {} seats",
        source,
        destination,
        actualTime,
        noOfSeats);

    try {
      FlightSearchRequest request = new FlightSearchRequest();
      request.setSource(source.toUpperCase());
      request.setDestination(destination.toUpperCase());

      // Set both new and legacy fields for maximum compatibility
      request.setDepartureDate(departureDate);
      request.setTime(actualTime);

      request.setNoOfSeats(noOfSeats);
      request.setSortByPrice(sortByPrice);
      request.setSortByHops(sortByHops);
      request.setPage(page);
      request.setSize(size);
      request.setMaxPrice(maxPrice);
      request.setMaxHops(maxHops);
      request.setAirline(airline);

      Page<FlightSearchResult> results = flightSearchService.searchFlights(request);

      logger.info("Found {} flights for search criteria", results.getTotalElements());
      return ResponseEntity.ok(results);
    } catch (Exception e) {
      logger.error("Error processing flight search request", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Determines the LocalDateTime to use based on the provided parameters. Priority: departureDate +
   * preferredTime > time parameter
   */
  private LocalDateTime determineDateTime(
      LocalDate departureDate, LocalTime preferredTime, LocalDateTime time) {
    if (departureDate != null) {
      // Use departureDate with preferredTime (or default to start of day if no time specified)
      LocalTime timeToUse = preferredTime != null ? preferredTime : LocalTime.of(0, 0);
      return departureDate.atTime(timeToUse);
    }
    // Fall back to legacy time parameter
    return time;
  }

  @Operation(
      summary = "Search flights with POST",
      description = "Alternative POST endpoint for complex flight search")
  @PostMapping
  public ResponseEntity<Page<FlightSearchResult>> searchFlightsPost(
      @Valid @RequestBody FlightSearchRequest request) {
    logger.info(
        "POST Flight search request: {} to {} on {} for {} seats",
        request.getSource(),
        request.getDestination(),
        request.getTime(),
        request.getNoOfSeats());

    // Normalize airport codes
    request.setSource(request.getSource().toUpperCase());
    request.setDestination(request.getDestination().toUpperCase());

    Page<FlightSearchResult> results = flightSearchService.searchFlights(request);

    logger.info("Found {} flights for POST search criteria", results.getTotalElements());
    return ResponseEntity.ok(results);
  }

  @Operation(
      summary = "Get flight suggestions",
      description = "Get popular destinations from a source airport")
  @GetMapping("/suggestions/{source}")
  public ResponseEntity<?> getFlightSuggestions(
      @Parameter(description = "Source airport code", example = "DEL") @PathVariable
          String source) {
    // This would typically return popular destinations, trending routes, etc.
    logger.info("Getting flight suggestions for source: {}", source);
    return ResponseEntity.ok("Flight suggestions feature coming soon");
  }
}
