CREATE TABLE schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flight_id BIGINT NOT NULL,
    source VARCHAR(10) NOT NULL,
    destination VARCHAR(10) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    available_seats INT NOT NULL,
    base_fare DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_flight FOREIGN KEY (flight_id) REFERENCES flights (id)
);

