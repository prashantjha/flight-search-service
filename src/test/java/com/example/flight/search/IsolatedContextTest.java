package com.example.flight.search;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@ComponentScan(
    basePackages = "com.example.flight.search",
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {JpaRepository.class, Neo4jRepository.class, ElasticsearchRepository.class}),
      @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Repository.*"),
      @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Config.*")
    })
class IsolatedContextTest {

  @TestConfiguration
  static class TestConfig {
    @Bean
    public String testBean() {
      return "Test Configuration Loaded";
    }
  }

  @Test
  void contextLoadsWithoutRepositories() {
    // This test loads only basic Spring context without any repository or configuration classes
    System.out.println("Isolated Spring context loaded successfully!");
  }
}
