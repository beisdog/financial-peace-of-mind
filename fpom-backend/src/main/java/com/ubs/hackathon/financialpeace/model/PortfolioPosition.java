package com.ubs.hackathon.financialpeace.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a Portfolio Position from the UBS Challenge Excel data.
 * Maps to the "Swiss AI - UBS Challenge 3 - Portfolio Positions.xlsx" structure.
 * Optimized for PostgreSQL database with proper indexes and sequences.
 */
@Entity
@Table(name = "portfolio_positions", indexes = {
    @Index(name = "idx_partner_id", columnList = "partner_id_fake"),
    @Index(name = "idx_account_id", columnList = "account_id_fake"),
    @Index(name = "idx_asset_class", columnList = "asset_class_description_short"),
    @Index(name = "idx_value_currency", columnList = "value_currency"),
    @Index(name = "idx_isin", columnList = "isin"),
    @Index(name = "idx_valuation_date", columnList = "valuation_date"),
    @Index(name = "idx_value_amount", columnList = "value_amount"),
    @Index(name = "idx_partner_asset_class", columnList = "partner_id_fake, asset_class_description_short"),
    @Index(name = "idx_account_currency", columnList = "account_id_fake, value_currency")
})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PortfolioPosition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "portfolio_position_seq")
    @SequenceGenerator(name = "portfolio_position_seq", sequenceName = "portfolio_position_id_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;
    
    @Column(name = "partner_id_fake", length = 50)
    private String partnerIdFake;
    
    @Column(name = "account_id_fake", length = 50)
    private String accountIdFake;
    
    @Column(name = "position_created_date")
    private LocalDateTime positionCreatedDate;
    
    @Column(name = "fi_unit_type_cd", length = 10)
    private String fiUnitTypeCode;
    
    @Column(name = "balance_amount", precision = 19, scale = 2)
    private BigDecimal balanceAmount;
    
    @Column(name = "value_amount", precision = 19, scale = 2)
    private BigDecimal valueAmount;
    
    @Column(name = "trade_amount", precision = 19, scale = 2)
    private BigDecimal tradeAmount;
    
    @Column(name = "valuation_date")
    private LocalDateTime valuationDate;
    
    @Column(name = "as_of_date")
    private LocalDateTime asOfDate;
    
    @Column(name = "value_currency", length = 3)
    private String valueCurrency;
    
    @Column(name = "source_currency", length = 3)
    private String sourceCurrency;
    
    @Column(name = "original_quantity", precision = 19, scale = 6)
    private BigDecimal originalQuantity;
    
    @Column(name = "market_value_amount", precision = 19, scale = 6)
    private BigDecimal marketValueAmount;
    
    @Column(name = "fx_rate", precision = 19, scale = 12)
    private BigDecimal fxRate;
    
    @Column(name = "valor", length = 50)
    private String valor;
    
    @Column(name = "isin", length = 12)
    private String isin;
    
    @Column(name = "instrument_name_short", length = 100)
    private String instrumentNameShort;
    
    @Column(name = "symbol_id", length = 50)
    private String symbolId;
    
    @Column(name = "title_group_id", length = 10)
    private String titleGroupId;
    
    @Column(name = "title_id", length = 10)
    private String titleId;
    
    @Column(name = "title_id_description", length = 100)
    private String titleIdDescription;
    
    @Column(name = "symbol_id_gpc", length = 50)
    private String symbolIdGpc;
    
    @Column(name = "product_description", length = 100)
    private String productDescription;
    
    @Column(name = "product_id", length = 50)
    private String productId;
    
    @Column(name = "product_id_description", length = 100)
    private String productIdDescription;
    
    @Column(name = "product_class_id", length = 50)
    private String productClassId;
    
    @Column(name = "product_class_description", length = 100)
    private String productClassDescription;
    
    @Column(name = "product_family_id", length = 50)
    private String productFamilyId;
    
    @Column(name = "product_family_description", length = 100)
    private String productFamilyDescription;
    
    @Column(name = "asset_class", length = 50)
    private String assetClass;
    
    @Column(name = "asset_class_subtype", length = 50)
    private String assetClassSubtype;
    
    @Column(name = "asset_class_description_short", length = 100)
    private String assetClassDescriptionShort;
    
    @Column(name = "asset_class_description_long", length = 100)
    private String assetClassDescriptionLong;
    
    @Column(name = "uac_instr_cat_type", length = 50)
    private String uacInstrCatType;
    
    @Column(name = "instrument_id", length = 100)
    private String instrumentId;
    
    @Column(name = "portfolio_currency", length = 3)
    private String portfolioCurrency;
    
    @Column(name = "portfolio_short_name", length = 50)
    private String portfolioShortName;
    
    @Column(name = "currency_id", length = 3)
    private String currencyId;
    
    @Column(name = "mandate_pricing_id", length = 50)
    private String mandatePricingId;
    
    @Column(name = "mandate_program", length = 50)
    private String mandateProgram;
    
    @Column(name = "mandate_pricing_name_short", length = 100)
    private String mandatePricingNameShort;
    
    @Column(name = "mandate_pricing_name_long", length = 200)
    private String mandatePricingNameLong;
    
    @Column(name = "mandate_pricing_type", length = 50)
    private String mandatePricingType;
    
    @Column(name = "mandate_program_secondary", length = 100)
    private String mandateProgramSecondary;
    
    @Column(name = "investment_strategy", length = 10)
    private String investmentStrategy;
    
    @Column(name = "investment_strategy_name", length = 100)
    private String investmentStrategyName;
    
    @Column(name = "solution_subtype_id", length = 50)
    private String solutionSubtypeId;
    
    @Column(name = "solution_subtype_name_short", length = 100)
    private String solutionSubtypeNameShort;
    
    @Column(name = "solution_name_short", length = 100)
    private String solutionNameShort;
    
    @Column(name = "solution_name_long", length = 200)
    private String solutionNameLong;
    
    @Column(name = "mandate_type", length = 50)
    private String mandateType;
    
    @Column(name = "mandate_subtype", length = 100)
    private String mandateSubtype;
    
    @Column(name = "mandate_group", length = 100)
    private String mandateGroup;
    
    @Column(name = "domicile", length = 5)
    private String domicile;
    
    @Column(name = "client_advisor_id_fake")
    private Integer clientAdvisorIdFake;
    
    @Override
    public String toString() {
        return "PortfolioPosition{" +
                "id=" + id +
                ", partnerIdFake='" + partnerIdFake + '\'' +
                ", accountIdFake='" + accountIdFake + '\'' +
                ", positionCreatedDate=" + positionCreatedDate +
                ", isin='" + isin + '\'' +
                ", instrumentNameShort='" + instrumentNameShort + '\'' +
                ", valueAmount=" + valueAmount +
                ", valueCurrency='" + valueCurrency + '\'' +
                '}';
    }
}
