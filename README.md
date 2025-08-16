# Flight Search Service

A high-performance, scalable Flight Search Service built as an independent microservice using Spring Boot 3.5.4 and Java 17.

## Architecture Overview

This service provides:
- **High-Performance Search**: Sub-second response times using Elasticsearch
- **Multi-layer Caching**: Redis for performance optimization
- **Graph-based Path Finding**: Neo4j for complex route discovery
- **Rich Filtering & Sorting**: Price, hops, airline, and time-based filtering
- **Comprehensive API Documentation**: OpenAPI 3.0 with Swagger UI
- **Code Quality**: Automated code formatting with Spotless

## Technology Stack

- **Framework**: Spring Boot 3.5.4 with Java 17
- **Search Engine**: Elasticsearch for fast text and structured search
- **Cache**: Redis for multi-layer caching
- **Graph Database**: Neo4j for path finding and route optimization
- **Primary Database**: MySQL with JPA for data persistence
- **API Documentation**: OpenAPI 3.0 with Swagger UI
- **Code Formatting**: Spotless with Google Java Format
- **Build System**: Maven

## API Endpoints

### Search Flights
```
GET /api/v1/search?source={source}&destination={destination}&time={time}&noOfSeats={seats}
```

**Parameters:**
- `source` (required): Source airport code (e.g., "DEL")
- `destination` (required): Destination airport code (e.g., "BOM")
- `time` (required): Departure time (ISO format: "2025-08-20T06:00:00")
- `noOfSeats` (required): Number of seats required
- `sortByPrice` (optional): Sort results by price
- `sortByHops` (optional): Sort results by number of hops
- `maxPrice` (optional): Maximum price filter
- `maxHops` (optional): Maximum hops filter
- `airline` (optional): Preferred airline filter
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)

### Response Format
```json
{
  "content": [
    {
      "uuid": "123e4567-e89b-12d3-a456-426614174000",
      "flightNumber": "AI101",
      "airline": "Air India",
      "departureTime": "2025-08-20T06:00:00",
      "arrivalTime": "2025-08-20T08:15:00",
      "price": 15000.0,
      "numberOfHops": 0,
      "schedules": [
        {
          "scheduleId": 1,
          "source": "DEL",
          "destination": "BOM",
          "departureTime": "2025-08-20T06:00:00",
          "arrivalTime": "2025-08-20T08:15:00",
          "availableSeats": 45
        }
      ]
    }
  ],
  "pageable": { ... },
  "totalElements": 100,
  "totalPages": 10
}
```

## Prerequisites

1. **Java 17** or higher
2. **Maven 3.8+**
3. **MySQL 8.0+**
4. **Elasticsearch 8.8+**
5. **Redis 6.0+**
6. **Neo4j 5.0+**

## Setup Instructions

### 1. Database Setup

#### MySQL Database
```sql
CREATE DATABASE flight_search_db;
```

#### Elasticsearch
Start Elasticsearch on port 9200:
```bash
# Using Docker
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" elasticsearch:8.8.2
```

#### Redis
Start Redis on port 6379:
```bash
# Using Docker
docker run -d --name redis -p 6379:6379 redis:6.2
```

#### Neo4j
Start Neo4j on port 7687:
```bash
# Using Docker
docker run -d --name neo4j -p 7474:7474 -p 7687:7687 -e NEO4J_AUTH=neo4j/password neo4j:5.0
```

### 2. Application Configuration

Update `src/main/resources/application.properties` with your database credentials:
```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/flight_search_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD

# Elasticsearch Configuration
spring.elasticsearch.uris=http://localhost:9200

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Neo4j Configuration
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=YOUR_NEO4J_PASSWORD
```

### 3. Build and Run

```bash
# Build the project
mvn clean compile

# Format code (recommended before committing)
mvn spotless:apply

# Check code formatting
mvn spotless:check

# Run the application
mvn spring-boot:run
```

The service will start on port 8081 with context path `/api/v1`.

## Code Quality & Formatting

This project uses **Spotless** for consistent code formatting across the entire codebase.

### Spotless Configuration
- **Google Java Format**: Enforces Google Java Style guidelines
- **Import Organization**: Automatically removes unused imports
- **Annotation Formatting**: Ensures consistent annotation styling
- **Automatic Execution**: Runs format check during compile phase

### Code Formatting Commands

**Apply formatting to all Java files:**
```bash
mvn spotless:apply
```

**Check if code follows formatting standards:**
```bash
mvn spotless:check
```

**Format specific files only:**
```bash
mvn spotless:apply -DspotlessFiles=src/main/java/com/example/flight/search/YourClass.java
```

### IDE Integration
For the best development experience, configure your IDE to use Google Java Format:

**IntelliJ IDEA:**
1. Install the "google-java-format" plugin
2. Enable it in Settings → google-java-format Settings
3. Set "Code Style" to use the plugin

**Eclipse:**
1. Download google-java-format Eclipse plugin
2. Install and enable in preferences

### Pre-commit Hooks (Recommended)
To ensure code formatting before commits, you can set up a pre-commit hook:

```bash
# Add to .git/hooks/pre-commit
#!/bin/sh
mvn spotless:check
if [ $? -ne 0 ]; then
  echo "Code formatting check failed. Run 'mvn spotless:apply' to fix."
  exit 1
fi
```

## Features

### 1. High-Performance Search
- Elasticsearch integration for sub-second search responses
- Optimized indexing for flight schedules and availability
- Real-time search with complex filtering

### 2. Multi-layer Caching
- Redis-based caching for frequent search queries
- Configurable cache TTL (10 minutes default)
- Cache invalidation on data updates

### 3. Graph-based Path Finding
- Neo4j for discovering multi-hop flight connections
- Shortest path algorithms for optimal routing
- Support for up to 3-hop connections

### 4. Rich Filtering & Sorting
- Price-based filtering and sorting
- Number of hops filtering
- Airline preference filtering
- Departure time filtering
- Available seats validation

### 5. Scalability Features
- Stateless microservice design
- Horizontal scaling support
- Database connection pooling
- Async data synchronization

## Key Components

### Entities
- `Flight`: Core flight information
- `Schedule`: Flight schedules with timing and availability
- `Hop`: Multi-segment flight connections
- `Seat`: Seat configuration and pricing

### Documents (Elasticsearch)
- `FlightSearchDocument`: Optimized search index
- `HopDocument`: Nested hop information

### Graph Models (Neo4j)
- `Airport`: Airport nodes with connections
- `FlightRoute`: Route relationships with metadata

### Services
- `FlightSearchService`: Main search orchestration
- `PathFindingService`: Graph-based route discovery
- `CacheService`: Redis caching operations
- `DataSyncService`: Data synchronization between systems

## Performance Optimizations

1. **Database Indexing**: Optimized indexes on frequently queried columns
2. **Elasticsearch Mapping**: Custom mappings for flight data
3. **Connection Pooling**: Configured for high throughput
4. **Caching Strategy**: Multi-level caching with Redis
5. **Async Processing**: Background data synchronization

## Monitoring & Health Checks

Access actuator endpoints:
- Health: `http://localhost:8081/api/v1/actuator/health`
- Metrics: `http://localhost:8081/api/v1/actuator/metrics`
- Prometheus: `http://localhost:8081/api/v1/actuator/prometheus`

## Testing

Run tests with proper formatting checks:
```bash
# Run all tests
mvn test

# Run tests with formatting check
mvn spotless:check test

# Apply formatting and run tests
mvn spotless:apply test
```

### Test Structure
- **Unit Tests**: Service layer and utility classes
- **Integration Tests**: Full Spring context with test containers
- **Minimal Context Tests**: Lightweight tests with minimal configuration

## Integration with Flight Booking Service

This service is designed to integrate seamlessly with the existing Flight Booking Service:

1. **Database Sync**: Regularly sync flight data from booking service
2. **Real-time Updates**: Update availability when bookings are made
3. **Consistent Data Model**: Compatible with existing database schema

## Future Enhancements

1. **Machine Learning**: Predictive pricing and recommendations
2. **Advanced Filtering**: Weather-based filtering, aircraft type preferences
3. **Real-time Updates**: WebSocket support for live availability updates
4. **Geographic Search**: Location-based airport suggestions
5. **Price Alerts**: Notification system for price changes
