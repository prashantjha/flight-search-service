package com.example.flight.search.controller;

import com.example.flight.search.repository.FlightRepository;
import com.example.flight.search.service.DataSyncService;
import com.example.flight.search.service.TestDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Operations", description = "Administrative operations for data management")
public class AdminController {

  @Autowired private DataSyncService dataSyncService;

  @Autowired private TestDataService testDataService;

  @Autowired private FlightRepository flightRepository;

  @Operation(
      summary = "Sync flight data to Elasticsearch",
      description = "Manually trigger synchronization of flight data from MySQL to Elasticsearch")
  @PostMapping("/sync/elasticsearch")
  public ResponseEntity<String> syncToElasticsearch() {
    dataSyncService.syncFlightDataToElasticsearch();
    return ResponseEntity.ok("Elasticsearch sync initiated");
  }

  @Operation(
      summary = "Sync airport data to Neo4j",
      description = "Manually trigger synchronization of airport data to Neo4j graph database")
  @PostMapping("/sync/neo4j")
  public ResponseEntity<String> syncToNeo4j() {
    dataSyncService.syncAirportDataToNeo4j();
    return ResponseEntity.ok("Neo4j sync initiated");
  }

  @Operation(summary = "Health check", description = "Check service health status")
  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Flight Search Service is running");
  }

  @Operation(
      summary = "Database health check",
      description = "Check database connectivity and table structure")
  @GetMapping("/health/database")
  public ResponseEntity<?> databaseHealth() {
    try {
      long flightCount = flightRepository.count();
      return ResponseEntity.ok(
          Map.of(
              "status", "healthy",
              "flightCount", flightCount,
              "message", "Database connection successful"));
    } catch (Exception e) {
      return ResponseEntity.status(500)
          .body(
              Map.of(
                  "status", "unhealthy",
                  "error", e.getMessage(),
                  "message", "Database connection failed"));
    }
  }

  @Operation(
      summary = "Populate test data",
      description = "Load comprehensive test data for flights, schedules, and airports")
  @PostMapping("/populate-test-data")
  public ResponseEntity<String> populateTestData() {
    testDataService.populateTestData();
    return ResponseEntity.ok("Test data populated successfully");
  }

  @Operation(
      summary = "Create additional test scenarios",
      description = "Add more test data for edge cases and filtering tests")
  @PostMapping("/populate-additional-data")
  public ResponseEntity<String> populateAdditionalData() {
    testDataService.createAdditionalTestData();
    return ResponseEntity.ok("Additional test data created");
  }

  @Operation(
      summary = "Populate test data for specific date",
      description = "Load test data for a specific date instead of current date")
  @PostMapping("/populate-test-data/{date}")
  public ResponseEntity<String> populateTestDataForDate(
      @Parameter(description = "Target date for test data", example = "2025-08-20")
          @PathVariable
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date) {
    testDataService.populateTestDataForDate(date);
    return ResponseEntity.ok("Test data populated successfully for date: " + date);
  }

  @Operation(
      summary = "Complete data refresh",
      description =
          "Clear all existing data from Neo4j, Elasticsearch, and Database, then repopulate with fresh data in the new expected format")
  @PostMapping("/refresh-all-data")
  public ResponseEntity<String> refreshAllData() {
    try {
      testDataService.refreshAllData();
      return ResponseEntity.ok(
          "🎉 Complete data refresh completed successfully! All systems now have fresh data in the new expected format.");
    } catch (Exception e) {
      return ResponseEntity.status(500).body("❌ Error during data refresh: " + e.getMessage());
    }
  }

  @Operation(
      summary = "Enhanced data population",
      description =
          "Populate data with enhanced format including comprehensive airport metadata, improved Elasticsearch documents, and proper hop calculations")
  @PostMapping("/populate-enhanced-data")
  public ResponseEntity<String> populateEnhancedData() {
    try {
      testDataService.populateTestData();
      return ResponseEntity.ok(
          "✅ Enhanced data population completed successfully with improved format for multi-hop search!");
    } catch (Exception e) {
      return ResponseEntity.status(500)
          .body("❌ Error during enhanced data population: " + e.getMessage());
    }
  }
}
