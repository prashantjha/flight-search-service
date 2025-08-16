package com.example.flight.search.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "Flight search request parameters")
public class FlightSearchRequest {

  @Schema(description = "Source airport code", example = "DEL", required = true)
  @NotBlank(message = "Source is required")
  private String source;

  @Schema(description = "Destination airport code", example = "BOM", required = true)
  @NotBlank(message = "Destination is required")
  private String destination;

  @Schema(description = "Departure date and time (legacy field)", example = "2025-08-20T06:00:00")
  @Future(message = "Departure time must be in the future")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime time;

  @Schema(description = "Departure date", example = "2025-08-20")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate departureDate;

  @Schema(description = "Preferred departure time", example = "09:30")
  @DateTimeFormat(pattern = "HH:mm")
  @JsonFormat(pattern = "HH:mm")
  private LocalTime preferredTime;

  @Schema(description = "Number of seats required", example = "2", required = true)
  @NotNull(message = "Number of seats is required") @Positive(message = "Number of seats must be positive") private Integer noOfSeats;

  @Schema(description = "Sort by price", example = "true")
  private Boolean sortByPrice = false;

  @Schema(description = "Sort by hops", example = "false")
  private Boolean sortByHops = false;

  @Schema(description = "Page number", example = "0")
  private Integer page = 0;

  @Schema(description = "Page size", example = "10")
  private Integer size = 10;

  @Schema(description = "Maximum price filter", example = "50000.0")
  private Double maxPrice;

  @Schema(description = "Maximum hops filter", example = "2")
  private Integer maxHops;

  @Schema(description = "Preferred airline", example = "Air India")
  private String airline;

  // Constructors
  public FlightSearchRequest() {}

  public FlightSearchRequest(
      String source, String destination, LocalDateTime time, Integer noOfSeats) {
    this.source = source;
    this.destination = destination;
    this.time = time;
    this.noOfSeats = noOfSeats;
  }

  // Helper methods to extract date and time components from legacy time field
  public LocalDateTime getDepartureDateTime() {
    if (departureDate != null) {
      LocalTime timeToUse = preferredTime != null ? preferredTime : LocalTime.of(0, 0);
      return departureDate.atTime(timeToUse);
    }
    return time;
  }

  public LocalDate getEffectiveDepartureDate() {
    return departureDate != null ? departureDate : (time != null ? time.toLocalDate() : null);
  }

  public LocalTime getEffectivePreferredTime() {
    return preferredTime != null ? preferredTime : (time != null ? time.toLocalTime() : null);
  }

  // Getters and Setters
  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }

  public LocalDate getDepartureDate() {
    return departureDate;
  }

  public void setDepartureDate(LocalDate departureDate) {
    this.departureDate = departureDate;
  }

  public LocalTime getPreferredTime() {
    return preferredTime;
  }

  public void setPreferredTime(LocalTime preferredTime) {
    this.preferredTime = preferredTime;
  }

  public Integer getNoOfSeats() {
    return noOfSeats;
  }

  public void setNoOfSeats(Integer noOfSeats) {
    this.noOfSeats = noOfSeats;
  }

  public Boolean getSortByPrice() {
    return sortByPrice;
  }

  public void setSortByPrice(Boolean sortByPrice) {
    this.sortByPrice = sortByPrice;
  }

  public Boolean getSortByHops() {
    return sortByHops;
  }

  public void setSortByHops(Boolean sortByHops) {
    this.sortByHops = sortByHops;
  }

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public Double getMaxPrice() {
    return maxPrice;
  }

  public void setMaxPrice(Double maxPrice) {
    this.maxPrice = maxPrice;
  }

  public Integer getMaxHops() {
    return maxHops;
  }

  public void setMaxHops(Integer maxHops) {
    this.maxHops = maxHops;
  }

  public String getAirline() {
    return airline;
  }

  public void setAirline(String airline) {
    this.airline = airline;
  }
}
