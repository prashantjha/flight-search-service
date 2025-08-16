package com.example.flight.search.graph;

import java.util.Set;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node
public class Airport {

  @Id private String code; // IATA code like "DEL", "BOM"

  @Property private String name;

  @Property private String city;

  @Property private String country;

  @Property private Double latitude;

  @Property private Double longitude;

  @Relationship(type = "CONNECTED_TO", direction = Relationship.Direction.OUTGOING)
  private Set<FlightRoute> routes;

  // Constructors
  public Airport() {}

  public Airport(String code, String name, String city, String country) {
    this.code = code;
    this.name = name;
    this.city = city;
    this.country = country;
  }

  // Getters and Setters
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public Set<FlightRoute> getRoutes() {
    return routes;
  }

  public void setRoutes(Set<FlightRoute> routes) {
    this.routes = routes;
  }
}
