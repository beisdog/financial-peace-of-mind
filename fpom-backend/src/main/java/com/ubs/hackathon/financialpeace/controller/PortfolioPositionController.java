package com.ubs.hackathon.financialpeace.controller;

import com.ubs.hackathon.financialpeace.model.PortfolioPosition;
import com.ubs.hackathon.financialpeace.repository.PortfolioPositionRepository;
import com.ubs.hackathon.financialpeace.service.PortfolioPositionImportService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Comprehensive CRUD REST Controller for Portfolio Position operations.
 * Provides full CRUD operations, import functionality, and various query endpoints.
 */
@RestController
@RequestMapping("/api/portfolio-positions")
@CrossOrigin(origins = "*")
public class PortfolioPositionController {
    
    private static final Logger logger = LoggerFactory.getLogger(PortfolioPositionController.class);
    
    @Autowired
    private PortfolioPositionImportService importService;
    
    @Autowired
    private PortfolioPositionRepository repository;
    
    // ==================== CRUD OPERATIONS ====================
    
    /**
     * Create a new portfolio position.
     */
    @PostMapping
    public ResponseEntity<PortfolioPosition> createPosition(@Valid @RequestBody PortfolioPosition position) {
        try {
            // Ensure ID is null for new entities
            position.setId(null);
            PortfolioPosition savedPosition = repository.save(position);
            logger.info("Created new portfolio position with ID: {}", savedPosition.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPosition);
        } catch (Exception e) {
            logger.error("Error creating portfolio position", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Get all portfolio positions with pagination and sorting.
     */
    @GetMapping
    public ResponseEntity<Page<PortfolioPosition>> getAllPositions(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        try {
            Page<PortfolioPosition> positions = repository.findAll(pageable);
            return ResponseEntity.ok(positions);
        } catch (Exception e) {
            logger.error("Error retrieving portfolio positions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get a specific portfolio position by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioPosition> getPositionById(@PathVariable Long id) {
        try {
            Optional<PortfolioPosition> position = repository.findById(id);
            if (position.isPresent()) {
                return ResponseEntity.ok(position.get());
            } else {
                logger.warn("Portfolio position not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving portfolio position with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update an existing portfolio position.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PortfolioPosition> updatePosition(
            @PathVariable Long id, 
            @Valid @RequestBody PortfolioPosition positionDetails) {
        try {
            Optional<PortfolioPosition> optionalPosition = repository.findById(id);
            
            if (!optionalPosition.isPresent()) {
                logger.warn("Portfolio position not found for update with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            PortfolioPosition existingPosition = optionalPosition.get();
            
            // Update fields
            updatePositionFields(existingPosition, positionDetails);
            
            PortfolioPosition updatedPosition = repository.save(existingPosition);
            logger.info("Updated portfolio position with ID: {}", id);
            return ResponseEntity.ok(updatedPosition);
            
        } catch (Exception e) {
            logger.error("Error updating portfolio position with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Partially update a portfolio position (PATCH).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<PortfolioPosition> patchPosition(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        try {
            Optional<PortfolioPosition> optionalPosition = repository.findById(id);
            
            if (!optionalPosition.isPresent()) {
                logger.warn("Portfolio position not found for patch with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            PortfolioPosition existingPosition = optionalPosition.get();
            
            // Apply partial updates
            applyPatchUpdates(existingPosition, updates);
            
            PortfolioPosition updatedPosition = repository.save(existingPosition);
            logger.info("Patched portfolio position with ID: {}", id);
            return ResponseEntity.ok(updatedPosition);
            
        } catch (Exception e) {
            logger.error("Error patching portfolio position with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Delete a portfolio position by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePosition(@PathVariable Long id) {
        try {
            if (!repository.existsById(id)) {
                logger.warn("Portfolio position not found for deletion with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            repository.deleteById(id);
            logger.info("Deleted portfolio position with ID: {}", id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Portfolio position deleted successfully");
            response.put("deletedId", id);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting portfolio position with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Error deleting position: " + e.getMessage()));
        }
    }
    
    /**
     * Delete multiple portfolio positions by IDs.
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> deletePositions(@RequestBody List<Long> ids) {
        try {
            List<PortfolioPosition> positionsToDelete = repository.findAllById(ids);
            int deletedCount = positionsToDelete.size();
            
            repository.deleteAll(positionsToDelete);
            logger.info("Batch deleted {} portfolio positions", deletedCount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Portfolio positions deleted successfully");
            response.put("requestedIds", ids);
            response.put("deletedCount", deletedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error batch deleting portfolio positions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Error deleting positions: " + e.getMessage()));
        }
    }
    
    // ==================== ACCOUNT OPERATIONS ====================
    
    /**
     * Get a list of all unique accounts with position counts and total values.
     */
    @GetMapping("/accounts")
    public ResponseEntity<List<Map<String, Object>>> getAllAccounts() {
        try {
            List<Object[]> accountSummary = repository.getAccountSummary();
            
            List<Map<String, Object>> accounts = accountSummary.stream()
                    .map(row -> {
                        Map<String, Object> account = new HashMap<>();
                        account.put("accountIdFake", row[0]);
                        account.put("partnerIdFake", row[1]);
                        account.put("positionCount", row[2]);
                        account.put("totalValue", row[3]);
                        account.put("currency", row[4]);
                        return account;
                    })
                    .toList();
            
            logger.info("Retrieved {} unique accounts", accounts.size());
            return ResponseEntity.ok(accounts);
            
        } catch (Exception e) {
            logger.error("Error retrieving accounts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get simple list of all unique account IDs.
     */
    @GetMapping("/accounts/list")
    public ResponseEntity<List<String>> getAccountIdsList() {
        try {
            List<String> accountIds = repository.findDistinctAccountIds();
            logger.info("Retrieved {} unique account IDs", accountIds.size());
            return ResponseEntity.ok(accountIds);
        } catch (Exception e) {
            logger.error("Error retrieving account IDs list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get account details with positions summary.
     */
    @GetMapping("/accounts/{accountIdFake}/details")
    public ResponseEntity<Map<String, Object>> getAccountDetails(@PathVariable String accountIdFake) {
        try {
            List<PortfolioPosition> positions = repository.findByAccountIdFake(accountIdFake);
            
            if (positions.isEmpty()) {
                logger.warn("No positions found for account: {}", accountIdFake);
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> accountDetails = new HashMap<>();
            accountDetails.put("accountIdFake", accountIdFake);
            accountDetails.put("partnerIdFake", positions.get(0).getPartnerIdFake());
            accountDetails.put("positionCount", positions.size());
            
            // Calculate total values by currency
            Map<String, BigDecimal> totalsByCurrency = positions.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            PortfolioPosition::getValueCurrency,
                            java.util.stream.Collectors.reducing(
                                    BigDecimal.ZERO,
                                    PortfolioPosition::getValueAmount,
                                    BigDecimal::add
                            )
                    ));
            
            // Asset class breakdown
            Map<String, Long> assetClassCounts = positions.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            PortfolioPosition::getAssetClassDescriptionShort,
                            java.util.stream.Collectors.counting()
                    ));
            
            accountDetails.put("totalsByCurrency", totalsByCurrency);
            accountDetails.put("assetClassBreakdown", assetClassCounts);
            accountDetails.put("positions", positions);
            
            return ResponseEntity.ok(accountDetails);
            
        } catch (Exception e) {
            logger.error("Error retrieving account details for: {}", accountIdFake, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ==================== QUERY OPERATIONS ====================
    
    /**
     * Get positions by partner ID.
     */
    @GetMapping("/partner/{partnerIdFake}")
    public ResponseEntity<List<PortfolioPosition>> getPositionsByPartner(
            @PathVariable String partnerIdFake) {
        List<PortfolioPosition> positions = repository.findByPartnerIdFake(partnerIdFake);
        return ResponseEntity.ok(positions);
    }
    
    /**
     * Get positions by account ID.
     */
    @GetMapping("/account/{accountIdFake}")
    public ResponseEntity<List<PortfolioPosition>> getPositionsByAccount(
            @PathVariable String accountIdFake) {
        List<PortfolioPosition> positions = repository.findByAccountIdFake(accountIdFake);
        return ResponseEntity.ok(positions);
    }
    
    /**
     * Get positions by asset class.
     */
    @GetMapping("/asset-class/{assetClass}")
    public ResponseEntity<List<PortfolioPosition>> getPositionsByAssetClass(
            @PathVariable String assetClass) {
        List<PortfolioPosition> positions = repository.findByAssetClassDescriptionShort(assetClass);
        return ResponseEntity.ok(positions);
    }
    
    /**
     * Get positions by currency.
     */
    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<PortfolioPosition>> getPositionsByCurrency(
            @PathVariable String currency) {
        List<PortfolioPosition> positions = repository.findByValueCurrency(currency);
        return ResponseEntity.ok(positions);
    }
    
    /**
     * Search positions by instrument name.
     */
    @GetMapping("/search")
    public ResponseEntity<List<PortfolioPosition>> searchPositions(
            @RequestParam String instrumentName) {
        List<PortfolioPosition> positions = repository.findByInstrumentNameShortContainingIgnoreCase(instrumentName);
        return ResponseEntity.ok(positions);
    }
    
    /**
     * Get positions with value amount greater than specified amount.
     */
    @GetMapping("/value-greater-than/{amount}")
    public ResponseEntity<List<PortfolioPosition>> getPositionsWithValueGreaterThan(
            @PathVariable BigDecimal amount) {
        List<PortfolioPosition> positions = repository.findByValueAmountGreaterThan(amount);
        return ResponseEntity.ok(positions);
    }
    
    /**
     * Get top positions by value amount.
     */
    @GetMapping("/top-positions")
    public ResponseEntity<List<PortfolioPosition>> getTopPositions(
            @RequestParam(defaultValue = "10") int limit) {
        List<PortfolioPosition> positions;
        if (limit <= 10) {
            positions = repository.findTop10ByOrderByValueAmountDesc();
        } else {
            // For limits > 10, use pageable
            Pageable topN = org.springframework.data.domain.PageRequest.of(0, limit, 
                Sort.by(Sort.Direction.DESC, "valueAmount"));
            positions = repository.findAll(topN).getContent();
        }
        return ResponseEntity.ok(positions);
    }
    
    // ==================== SUMMARY AND ANALYTICS ====================
    
    /**
     * Get portfolio summary statistics.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getPortfolioSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            long totalPositions = repository.count();
            List<Object[]> valueByAssetClass = repository.getTotalValueByAssetClass();
            List<Object[]> valueByCurrency = repository.getTotalValueByCurrency();
            List<String> distinctAssetClasses = repository.findDistinctAssetClasses();
            List<String> distinctCurrencies = repository.findDistinctCurrencies();
            List<String> distinctMandateTypes = repository.findDistinctMandateTypes();
            
            summary.put("totalPositions", totalPositions);
            summary.put("valueByAssetClass", valueByAssetClass);
            summary.put("valueByCurrency", valueByCurrency);
            summary.put("assetClasses", distinctAssetClasses);
            summary.put("currencies", distinctCurrencies);
            summary.put("mandateTypes", distinctMandateTypes);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            logger.error("Error generating portfolio summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Error generating summary: " + e.getMessage()));
        }
    }
    
    /**
     * Get portfolio summary for a specific partner.
     */
    @GetMapping("/summary/partner/{partnerIdFake}")
    public ResponseEntity<Map<String, Object>> getPartnerPortfolioSummary(
            @PathVariable String partnerIdFake) {
        
        Map<String, Object> summary = new HashMap<>();
        
        try {
            Long positionCount = repository.countByPartnerIdFake(partnerIdFake);
            List<Object[]> portfolioSummary = repository.getPortfolioSummaryByPartner(partnerIdFake);
            List<Object[]> chfValuePositions = repository.getPositionsWithChfValue(partnerIdFake);
            
            summary.put("partnerId", partnerIdFake);
            summary.put("positionCount", positionCount);
            summary.put("assetClassBreakdown", portfolioSummary);
            summary.put("chfValuePositions", chfValuePositions);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            logger.error("Error generating partner portfolio summary for {}", partnerIdFake, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Error generating partner summary: " + e.getMessage()));
        }
    }
    
    // ==================== IMPORT OPERATIONS ====================
    
    /**
     * Import portfolio positions from Excel file.
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importPositions(
            @RequestParam(defaultValue = "false") boolean clearExisting) {
        
        logger.info("Starting portfolio positions import. Clear existing: {}", clearExisting);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            long existingCount = importService.getExistingPositionCount();
            int importedCount = importService.importPortfolioPositions(clearExisting);
            long finalCount = importService.getExistingPositionCount();
            
            response.put("success", true);
            response.put("message", "Import completed successfully");
            response.put("importedCount", importedCount);
            response.put("existingCountBefore", existingCount);
            response.put("totalCountAfter", finalCount);
            response.put("clearedExisting", clearExisting);
            
            logger.info("Import completed. Imported: {}, Total in DB: {}", importedCount, finalCount);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            logger.error("Error importing portfolio positions", e);
            response.put("success", false);
            response.put("error", "IO Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error during import", e);
            response.put("success", false);
            response.put("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ==================== UTILITY OPERATIONS ====================
    
    /**
     * Get database statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalCount = repository.count();
            stats.put("totalRecords", totalCount);
            stats.put("databaseStatus", totalCount > 0 ? "populated" : "empty");
            
            if (totalCount > 0) {
                List<String> assetClasses = repository.findDistinctAssetClasses();
                List<String> currencies = repository.findDistinctCurrencies();
                long uniqueAccounts = repository.countDistinctAccountIds();
                long uniquePartners = repository.countDistinctPartnerIds();
                
                stats.put("uniqueAssetClasses", assetClasses.size());
                stats.put("uniqueCurrencies", currencies.size());
                stats.put("uniqueAccounts", uniqueAccounts);
                stats.put("uniquePartners", uniquePartners);
            }
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting database stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Error getting stats: " + e.getMessage()));
        }
    }
    
    /**
     * Clear all portfolio positions. Use with caution!
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, Object>> clearAllPositions() {
        logger.warn("Request to clear all portfolio positions");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            long countBefore = repository.count();
            importService.clearAllPositions();
            long countAfter = repository.count();
            
            response.put("success", true);
            response.put("message", "All positions cleared");
            response.put("recordsClearedCount", countBefore);
            response.put("remainingCount", countAfter);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error clearing positions", e);
            response.put("success", false);
            response.put("error", "Error clearing positions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Update all fields from source to target position.
     */
    private void updatePositionFields(PortfolioPosition target, PortfolioPosition source) {
        target.setPartnerIdFake(source.getPartnerIdFake());
        target.setAccountIdFake(source.getAccountIdFake());
        target.setPositionCreatedDate(source.getPositionCreatedDate());
        target.setFiUnitTypeCode(source.getFiUnitTypeCode());
        target.setBalanceAmount(source.getBalanceAmount());
        target.setValueAmount(source.getValueAmount());
        target.setTradeAmount(source.getTradeAmount());
        target.setValuationDate(source.getValuationDate());
        target.setAsOfDate(source.getAsOfDate());
        target.setValueCurrency(source.getValueCurrency());
        target.setSourceCurrency(source.getSourceCurrency());
        target.setOriginalQuantity(source.getOriginalQuantity());
        target.setMarketValueAmount(source.getMarketValueAmount());
        target.setFxRate(source.getFxRate());
        target.setValor(source.getValor());
        target.setIsin(source.getIsin());
        target.setInstrumentNameShort(source.getInstrumentNameShort());
        target.setSymbolId(source.getSymbolId());
        target.setTitleGroupId(source.getTitleGroupId());
        target.setTitleId(source.getTitleId());
        target.setTitleIdDescription(source.getTitleIdDescription());
        target.setSymbolIdGpc(source.getSymbolIdGpc());
        target.setProductDescription(source.getProductDescription());
        target.setProductId(source.getProductId());
        target.setProductIdDescription(source.getProductIdDescription());
        target.setProductClassId(source.getProductClassId());
        target.setProductClassDescription(source.getProductClassDescription());
        target.setProductFamilyId(source.getProductFamilyId());
        target.setProductFamilyDescription(source.getProductFamilyDescription());
        target.setAssetClass(source.getAssetClass());
        target.setAssetClassSubtype(source.getAssetClassSubtype());
        target.setAssetClassDescriptionShort(source.getAssetClassDescriptionShort());
        target.setAssetClassDescriptionLong(source.getAssetClassDescriptionLong());
        target.setUacInstrCatType(source.getUacInstrCatType());
        target.setInstrumentId(source.getInstrumentId());
        target.setPortfolioCurrency(source.getPortfolioCurrency());
        target.setPortfolioShortName(source.getPortfolioShortName());
        target.setCurrencyId(source.getCurrencyId());
        target.setMandatePricingId(source.getMandatePricingId());
        target.setMandateProgram(source.getMandateProgram());
        target.setMandatePricingNameShort(source.getMandatePricingNameShort());
        target.setMandatePricingNameLong(source.getMandatePricingNameLong());
        target.setMandatePricingType(source.getMandatePricingType());
        target.setMandateProgramSecondary(source.getMandateProgramSecondary());
        target.setInvestmentStrategy(source.getInvestmentStrategy());
        target.setInvestmentStrategyName(source.getInvestmentStrategyName());
        target.setSolutionSubtypeId(source.getSolutionSubtypeId());
        target.setSolutionSubtypeNameShort(source.getSolutionSubtypeNameShort());
        target.setSolutionNameShort(source.getSolutionNameShort());
        target.setSolutionNameLong(source.getSolutionNameLong());
        target.setMandateType(source.getMandateType());
        target.setMandateSubtype(source.getMandateSubtype());
        target.setMandateGroup(source.getMandateGroup());
        target.setDomicile(source.getDomicile());
        target.setClientAdvisorIdFake(source.getClientAdvisorIdFake());
    }
    
    /**
     * Apply partial updates to a portfolio position.
     */
    private void applyPatchUpdates(PortfolioPosition target, Map<String, Object> updates) {
        updates.forEach((field, value) -> {
            try {
                switch (field) {
                    case "partnerIdFake":
                        target.setPartnerIdFake((String) value);
                        break;
                    case "accountIdFake":
                        target.setAccountIdFake((String) value);
                        break;
                    case "positionCreatedDate":
                        if (value instanceof String) {
                            target.setPositionCreatedDate(LocalDateTime.parse((String) value));
                        }
                        break;
                    case "valueAmount":
                        if (value instanceof Number) {
                            target.setValueAmount(new BigDecimal(value.toString()));
                        }
                        break;
                    case "valueCurrency":
                        target.setValueCurrency((String) value);
                        break;
                    case "instrumentNameShort":
                        target.setInstrumentNameShort((String) value);
                        break;
                    // Add more fields as needed for common patch operations
                    default:
                        logger.warn("Unknown field for patch update: {}", field);
                }
            } catch (Exception e) {
                logger.error("Error applying patch update for field {}: {}", field, e.getMessage());
            }
        });
    }
}
