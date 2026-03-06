CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    age INT,
    gender VARCHAR(10),
    blood_group VARCHAR(5),
    phone VARCHAR(15),
    address VARCHAR(100),
    username VARCHAR(30) UNIQUE,
    password VARCHAR(30)
);
