# Flight Search Service - Troubleshooting Guide

## Common Startup Issues & Solutions

### Issue 1: Database Connection Problems
**Error**: `Cannot create PoolableConnectionFactory` or `Access denied for user`

**Solutions**:
1. **Check your MySQL credentials**:
   ```sql
   # Connect to MySQL and verify
   mysql -u root -p
   SHOW DATABASES;
   USE flight_booking;
   SHOW TABLES;
   ```

2. **Update application.properties with correct credentials**:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/flight_booking?useSSL=false&serverTimezone=UTC
   spring.datasource.username=YOUR_ACTUAL_USERNAME
   spring.datasource.password=YOUR_ACTUAL_PASSWORD
   ```

### Issue 2: External Service Dependencies (Elasticsearch/Redis)
**Error**: `Connection refused` for Elasticsearch or Redis

**Solution**: Start with minimal config:
```bash
# Use minimal configuration
mvn spring-boot:run -Dspring.profiles.active=minimal
```

### Issue 3: JPA/Hibernate Schema Issues
**Error**: `Table 'flight_booking.flights' doesn't exist`

**Solution**: Ensure your database has the required tables:
```sql
USE flight_booking;
SHOW TABLES;
# Should show: flights, schedules, hops, seats, bookings, etc.
```

### Issue 4: Port Already in Use
**Error**: `Port 8081 already in use`

**Solution**: 
```bash
# Kill process using port 8081
sudo lsof -ti:8081 | xargs kill -9
# Or use different port
mvn spring-boot:run -Dserver.port=8082
```

## Step-by-Step Startup Process

### Step 1: Test Minimal Configuration
```bash
# Copy the minimal config
cp src/main/resources/application-minimal.properties src/main/resources/application.properties

# Try to start
mvn spring-boot:run
```

### Step 2: Verify Database Connection
```bash
# Test MySQL connection
mysql -u root -p -e "USE flight_booking; SHOW TABLES;"
```

### Step 3: Check for Compilation Errors
```bash
# Clean and compile
mvn clean compile
```

### Step 4: Start with Debug Logging
```bash
# Start with debug output
mvn spring-boot:run -Ddebug=true
```

## Quick Fixes

### Fix 1: Use In-Memory Database for Testing
If MySQL issues persist, temporarily use H2:
```properties
# Add to application.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

### Fix 2: Disable Problematic Auto-Configurations
```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
```

### Fix 3: Basic Health Check Endpoint Only
Temporarily comment out complex dependencies in controllers and services.

## What to Check First

1. ✅ **Java Version**: Ensure Java 17+ is installed
   ```bash
   java -version
   ```

2. ✅ **MySQL Running**: Check MySQL service is running
   ```bash
   brew services list | grep mysql
   # or
   sudo systemctl status mysql
   ```

3. ✅ **Database Exists**: Verify flight_booking database exists
   ```sql
   SHOW DATABASES LIKE 'flight_booking';
   ```

4. ✅ **Correct Credentials**: Test database login
   ```bash
   mysql -u root -p flight_booking
   ```

## Still Having Issues?

If the application still won't start:

1. **Share the exact error message** from the console
2. **Check the application logs** in `target/` directory
3. **Try the minimal configuration approach**
4. **Verify all prerequisites are installed and running**

## Contact Points for Help

- Check logs in: `target/spring-boot-run.log`
- Look for stack traces starting with `Caused by:`
- Focus on the root cause (usually the last "Caused by" in the chain)
