package com.example.flight.search.document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "flight_search")
public class FlightSearchDocument {

  @Id private String id; // Combined flight_id + schedule_id

  @Field(type = FieldType.Keyword)
  private String flightNumber;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String airline;

  @Field(type = FieldType.Keyword)
  private String source;

  @Field(type = FieldType.Keyword)
  private String destination;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime departureTime;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime arrivalTime;

  @Field(type = FieldType.Double)
  private BigDecimal price;

  @Field(type = FieldType.Integer)
  private Integer availableSeats;

  @Field(type = FieldType.Integer)
  private Integer numberOfHops;

  @Field(type = FieldType.Long)
  private Long scheduleId;

  @Field(type = FieldType.Long)
  private Long flightId;

  @Field(type = FieldType.Nested)
  private List<HopDocument> hops;

  @Field(type = FieldType.Long)
  private Long durationMinutes;

  @Field(type = FieldType.Keyword)
  private List<String> searchTags; // For enhanced search (direct, one-stop, etc.)

  // Constructors
  public FlightSearchDocument() {}

  public FlightSearchDocument(
      String flightNumber,
      String airline,
      String source,
      String destination,
      LocalDateTime departureTime,
      LocalDateTime arrivalTime,
      BigDecimal price,
      Integer availableSeats,
      Integer numberOfHops,
      Long scheduleId,
      Long flightId) {
    this.flightNumber = flightNumber;
    this.airline = airline;
    this.source = source;
    this.destination = destination;
    this.departureTime = departureTime;
    this.arrivalTime = arrivalTime;
    this.price = price;
    this.availableSeats = availableSeats;
    this.numberOfHops = numberOfHops;
    this.scheduleId = scheduleId;
    this.flightId = flightId;
    this.id = flightId + "_" + scheduleId;
    this.durationMinutes = java.time.Duration.between(departureTime, arrivalTime).toMinutes();
  }

  // Getters and Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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
    if (this.arrivalTime != null) {
      this.durationMinutes =
          java.time.Duration.between(departureTime, this.arrivalTime).toMinutes();
    }
  }

  public LocalDateTime getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(LocalDateTime arrivalTime) {
    this.arrivalTime = arrivalTime;
    if (this.departureTime != null) {
      this.durationMinutes =
          java.time.Duration.between(this.departureTime, arrivalTime).toMinutes();
    }
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Integer getAvailableSeats() {
    return availableSeats;
  }

  public void setAvailableSeats(Integer availableSeats) {
    this.availableSeats = availableSeats;
  }

  public Integer getNumberOfHops() {
    return numberOfHops;
  }

  public void setNumberOfHops(Integer numberOfHops) {
    this.numberOfHops = numberOfHops;
  }

  public Long getScheduleId() {
    return scheduleId;
  }

  public void setScheduleId(Long scheduleId) {
    this.scheduleId = scheduleId;
  }

  public Long getFlightId() {
    return flightId;
  }

  public void setFlightId(Long flightId) {
    this.flightId = flightId;
  }

  public List<HopDocument> getHops() {
    return hops;
  }

  public void setHops(List<HopDocument> hops) {
    this.hops = hops;
  }

  public Long getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(Long durationMinutes) {
    this.durationMinutes = durationMinutes;
  }

  public List<String> getSearchTags() {
    return searchTags;
  }

  public void setSearchTags(List<String> searchTags) {
    this.searchTags = searchTags;
  }
}
