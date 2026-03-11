CREATE TABLE receivers (
    receiver_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT,
    gender VARCHAR(10),
    blood_group_needed VARCHAR(5) NOT NULL,
    phone VARCHAR(15),
    address VARCHAR(255),
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
