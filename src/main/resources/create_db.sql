-- Ensure the UUID extension is enabled for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create the users table
CREATE TABLE users (
                       userid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       username VARCHAR(255) NOT NULL,
                       phonenumber VARCHAR(15) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       totp_secret VARCHAR(255) NOT NULL,
                       isactive BOOLEAN DEFAULT TRUE,
                       isverified BOOLEAN DEFAULT FALSE,
                       createdat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updatedat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for phone number search performance
CREATE INDEX idx_users_phonenumber ON users(phonenumber);

-- Insert sample data
INSERT INTO users (username, phonenumber, password_hash, role, totp_secret, isverified) VALUES
                                                                                ('Alice Smith', '7358772583', '$2a$12$KkR/e4v8D5.U1h7KxW.lOe.zW.Vp7QG5Nq5O7M2b7E8Z5Y8X5F9aK', 'USER', 'JBSWY3DPEHPK3PXP', TRUE),
                                                                                ('Bob Johnson', '+1987654321', '$2a$12$KkR/e4v8D5.U1h7KxW.lOe.zW.Vp7QG5Nq5O7M2b7E8Z5Y8X5F9aK', 'ADMIN', 'ORUGS4ZANFZSAYJA', TRUE);
-- 2. Create the Restaurants Table
CREATE TABLE restaurants (
    restaurantid UUID DEFAULT gen_random_uuid(),
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

    createdat TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updatedat TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_restaurants PRIMARY KEY (restaurantid),

    CONSTRAINT fk_restaurants_users FOREIGN KEY (userid)
        REFERENCES users (userid)
        ON DELETE RESTRICT,

    CONSTRAINT chk_restaurant_name_not_blank CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_restaurant_rating CHECK (rating >= 0.0 AND rating <= 5.0),
    CONSTRAINT chk_restaurant_review_count CHECK (review_count >= 0),
    CONSTRAINT chk_restaurant_latitude CHECK (lat >= -90.000000 AND lat <= 90.000000),
    CONSTRAINT chk_restaurant_longitude CHECK (lng >= -180.000000 AND lng <= 180.000000)
);

-- 3. Optimization Indexes for common operational search workflows
CREATE UNIQUE INDEX idx_restaurants_userid ON restaurants(userid);
CREATE INDEX idx_restaurants_geo ON restaurants(lat, lng);

-- 4. Create the Customers Table
CREATE TABLE customers (
    customerid UUID DEFAULT gen_random_uuid(),
    userid UUID NOT NULL,

    name VARCHAR(100) NOT NULL,
    address TEXT,
    dob TIMESTAMP WITHOUT TIME ZONE,
    gender VARCHAR(20),

    lat NUMERIC(9, 6) NOT NULL,
    lng NUMERIC(11, 6) NOT NULL,

    isactive BOOLEAN NOT NULL DEFAULT TRUE,
    isverified BOOLEAN NOT NULL DEFAULT FALSE,

    createdat TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updatedat TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_customers PRIMARY KEY (customerid),

    CONSTRAINT fk_customers_users FOREIGN KEY (userid)
        REFERENCES users (userid)
        ON DELETE RESTRICT,

    CONSTRAINT chk_customer_name_not_blank CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_customer_latitude CHECK (lat >= -90.000000 AND lat <= 90.000000),
    CONSTRAINT chk_customer_longitude CHECK (lng >= -180.000000 AND lng <= 180.000000)
);

-- 5. Optimization Indexes for Customers
CREATE UNIQUE INDEX idx_customers_userid ON customers(userid);
CREATE INDEX idx_customers_geo ON customers(lat, lng);

-- 6. Create the Riders Table
CREATE TABLE riders (
    riderid UUID DEFAULT gen_random_uuid(),
    userid UUID NOT NULL,

    name VARCHAR(100) NOT NULL,
    address TEXT,
    dob TIMESTAMP WITHOUT TIME ZONE,
    gender VARCHAR(20),

    vehiclenumber VARCHAR(50) NOT NULL,
    dlnumber VARCHAR(50) NOT NULL,

    lat NUMERIC(9, 6) NOT NULL,
    lng NUMERIC(11, 6) NOT NULL,

    isactive BOOLEAN NOT NULL DEFAULT TRUE,
    isverified BOOLEAN NOT NULL DEFAULT FALSE,

    createdat TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updatedat TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_riders PRIMARY KEY (riderid),

    CONSTRAINT fk_riders_users FOREIGN KEY (userid)
        REFERENCES users (userid)
        ON DELETE RESTRICT,

    CONSTRAINT chk_rider_name_not_blank CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_rider_latitude CHECK (lat >= -90.000000 AND lat <= 90.000000),
    CONSTRAINT chk_rider_longitude CHECK (lng >= -180.000000 AND lng <= 180.000000)
);

-- 7. Optimization Indexes for Riders
CREATE UNIQUE INDEX idx_riders_userid ON riders(userid);
CREATE INDEX idx_riders_geo ON riders(lat, lng);

select * from customers;
select * from users