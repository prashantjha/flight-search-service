package com.example.flight.search.repository;

import com.example.flight.search.entity.Flight;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

  Optional<Flight> findByFlightNumber(String flightNumber);

  List<Flight> findByAirlineContainingIgnoreCase(String airline);

  @Query(
      "SELECT DISTINCT f FROM Flight f "
          + "JOIN f.schedules s "
          + "WHERE s.source = :source AND s.destination = :destination "
          + "AND s.availableSeats >= :requiredSeats")
  List<Flight> findDirectFlights(
      @Param("source") String source,
      @Param("destination") String destination,
      @Param("requiredSeats") Integer requiredSeats);

  @Query(
      "SELECT DISTINCT f FROM Flight f "
          + "JOIN f.schedules s "
          + "WHERE (s.source = :source OR s.destination = :destination) "
          + "AND s.availableSeats >= :requiredSeats")
  List<Flight> findConnectingFlights(
      @Param("source") String source,
      @Param("destination") String destination,
      @Param("requiredSeats") Integer requiredSeats);
}
