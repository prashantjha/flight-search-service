package com.example.flight.search.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "flights")
public class Flight {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "flight_number", nullable = false, unique = true)
  private String flightNumber;

  @Column(nullable = false)
  private String airline;

  @Column(name = "total_capacity", nullable = false)
  private Integer totalCapacity;

  @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Schedule> schedules;

  @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Hop> hops;

  @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Seat> seats;

  // Constructors
  public Flight() {}

  public Flight(String flightNumber, String airline, Integer totalCapacity) {
    this.flightNumber = flightNumber;
    this.airline = airline;
    this.totalCapacity = totalCapacity;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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

  public Integer getTotalCapacity() {
    return totalCapacity;
  }

  public void setTotalCapacity(Integer totalCapacity) {
    this.totalCapacity = totalCapacity;
  }

  public List<Schedule> getSchedules() {
    return schedules;
  }

  public void setSchedules(List<Schedule> schedules) {
    this.schedules = schedules;
  }

  public List<Hop> getHops() {
    return hops;
  }

  public void setHops(List<Hop> hops) {
    this.hops = hops;
  }

  public List<Seat> getSeats() {
    return seats;
  }

  public void setSeats(List<Seat> seats) {
    this.seats = seats;
  }
}
