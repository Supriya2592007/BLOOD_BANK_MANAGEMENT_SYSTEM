CREATE TABLE blood_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    requester_name VARCHAR(100),
    blood_group VARCHAR(5),
    units_required INT,
    requester_type VARCHAR(20),
    contact_number VARCHAR(15),
    status VARCHAR(20),
    request_date DATE
);
