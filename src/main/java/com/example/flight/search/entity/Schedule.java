package com.example.flight.search.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
    name = "schedules",
    indexes = {
      @Index(
          name = "idx_schedule_source_dest_time",
          columnList = "source, destination, departure_time"),
      @Index(name = "idx_schedule_departure_time", columnList = "departure_time"),
      @Index(name = "idx_schedule_available_seats", columnList = "available_seats")
    })
public class Schedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "flight_id", nullable = false)
  private Flight flight;

  @Column(nullable = false, length = 10)
  private String source;

  @Column(nullable = false, length = 10)
  private String destination;

  @Column(name = "departure_time", nullable = false)
  private LocalDateTime departureTime;

  @Column(name = "arrival_time", nullable = false)
  private LocalDateTime arrivalTime;

  @Column(name = "available_seats", nullable = false)
  private Integer availableSeats;

  @Column(name = "base_fare", nullable = false, precision = 10, scale = 2)
  private BigDecimal baseFare;

  @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Hop> hops;

  // Constructors
  public Schedule() {}

  public Schedule(
      Flight flight,
      String source,
      String destination,
      LocalDateTime departureTime,
      LocalDateTime arrivalTime,
      Integer availableSeats,
      BigDecimal baseFare) {
    this.flight = flight;
    this.source = source;
    this.destination = destination;
    this.departureTime = departureTime;
    this.arrivalTime = arrivalTime;
    this.availableSeats = availableSeats;
    this.baseFare = baseFare;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Flight getFlight() {
    return flight;
  }

  public void setFlight(Flight flight) {
    this.flight = flight;
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

  public BigDecimal getBaseFare() {
    return baseFare;
  }

  public void setBaseFare(BigDecimal baseFare) {
    this.baseFare = baseFare;
  }

  public List<Hop> getHops() {
    return hops;
  }

  public void setHops(List<Hop> hops) {
    this.hops = hops;
  }
}
