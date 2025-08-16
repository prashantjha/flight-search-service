package com.example.flight.search.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "hops",
    indexes = {
      @Index(name = "idx_hop_source_dest", columnList = "source, destination"),
      @Index(name = "idx_hop_order", columnList = "hop_order")
    })
public class Hop {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "flight_id", nullable = false)
  private Flight flight;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "schedule_id", nullable = false)
  private Schedule schedule;

  @Column(name = "hop_order", nullable = false)
  private Integer hopOrder;

  @Column(nullable = false, length = 10)
  private String source;

  @Column(nullable = false, length = 10)
  private String destination;

  // Constructors
  public Hop() {}

  public Hop(
      Flight flight, Schedule schedule, Integer hopOrder, String source, String destination) {
    this.flight = flight;
    this.schedule = schedule;
    this.hopOrder = hopOrder;
    this.source = source;
    this.destination = destination;
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

  public Schedule getSchedule() {
    return schedule;
  }

  public void setSchedule(Schedule schedule) {
    this.schedule = schedule;
  }

  public Integer getHopOrder() {
    return hopOrder;
  }

  public void setHopOrder(Integer hopOrder) {
    this.hopOrder = hopOrder;
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
}
