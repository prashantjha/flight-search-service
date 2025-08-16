import javax.sql.DataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@TestConfiguration
public class TestDatabaseConfig {

  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUrl("jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    return dataSource;
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(
        "CREATE TABLE schedules ("
            + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
            + "flight_id BIGINT NOT NULL,"
            + "source VARCHAR(10) NOT NULL,"
            + "destination VARCHAR(10) NOT NULL,"
            + "departure_time TIMESTAMP NOT NULL,"
            + "arrival_time TIMESTAMP NOT NULL,"
            + "available_seats INT NOT NULL,"
            + "base_fare DECIMAL(10,2) NOT NULL,"
            + "CONSTRAINT fk_flight FOREIGN KEY (flight_id) REFERENCES flights (id))");
    return jdbcTemplate;
  }
}
