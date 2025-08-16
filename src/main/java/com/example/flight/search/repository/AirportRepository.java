package com.example.flight.search.repository;

import com.example.flight.search.graph.Airport;
import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AirportRepository extends Neo4jRepository<Airport, String> {

  @Query(
      "MATCH (start:Airport {code: $source})-[r:CONNECTED_TO*1..3]->(end:Airport {code: $destination}) "
          + "RETURN start, r, end "
          + "ORDER BY length(r) ASC, reduce(totalPrice = 0, rel in r | totalPrice + rel.avgPrice) ASC "
          + "LIMIT $maxResults")
  List<List<Airport>> findShortestPaths(
      @Param("source") String source,
      @Param("destination") String destination,
      @Param("maxResults") Integer maxResults);

  @Query(
      "MATCH (start:Airport {code: $source})-[r:CONNECTED_TO]->(intermediate:Airport)-[r2:CONNECTED_TO]->(end:Airport {code: $destination}) "
          + "RETURN start, intermediate, end, r, r2 "
          + "ORDER BY r.avgPrice + r2.avgPrice ASC "
          + "LIMIT $maxResults")
  List<List<Airport>> findOneStopConnections(
      @Param("source") String source,
      @Param("destination") String destination,
      @Param("maxResults") Integer maxResults);

  @Query(
      "MATCH (airport:Airport) "
          + "WHERE airport.city =~ ('.*' + $city + '.*') OR airport.name =~ ('.*' + $city + '.*') "
          + "RETURN airport")
  List<Airport> findByCity(@Param("city") String city);

  @Query(
      "MATCH (start:Airport {code: $source})-[r:CONNECTED_TO]->(destination:Airport) "
          + "RETURN destination "
          + "ORDER BY r.frequency DESC")
  List<Airport> findDirectConnections(@Param("source") String source);

  @Query(
      "MATCH path = (start:Airport {code: $source})-[*$hops]->(end:Airport {code: $destination}) "
          + "WHERE ALL(node IN nodes(path) WHERE size([n IN nodes(path) WHERE n = node]) = 1) "
          + // No circular paths
          "RETURN [node IN nodes(path) | node] AS routePath "
          + "ORDER BY length(path) ASC "
          + "LIMIT $maxResults")
  List<List<Airport>> findPathsWithExactHops(
      @Param("source") String source,
      @Param("destination") String destination,
      @Param("hops") int hops,
      @Param("maxResults") int maxResults);
}
