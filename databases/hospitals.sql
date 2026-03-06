CREATE TABLE hospitals (
    hospital_id INT AUTO_INCREMENT PRIMARY KEY,
    hospital_name VARCHAR(100),
    location VARCHAR(100),
    contact_number VARCHAR(15),
    username VARCHAR(30) UNIQUE,
    password VARCHAR(30)
);
