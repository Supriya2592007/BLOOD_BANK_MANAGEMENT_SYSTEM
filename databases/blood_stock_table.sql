CREATE TABLE blood_stock (
    blood_group VARCHAR(5) PRIMARY KEY,
    units_available INT
);

INSERT INTO blood_stock VALUES
('A+',1000),
('A-',1000),
('B+',1000),
('B-',1000),
('AB+',1000),
('AB-',1000),
('O+',1000),
('O-',1000);
