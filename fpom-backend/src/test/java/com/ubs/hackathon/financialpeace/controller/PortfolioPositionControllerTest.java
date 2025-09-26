package com.ubs.hackathon.financialpeace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubs.hackathon.financialpeace.model.PortfolioPosition;
import com.ubs.hackathon.financialpeace.repository.PortfolioPositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Portfolio Position CRUD operations.
 */
@SpringBootTest
@AutoConfigureTestWebMvc
@ActiveProfiles("test")
@Transactional
class PortfolioPositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PortfolioPositionRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private PortfolioPosition testPosition;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        
        testPosition = new PortfolioPosition();
        testPosition.setPartnerIdFake("TEST_PARTNER");
        testPosition.setAccountIdFake("TEST_ACCOUNT");
        testPosition.setValueAmount(new BigDecimal("50000.00"));
        testPosition.setValueCurrency("CHF");
        testPosition.setInstrumentNameShort("Test Instrument");
        testPosition.setAssetClassDescriptionShort("Equities");
        testPosition.setIsin("TEST123456789");
        testPosition.setPositionCreatedDate(LocalDateTime.now());
        testPosition.setValuationDate(LocalDateTime.now());
    }

    @Test
    void createPosition_ShouldReturnCreatedPosition() throws Exception {
        mockMvc.perform(post("/api/portfolio-positions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPosition)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partnerIdFake", is("TEST_PARTNER")))
                .andExpect(jsonPath("$.accountIdFake", is("TEST_ACCOUNT")))
                .andExpect(jsonPath("$.valueAmount", is(50000.00)))
                .andExpect(jsonPath("$.valueCurrency", is("CHF")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void getAllPositions_ShouldReturnPagedResults() throws Exception {
        // Create test data
        PortfolioPosition saved = repository.save(testPosition);

        mockMvc.perform(get("/api/portfolio-positions")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.content[0].partnerIdFake", is("TEST_PARTNER")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void getPositionById_ShouldReturnPosition() throws Exception {
        PortfolioPosition saved = repository.save(testPosition);

        mockMvc.perform(get("/api/portfolio-positions/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.partnerIdFake", is("TEST_PARTNER")))
                .andExpect(jsonPath("$.valueAmount", is(50000.00)));
    }

    @Test
    void getPositionById_WhenNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/portfolio-positions/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePosition_ShouldReturnUpdatedPosition() throws Exception {
        PortfolioPosition saved = repository.save(testPosition);
        
        saved.setValueAmount(new BigDecimal("75000.00"));
        saved.setInstrumentNameShort("Updated Instrument");

        mockMvc.perform(put("/api/portfolio-positions/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.valueAmount", is(75000.00)))
                .andExpect(jsonPath("$.instrumentNameShort", is("Updated Instrument")));
    }

    @Test
    void patchPosition_ShouldPartiallyUpdatePosition() throws Exception {
        PortfolioPosition saved = repository.save(testPosition);

        String patchJson = """
            {
                "valueAmount": 60000.00,
                "instrumentNameShort": "Patched Instrument"
            }
            """;

        mockMvc.perform(patch("/api/portfolio-positions/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.valueAmount", is(60000.00)))
                .andExpect(jsonPath("$.instrumentNameShort", is("Patched Instrument")))
                .andExpect(jsonPath("$.partnerIdFake", is("TEST_PARTNER"))); // Unchanged field
    }

    @Test
    void deletePosition_ShouldReturnSuccessMessage() throws Exception {
        PortfolioPosition saved = repository.save(testPosition);

        mockMvc.perform(delete("/api/portfolio-positions/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Portfolio position deleted successfully")))
                .andExpect(jsonPath("$.deletedId", is(saved.getId().intValue())));

        // Verify deletion
        mockMvc.perform(get("/api/portfolio-positions/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPositionsByPartner_ShouldReturnFilteredResults() throws Exception {
        repository.save(testPosition);
        
        // Create position with different partner
        PortfolioPosition otherPosition = new PortfolioPosition();
        otherPosition.setPartnerIdFake("OTHER_PARTNER");
        otherPosition.setAccountIdFake("OTHER_ACCOUNT");
        otherPosition.setValueAmount(new BigDecimal("25000.00"));
        otherPosition.setValueCurrency("USD");
        repository.save(otherPosition);

        mockMvc.perform(get("/api/portfolio-positions/partner/{partnerIdFake}", "TEST_PARTNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].partnerIdFake", is("TEST_PARTNER")));
    }

    @Test
    void getPositionsByAccount_ShouldReturnFilteredResults() throws Exception {
        repository.save(testPosition);

        mockMvc.perform(get("/api/portfolio-positions/account/{accountIdFake}", "TEST_ACCOUNT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].accountIdFake", is("TEST_ACCOUNT")));
    }

    @Test
    void searchPositions_ShouldReturnMatchingResults() throws Exception {
        repository.save(testPosition);

        mockMvc.perform(get("/api/portfolio-positions/search")
                .param("instrumentName", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].instrumentNameShort", containsString("Test")));
    }

    @Test
    void getPortfolioSummary_ShouldReturnSummaryData() throws Exception {
        repository.save(testPosition);

        mockMvc.perform(get("/api/portfolio-positions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPositions", is(1)))
                .andExpect(jsonPath("$.assetClasses", hasItem("Equities")))
                .andExpect(jsonPath("$.currencies", hasItem("CHF")));
    }

    @Test
    void getStats_ShouldReturnDatabaseStats() throws Exception {
        repository.save(testPosition);

        mockMvc.perform(get("/api/portfolio-positions/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords", is(1)))
                .andExpect(jsonPath("$.databaseStatus", is("populated")))
                .andExpect(jsonPath("$.uniqueAccounts", is(1)))
                .andExpect(jsonPath("$.uniquePartners", is(1)));
    }
}
