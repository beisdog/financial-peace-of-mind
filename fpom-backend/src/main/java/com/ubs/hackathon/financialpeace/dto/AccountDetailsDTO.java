package com.ubs.hackathon.financialpeace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for detailed Account information.
 * Contains comprehensive account data including position breakdowns and analytics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsDTO {
    
    /**
     * Fake account identifier
     */
    private String accountIdFake;
    
    /**
     * Fake partner identifier associated with this account
     */
    private String partnerIdFake;
    
    /**
     * Total number of positions in this account
     */
    private Integer positionCount;
    
    /**
     * Total values broken down by currency
     */
    private Map<String, BigDecimal> totalsByCurrency;
    
    /**
     * Asset class breakdown (asset class -> count of positions)
     */
    private Map<String, Long> assetClassBreakdown;
    
    /**
     * List of positions in this account (optional, can be null for summary only)
     */
    private List<PositionSummaryDTO> positions;
    
    /**
     * Calculated total value in CHF (if FX rates available)
     */
    private BigDecimal totalValueChf;
    
    /**
     * Primary currency for this account (most common currency)
     */
    private String primaryCurrency;
    
    /**
     * Risk metrics for this account
     */
    private AccountRiskMetricsDTO riskMetrics;
    
    /**
     * Nested DTO for position summary information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionSummaryDTO {
        private Long id;
        private String instrumentNameShort;
        private String isin;
        private BigDecimal valueAmount;
        private String valueCurrency;
        private String assetClassDescriptionShort;
        private BigDecimal fxRate;
    }
    
    /**
     * Nested DTO for account risk metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountRiskMetricsDTO {
        private Integer currencyCount;
        private Integer assetClassCount;
        private BigDecimal concentrationRisk; // Percentage of largest position
        private Boolean hasFxExposure;
        private String riskLevel; // LOW, MEDIUM, HIGH
    }
    
    /**
     * Helper method to get the total value across all currencies
     */
    public BigDecimal getTotalValue() {
        if (totalsByCurrency == null || totalsByCurrency.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return totalsByCurrency.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Helper method to get average position value
     */
    public BigDecimal getAveragePositionValue() {
        if (positionCount == null || positionCount == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalValue = getTotalValue();
        return totalValue.divide(BigDecimal.valueOf(positionCount), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Helper method to check if account is diversified
     */
    public boolean isDiversified() {
        return assetClassBreakdown != null && assetClassBreakdown.size() > 2;
    }
    
    /**
     * Helper method to get dominant asset class
     */
    public String getDominantAssetClass() {
        if (assetClassBreakdown == null || assetClassBreakdown.isEmpty()) {
            return null;
        }
        
        return assetClassBreakdown.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
