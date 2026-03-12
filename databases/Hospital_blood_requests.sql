CREATE TABLE Hospital_blood_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    hospital_id INT NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    units INT NOT NULL,
    status VARCHAR(20) DEFAULT 'Pending',
    request_date DATE NOT NULL,
    -- Links request to the specific hospital
    CONSTRAINT fk_hospital 
        FOREIGN KEY (hospital_id) 
        REFERENCES hospitals(hospital_id) 
        ON DELETE CASCADE
);
