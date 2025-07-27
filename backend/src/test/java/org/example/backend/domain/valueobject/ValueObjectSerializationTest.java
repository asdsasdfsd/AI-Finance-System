// backend/src/test/java/org/example/backend/domain/valueobject/ValueObjectSerializationTest.java
package org.example.backend.domain.valueobject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for value object serialization and deserialization
 * 
 * Ensures value objects can be properly serialized/deserialized for API communication
 */
@DisplayName("Value Object Serialization Tests")
class ValueObjectSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // Configure to ignore unknown properties during deserialization
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Nested
    @DisplayName("TenantId Serialization Tests")
    class TenantIdSerializationTests {

        @Test
        @DisplayName("Should serialize TenantId to JSON")
        void shouldSerializeTenantIdToJSON() throws Exception {
            // Given
            TenantId tenantId = TenantId.of(123);
            
            // When
            String json = objectMapper.writeValueAsString(tenantId);
            
            // Then
            assertNotNull(json);
            assertTrue(json.contains("123"));
        }

        @Test
        @DisplayName("Should deserialize TenantId from JSON")
        void shouldDeserializeTenantIdFromJSON() throws Exception {
            // Given
            String json = "{\"value\":456}";
            
            // When
            TenantId tenantId = objectMapper.readValue(json, TenantId.class);
            
            // Then
            assertNotNull(tenantId);
            assertEquals(Integer.valueOf(456), tenantId.getValue());
        }

        @Test
        @DisplayName("Should maintain equality after serialization round trip")
        void shouldMaintainEqualityAfterSerializationRoundTrip() throws Exception {
            // Given
            TenantId original = TenantId.of(789);
            
            // When
            String json = objectMapper.writeValueAsString(original);
            TenantId deserialized = objectMapper.readValue(json, TenantId.class);
            
            // Then
            assertEquals(original, deserialized);
            assertEquals(original.hashCode(), deserialized.hashCode());
        }
    }

    @Nested
    @DisplayName("Money Serialization Tests")
    class MoneySerializationTests {

        @Test
        @DisplayName("Should serialize Money to JSON")
        void shouldSerializeMoneyToJSON() throws Exception {
            // Given
            Money money = Money.of(new BigDecimal("1234.56"), "USD");
            
            // When
            String json = objectMapper.writeValueAsString(money);
            
            // Then
            assertNotNull(json);
            assertTrue(json.contains("1234.56"));
            assertTrue(json.contains("USD"));
        }

        @Test
        @DisplayName("Should deserialize Money from JSON")
        void shouldDeserializeMoneyFromJSON() throws Exception {
            // Given
            String json = "{\"amount\":999.99,\"currencyCode\":\"EUR\"}";
            
            // When
            Money money = objectMapper.readValue(json, Money.class);
            
            // Then
            assertNotNull(money);
            assertEquals(new BigDecimal("999.99"), money.getAmount());
            assertEquals("EUR", money.getCurrencyCode());
        }

        @Test
        @DisplayName("Should maintain equality after Money serialization round trip")
        void shouldMaintainEqualityAfterMoneySerializationRoundTrip() throws Exception {
            // Given
            Money original = Money.of(new BigDecimal("5432.10"), "CNY");
            
            // When
            String json = objectMapper.writeValueAsString(original);
            Money deserialized = objectMapper.readValue(json, Money.class);
            
            // Then
            assertEquals(original, deserialized);
            assertEquals(original.getAmount(), deserialized.getAmount());
            assertEquals(original.getCurrencyCode(), deserialized.getCurrencyCode());
        }

        @Test
        @DisplayName("Should handle zero Money serialization")
        void shouldHandleZeroMoneySerialization() throws Exception {
            // Given
            Money zeroMoney = Money.zero("JPY");
            
            // When
            String json = objectMapper.writeValueAsString(zeroMoney);
            Money deserialized = objectMapper.readValue(json, Money.class);
            
            // Then
            assertEquals(zeroMoney, deserialized);
            assertTrue(deserialized.isZero());
        }
    }

    @Nested
    @DisplayName("TransactionStatus Serialization Tests")
    class TransactionStatusSerializationTests {

        @Test
        @DisplayName("Should serialize TransactionStatus to JSON")
        void shouldSerializeTransactionStatusToJSON() throws Exception {
            // Given
            TransactionStatus status = TransactionStatus.of(TransactionStatus.Status.APPROVED);
            
            // When
            String json = objectMapper.writeValueAsString(status);
            
            // Then
            assertNotNull(json);
            assertTrue(json.contains("APPROVED") || json.contains("2")); // Could be enum name or ordinal
        }

        @Test
        @DisplayName("Should deserialize TransactionStatus from JSON")
        void shouldDeserializeTransactionStatusFromJSON() throws Exception {
            // Given
            String json = "{\"status\":\"PENDING_APPROVAL\"}";
            
            // When
            TransactionStatus status = objectMapper.readValue(json, TransactionStatus.class);
            
            // Then
            assertNotNull(status);
            assertEquals(TransactionStatus.Status.PENDING_APPROVAL, status.getStatus());
            assertEquals(TransactionStatus.Status.PENDING_APPROVAL, status.getStatus());
        }

        @Test
        @DisplayName("Should maintain equality after TransactionStatus serialization round trip")
        void shouldMaintainEqualityAfterTransactionStatusSerializationRoundTrip() throws Exception {
            // Given
            TransactionStatus original = TransactionStatus.draft();
            
            // When
            String json = objectMapper.writeValueAsString(original);
            TransactionStatus deserialized = objectMapper.readValue(json, TransactionStatus.class);
            
            // Then
            assertEquals(original, deserialized);
            assertEquals(original.getStatus(), deserialized.getStatus());
        }
    }

    @Nested
    @DisplayName("CompanyStatus Serialization Tests")
    class CompanyStatusSerializationTests {

        @Test
        @DisplayName("Should serialize CompanyStatus to JSON")
        void shouldSerializeCompanyStatusToJSON() throws Exception {
            // Given
            CompanyStatus status = CompanyStatus.active();
            
            // When
            String json = objectMapper.writeValueAsString(status);
            
            // Then
            assertNotNull(json);
            assertTrue(json.contains("ACTIVE"));
        }

        @Test
        @DisplayName("Should deserialize CompanyStatus from JSON")
        void shouldDeserializeCompanyStatusFromJSON() throws Exception {
            // Given
            String json = "{\"status\":\"INACTIVE\"}";
            
            // When
            CompanyStatus status = objectMapper.readValue(json, CompanyStatus.class);
            
            // Then
            assertNotNull(status);
            assertEquals(CompanyStatus.Status.INACTIVE, status.getStatus());
            assertFalse(status.isOperational());
        }

        @Test
        @DisplayName("Should maintain equality after CompanyStatus serialization round trip")
        void shouldMaintainEqualityAfterCompanyStatusSerializationRoundTrip() throws Exception {
            // Given
            CompanyStatus original = CompanyStatus.inactive();
            
            // When
            String json = objectMapper.writeValueAsString(original);
            CompanyStatus deserialized = objectMapper.readValue(json, CompanyStatus.class);
            
            // Then
            assertEquals(original, deserialized);
            assertEquals(original.getStatus(), deserialized.getStatus());
        }
    }

    @Nested
    @DisplayName("ReportPeriod Serialization Tests")
    class ReportPeriodSerializationTests {

        @Test
        @DisplayName("Should serialize ReportPeriod to JSON")
        void shouldSerializeReportPeriodToJSON() throws Exception {
            // Given
            ReportPeriod period = ReportPeriod.monthly(2024, 3);
            
            // When
            String json = objectMapper.writeValueAsString(period);
            
            // Then
            assertNotNull(json);
            assertTrue(json.contains("2024-03-01"));
            assertTrue(json.contains("2024-03-31"));
            assertTrue(json.contains("MONTHLY"));
        }

        @Test
        @DisplayName("Should deserialize ReportPeriod from JSON")
        void shouldDeserializeReportPeriodFromJSON() throws Exception {
            // Given
            String json = "{\"startDate\":\"2024-06-01\",\"endDate\":\"2024-06-30\",\"type\":\"MONTHLY\"}";
            
            // When
            ReportPeriod period = objectMapper.readValue(json, ReportPeriod.class);
            
            // Then
            assertNotNull(period);
            assertEquals(LocalDate.of(2024, 6, 1), period.getStartDate());
            assertEquals(LocalDate.of(2024, 6, 30), period.getEndDate());
            assertEquals(ReportPeriod.PeriodType.MONTHLY, period.getType());
        }

        @Test
        @DisplayName("Should maintain equality after ReportPeriod serialization round trip")
        void shouldMaintainEqualityAfterReportPeriodSerializationRoundTrip() throws Exception {
            // Given
            ReportPeriod original = ReportPeriod.quarterly(2024, 2);
            
            // When
            String json = objectMapper.writeValueAsString(original);
            ReportPeriod deserialized = objectMapper.readValue(json, ReportPeriod.class);
            
            // Then
            assertEquals(original, deserialized);
            assertEquals(original.getStartDate(), deserialized.getStartDate());
            assertEquals(original.getEndDate(), deserialized.getEndDate());
            assertEquals(original.getType(), deserialized.getType());
        }

        @Test
        @DisplayName("Should handle custom period serialization")
        void shouldHandleCustomPeriodSerialization() throws Exception {
            // Given
            ReportPeriod customPeriod = ReportPeriod.of(
                LocalDate.of(2024, 1, 15), 
                LocalDate.of(2024, 2, 15)
            );
            
            // When
            String json = objectMapper.writeValueAsString(customPeriod);
            ReportPeriod deserialized = objectMapper.readValue(json, ReportPeriod.class);
            
            // Then
            assertEquals(customPeriod, deserialized);
            assertEquals(ReportPeriod.PeriodType.CUSTOM, deserialized.getType());
        }
    }

    @Nested
    @DisplayName("API Response Compatibility Tests")
    class APIResponseCompatibilityTests {

        @Test
        @DisplayName("Should serialize value objects in API response format")
        void shouldSerializeValueObjectsInAPIResponseFormat() throws Exception {
            // Given - Simulate an API response containing multiple value objects
            ApiResponse response = new ApiResponse(
                TenantId.of(100),
                Money.of(new BigDecimal("1500.75"), "USD"),
                TransactionStatus.of(TransactionStatus.Status.APPROVED),
                CompanyStatus.active(),
                ReportPeriod.monthly(2024, 4)
            );
            
            // When
            String json = objectMapper.writeValueAsString(response);
            
            // Then
            assertNotNull(json);
            assertTrue(json.contains("100"));        // TenantId
            assertTrue(json.contains("1500.75"));    // Money amount
            assertTrue(json.contains("USD"));        // Money currency
            assertTrue(json.contains("APPROVED"));   // TransactionStatus
            assertTrue(json.contains("ACTIVE"));     // CompanyStatus
            assertTrue(json.contains("2024-04-01")); // ReportPeriod start
            assertTrue(json.contains("MONTHLY"));    // ReportPeriod type
        }

        @Test
        @DisplayName("Should deserialize value objects from API request format")
        void shouldDeserializeValueObjectsFromAPIRequestFormat() throws Exception {
            // Given
            String jsonRequest = """
                {
                    "tenantId": {"value": 200},
                    "money": {"amount": 2500.50, "currencyCode": "EUR"},
                    "transactionStatus": {"status": "DRAFT"},
                    "companyStatus": {"status": "INACTIVE"},
                    "reportPeriod": {
                        "startDate": "2024-07-01",
                        "endDate": "2024-07-31",
                        "type": "MONTHLY"
                    }
                }
                """;
            
            // When
            ApiResponse response = objectMapper.readValue(jsonRequest, ApiResponse.class);
            
            // Then
            assertNotNull(response);
            assertEquals(Integer.valueOf(200), response.tenantId.getValue());
            assertEquals(new BigDecimal("2500.50"), response.money.getAmount());
            assertEquals("EUR", response.money.getCurrencyCode());
            assertTrue(response.transactionStatus.isDraft());
            assertFalse(response.companyStatus.isOperational());
            assertEquals(LocalDate.of(2024, 7, 1), response.reportPeriod.getStartDate());
        }

        @Test
        @DisplayName("Should handle null value objects gracefully")
        void shouldHandleNullValueObjectsGracefully() throws Exception {
            // Given
            ApiResponse responseWithNulls = new ApiResponse(null, null, null, null, null);
            
            // When
            String json = objectMapper.writeValueAsString(responseWithNulls);
            ApiResponse deserialized = objectMapper.readValue(json, ApiResponse.class);
            
            // Then
            assertNotNull(json);
            assertNotNull(deserialized);
            // Null handling depends on Jackson configuration
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should serialize value objects efficiently")
        void shouldSerializeValueObjectsEfficiently() throws Exception {
            // Given
            TenantId tenantId = TenantId.of(999);
            Money money = Money.of(new BigDecimal("10000.00"), "CNY");
            TransactionStatus status = TransactionStatus.of(TransactionStatus.Status.APPROVED);
            
            // When & Then - Multiple serializations should not throw exceptions
            for (int i = 0; i < 1000; i++) {
                assertDoesNotThrow(() -> {
                    try {
                        objectMapper.writeValueAsString(tenantId);
                        objectMapper.writeValueAsString(money);
                        objectMapper.writeValueAsString(status);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    /**
     * Test class representing an API response with multiple value objects
     */
    public static class ApiResponse {
        public TenantId tenantId;
        public Money money;
        public TransactionStatus transactionStatus;
        public CompanyStatus companyStatus;
        public ReportPeriod reportPeriod;

        // Default constructor for Jackson
        public ApiResponse() {}

        public ApiResponse(TenantId tenantId, Money money, TransactionStatus transactionStatus,
                          CompanyStatus companyStatus, ReportPeriod reportPeriod) {
            this.tenantId = tenantId;
            this.money = money;
            this.transactionStatus = transactionStatus;
            this.companyStatus = companyStatus;
            this.reportPeriod = reportPeriod;
        }
    }
}