package com.example.flight.search.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Schedule information for flight segments")
public class ScheduleDto {

  @Schema(description = "Schedule ID", example = "1")
  private Long scheduleId;

  @Schema(description = "Source airport code", example = "DEL")
  private String source;

  @Schema(description = "Destination airport code", example = "BOM")
  private String destination;

  @Schema(description = "Departure time", example = "2025-08-20T06:00:00")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime departureTime;

  @Schema(description = "Arrival time", example = "2025-08-20T08:15:00")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime arrivalTime;

  @Schema(description = "Available seats", example = "45")
  private Integer availableSeats;

  // Constructors
  public ScheduleDto() {}

  public ScheduleDto(
      Long scheduleId,
      String source,
      String destination,
      LocalDateTime departureTime,
      LocalDateTime arrivalTime,
      Integer availableSeats) {
    this.scheduleId = scheduleId;
    this.source = source;
    this.destination = destination;
    this.departureTime = departureTime;
    this.arrivalTime = arrivalTime;
    this.availableSeats = availableSeats;
  }

  // Getters and Setters
  public Long getScheduleId() {
    return scheduleId;
  }

  public void setScheduleId(Long scheduleId) {
    this.scheduleId = scheduleId;
  }

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

  public Integer getAvailableSeats() {
    return availableSeats;
  }

  public void setAvailableSeats(Integer availableSeats) {
    this.availableSeats = availableSeats;
  }
}
