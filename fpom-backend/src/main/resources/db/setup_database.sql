-- PostgreSQL Database Setup Script for Financial Peace of Mind
-- Run this as a PostgreSQL superuser (postgres)

-- Create database
CREATE DATABASE fpom
    WITH 
    OWNER = fpom
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    TEMPLATE template0;

-- Create user if not exists
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'fpom') THEN
        CREATE USER fpom WITH PASSWORD 'fpom';
    END IF;
END
$$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE fpom TO fpom;
GRANT CREATE ON SCHEMA public TO fpom;
GRANT ALL ON SCHEMA public TO fpom;

-- Connect to the fpom database and set up additional permissions
\c fpom;

-- Grant permissions on future tables and sequences
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO fpom;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO fpom;

-- Create extensions that might be useful for financial data
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Set up sequence for portfolio positions (will be created automatically by Hibernate)
-- This is just for reference - Hibernate will manage this
-- CREATE SEQUENCE IF NOT EXISTS portfolio_position_id_seq
--     INCREMENT 1
--     START 1
--     MINVALUE 1
--     MAXVALUE 9223372036854775807
--     CACHE 1
--     OWNED BY portfolio_positions.id;

COMMENT ON DATABASE fpom IS 'Financial Peace of Mind - UBS Swiss AI Weeks Hackathon Database';
