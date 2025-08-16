#!/bin/bash

# Flight Search Service Setup Script
echo "🚀 Setting up Flight Search Service..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

echo "📦 Starting required services..."

# Stop and remove existing containers if they exist
echo "🧹 Cleaning up existing containers..."
docker stop elasticsearch-flight 2>/dev/null || true
docker rm elasticsearch-flight 2>/dev/null || true

# Note: Using existing services
echo "📄 Using existing MySQL database 'flight_booking'"
echo "🔴 Using existing Redis service (no Docker needed)"
echo "🕸️ Using existing Neo4j service at http://localhost:7474"

# Start Elasticsearch with correct version
echo "🔍 Starting Elasticsearch 8.13.4..."
docker run -d \
  --name elasticsearch-flight \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms1g -Xmx1g" \
  -p 9200:9200 \
  docker.elastic.co/elasticsearch/elasticsearch:8.13.4

echo "⏳ Waiting for Elasticsearch to start up..."
sleep 45

# Check service health
echo "🔍 Checking service health..."

# Elasticsearch Health Check
echo "Waiting for Elasticsearch..."
until curl -s "http://localhost:9200/_cluster/health" > /dev/null; do
    echo "Elasticsearch is unavailable - sleeping"
    sleep 5
done
echo "✅ Elasticsearch is ready!"

# Redis Health Check (local service)
echo "Checking Redis..."
if redis-cli ping > /dev/null 2>&1; then
    echo "✅ Redis is ready!"
else
    echo "⚠️ Redis might not be running locally. Please start Redis service."
fi

# Neo4j Health Check (local service)
echo "Checking Neo4j..."
if curl -s "http://localhost:7474" > /dev/null; then
    echo "✅ Neo4j is ready!"
else
    echo "⚠️ Neo4j might not be running. Please start Neo4j service."
fi

echo ""
echo "🎉 Setup completed!"
echo ""
echo "📊 Service URLs:"
echo "   MySQL: Using existing flight_booking database"
echo "   Redis: localhost:6379 (local service)"
echo "   Elasticsearch: http://localhost:9200 (Docker)"
echo "   Neo4j Browser: http://localhost:7474 (local service - neo4j/password)"
echo ""
echo "📋 Next steps:"
echo "   1. Run: mvn clean compile"
echo "   2. Run: mvn spring-boot:run"
echo "   3. Visit: http://localhost:8081/api/v1/swagger-ui.html"
echo "   4. Populate data: POST /api/v1/admin/populate-test-data"
echo ""
echo "🔧 Service Status:"
echo "   ✅ Elasticsearch: Docker container (8.13.4)"
echo "   ✅ Redis: Local service"
echo "   ✅ Neo4j: Local service"
echo "   ✅ MySQL: Existing flight_booking database"
