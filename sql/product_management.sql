-- ============================================================
--  Product Management System - Database Script
--  Run this file once in MySQL (Workbench / command line):
--      mysql -u root -p < product_management.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS product_management;
USE product_management;

-- ------------------------------------------------------------
-- Table 1: users
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    uid      INT          PRIMARY KEY AUTO_INCREMENT,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(150) NOT NULL UNIQUE,
    mobile   VARCHAR(15)  NOT NULL,
    password VARCHAR(255) NOT NULL      -- stores a salted PBKDF2 hash, never plain text
);

-- ------------------------------------------------------------
-- Table 2: products
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    pid    INT           PRIMARY KEY AUTO_INCREMENT,
    pname  VARCHAR(100)  NOT NULL,
    pprice DECIMAL(10,2) NOT NULL,
    pqty   INT           NOT NULL
);

-- ------------------------------------------------------------
-- Optional sample data (the dashboard example row from the assignment)
-- ------------------------------------------------------------
INSERT INTO products (pname, pprice, pqty) VALUES ('Mobile', 15000.00, 50);
SELECT * FROM products;
