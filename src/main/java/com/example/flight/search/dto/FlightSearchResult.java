package com.example.flight.search.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Flight search result")
public class FlightSearchResult {

  @Schema(description = "Unique identifier for the flight search result")
  private UUID uuid;

  @Schema(description = "Flight number", example = "AI101")
  private String flightNumber;

  @Schema(description = "Airline name", example = "Air India")
  private String airline;

  @Schema(description = "Departure time", example = "2025-08-20T06:00:00")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime departureTime;

  @Schema(description = "Arrival time", example = "2025-08-20T08:15:00")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime arrivalTime;

  @Schema(description = "Total price for the flight", example = "15000.0")
  private BigDecimal price;

  @Schema(description = "Number of hops/stops", example = "0")
  private Integer numberOfHops;

  @Schema(description = "Flight schedules including all segments")
  private List<ScheduleDto> schedules;

  // Constructors
  public FlightSearchResult() {
    this.uuid = UUID.randomUUID();
  }

  public FlightSearchResult(
      String flightNumber,
      String airline,
      LocalDateTime departureTime,
      LocalDateTime arrivalTime,
      BigDecimal price,
      Integer numberOfHops,
      List<ScheduleDto> schedules) {
    this.uuid = UUID.randomUUID();
    this.flightNumber = flightNumber;
    this.airline = airline;
    this.departureTime = departureTime;
    this.arrivalTime = arrivalTime;
    this.price = price;
    this.numberOfHops = numberOfHops;
    this.schedules = schedules;
  }

  // Getters and Setters
  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public String getFlightNumber() {
    return flightNumber;
  }

  public void setFlightNumber(String flightNumber) {
    this.flightNumber = flightNumber;
  }

  public String getAirline() {
    return airline;
  }

  public void setAirline(String airline) {
    this.airline = airline;
  }

  public LocalDateTime getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(LocalDateTime departureTime) {
    this.departureTime = departureTime;
  }

  public LocalDateTime getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(LocalDateTime arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Integer getNumberOfHops() {
    return numberOfHops;
  }

  public void setNumberOfHops(Integer numberOfHops) {
    this.numberOfHops = numberOfHops;
  }

  public List<ScheduleDto> getSchedules() {
    return schedules;
  }

  public void setSchedules(List<ScheduleDto> schedules) {
    this.schedules = schedules;
  }
}
