-- PostgreSQL Performance Optimization Script
-- Run this after the application has created the tables

-- Connect to fpom database
\c fpom;

-- Additional Performance Indexes (beyond what Hibernate creates)
-- These indexes are designed for common financial analysis queries

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_partner_asset_currency 
    ON portfolio_positions(partner_id_fake, asset_class_description_short, value_currency);

CREATE INDEX IF NOT EXISTS idx_account_valuation_date 
    ON portfolio_positions(account_id_fake, valuation_date DESC);

CREATE INDEX IF NOT EXISTS idx_instrument_search 
    ON portfolio_positions USING gin(to_tsvector('english', instrument_name_short));

-- Partial indexes for active positions (assuming non-null value amounts represent active positions)
CREATE INDEX IF NOT EXISTS idx_active_positions_value 
    ON portfolio_positions(value_amount DESC) 
    WHERE value_amount IS NOT NULL AND value_amount > 0;

-- Index for FX rate analysis
CREATE INDEX IF NOT EXISTS idx_fx_exposure 
    ON portfolio_positions(source_currency, value_currency, fx_rate) 
    WHERE source_currency != value_currency;

-- Index for date range queries
CREATE INDEX IF NOT EXISTS idx_valuation_date_range 
    ON portfolio_positions USING BRIN(valuation_date);

-- Statistics and vacuum settings for the main table
ALTER TABLE portfolio_positions SET (
    fillfactor = 90,  -- Leave some space for updates
    autovacuum_vacuum_scale_factor = 0.1,
    autovacuum_analyze_scale_factor = 0.05
);

-- Create a view for quick portfolio summaries
CREATE OR REPLACE VIEW portfolio_summary AS
SELECT 
    partner_id_fake,
    account_id_fake,
    value_currency,
    asset_class_description_short,
    COUNT(*) as position_count,
    SUM(value_amount) as total_value,
    AVG(value_amount) as avg_value,
    MIN(valuation_date) as first_valuation,
    MAX(valuation_date) as last_valuation
FROM portfolio_positions
WHERE value_amount IS NOT NULL
GROUP BY partner_id_fake, account_id_fake, value_currency, asset_class_description_short;

-- Grant permissions on the view
GRANT SELECT ON portfolio_summary TO fpom;

-- Create a function for currency conversion (example)
CREATE OR REPLACE FUNCTION convert_to_chf(amount DECIMAL, fx_rate DECIMAL)
RETURNS DECIMAL AS $$
BEGIN
    RETURN amount * COALESCE(fx_rate, 1.0);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Create a materialized view for performance-critical aggregations
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_partner_asset_allocation AS
SELECT 
    partner_id_fake,
    asset_class_description_short,
    value_currency,
    COUNT(*) as position_count,
    SUM(value_amount) as total_value,
    SUM(convert_to_chf(value_amount, fx_rate)) as total_value_chf,
    AVG(value_amount) as avg_position_value,
    MAX(valuation_date) as last_updated
FROM portfolio_positions
WHERE value_amount IS NOT NULL
GROUP BY partner_id_fake, asset_class_description_short, value_currency;

-- Create unique index on materialized view
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_partner_asset_unique 
    ON mv_partner_asset_allocation(partner_id_fake, asset_class_description_short, value_currency);

-- Grant permissions
GRANT SELECT ON mv_partner_asset_allocation TO fpom;

-- Create a function to refresh the materialized view
CREATE OR REPLACE FUNCTION refresh_portfolio_analytics()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_partner_asset_allocation;
END;
$$ LANGUAGE plpgsql;

-- Set up automatic statistics collection
SELECT pg_stat_reset();

-- Analyze the table to update statistics
-- (This will be run automatically by PostgreSQL, but can be run manually after large data loads)
-- ANALYZE portfolio_positions;

COMMENT ON VIEW portfolio_summary IS 'Quick portfolio summary view for dashboard queries';
COMMENT ON MATERIALIZED VIEW mv_partner_asset_allocation IS 'Pre-calculated asset allocation by partner for performance';
COMMENT ON FUNCTION refresh_portfolio_analytics() IS 'Refresh materialized views for portfolio analytics';
