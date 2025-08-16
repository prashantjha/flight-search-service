# 🚀 Flight Search Service - Updated Setup Guide

## Your Current Environment Setup

✅ **Redis**: Running locally (no Docker needed)  
✅ **Neo4j**: Running locally at http://localhost:7474 (neo4j/password)  
✅ **MySQL**: Existing flight_booking database  
🐳 **Elasticsearch**: Will run via Docker (8.13.4)  

## Quick Start (Updated for Your Environment)

### 1. Start Elasticsearch (Only Service Needed)
```bash
# The script will only start Elasticsearch, others are already running
chmod +x setup-services.sh
./setup-services.sh
```

This will:
- Start **Elasticsearch 8.13.4** in Docker container
- Verify your existing **Redis** service 
- Verify your existing **Neo4j** service
- Use your existing **MySQL** flight_booking database

### 2. Build and Start the Application
```bash
# Build the project
mvn clean compile

# Start the application
mvn spring-boot:run
```

### 3. Populate All Search Systems
```bash
# This will populate both Elasticsearch and Neo4j with flight data
curl -X POST http://localhost:8081/api/v1/admin/populate-test-data
```

## 📊 Service Architecture Now

- **MySQL** (flight_booking): Source data storage
- **Elasticsearch 8.13.4**: High-performance flight search indexing  
- **Redis** (local): Multi-layer caching for sub-second responses
- **Neo4j** (local): Graph-based connecting flight discovery

## 🧪 Testing High-Performance Search

### Direct Flights (Elasticsearch):
```bash
curl "http://localhost:8081/api/v1/search?source=DEL&destination=BOM&time=2025-08-20T06:00:00&noOfSeats=2"
```

### Connecting Flights (Neo4j Graph):
```bash
curl "http://localhost:8081/api/v1/search?source=DEL&destination=CCU&time=2025-08-20T06:00:00&noOfSeats=2"
```

### With Caching (Redis):
```bash
# Second call will be cached and much faster
curl "http://localhost:8081/api/v1/search?source=DEL&destination=BOM&time=2025-08-20T06:00:00&noOfSeats=2"
```

## Performance Expectations

- **Elasticsearch Direct Search**: 50-200ms
- **Neo4j Connecting Flights**: 200-500ms  
- **Redis Cached Results**: 10-50ms
- **No Database Fallback**: MySQL only used for data sync

## Service URLs

- **Application**: http://localhost:8081/api/v1
- **Swagger UI**: http://localhost:8081/api/v1/swagger-ui.html
- **Neo4j Browser**: http://localhost:7474 (neo4j/password)
- **Elasticsearch**: http://localhost:9200
- **Redis**: localhost:6379

## Monitoring & Health

- **Health Check**: http://localhost:8081/api/v1/admin/health
- **Metrics**: http://localhost:8081/api/v1/actuator/metrics
- **Elasticsearch Health**: http://localhost:9200/_cluster/health

Your setup is now optimized for your existing local services while using the correct Elasticsearch version!
