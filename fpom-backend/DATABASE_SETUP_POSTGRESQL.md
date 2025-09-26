# PostgreSQL Database Setup Guide

This guide explains how to set up and configure PostgreSQL for the Financial Peace of Mind application.

## Database Configuration

### Connection Details
- **Host**: localhost
- **Port**: 85432 (custom port)
- **Database**: fpom
- **Username**: fpom
- **Password**: fpom

## Prerequisites

1. **Install PostgreSQL** (version 12 or higher recommended)
   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install postgresql postgresql-contrib
   
   # macOS (using Homebrew)
   brew install postgresql
   
   # Windows
   # Download from https://www.postgresql.org/download/windows/
   ```

2. **Start PostgreSQL Service**
   ```bash
   # Ubuntu/Debian
   sudo systemctl start postgresql
   sudo systemctl enable postgresql
   
   # macOS
   brew services start postgresql
   
   # Windows
   # Use pgAdmin or Windows Services
   ```

## Database Setup

### Method 1: Automated Setup (Recommended)

Run the provided SQL setup script as the PostgreSQL superuser:

```bash
# Connect as postgres superuser
sudo -u postgres psql

# Run the setup script
\i /path/to/your/project/src/main/resources/db/setup_database.sql
```

### Method 2: Manual Setup

1. **Create Database and User**:
   ```sql
   -- Connect as postgres superuser
   sudo -u postgres psql
   
   -- Create user
   CREATE USER fpom WITH PASSWORD 'fpom';
   
   -- Create database
   CREATE DATABASE fpom OWNER fpom;
   
   -- Grant privileges
   GRANT ALL PRIVILEGES ON DATABASE fpom TO fpom;
   ```

2. **Configure PostgreSQL Port** (if using port 85432):
   
   Edit PostgreSQL configuration file:
   ```bash
   # Find the config file location
   sudo -u postgres psql -c "SHOW config_file;"
   
   # Edit postgresql.conf
   sudo nano /etc/postgresql/14/main/postgresql.conf
   ```
   
   Change the port setting:
   ```
   port = 85432
   ```
   
   Restart PostgreSQL:
   ```bash
   sudo systemctl restart postgresql
   ```

3. **Test Connection**:
   ```bash
   psql -h localhost -p 85432 -U fpom -d fpom
   ```

## Application Configuration

The application is configured to use PostgreSQL via `application.properties`:

```properties
# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:85432/fpom
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=fpom
spring.datasource.password=fpom
```

## Performance Optimization

### Run Performance Scripts

After the application creates the tables, run the performance optimization script:

```bash
psql -h localhost -p 85432 -U fpom -d fpom -f src/main/resources/db/performance_optimization.sql
```

This script creates:
- Additional performance indexes
- Database views for quick queries
- Materialized views for analytics
- PostgreSQL-specific optimizations

### Key Performance Features

1. **Indexes**: Optimized for common portfolio queries
   - Partner/account lookups
   - Asset class analysis
   - Currency filtering
   - Date range queries

2. **Views**:
   - `portfolio_summary`: Quick aggregated data
   - `mv_partner_asset_allocation`: Pre-calculated asset allocation

3. **Functions**:
   - `convert_to_chf()`: Currency conversion
   - `refresh_portfolio_analytics()`: Update materialized views

## Monitoring and Maintenance

### Check Database Status
```sql
-- Connection info
SELECT * FROM pg_stat_database WHERE datname = 'fpom';

-- Table sizes
SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(tablename::text)) 
FROM pg_tables WHERE schemaname = 'public';

-- Index usage
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch 
FROM pg_stat_user_indexes;
```

### Maintenance Tasks
```sql
-- Update table statistics
ANALYZE portfolio_positions;

-- Refresh materialized view
SELECT refresh_portfolio_analytics();

-- Check for unused indexes
SELECT schemaname, tablename, indexname, idx_scan 
FROM pg_stat_user_indexes 
WHERE idx_scan = 0 AND schemaname = 'public';
```

## Backup and Recovery

### Backup Database
```bash
# Full database backup
pg_dump -h localhost -p 85432 -U fpom fpom > fpom_backup.sql

# Data only backup
pg_dump -h localhost -p 85432 -U fpom --data-only fpom > fpom_data.sql

# Custom format (recommended for large databases)
pg_dump -h localhost -p 85432 -U fpom -Fc fpom > fpom_backup.dump
```

### Restore Database
```bash
# From SQL file
psql -h localhost -p 85432 -U fpom fpom < fpom_backup.sql

# From custom format
pg_restore -h localhost -p 85432 -U fpom -d fpom fpom_backup.dump
```

## Troubleshooting

### Common Issues

1. **Connection Refused**:
   - Check if PostgreSQL is running: `sudo systemctl status postgresql`
   - Verify port configuration in postgresql.conf
   - Check firewall settings

2. **Authentication Failed**:
   - Verify user credentials
   - Check pg_hba.conf for authentication method
   - Ensure user has proper permissions

3. **Port Already in Use**:
   ```bash
   # Check what's using the port
   sudo netstat -tlnp | grep :85432
   
   # Change to different port in postgresql.conf
   ```

4. **Performance Issues**:
   - Run ANALYZE to update statistics
   - Check slow queries: `SELECT * FROM pg_stat_statements ORDER BY total_time DESC;`
   - Monitor with: `SELECT * FROM pg_stat_activity;`

### Useful Commands

```bash
# Start/stop PostgreSQL
sudo systemctl start postgresql
sudo systemctl stop postgresql

# Connect to database
psql -h localhost -p 85432 -U fpom -d fpom

# List databases
psql -h localhost -p 85432 -U fpom -l

# Check PostgreSQL version
psql -h localhost -p 85432 -U fpom -c "SELECT version();"
```

## Security Recommendations

1. **Change Default Passwords**: Use strong passwords in production
2. **Restrict Connections**: Configure pg_hba.conf appropriately
3. **Use SSL**: Enable SSL connections for production
4. **Regular Updates**: Keep PostgreSQL updated
5. **Backup Encryption**: Encrypt backups for sensitive financial data

## Development vs Production

### Development (current setup)
- Local PostgreSQL instance
- Simple authentication
- Full logging enabled
- Development-friendly settings

### Production Recommendations
- Separate database server
- SSL/TLS encryption
- Connection pooling
- Monitoring and alerting
- Regular backup strategy
- Read replicas for analytics
