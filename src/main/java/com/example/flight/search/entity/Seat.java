package com.example.flight.search.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
    name = "seats",
    indexes = {
      @Index(name = "idx_seat_class", columnList = "seat_class"),
      @Index(name = "idx_seat_available", columnList = "is_available")
    })
public class Seat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "flight_id", nullable = false)
  private Flight flight;

  @Column(name = "seat_number", nullable = false, length = 5)
  private String seatNumber;

  @Column(name = "seat_class", nullable = false, length = 20)
  private String seatClass;

  @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal basePrice;

  @Column(nullable = false, precision = 3, scale = 2)
  private BigDecimal multiplier;

  @Column(name = "is_available", nullable = false)
  private Boolean isAvailable = true;

  // Constructors
  public Seat() {}

  public Seat(
      Flight flight,
      String seatNumber,
      String seatClass,
      BigDecimal basePrice,
      BigDecimal multiplier,
      Boolean isAvailable) {
    this.flight = flight;
    this.seatNumber = seatNumber;
    this.seatClass = seatClass;
    this.basePrice = basePrice;
    this.multiplier = multiplier;
    this.isAvailable = isAvailable;
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

  public String getSeatNumber() {
    return seatNumber;
  }

  public void setSeatNumber(String seatNumber) {
    this.seatNumber = seatNumber;
  }

  public String getSeatClass() {
    return seatClass;
  }

  public void setSeatClass(String seatClass) {
    this.seatClass = seatClass;
  }

  public BigDecimal getBasePrice() {
    return basePrice;
  }

  public void setBasePrice(BigDecimal basePrice) {
    this.basePrice = basePrice;
  }

  public BigDecimal getMultiplier() {
    return multiplier;
  }

  public void setMultiplier(BigDecimal multiplier) {
    this.multiplier = multiplier;
  }

  public Boolean getIsAvailable() {
    return isAvailable;
  }

  public void setIsAvailable(Boolean isAvailable) {
    this.isAvailable = isAvailable;
  }
}
