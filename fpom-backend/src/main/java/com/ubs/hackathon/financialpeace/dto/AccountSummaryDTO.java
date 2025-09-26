package com.ubs.hackathon.financialpeace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Account Summary information.
 * Contains aggregated account data with position counts and total values.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryDTO {
    
    /**
     * Fake account identifier
     */
    private String accountIdFake;
    
    /**
     * Fake partner identifier associated with this account
     */
    private String partnerIdFake;
    
    /**
     * Number of positions in this account
     */
    private Long positionCount;
    
    /**
     * Total value of all positions in this account
     */
    private BigDecimal totalValue;
    
    /**
     * Currency of the total value
     */
    private String currency;
    
    /**
     * Average position value in this account
     */
    private BigDecimal averagePositionValue;
    
    /**
     * Constructor for basic account summary (without average)
     */
    public AccountSummaryDTO(String accountIdFake, String partnerIdFake, Long positionCount, BigDecimal totalValue, String currency) {
        this.accountIdFake = accountIdFake;
        this.partnerIdFake = partnerIdFake;
        this.positionCount = positionCount;
        this.totalValue = totalValue;
        this.currency = currency;
        
        // Calculate average if possible
        if (positionCount != null && positionCount > 0 && totalValue != null) {
            this.averagePositionValue = totalValue.divide(BigDecimal.valueOf(positionCount), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    /**
     * Helper method to check if this account has significant holdings
     */
    public boolean hasSignificantHoldings(BigDecimal threshold) {
        return totalValue != null && totalValue.compareTo(threshold) > 0;
    }
    
    /**
     * Helper method to get formatted total value
     */
    public String getFormattedTotalValue() {
        if (totalValue == null || currency == null) {
            return "N/A";
        }
        return String.format("%s %.2f", currency, totalValue);
    }
}
