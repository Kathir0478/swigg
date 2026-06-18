-- Ensure the UUID extension is enabled for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE users (
    userid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) NOT NULL,
    phonenumber VARCHAR(15) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER', 'CUSTOMER', 'RESTAURANT', 'RIDER')),
    totp_secret VARCHAR(255) NOT NULL,
    isactive BOOLEAN NOT NULL DEFAULT TRUE,
    isverified BOOLEAN NOT NULL DEFAULT FALSE,
    createdat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for users table
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_phonenumber ON users(phonenumber);
CREATE INDEX idx_users_isactive_isverified ON users(isactive, isverified);

-- ============================================
-- CUSTOMERS TABLE
-- ============================================
CREATE TABLE customers (
    customerid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    userid UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    dob TIMESTAMP NOT NULL,
    gender VARCHAR(20) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'UNDEFINED')),
    lat NUMERIC(9, 6) NOT NULL,
    lng NUMERIC(11, 6) NOT NULL,
    isactive BOOLEAN NOT NULL DEFAULT TRUE,
    isverified BOOLEAN NOT NULL DEFAULT FALSE,
    createdat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customers_users FOREIGN KEY (userid)
        REFERENCES users (userid)
        ON DELETE RESTRICT,
    CONSTRAINT chk_customer_name_not_blank CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_customer_latitude CHECK (lat >= -90.000000 AND lat <= 90.000000),
    CONSTRAINT chk_customer_longitude CHECK (lng >= -180.000000 AND lng <= 180.000000)
);

-- Indexes for customers table
CREATE INDEX idx_customers_userid ON customers(userid);
CREATE INDEX idx_customers_isactive_isverified ON customers(isactive, isverified);
CREATE INDEX idx_customers_geo ON customers(lat, lng);

-- ============================================
-- RESTAURANTS TABLE
-- ============================================
CREATE TABLE restaurants (
    restaurantid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    userid UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    opentime TIME,
    closetime TIME,
    imageurl VARCHAR(255),
    address TEXT,
    rating NUMERIC(2, 1) NOT NULL DEFAULT 0.0,
    review_count INT NOT NULL DEFAULT 0,
    lat NUMERIC(9, 6) NOT NULL,
    lng NUMERIC(11, 6) NOT NULL,
    isactive BOOLEAN NOT NULL DEFAULT TRUE,
    isverified BOOLEAN NOT NULL DEFAULT FALSE,
    createdat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_restaurants_users FOREIGN KEY (userid)
        REFERENCES users (userid)
        ON DELETE RESTRICT,
    CONSTRAINT chk_restaurant_name_not_blank CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_restaurant_rating CHECK (rating >= 0.0 AND rating <= 5.0),
    CONSTRAINT chk_restaurant_review_count CHECK (review_count >= 0),
    CONSTRAINT chk_restaurant_latitude CHECK (lat >= -90.000000 AND lat <= 90.000000),
    CONSTRAINT chk_restaurant_longitude CHECK (lng >= -180.000000 AND lng <= 180.000000)
);

-- Indexes for restaurants table
CREATE INDEX idx_restaurants_userid ON restaurants(userid);
CREATE INDEX idx_restaurants_isactive_isverified ON restaurants(isactive, isverified);
CREATE INDEX idx_restaurants_geo ON restaurants(lat, lng);

-- ============================================
-- RIDERS TABLE
-- ============================================
CREATE TABLE riders (
    riderid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    userid UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    dob TIMESTAMP NOT NULL,
    gender VARCHAR(20) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'UNDEFINED')),
    lat NUMERIC(9, 6) NOT NULL,
    lng NUMERIC(11, 6) NOT NULL,
    vehiclenumber VARCHAR(50) NOT NULL,
    dlnumber VARCHAR(50) NOT NULL,
    isactive BOOLEAN NOT NULL DEFAULT TRUE,
    isverified BOOLEAN NOT NULL DEFAULT FALSE,
    createdat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_riders_users FOREIGN KEY (userid)
        REFERENCES users (userid)
        ON DELETE RESTRICT,
    CONSTRAINT chk_rider_name_not_blank CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_rider_latitude CHECK (lat >= -90.000000 AND lat <= 90.000000),
    CONSTRAINT chk_rider_longitude CHECK (lng >= -180.000000 AND lng <= 180.000000)
);

-- Indexes for riders table
CREATE INDEX idx_riders_userid ON riders(userid);
CREATE INDEX idx_riders_isactive_isverified ON riders(isactive, isverified);
CREATE INDEX idx_riders_geo ON riders(lat, lng);

-- ============================================
-- FOODS TABLE
-- ============================================
CREATE TABLE foods (
    foodid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    rating NUMERIC(2, 1) NOT NULL DEFAULT 0.0,
    reviewcount INT NOT NULL DEFAULT 0,
    category VARCHAR(20) NOT NULL CHECK (category IN ('VEG', 'NON_VEG', 'STARTER', 'BEVERAGE', 'DESSERT', 'SALAD', 'SOUP', 'MAIN_COURSE', 'SIDE_DISH', 'APPETIZER')),
    restaurantid UUID NOT NULL,
    isavailable BOOLEAN NOT NULL DEFAULT TRUE,
    isactive BOOLEAN NOT NULL DEFAULT TRUE,
    createdat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_foods_restaurants FOREIGN KEY (restaurantid)
        REFERENCES restaurants (restaurantid)
        ON DELETE RESTRICT,
    CONSTRAINT chk_food_name_not_blank CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_food_price CHECK (price > 0.0),
    CONSTRAINT chk_food_rating CHECK (rating >= 0.0 AND rating <= 5.0),
    CONSTRAINT chk_food_reviewcount CHECK (reviewcount >= 0)
);

-- Indexes for foods table
CREATE INDEX idx_foods_restaurantid ON foods(restaurantid);
CREATE INDEX idx_foods_category ON foods(category);
CREATE INDEX idx_foods_isactive ON foods(isactive);
CREATE INDEX idx_foods_isavailable ON foods(isavailable);
CREATE INDEX idx_foods_restaurantid_isactive ON foods(restaurantid, isactive);
CREATE INDEX idx_foods_restaurantid_isavailable ON foods(restaurantid, isavailable);

-- ============================================
-- CARTS TABLE
-- ============================================
CREATE TABLE carts (
    cartid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customerid UUID NOT NULL,
    restaurantid UUID NOT NULL,
    totalprice NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'ABANDONED', 'ORDERED')),
    isactive BOOLEAN NOT NULL,
    createdat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carts_customers FOREIGN KEY (customerid)
        REFERENCES customers (customerid)
        ON DELETE RESTRICT,
    CONSTRAINT fk_carts_restaurants FOREIGN KEY (restaurantid)
        REFERENCES restaurants (restaurantid)
        ON DELETE RESTRICT
);

-- Indexes for carts table
CREATE INDEX idx_carts_customerid ON carts(customerid);
CREATE INDEX idx_carts_restaurantid ON carts(restaurantid);
CREATE INDEX idx_carts_isactive ON carts(isactive);
CREATE INDEX idx_carts_status ON carts(status);
CREATE INDEX idx_carts_customerid_isactive ON carts(customerid, isactive);

-- ============================================
-- CART_ITEMS TABLE (for @ElementCollection)
-- ============================================
CREATE TABLE cart_items (
    cartid UUID NOT NULL,
    foodid UUID NOT NULL,
    PRIMARY KEY (cartid, foodid),
    CONSTRAINT fk_cart_items_carts FOREIGN KEY (cartid)
        REFERENCES carts (cartid)
        ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_foods FOREIGN KEY (foodid)
        REFERENCES foods (foodid)
        ON DELETE CASCADE
);

-- ============================================
-- ORDERS TABLE
-- ============================================
CREATE TABLE orders (
    orderid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customerid UUID NOT NULL,
    riderid UUID,
    restaurantid UUID NOT NULL,
    cartid UUID NOT NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED')),
    instruction TEXT,
    isactive BOOLEAN NOT NULL,
    createdat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customers FOREIGN KEY (customerid)
        REFERENCES customers (customerid)
        ON DELETE RESTRICT,
    CONSTRAINT fk_orders_riders FOREIGN KEY (riderid)
        REFERENCES riders (riderid)
        ON DELETE SET NULL,
    CONSTRAINT fk_orders_restaurants FOREIGN KEY (restaurantid)
        REFERENCES restaurants (restaurantid)
        ON DELETE RESTRICT,
    CONSTRAINT fk_orders_carts FOREIGN KEY (cartid)
        REFERENCES carts (cartid)
        ON DELETE RESTRICT
);

-- Indexes for orders table
CREATE INDEX idx_orders_customerid ON orders(customerid);
CREATE INDEX idx_orders_riderid ON orders(riderid);
CREATE INDEX idx_orders_restaurantid ON orders(restaurantid);
CREATE INDEX idx_orders_cartid ON orders(cartid);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_isactive ON orders(isactive);
CREATE INDEX idx_orders_customerid_isactive ON orders(customerid, isactive);
CREATE INDEX idx_orders_riderid_isactive ON orders(riderid, isactive);
CREATE INDEX idx_orders_restaurantid_isactive ON orders(restaurantid, isactive);

-- ============================================
-- SAMPLE DATA POPULATION
-- ============================================

-- Insert sample users
INSERT INTO users (username, phonenumber, password_hash, role, totp_secret, isverified) VALUES
('Admin User', '9876543210', '$2a$12$KkR/e4v8D5.U1h7KxW.lOe.zW.Vp7QG5Nq5O7M2b7E8Z5Y8X5F9aK', 'ADMIN', 'JBSWY3DPEHPK3PXP', TRUE),
('John Customer', '9123456780', '$2a$12$KkR/e4v8D5.U1h7KxW.lOe.zW.Vp7QG5Nq5O7M2b7E8Z5Y8X5F9aK', 'CUSTOMER', 'ORUGS4ZANFZSAYJA', TRUE),
('Pizza Palace', '9234567890', '$2a$12$KkR/e4v8D5.U1h7KxW.lOe.zW.Vp7QG5Nq5O7M2b7E8Z5Y8X5F9aK', 'RESTAURANT', 'KRSXG5DSO5XE2TKE', TRUE),
('Mike Rider', '9345678901', '$2a$12$KkR/e4v8D5.U1h7KxW.lOe.zW.Vp7QG5Nq5O7M2b7E8Z5Y8X5F9aK', 'RIDER', 'LZQF6K5M5XWG3L4Q', TRUE);

-- Insert sample customers
INSERT INTO customers (userid, name, address, dob, gender, lat, lng) VALUES
((SELECT userid FROM users WHERE username = 'John Customer'), 'John Doe', '123 Main St, Chennai', '1990-05-15 00:00:00', 'MALE', 13.082680, 80.270721);

-- Insert sample restaurants
INSERT INTO restaurants (userid, name, description, opentime, closetime, imageurl, address, lat, lng) VALUES
((SELECT userid FROM users WHERE username = 'Pizza Palace'), 'Pizza Palace', 'Authentic Italian pizza', '10:00:00', '23:00:00', 'https://example.com/pizza.jpg', '456 Pizza Ave, Chennai', 13.084620, 80.275620);

-- Insert sample riders
INSERT INTO riders (userid, name, address, dob, gender, lat, lng, vehiclenumber, dlnumber) VALUES
((SELECT userid FROM users WHERE username = 'Mike Rider'), 'Mike Johnson', '789 Rider Rd, Chennai', '1988-08-20 00:00:00', 'MALE', 13.090000, 80.280000, 'TN123456', 'DL98765432');

-- Insert sample foods
INSERT INTO foods (name, description, price, category, restaurantid) VALUES
('Margherita Pizza', 'Classic tomato and cheese pizza', 299.99, 'VEG', (SELECT restaurantid FROM restaurants WHERE name = 'Pizza Palace')),
('Pepperoni Pizza', 'Spicy pepperoni pizza', 349.99, 'NON_VEG', (SELECT restaurantid FROM restaurants WHERE name = 'Pizza Palace')),
('Garlic Bread', 'Crispy garlic bread', 99.99, 'STARTER', (SELECT restaurantid FROM restaurants WHERE name = 'Pizza Palace')),
('Coca Cola', 'Cold beverage', 49.99, 'BEVERAGE', (SELECT restaurantid FROM restaurants WHERE name = 'Pizza Palace')),
('Chocolate Cake', 'Rich chocolate dessert', 149.99, 'DESSERT', (SELECT restaurantid FROM restaurants WHERE name = 'Pizza Palace'));

-- Insert sample cart
INSERT INTO carts (customerid, restaurantid, totalprice, status, isactive) VALUES
((SELECT customerid FROM customers WHERE name = 'John Doe'), (SELECT restaurantid FROM restaurants WHERE name = 'Pizza Palace'), 449.98, 'ACTIVE', TRUE);

-- Insert sample cart items
INSERT INTO cart_items (cartid, foodid) VALUES
((SELECT cartid FROM carts WHERE customerid = (SELECT customerid FROM customers WHERE name = 'John Doe')), (SELECT foodid FROM foods WHERE name = 'Margherita Pizza')),
((SELECT cartid FROM carts WHERE customerid = (SELECT customerid FROM customers WHERE name = 'John Doe')), (SELECT foodid FROM foods WHERE name = 'Garlic Bread'));

-- Insert sample order
INSERT INTO orders (customerid, restaurantid, cartid, status, isactive) VALUES
((SELECT customerid FROM customers WHERE name = 'John Doe'), (SELECT restaurantid FROM restaurants WHERE name = 'Pizza Palace'), (SELECT cartid FROM carts WHERE customerid = (SELECT customerid FROM customers WHERE name = 'John Doe')), 'PENDING', TRUE);

