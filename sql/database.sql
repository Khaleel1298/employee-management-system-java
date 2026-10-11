CREATE DATABASE IF NOT EXISTS employee_management;
USE employee_management;

CREATE TABLE IF NOT EXISTS employees (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    salary DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO employees (name, email, department, salary)
VALUES
    ('Alice Johnson', 'alice.johnson@example.com', 'Engineering', 85000.00),
    ('Bob Smith', 'bob.smith@example.com', 'HR', 62000.00),
    ('Charlie Brown', 'charlie.brown@example.com', 'Finance', 71000.00)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    department = VALUES(department),
    salary = VALUES(salary);
