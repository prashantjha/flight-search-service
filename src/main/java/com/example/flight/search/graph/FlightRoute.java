package com.example.flight.search.graph;

import java.math.BigDecimal;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class FlightRoute {

  @Id @GeneratedValue private Long id;

  @Property private String airline;

  @Property private String flightNumber;

  @Property private Integer distance; // in kilometers

  @Property private Integer avgDurationMinutes;

  @Property private BigDecimal avgPrice;

  @Property private Integer frequency; // flights per day

  @TargetNode private Airport destination;

  // Constructors
  public FlightRoute() {}

  public FlightRoute(String airline, String flightNumber, Airport destination) {
    this.airline = airline;
    this.flightNumber = flightNumber;
    this.destination = destination;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAirline() {
    return airline;
  }

  public void setAirline(String airline) {
    this.airline = airline;
  }

  public String getFlightNumber() {
    return flightNumber;
  }

  public void setFlightNumber(String flightNumber) {
    this.flightNumber = flightNumber;
  }

  public Integer getDistance() {
    return distance;
  }

  public void setDistance(Integer distance) {
    this.distance = distance;
  }

  public Integer getAvgDurationMinutes() {
    return avgDurationMinutes;
  }

  public void setAvgDurationMinutes(Integer avgDurationMinutes) {
    this.avgDurationMinutes = avgDurationMinutes;
  }

  public BigDecimal getAvgPrice() {
    return avgPrice;
  }

  public void setAvgPrice(BigDecimal avgPrice) {
    this.avgPrice = avgPrice;
  }

  public Integer getFrequency() {
    return frequency;
  }

  public void setFrequency(Integer frequency) {
    this.frequency = frequency;
  }

  public Airport getDestination() {
    return destination;
  }

  public void setDestination(Airport destination) {
    this.destination = destination;
  }
}
