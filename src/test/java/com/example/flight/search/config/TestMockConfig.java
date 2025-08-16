package com.example.flight.search.config;

import com.example.flight.search.repository.AirportRepository;
import com.example.flight.search.repository.FlightSearchRepository;
import com.example.flight.search.repository.ScheduleRepository;
import com.example.flight.search.service.CacheService;
import com.example.flight.search.service.PathFindingService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestMockConfig {

  @MockBean private FlightSearchRepository flightSearchRepository;

  @MockBean private ScheduleRepository scheduleRepository;

  @MockBean private AirportRepository airportRepository;

  @MockBean private CacheService cacheService;

  @MockBean private PathFindingService pathFindingService;
}
