package com.example.flight.search.repository;

import com.example.flight.search.document.FlightSearchDocument;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightSearchRepository
    extends ElasticsearchRepository<FlightSearchDocument, String> {

  Page<FlightSearchDocument>
      findBySourceAndDestinationAndDepartureTimeAfterAndAvailableSeatsGreaterThanEqual(
          String source,
          String destination,
          LocalDateTime departureTime,
          Integer availableSeats,
          Pageable pageable);

  Page<FlightSearchDocument>
      findBySourceAndDestinationAndDepartureTimeAfterAndAvailableSeatsGreaterThanEqualAndPriceLessThanEqual(
          String source,
          String destination,
          LocalDateTime departureTime,
          Integer availableSeats,
          Double maxPrice,
          Pageable pageable);

  Page<FlightSearchDocument>
      findBySourceAndDestinationAndDepartureTimeAfterAndAvailableSeatsGreaterThanEqualAndNumberOfHopsLessThanEqual(
          String source,
          String destination,
          LocalDateTime departureTime,
          Integer availableSeats,
          Integer maxHops,
          Pageable pageable);

  Page<FlightSearchDocument>
      findBySourceAndDestinationAndDepartureTimeAfterAndAvailableSeatsGreaterThanEqualAndAirline(
          String source,
          String destination,
          LocalDateTime departureTime,
          Integer availableSeats,
          String airline,
          Pageable pageable);

  List<FlightSearchDocument> findByFlightNumberAndDepartureTimeAfter(
      String flightNumber, LocalDateTime departureTime);

  Page<FlightSearchDocument>
      findBySourceAndDestinationAndDepartureTimeBetweenAndAvailableSeatsGreaterThanEqual(
          String source,
          String destination,
          LocalDateTime startTime,
          LocalDateTime endTime,
          Integer availableSeats,
          Pageable pageable);

  List<FlightSearchDocument> findBySourceAndDestination(String source, String destination);
}
