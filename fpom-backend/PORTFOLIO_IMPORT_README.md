# Portfolio Position Import Utility

This implementation provides a complete solution for importing and managing portfolio position data from the UBS Challenge Excel file with full CRUD operations and PostgreSQL database integration.

## What was created:

### 1. Entity Model
- **`PortfolioPosition.java`** - JPA entity with Lombok annotations, PostgreSQL optimizations, and proper indexes

### 2. Data Access Layer
- **`PortfolioPositionRepository.java`** - JPA repository with 30+ custom queries for portfolio analysis and account operations

### 3. Service Layer  
- **`PortfolioPositionImportService.java`** - Service for importing Excel data with batch processing and error handling

### 4. REST API Layer
- **`PortfolioPositionController.java`** - Comprehensive CRUD REST controller with 25+ endpoints

### 5. Database Integration
- **PostgreSQL Database** - Production-ready database with optimization scripts
- **Database Scripts** - Setup and performance optimization SQL scripts

### 6. Configuration
- **`FinancialPeaceOfMindApplication.java`** - Main Spring Boot application class
- **`application.properties`** - PostgreSQL configuration with development/test profiles

## Database Configuration

### PostgreSQL Connection Details
- **Host**: localhost
- **Port**: 85432
- **Database**: fpom
- **Username**: fpom
- **Password**: fpom

## Key Features:

✅ **Full CRUD Operations**: Create, Read, Update, Delete portfolio positions  
✅ **Excel Import**: Reads the `Swiss AI - UBS Challenge 3 - Portfolio Positions.xlsx` file from resources  
✅ **Batch Processing**: Imports data in batches of 1000 records for optimal performance  
✅ **PostgreSQL Integration**: Production-ready database with proper indexes and optimizations  
✅ **Account Management**: Complete account listing and detailed account operations  
✅ **Advanced Queries**: 30+ repository methods for complex portfolio analysis  
✅ **Error Recovery**: Continues import even if individual rows fail to parse  
✅ **REST API**: 25+ endpoints for complete portfolio management  
✅ **Performance Optimization**: Database views, indexes, and materialized views  

## Setup Instructions:

### 1. Database Setup
First, set up PostgreSQL database:

```bash
# See detailed instructions in DATABASE_SETUP_POSTGRESQL.md
sudo -u postgres psql
\i src/main/resources/db/setup_database.sql
```

### 2. Start the application:
```bash
mvn clean install
mvn spring-boot:run
```

### 3. Import the Excel data:
```bash
# Import data (keeps existing records)
POST http://localhost:8080/api/portfolio-positions/import

# Import data (clears existing records first)  
POST http://localhost:8080/api/portfolio-positions/import?clearExisting=true
```

## 🔧 Complete CRUD API

### Create Operations
```bash
# Create new position
POST http://localhost:8080/api/portfolio-positions
Content-Type: application/json

{
  "partnerIdFake": "ABC123",
  "accountIdFake": "ACC456", 
  "valueAmount": 50000.00,
  "valueCurrency": "CHF",
  "instrumentNameShort": "Test Bond",
  "assetClassDescriptionShort": "Bonds"
}
```

### Read Operations
```bash
# Get all positions (paginated)
GET http://localhost:8080/api/portfolio-positions?page=0&size=20&sort=valueAmount,desc

# Get position by ID
GET http://localhost:8080/api/portfolio-positions/123

# Get positions by partner
GET http://localhost:8080/api/portfolio-positions/partner/ABC123

# Get positions by account
GET http://localhost:8080/api/portfolio-positions/account/ACC456

# Search by instrument name
GET http://localhost:8080/api/portfolio-positions/search?instrumentName=master

# Get top positions
GET http://localhost:8080/api/portfolio-positions/top-positions?limit=10

# Get positions by asset class
GET http://localhost:8080/api/portfolio-positions/asset-class/Equities

# Get positions by currency
GET http://localhost:8080/api/portfolio-positions/currency/CHF

# Get positions above value threshold
GET http://localhost:8080/api/portfolio-positions/value-greater-than/100000
```

### Update Operations
```bash
# Full update (PUT)
PUT http://localhost:8080/api/portfolio-positions/123
Content-Type: application/json
{...full_position_object...}

# Partial update (PATCH)  
PATCH http://localhost:8080/api/portfolio-positions/123
Content-Type: application/json
{
  "valueAmount": 60000.00,
  "instrumentNameShort": "Updated Name"
}
```

### Delete Operations
```bash
# Delete single position
DELETE http://localhost:8080/api/portfolio-positions/123

# Batch delete multiple positions
DELETE http://localhost:8080/api/portfolio-positions/batch
Content-Type: application/json
[1, 2, 3, 4, 5]

# Clear all positions (use with caution!)
DELETE http://localhost:8080/api/portfolio-positions/clear-all
```

## 👥 Account Management API

### Account Operations
```bash
# Get all accounts with summary
GET http://localhost:8080/api/portfolio-positions/accounts

# Get simple list of account IDs
GET http://localhost:8080/api/portfolio-positions/accounts/list

# Get detailed account information
GET http://localhost:8080/api/portfolio-positions/accounts/ACC456/details
```

**Account Response Example:**
```json
[
  {
    "accountIdFake": "OnxRuqYGIu94OZB",
    "partnerIdFake": "OEM4B4lTFX", 
    "positionCount": 15,
    "totalValue": 750000.00,
    "currency": "CHF"
  }
]
```

## 📊 Analytics and Summary API

```bash
# Portfolio summary statistics
GET http://localhost:8080/api/portfolio-positions/summary

# Partner-specific portfolio summary
GET http://localhost:8080/api/portfolio-positions/summary/partner/ABC123

# Database statistics
GET http://localhost:8080/api/portfolio-positions/stats
```

## 🗄️ Repository Query Methods Available:

The `PortfolioPositionRepository` includes 30+ query methods:

**Basic Finders:**
- `findByPartnerIdFake()`, `findByAccountIdFake()`
- `findByAssetClassDescriptionShort()`, `findByValueCurrency()`
- `findByIsin()`, `findByInstrumentNameShortContainingIgnoreCase()`

**Account Queries:**
- `getAccountSummary()`, `findDistinctAccountIds()`
- `countDistinctAccountIds()`, `countByAccountIdFake()`

**Aggregation Queries:**
- `getTotalValueByAssetClass()`, `getTotalValueByCurrency()`
- `getPortfolioSummaryByPartner()`, `getAssetAllocationByPartner()`

**Advanced Queries:**
- `findPositionsByMultipleCriteria()`
- `findPositionsWithSignificantFxExposure()`
- `findLargePositions()`, `getMandateTypeDistribution()`

## 🏃‍♂️ Quick Start Examples:

### 1. Import Data and Get Summary
```bash
# Import Excel data
curl -X POST http://localhost:8080/api/portfolio-positions/import?clearExisting=true

# Get summary
curl http://localhost:8080/api/portfolio-positions/summary
```

### 2. Create and Manage Positions
```bash
# Create position
curl -X POST http://localhost:8080/api/portfolio-positions \
  -H "Content-Type: application/json" \
  -d '{"partnerIdFake":"TEST123","accountIdFake":"TESTACC","valueAmount":25000,"valueCurrency":"CHF"}'

# Update position
curl -X PATCH http://localhost:8080/api/portfolio-positions/1 \
  -H "Content-Type: application/json" \
  -d '{"valueAmount":30000}'
```

### 3. Account Analysis
```bash
# List all accounts
curl http://localhost:8080/api/portfolio-positions/accounts

# Get account details
curl http://localhost:8080/api/portfolio-positions/accounts/OnxRuqYGIu94OZB/details
```

## 🚀 Performance Features:

### Database Optimizations
- **PostgreSQL Integration** with proper indexes and constraints
- **Batch Processing** for large data imports (33,000+ rows)
- **Database Views** for quick analytical queries
- **Materialized Views** for pre-calculated aggregations
- **Custom Indexes** for common query patterns

### API Performance
- **Pagination** support for large result sets
- **Efficient Queries** using JPA Criteria API
- **Connection Pooling** with HikariCP
- **Batch Operations** for multiple record updates/deletes

## 📋 Import Process Details:

1. **File Location**: Excel file must be in `src/main/resources/`
2. **Column Mapping**: All 57 columns mapped to entity fields
3. **Date Handling**: Supports Excel date cells and ISO string formats
4. **Error Handling**: Skips problematic rows and logs details
5. **Performance**: Uses PostgreSQL batch inserts for large datasets
6. **Database**: Creates proper indexes and constraints automatically

## 🔍 Example Responses:

### Import Response:
```json
{
  "success": true,
  "message": "Import completed successfully", 
  "importedCount": 33634,
  "existingCountBefore": 0,
  "totalCountAfter": 33634,
  "clearedExisting": false
}
```

### Account Details Response:
```json
{
  "accountIdFake": "OnxRuqYGIu94OZB",
  "partnerIdFake": "OEM4B4lTFX",
  "positionCount": 15,
  "totalsByCurrency": {
    "CHF": 450000.00,
    "USD": 300000.00
  },
  "assetClassBreakdown": {
    "Equities": 8,
    "Bonds": 7
  }
}
```

## 📚 Documentation Files:

- **`DATABASE_SETUP_POSTGRESQL.md`** - Complete PostgreSQL setup guide
- **`PORTFOLIO_IMPORT_README.md`** - Original import utility documentation
- **SQL Scripts** in `src/main/resources/db/` for database setup and optimization

The solution is production-ready with comprehensive CRUD operations, PostgreSQL integration, account management, and advanced portfolio analysis capabilities!
