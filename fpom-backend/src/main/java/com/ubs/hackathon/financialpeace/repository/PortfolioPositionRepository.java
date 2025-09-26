package com.ubs.hackathon.financialpeace.repository;

import com.ubs.hackathon.financialpeace.model.PortfolioPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for PortfolioPosition entity.
 * Provides CRUD operations and custom query methods for portfolio position data.
 */
@Repository
public interface PortfolioPositionRepository extends JpaRepository<PortfolioPosition, Long> {
    
    // ==================== BASIC FINDERS ====================
    
    /**
     * Find all positions for a specific partner
     */
    List<PortfolioPosition> findByPartnerIdFake(String partnerIdFake);
    
    /**
     * Find all positions for a specific account
     */
    List<PortfolioPosition> findByAccountIdFake(String accountIdFake);
    
    /**
     * Find positions by asset class
     */
    List<PortfolioPosition> findByAssetClassDescriptionShort(String assetClass);
    
    /**
     * Find positions by currency
     */
    List<PortfolioPosition> findByValueCurrency(String currency);
    
    /**
     * Find positions by ISIN
     */
    List<PortfolioPosition> findByIsin(String isin);
    
    /**
     * Find positions by instrument name (case-insensitive)
     */
    List<PortfolioPosition> findByInstrumentNameShortContainingIgnoreCase(String instrumentName);
    
    /**
     * Find positions by mandate type
     */
    List<PortfolioPosition> findByMandateType(String mandateType);
    
    /**
     * Find positions with value amount greater than specified amount
     */
    List<PortfolioPosition> findByValueAmountGreaterThan(BigDecimal amount);
    
    /**
     * Find positions created after a specific date
     */
    List<PortfolioPosition> findByPositionCreatedDateAfter(LocalDateTime date);
    
    /**
     * Find positions by investment strategy
     */
    List<PortfolioPosition> findByInvestmentStrategyName(String strategyName);
    
    /**
     * Find positions by multiple asset classes
     */
    List<PortfolioPosition> findByAssetClassDescriptionShortIn(List<String> assetClasses);
    
    /**
     * Find positions by valuation date range
     */
    List<PortfolioPosition> findByValuationDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find top positions by value amount
     */
    List<PortfolioPosition> findTop10ByOrderByValueAmountDesc();
    
    // ==================== ACCOUNT QUERIES ====================
    
    /**
     * Get account summary with position counts and total values
     */
    @Query("SELECT p.accountIdFake, p.partnerIdFake, COUNT(p), SUM(p.valueAmount), p.valueCurrency " +
           "FROM PortfolioPosition p " +
           "GROUP BY p.accountIdFake, p.partnerIdFake, p.valueCurrency " +
           "ORDER BY p.accountIdFake, p.valueCurrency")
    List<Object[]> getAccountSummary();
    
    /**
     * Get unique account IDs
     */
    @Query("SELECT DISTINCT p.accountIdFake FROM PortfolioPosition p ORDER BY p.accountIdFake")
    List<String> findDistinctAccountIds();
    
    /**
     * Get unique partner IDs
     */
    @Query("SELECT DISTINCT p.partnerIdFake FROM PortfolioPosition p ORDER BY p.partnerIdFake")
    List<String> findDistinctPartnerIds();
    
    /**
     * Count unique account IDs
     */
    @Query("SELECT COUNT(DISTINCT p.accountIdFake) FROM PortfolioPosition p")
    Long countDistinctAccountIds();
    
    /**
     * Count unique partner IDs
     */
    @Query("SELECT COUNT(DISTINCT p.partnerIdFake) FROM PortfolioPosition p")
    Long countDistinctPartnerIds();
    
    /**
     * Count positions by partner
     */
    Long countByPartnerIdFake(String partnerIdFake);
    
    /**
     * Count positions by account
     */
    Long countByAccountIdFake(String accountIdFake);
    
    // ==================== AGGREGATION QUERIES ====================
    
    /**
     * Get total value amount by asset class
     */
    @Query("SELECT p.assetClassDescriptionShort, SUM(p.valueAmount) " +
           "FROM PortfolioPosition p " +
           "GROUP BY p.assetClassDescriptionShort " +
           "ORDER BY SUM(p.valueAmount) DESC")
    List<Object[]> getTotalValueByAssetClass();
    
    /**
     * Get total value amount by currency
     */
    @Query("SELECT p.valueCurrency, SUM(p.valueAmount) " +
           "FROM PortfolioPosition p " +
           "GROUP BY p.valueCurrency " +
           "ORDER BY SUM(p.valueAmount) DESC")
    List<Object[]> getTotalValueByCurrency();
    
    /**
     * Get portfolio summary for a specific partner
     */
    @Query("SELECT p.assetClassDescriptionShort, COUNT(p), SUM(p.valueAmount) " +
           "FROM PortfolioPosition p " +
           "WHERE p.partnerIdFake = :partnerIdFake " +
           "GROUP BY p.assetClassDescriptionShort " +
           "ORDER BY SUM(p.valueAmount) DESC")
    List<Object[]> getPortfolioSummaryByPartner(@Param("partnerIdFake") String partnerIdFake);
    
    /**
     * Get portfolio summary for a specific account
     */
    @Query("SELECT p.assetClassDescriptionShort, COUNT(p), SUM(p.valueAmount) " +
           "FROM PortfolioPosition p " +
           "WHERE p.accountIdFake = :accountIdFake " +
           "GROUP BY p.assetClassDescriptionShort " +
           "ORDER BY SUM(p.valueAmount) DESC")
    List<Object[]> getPortfolioSummaryByAccount(@Param("accountIdFake") String accountIdFake);
    
    /**
     * Get positions with their FX-adjusted CHF value for a partner
     */
    @Query("SELECT p, (p.valueAmount * p.fxRate) as chfValue " +
           "FROM PortfolioPosition p " +
           "WHERE p.partnerIdFake = :partnerIdFake " +
           "ORDER BY (p.valueAmount * p.fxRate) DESC")
    List<Object[]> getPositionsWithChfValue(@Param("partnerIdFake") String partnerIdFake);
    
    /**
     * Get positions with their FX-adjusted CHF value for an account
     */
    @Query("SELECT p, (p.valueAmount * p.fxRate) as chfValue " +
           "FROM PortfolioPosition p " +
           "WHERE p.accountIdFake = :accountIdFake " +
           "ORDER BY (p.valueAmount * p.fxRate) DESC")
    List<Object[]> getPositionsWithChfValueByAccount(@Param("accountIdFake") String accountIdFake);
    
    /**
     * Get total value by partner
     */
    @Query("SELECT p.partnerIdFake, COUNT(p), SUM(p.valueAmount), p.valueCurrency " +
           "FROM PortfolioPosition p " +
           "GROUP BY p.partnerIdFake, p.valueCurrency " +
           "ORDER BY SUM(p.valueAmount) DESC")
    List<Object[]> getTotalValueByPartner();
    
    /**
     * Get total value by account
     */
    @Query("SELECT p.accountIdFake, p.partnerIdFake, COUNT(p), SUM(p.valueAmount), p.valueCurrency " +
           "FROM PortfolioPosition p " +
           "GROUP BY p.accountIdFake, p.partnerIdFake, p.valueCurrency " +
           "ORDER BY SUM(p.valueAmount) DESC")
    List<Object[]> getTotalValueByAccount();
    
    // ==================== DISTINCT VALUES ====================
    
    /**
     * Get unique asset classes
     */
    @Query("SELECT DISTINCT p.assetClassDescriptionShort FROM PortfolioPosition p ORDER BY p.assetClassDescriptionShort")
    List<String> findDistinctAssetClasses();
    
    /**
     * Get unique currencies
     */
    @Query("SELECT DISTINCT p.valueCurrency FROM PortfolioPosition p ORDER BY p.valueCurrency")
    List<String> findDistinctCurrencies();
    
    /**
     * Get unique mandate types
     */
    @Query("SELECT DISTINCT p.mandateType FROM PortfolioPosition p ORDER BY p.mandateType")
    List<String> findDistinctMandateTypes();
    
    /**
     * Get unique investment strategies
     */
    @Query("SELECT DISTINCT p.investmentStrategyName FROM PortfolioPosition p WHERE p.investmentStrategyName IS NOT NULL ORDER BY p.investmentStrategyName")
    List<String> findDistinctInvestmentStrategies();
    
    /**
     * Get unique instruments (ISIN and name)
     */
    @Query("SELECT DISTINCT p.isin, p.instrumentNameShort FROM PortfolioPosition p ORDER BY p.instrumentNameShort")
    List<Object[]> findDistinctInstruments();
    
    /**
     * Get unique domiciles
     */
    @Query("SELECT DISTINCT p.domicile FROM PortfolioPosition p WHERE p.domicile IS NOT NULL ORDER BY p.domicile")
    List<String> findDistinctDomiciles();
    
    // ==================== ADVANCED QUERIES ====================
    
    /**
     * Find positions by multiple criteria
     */
    @Query("SELECT p FROM PortfolioPosition p WHERE " +
           "(:partnerIdFake IS NULL OR p.partnerIdFake = :partnerIdFake) AND " +
           "(:assetClass IS NULL OR p.assetClassDescriptionShort = :assetClass) AND " +
           "(:currency IS NULL OR p.valueCurrency = :currency) AND " +
           "(:minValue IS NULL OR p.valueAmount >= :minValue)")
    List<PortfolioPosition> findPositionsByMultipleCriteria(
            @Param("partnerIdFake") String partnerIdFake,
            @Param("assetClass") String assetClass,
            @Param("currency") String currency,
            @Param("minValue") BigDecimal minValue);
    
    /**
     * Get asset allocation for a partner (percentages)
     */
    @Query("SELECT p.assetClassDescriptionShort, " +
           "SUM(p.valueAmount) as totalValue, " +
           "COUNT(p) as positionCount, " +
           "AVG(p.valueAmount) as avgValue, " +
           "MIN(p.valueAmount) as minValue, " +
           "MAX(p.valueAmount) as maxValue " +
           "FROM PortfolioPosition p " +
           "WHERE p.partnerIdFake = :partnerIdFake " +
           "GROUP BY p.assetClassDescriptionShort " +
           "ORDER BY SUM(p.valueAmount) DESC")
    List<Object[]> getAssetAllocationByPartner(@Param("partnerIdFake") String partnerIdFake);
    
    /**
     * Get currency exposure for a partner
     */
    @Query("SELECT p.valueCurrency, " +
           "SUM(p.valueAmount) as totalValue, " +
           "COUNT(p) as positionCount, " +
           "AVG(p.fxRate) as avgFxRate " +
           "FROM PortfolioPosition p " +
           "WHERE p.partnerIdFake = :partnerIdFake " +
           "GROUP BY p.valueCurrency " +
           "ORDER BY SUM(p.valueAmount) DESC")
    List<Object[]> getCurrencyExposureByPartner(@Param("partnerIdFake") String partnerIdFake);
    
    /**
     * Find large positions (top percentile by value)
     */
    @Query("SELECT p FROM PortfolioPosition p WHERE p.valueAmount >= " +
           "(SELECT PERCENTILE_CONT(0.9) WITHIN GROUP (ORDER BY p2.valueAmount) FROM PortfolioPosition p2) " +
           "ORDER BY p.valueAmount DESC")
    List<PortfolioPosition> findLargePositions();
    
    /**
     * Get performance summary by asset class and currency
     */
    @Query("SELECT p.assetClassDescriptionShort, p.valueCurrency, " +
           "COUNT(p) as positionCount, " +
           "SUM(p.valueAmount) as totalValue, " +
           "AVG(p.valueAmount) as avgValue, " +
           "SUM(p.marketValueAmount) as totalMarketValue " +
           "FROM PortfolioPosition p " +
           "GROUP BY p.assetClassDescriptionShort, p.valueCurrency " +
           "ORDER BY SUM(p.valueAmount) DESC")
    List<Object[]> getPerformanceSummaryByAssetClassAndCurrency();
    
    /**
     * Find positions with significant FX exposure
     */
    @Query("SELECT p FROM PortfolioPosition p WHERE p.valueCurrency != p.sourceCurrency " +
           "AND ABS(p.fxRate - 1.0) > 0.1 ORDER BY ABS(p.fxRate - 1.0) DESC")
    List<PortfolioPosition> findPositionsWithSignificantFxExposure();
    
    /**
     * Get mandate type distribution
     */
    @Query("SELECT p.mandateType, COUNT(p), SUM(p.valueAmount) " +
           "FROM PortfolioPosition p " +
           "GROUP BY p.mandateType " +
           "ORDER BY SUM(p.valueAmount) DESC")
    List<Object[]> getMandateTypeDistribution();
}
