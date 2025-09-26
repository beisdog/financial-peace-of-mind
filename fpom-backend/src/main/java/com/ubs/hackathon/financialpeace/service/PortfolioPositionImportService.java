package com.ubs.hackathon.financialpeace.service;

import com.ubs.hackathon.financialpeace.model.PortfolioPosition;
import com.ubs.hackathon.financialpeace.repository.PortfolioPositionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for importing Portfolio Position data from Excel files.
 * Handles the parsing and batch insertion of portfolio position data from the UBS Challenge Excel file.
 */
@Service
public class PortfolioPositionImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(PortfolioPositionImportService.class);
    
    private static final String EXCEL_FILE_PATH = "Swiss AI - UBS Challenge 3 - Portfolio Positions.xlsx";
    private static final int BATCH_SIZE = 1000;
    
    @Autowired
    private PortfolioPositionRepository portfolioPositionRepository;
    
    /**
     * Import portfolio positions from the Excel file in resources.
     * 
     * @return number of positions imported
     * @throws IOException if file reading fails
     */
    @Transactional
    public int importPortfolioPositions() throws IOException {
        logger.info("Starting import of portfolio positions from Excel file: {}", EXCEL_FILE_PATH);
        
        ClassPathResource resource = new ClassPathResource(EXCEL_FILE_PATH);
        
        if (!resource.exists()) {
            throw new IOException("Excel file not found in resources: " + EXCEL_FILE_PATH);
        }
        
        try (InputStream inputStream = resource.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0); // First sheet: "Portfolio Positions"
            
            if (sheet == null) {
                throw new IOException("No sheets found in the Excel file");
            }
            
            logger.info("Found sheet: {} with {} rows", sheet.getSheetName(), sheet.getLastRowNum());
            
            List<PortfolioPosition> positions = new ArrayList<>();
            int importedCount = 0;
            int skippedCount = 0;
            
            // Skip header row (row 0) and process data rows
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                
                if (row == null || isEmptyRow(row)) {
                    skippedCount++;
                    continue;
                }
                
                try {
                    PortfolioPosition position = parseRowToPortfolioPosition(row);
                    if (position != null) {
                        positions.add(position);
                        
                        // Batch insert for performance
                        if (positions.size() >= BATCH_SIZE) {
                            portfolioPositionRepository.saveAll(positions);
                            importedCount += positions.size();
                            positions.clear();
                            logger.debug("Batch inserted {} positions. Total imported so far: {}", BATCH_SIZE, importedCount);
                        }
                    } else {
                        skippedCount++;
                    }
                } catch (Exception e) {
                    logger.error("Error processing row {}: {}", rowIndex + 1, e.getMessage());
                    skippedCount++;
                }
            }
            
            // Insert remaining positions
            if (!positions.isEmpty()) {
                portfolioPositionRepository.saveAll(positions);
                importedCount += positions.size();
            }
            
            logger.info("Import completed. Imported: {}, Skipped: {}, Total rows processed: {}", 
                       importedCount, skippedCount, sheet.getLastRowNum());
            
            return importedCount;
        }
    }
    
    /**
     * Parse a single Excel row into a PortfolioPosition entity.
     */
    private PortfolioPosition parseRowToPortfolioPosition(Row row) {
        try {
            PortfolioPosition position = new PortfolioPosition();
            
            // Map Excel columns to entity fields
            position.setPartnerIdFake(getCellValueAsString(row, 0));
            position.setAccountIdFake(getCellValueAsString(row, 1));
            position.setPositionCreatedDate(getCellValueAsDateTime(row, 2));
            position.setFiUnitTypeCode(getCellValueAsString(row, 3));
            position.setBalanceAmount(getCellValueAsBigDecimal(row, 4));
            position.setValueAmount(getCellValueAsBigDecimal(row, 5));
            position.setTradeAmount(getCellValueAsBigDecimal(row, 6));
            position.setValuationDate(getCellValueAsDateTime(row, 7));
            position.setAsOfDate(getCellValueAsDateTime(row, 8));
            position.setValueCurrency(getCellValueAsString(row, 9));
            position.setSourceCurrency(getCellValueAsString(row, 10));
            position.setOriginalQuantity(getCellValueAsBigDecimal(row, 11));
            position.setMarketValueAmount(getCellValueAsBigDecimal(row, 12));
            position.setFxRate(getCellValueAsBigDecimal(row, 13));
            position.setValor(getCellValueAsString(row, 14));
            position.setIsin(getCellValueAsString(row, 15));
            position.setInstrumentNameShort(getCellValueAsString(row, 16));
            position.setSymbolId(getCellValueAsString(row, 17));
            position.setTitleGroupId(getCellValueAsString(row, 18));
            position.setTitleId(getCellValueAsString(row, 19));
            position.setTitleIdDescription(getCellValueAsString(row, 20));
            position.setSymbolIdGpc(getCellValueAsString(row, 21));
            position.setProductDescription(getCellValueAsString(row, 22));
            position.setProductId(getCellValueAsString(row, 23));
            position.setProductIdDescription(getCellValueAsString(row, 24));
            position.setProductClassId(getCellValueAsString(row, 25));
            position.setProductClassDescription(getCellValueAsString(row, 26));
            position.setProductFamilyId(getCellValueAsString(row, 27));
            position.setProductFamilyDescription(getCellValueAsString(row, 28));
            position.setAssetClass(getCellValueAsString(row, 29));
            position.setAssetClassSubtype(getCellValueAsString(row, 30));
            position.setAssetClassDescriptionShort(getCellValueAsString(row, 31));
            position.setAssetClassDescriptionLong(getCellValueAsString(row, 32));
            position.setUacInstrCatType(getCellValueAsString(row, 33));
            position.setInstrumentId(getCellValueAsString(row, 34));
            position.setPortfolioCurrency(getCellValueAsString(row, 35));
            position.setPortfolioShortName(getCellValueAsString(row, 36));
            position.setCurrencyId(getCellValueAsString(row, 37));
            // Skip Product ID_1 (column 38) as it's duplicate
            position.setMandatePricingId(getCellValueAsString(row, 39));
            position.setMandateProgram(getCellValueAsString(row, 40));
            // Skip Mandate Pricing ID_1 (column 41) as it's duplicate  
            position.setMandatePricingNameShort(getCellValueAsString(row, 42));
            position.setMandatePricingNameLong(getCellValueAsString(row, 43));
            position.setMandatePricingType(getCellValueAsString(row, 44));
            position.setMandateProgramSecondary(getCellValueAsString(row, 45));
            position.setInvestmentStrategy(getCellValueAsString(row, 46));
            position.setInvestmentStrategyName(getCellValueAsString(row, 47));
            position.setSolutionSubtypeId(getCellValueAsString(row, 48));
            position.setSolutionSubtypeNameShort(getCellValueAsString(row, 49));
            position.setSolutionNameShort(getCellValueAsString(row, 50));
            position.setSolutionNameLong(getCellValueAsString(row, 51));
            position.setMandateType(getCellValueAsString(row, 52));
            position.setMandateSubtype(getCellValueAsString(row, 53));
            position.setMandateGroup(getCellValueAsString(row, 54));
            position.setDomicile(getCellValueAsString(row, 55));
            position.setClientAdvisorIdFake(getCellValueAsInteger(row, 56));
            
            return position;
            
        } catch (Exception e) {
            logger.error("Error parsing row to PortfolioPosition: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Extract string value from cell, handling nulls and trimming whitespace.
     */
    private String getCellValueAsString(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        
        String value = null;
        switch (cell.getCellType()) {
            case STRING:
                value = cell.getStringCellValue();
                break;
            case NUMERIC:
                // Handle numeric values that should be strings (like IDs)
                value = String.valueOf((long) cell.getNumericCellValue());
                break;
            case BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;
            default:
                return null;
        }
        
        return value != null ? value.trim() : null;
    }
    
    /**
     * Extract BigDecimal value from cell for monetary amounts and rates.
     */
    private BigDecimal getCellValueAsBigDecimal(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null || cell.getCellType() != CellType.NUMERIC) return null;
        
        try {
            double numericValue = cell.getNumericCellValue();
            return BigDecimal.valueOf(numericValue);
        } catch (Exception e) {
            logger.debug("Error converting cell to BigDecimal at column {}: {}", columnIndex, e.getMessage());
            return null;
        }
    }
    
    /**
     * Extract Integer value from cell.
     */
    private Integer getCellValueAsInteger(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String stringValue = cell.getStringCellValue().trim();
                return stringValue.isEmpty() ? null : Integer.parseInt(stringValue);
            }
        } catch (Exception e) {
            logger.debug("Error converting cell to Integer at column {}: {}", columnIndex, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extract LocalDateTime from cell, handling both date cells and ISO string formats.
     */
    private LocalDateTime getCellValueAsDateTime(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String dateString = cell.getStringCellValue().trim();
                if (dateString.isEmpty()) return null;
                
                // Handle ISO format like "2022-08-29T22:00:00.000Z"
                if (dateString.endsWith("Z")) {
                    dateString = dateString.substring(0, dateString.length() - 1);
                }
                
                try {
                    return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException e) {
                    // Try alternative formats if needed
                    logger.debug("Could not parse date string '{}': {}", dateString, e.getMessage());
                    return null;
                }
            }
        } catch (Exception e) {
            logger.debug("Error converting cell to DateTime at column {}: {}", columnIndex, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Check if a row is empty (all cells are null or empty).
     */
    private boolean isEmptyRow(Row row) {
        for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
            Cell cell = row.getCell(cellIndex);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String cellValue = getCellValueAsString(row, cellIndex);
                if (cellValue != null && !cellValue.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Get count of existing portfolio positions in database.
     */
    public long getExistingPositionCount() {
        return portfolioPositionRepository.count();
    }
    
    /**
     * Clear all existing portfolio positions from database.
     * Use with caution!
     */
    @Transactional
    public void clearAllPositions() {
        logger.warn("Clearing all portfolio positions from database");
        portfolioPositionRepository.deleteAll();
    }
    
    /**
     * Import positions with option to clear existing data first.
     */
    @Transactional
    public int importPortfolioPositions(boolean clearExisting) throws IOException {
        if (clearExisting) {
            clearAllPositions();
        }
        
        return importPortfolioPositions();
    }
}
