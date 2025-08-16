package com.example.flight.search.repository;

import com.example.flight.search.entity.Schedule;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  @Query(
      "SELECT s FROM Schedule s "
          + "WHERE s.source = :source "
          + "AND s.destination = :destination "
          + "AND s.departureTime >= :departureTime "
          + "AND s.availableSeats >= :requiredSeats "
          + "ORDER BY s.departureTime ASC")
  List<Schedule> findDirectFlightSchedules(
      @Param("source") String source,
      @Param("destination") String destination,
      @Param("departureTime") LocalDateTime departureTime,
      @Param("requiredSeats") Integer requiredSeats);

  @Query(
      "SELECT s FROM Schedule s "
          + "WHERE s.source = :source "
          + "AND s.departureTime >= :fromTime "
          + "AND s.departureTime <= :toTime "
          + "AND s.availableSeats >= :requiredSeats "
          + "ORDER BY s.departureTime ASC")
  List<Schedule> findDepartureSchedules(
      @Param("source") String source,
      @Param("fromTime") LocalDateTime fromTime,
      @Param("toTime") LocalDateTime toTime,
      @Param("requiredSeats") Integer requiredSeats);

  @Query(
      "SELECT s FROM Schedule s "
          + "WHERE s.destination = :destination "
          + "AND s.arrivalTime >= :fromTime "
          + "AND s.arrivalTime <= :toTime "
          + "AND s.availableSeats >= :requiredSeats "
          + "ORDER BY s.arrivalTime ASC")
  List<Schedule> findArrivalSchedules(
      @Param("destination") String destination,
      @Param("fromTime") LocalDateTime fromTime,
      @Param("toTime") LocalDateTime toTime,
      @Param("requiredSeats") Integer requiredSeats);

  List<Schedule> findBySourceAndDestinationAndAvailableSeatsGreaterThanEqual(
      String source, String destination, Integer requiredSeats);

  @Query(
      "SELECT s FROM Schedule s JOIN s.flight f "
          + "WHERE s.source = :source "
          + "AND s.destination = :destination "
          + "AND f.airline = :airline "
          + "AND s.availableSeats >= :requiredSeats "
          + "ORDER BY s.departureTime ASC")
  List<Schedule> findBySourceDestinationAndAirline(
      @Param("source") String source,
      @Param("destination") String destination,
      @Param("airline") String airline,
      @Param("requiredSeats") Integer requiredSeats);

  @Query("SELECT s FROM Schedule s WHERE s.source = :source AND s.destination = :destination")
  List<Schedule> findBySourceAndDestination(
      @Param("source") String source, @Param("destination") String destination);

  @Query("SELECT DISTINCT s.destination FROM Schedule s WHERE s.source = :source")
  List<String> findDestinationsBySource(@Param("source") String source);
}
