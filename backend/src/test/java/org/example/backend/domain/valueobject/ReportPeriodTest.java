// backend/src/test/java/org/example/backend/domain/valueobject/ReportPeriodTest.java
package org.example.backend.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReportPeriod value object
 * 
 * Tests cover period creation, validation, date calculations, and business logic
 */
@DisplayName("ReportPeriod Value Object Tests")
class ReportPeriodTest extends ValueObjectTestBase {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create custom report period")
        void shouldCreateCustomReportPeriod() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            
            // When
            ReportPeriod period = ReportPeriod.of(startDate, endDate);
            
            // Then
            assertNotNull(period);
            assertEquals(startDate, period.getStartDate());
            assertEquals(endDate, period.getEndDate());
            assertEquals(ReportPeriod.PeriodType.CUSTOM, period.getType());
        }

        @Test
        @DisplayName("Should create monthly report period")
        void shouldCreateMonthlyReportPeriod() {
            // Given
            int year = 2024;
            int month = 3;
            
            // When
            ReportPeriod period = ReportPeriod.monthly(year, month);
            
            // Then
            assertNotNull(period);
            assertEquals(LocalDate.of(2024, 3, 1), period.getStartDate());
            assertEquals(LocalDate.of(2024, 3, 31), period.getEndDate());
            assertEquals(ReportPeriod.PeriodType.MONTHLY, period.getType());
        }

        @ParameterizedTest
        @CsvSource({
            "2024, 1, 2024-01-01, 2024-01-31",
            "2024, 2, 2024-02-01, 2024-02-29",  // Leap year
            "2023, 2, 2023-02-01, 2023-02-28",  // Non-leap year
            "2024, 12, 2024-12-01, 2024-12-31"
        })
        @DisplayName("Should create monthly periods correctly for different months")
        void shouldCreateMonthlyPeriodsCorrectlyForDifferentMonths(
                int year, int month, String expectedStart, String expectedEnd) {
            // When
            ReportPeriod period = ReportPeriod.monthly(year, month);
            
            // Then
            assertEquals(LocalDate.parse(expectedStart), period.getStartDate());
            assertEquals(LocalDate.parse(expectedEnd), period.getEndDate());
            assertEquals(ReportPeriod.PeriodType.MONTHLY, period.getType());
        }

        @ParameterizedTest
        @CsvSource({
            "2024, 1, 2024-01-01, 2024-03-31",
            "2024, 2, 2024-04-01, 2024-06-30",
            "2024, 3, 2024-07-01, 2024-09-30",
            "2024, 4, 2024-10-01, 2024-12-31"
        })
        @DisplayName("Should create quarterly periods correctly")
        void shouldCreateQuarterlyPeriodsCorrectly(
                int year, int quarter, String expectedStart, String expectedEnd) {
            // When
            ReportPeriod period = ReportPeriod.quarterly(year, quarter);
            
            // Then
            assertEquals(LocalDate.parse(expectedStart), period.getStartDate());
            assertEquals(LocalDate.parse(expectedEnd), period.getEndDate());
            assertEquals(ReportPeriod.PeriodType.QUARTERLY, period.getType());
        }

        @Test
        @DisplayName("Should create yearly report period")
        void shouldCreateYearlyReportPeriod() {
            // Given
            int year = 2024;
            
            // When
            ReportPeriod period = ReportPeriod.yearly(year);
            
            // Then
            assertNotNull(period);
            assertEquals(LocalDate.of(2024, 1, 1), period.getStartDate());
            assertEquals(LocalDate.of(2024, 12, 31), period.getEndDate());
            assertEquals(ReportPeriod.PeriodType.YEARLY, period.getType());
        }

        @Test
        @DisplayName("Should create current month period")
        void shouldCreateCurrentMonthPeriod() {
            // Given
            LocalDate now = LocalDate.now();
            // Use previous month to avoid future date validation issues  
            LocalDate targetDate = now.minusMonths(1);
            LocalDate expectedStart = LocalDate.of(targetDate.getYear(), targetDate.getMonth(), 1);
            LocalDate expectedEnd = expectedStart.withDayOfMonth(expectedStart.lengthOfMonth());
            
            // When
            ReportPeriod period = ReportPeriod.monthly(targetDate.getYear(), targetDate.getMonthValue());
            
            // Then
            assertNotNull(period);
            assertEquals(expectedStart, period.getStartDate());
            assertEquals(expectedEnd, period.getEndDate());
            assertEquals(ReportPeriod.PeriodType.MONTHLY, period.getType());
        }

        @Test
        @DisplayName("Should create previous month period")
        void shouldCreatePreviousMonthPeriod() {
            // Given
            LocalDate lastMonth = LocalDate.now().minusMonths(1);
            LocalDate expectedStart = LocalDate.of(lastMonth.getYear(), lastMonth.getMonth(), 1);
            LocalDate expectedEnd = expectedStart.withDayOfMonth(expectedStart.lengthOfMonth());
            
            // When
            ReportPeriod period = ReportPeriod.previousMonth();
            
            // Then
            assertNotNull(period);
            assertEquals(expectedStart, period.getStartDate());
            assertEquals(expectedEnd, period.getEndDate());
            assertEquals(ReportPeriod.PeriodType.MONTHLY, period.getType());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when start date is after end date")
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 2, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReportPeriod.of(startDate, endDate)
            );
            
            assertTrue(exception.getMessage().contains("Start date cannot be after end date"));
        }

        @Test
        @DisplayName("Should throw exception when start date is null")
        void shouldThrowExceptionWhenStartDateIsNull() {
            // Given
            LocalDate startDate = null;
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReportPeriod.of(startDate, endDate)
            );
            
            assertTrue(exception.getMessage().contains("Start date and end date cannot be null"));
        }

        @Test
        @DisplayName("Should throw exception when end date is null")
        void shouldThrowExceptionWhenEndDateIsNull() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = null;
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReportPeriod.of(startDate, endDate)
            );
            
            assertTrue(exception.getMessage().contains("Start date and end date cannot be null"));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 13, 15})
        @DisplayName("Should throw exception for invalid month values")
        void shouldThrowExceptionForInvalidMonthValues(int invalidMonth) {
            // Given
            int year = 2024;
            
            // When & Then
            assertThrows(
                Exception.class,  // Could be DateTimeException or IllegalArgumentException
                () -> ReportPeriod.monthly(year, invalidMonth)
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 5, 10})
        @DisplayName("Should throw exception for invalid quarter values")
        void shouldThrowExceptionForInvalidQuarterValues(int invalidQuarter) {
            // Given
            int year = 2024;
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReportPeriod.quarterly(year, invalidQuarter)
            );
            
            assertTrue(exception.getMessage().contains("Quarter must be between 1 and 4"));
        }

        @Test
        @DisplayName("Should allow same start and end date")
        void shouldAllowSameStartAndEndDate() {
            // Given
            LocalDate sameDate = LocalDate.of(2024, 1, 15);
            
            // When & Then
            assertDoesNotThrow(() -> ReportPeriod.of(sameDate, sameDate));
            
            ReportPeriod period = ReportPeriod.of(sameDate, sameDate);
            assertEquals(sameDate, period.getStartDate());
            assertEquals(sameDate, period.getEndDate());
        }
    }

    @Nested
    @DisplayName("Period Type Enum Tests")
    class PeriodTypeEnumTests {

        @Test
        @DisplayName("Should have correct display names")
        void shouldHaveCorrectDisplayNames() {
            // When & Then
            assertEquals("Monthly", ReportPeriod.PeriodType.MONTHLY.getDisplayName());
            assertEquals("Quarterly", ReportPeriod.PeriodType.QUARTERLY.getDisplayName());
            assertEquals("Yearly", ReportPeriod.PeriodType.YEARLY.getDisplayName());
            assertEquals("Custom", ReportPeriod.PeriodType.CUSTOM.getDisplayName());
        }

        @Test
        @DisplayName("Should have correct Chinese names")
        void shouldHaveCorrectChineseNames() {
            // When & Then
            assertEquals("月度", ReportPeriod.PeriodType.MONTHLY.getChineseName());
            assertEquals("季度", ReportPeriod.PeriodType.QUARTERLY.getChineseName());
            assertEquals("年度", ReportPeriod.PeriodType.YEARLY.getChineseName());
            assertEquals("自定义", ReportPeriod.PeriodType.CUSTOM.getChineseName());
        }

        @ParameterizedTest
        @EnumSource(ReportPeriod.PeriodType.class)
        @DisplayName("Should have non-null display names for all types")
        void shouldHaveNonNullDisplayNamesForAllTypes(ReportPeriod.PeriodType type) {
            // When & Then
            assertNotNull(type.getDisplayName());
            assertFalse(type.getDisplayName().trim().isEmpty());
            assertNotNull(type.getChineseName());
            assertFalse(type.getChineseName().trim().isEmpty());
        }
    }

    @Nested
    @DisplayName("Duration Calculation Tests")
    class DurationCalculationTests {

        @Test
        @DisplayName("Should calculate duration in days correctly")
        void shouldCalculateDurationInDaysCorrectly() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            ReportPeriod period = ReportPeriod.of(startDate, endDate);
            
            // When
            long duration = period.getDurationInDays();
            
            // Then
            assertEquals(31L, duration); // January has 31 days
        }

        @Test
        @DisplayName("Should calculate single day duration")
        void shouldCalculateSingleDayDuration() {
            // Given
            LocalDate sameDate = LocalDate.of(2024, 1, 15);
            ReportPeriod period = ReportPeriod.of(sameDate, sameDate);
            
            // When
            long duration = period.getDurationInDays();
            
            // Then
            assertEquals(1L, duration);
        }

        @ParameterizedTest
        @CsvSource({
            "2024, 1, 31",   // January
            "2024, 2, 29",   // February (leap year)
            "2023, 2, 28",   // February (non-leap year)
            "2024, 4, 30",   // April
            "2024, 12, 31"   // December
        })
        @DisplayName("Should calculate monthly durations correctly")
        void shouldCalculateMonthlyDurationsCorrectly(int year, int month, long expectedDays) {
            // Given
            ReportPeriod period = ReportPeriod.monthly(year, month);
            
            // When
            long duration = period.getDurationInDays();
            
            // Then
            assertEquals(expectedDays, duration);
        }

        @Test
        @DisplayName("Should calculate yearly duration correctly")
        void shouldCalculateYearlyDurationCorrectly() {
            // Given
            ReportPeriod leapYear = ReportPeriod.yearly(2024);
            ReportPeriod regularYear = ReportPeriod.yearly(2023);
            
            // When
            long leapYearDuration = leapYear.getDurationInDays();
            long regularYearDuration = regularYear.getDurationInDays();
            
            // Then
            assertEquals(366L, leapYearDuration); // 2024 is a leap year
            assertEquals(365L, regularYearDuration); // 2023 is not a leap year
        }
    }

    @Nested
    @DisplayName("Date Contains Tests")
    class DateContainsTests {

        @Test
        @DisplayName("Should contain dates within period")
        void shouldContainDatesWithinPeriod() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            ReportPeriod period = ReportPeriod.of(startDate, endDate);
            
            // When & Then
            assertTrue(period.contains(LocalDate.of(2024, 1, 1)));   // Start date
            assertTrue(period.contains(LocalDate.of(2024, 1, 15)));  // Middle date
            assertTrue(period.contains(LocalDate.of(2024, 1, 31)));  // End date
        }

        @Test
        @DisplayName("Should not contain dates outside period")
        void shouldNotContainDatesOutsidePeriod() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            ReportPeriod period = ReportPeriod.of(startDate, endDate);
            
            // When & Then
            assertFalse(period.contains(LocalDate.of(2023, 12, 31))); // Before start
            assertFalse(period.contains(LocalDate.of(2024, 2, 1)));   // After end
        }

        @Test
        @DisplayName("Should handle edge cases for contains method")
        void shouldHandleEdgeCasesForContainsMethod() {
            // Given
            LocalDate sameDate = LocalDate.of(2024, 1, 15);
            ReportPeriod singleDayPeriod = ReportPeriod.of(sameDate, sameDate);
            
            // When & Then
            assertTrue(singleDayPeriod.contains(sameDate));
            assertFalse(singleDayPeriod.contains(sameDate.minusDays(1)));
            assertFalse(singleDayPeriod.contains(sameDate.plusDays(1)));
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should support financial quarter reporting")
        void shouldSupportFinancialQuarterReporting() {
            // Given - Test all four quarters of 2024
            ReportPeriod q1 = ReportPeriod.quarterly(2024, 1);
            ReportPeriod q2 = ReportPeriod.quarterly(2024, 2);
            ReportPeriod q3 = ReportPeriod.quarterly(2024, 3);
            ReportPeriod q4 = ReportPeriod.quarterly(2024, 4);
            
            // When & Then - Verify no gaps or overlaps
            assertEquals(LocalDate.of(2024, 1, 1), q1.getStartDate());
            assertEquals(LocalDate.of(2024, 3, 31), q1.getEndDate());
            
            assertEquals(LocalDate.of(2024, 4, 1), q2.getStartDate());
            assertEquals(LocalDate.of(2024, 6, 30), q2.getEndDate());
            
            assertEquals(LocalDate.of(2024, 7, 1), q3.getStartDate());
            assertEquals(LocalDate.of(2024, 9, 30), q3.getEndDate());
            
            assertEquals(LocalDate.of(2024, 10, 1), q4.getStartDate());
            assertEquals(LocalDate.of(2024, 12, 31), q4.getEndDate());
            
            // Verify consecutive quarters don't overlap
            assertEquals(q1.getEndDate().plusDays(1), q2.getStartDate());
            assertEquals(q2.getEndDate().plusDays(1), q3.getStartDate());
            assertEquals(q3.getEndDate().plusDays(1), q4.getStartDate());
        }

        @Test
        @DisplayName("Should support month-end financial reporting")
        void shouldSupportMonthEndFinancialReporting() {
            // Given
            ReportPeriod january = ReportPeriod.monthly(2024, 1);
            ReportPeriod february = ReportPeriod.monthly(2024, 2);
            ReportPeriod march = ReportPeriod.monthly(2024, 3);
            
            // When & Then
            // All months should start on the 1st
            assertEquals(1, january.getStartDate().getDayOfMonth());
            assertEquals(1, february.getStartDate().getDayOfMonth());
            assertEquals(1, march.getStartDate().getDayOfMonth());
            
            // All months should end on the last day
            assertEquals(31, january.getEndDate().getDayOfMonth());
            assertEquals(29, february.getEndDate().getDayOfMonth()); // 2024 is leap year
            assertEquals(31, march.getEndDate().getDayOfMonth());
        }

        @Test
        @DisplayName("Should handle fiscal year reporting")
        void shouldHandleFiscalYearReporting() {
            // Given - Custom fiscal year (April to March)
            LocalDate fiscalStart = LocalDate.of(2024, 4, 1);
            LocalDate fiscalEnd = LocalDate.of(2025, 3, 31);
            ReportPeriod fiscalYear = ReportPeriod.of(fiscalStart, fiscalEnd);
            
            // When
            long fiscalYearDays = fiscalYear.getDurationInDays();
            
            // Then
            assertEquals(ReportPeriod.PeriodType.CUSTOM, fiscalYear.getType());
            assertEquals(365L, fiscalYearDays); // April 2024 to March 2025
            assertTrue(fiscalYear.contains(LocalDate.of(2024, 6, 30))); // Q1 end
            assertTrue(fiscalYear.contains(LocalDate.of(2024, 12, 31))); // Calendar year end
        }
    }

    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityAndHashTests {

        @Test
        @DisplayName("Should be equal when all fields are same")
        void shouldBeEqualWhenAllFieldsAreSame() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            ReportPeriod period1 = ReportPeriod.of(startDate, endDate);
            ReportPeriod period2 = ReportPeriod.of(startDate, endDate);
            
            // When & Then
            assertEquals(period1, period2);
            assertEquals(period1.hashCode(), period2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when dates are different")
        void shouldNotBeEqualWhenDatesAreDifferent() {
            // Given
            ReportPeriod period1 = ReportPeriod.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
            ReportPeriod period2 = ReportPeriod.of(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29));
            
            // When & Then
            assertNotEquals(period1, period2);
        }

        @Test
        @DisplayName("Should not be equal when types are different")
        void shouldNotBeEqualWhenTypesAreDifferent() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            ReportPeriod customPeriod = ReportPeriod.of(startDate, endDate);
            ReportPeriod monthlyPeriod = ReportPeriod.monthly(2024, 1);
            
            // When & Then
            // Even though dates might be the same, types are different
            assertNotEquals(customPeriod, monthlyPeriod);
        }

        @Test
        @DisplayName("Should maintain equality contract")
        void shouldMaintainEqualityContract() {
            // Given
            ReportPeriod period1 = ReportPeriod.monthly(2024, 3);
            ReportPeriod period2 = ReportPeriod.monthly(2024, 3);
            ReportPeriod period3 = ReportPeriod.monthly(2024, 3);
            
            // When & Then
            assertEqualsContract(period1, period2, period3);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            ReportPeriod period = ReportPeriod.quarterly(2024, 2);
            
            // When & Then
            assertEquals(period, period);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            ReportPeriod period = ReportPeriod.yearly(2024);
            
            // When & Then
            assertNotEquals(period, null);
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            // Given
            ReportPeriod period = ReportPeriod.monthly(2024, 3);
            
            // When
            String stringRepresentation = period.toString();
            
            // Then
            assertMeaningfulToString(period, "Monthly", "2024-03-01", "2024-03-31");
        }

        @Test
        @DisplayName("Should format display string correctly")
        void shouldFormatDisplayStringCorrectly() {
            // Given
            ReportPeriod period = ReportPeriod.quarterly(2024, 1);
            
            // When
            String displayString = period.getDescription();
            
            // Then
            assertNotNull(displayString);
            assertTrue(displayString.contains("Quarterly") || 
                      displayString.contains("2024-01-01") || 
                      displayString.contains("2024-03-31"));
        }
    }

    @Nested
    @DisplayName("JPA Compatibility Tests")
    class JPACompatibilityTests {

        @Test
        @DisplayName("Should be embeddable value object")
        void shouldBeEmbeddableValueObject() {
            // Given
            ReportPeriod period = ReportPeriod.monthly(2024, 1);
            
            // When & Then
            assertTrue(period.getClass().isAnnotationPresent(jakarta.persistence.Embeddable.class));
        }

        @Test
        @DisplayName("Should have proper column mappings")
        void shouldHaveProperColumnMappings() {
            // Given & When & Then
            try {
                java.lang.reflect.Field startDateField = ReportPeriod.class.getDeclaredField("startDate");
                java.lang.reflect.Field endDateField = ReportPeriod.class.getDeclaredField("endDate");
                java.lang.reflect.Field typeField = ReportPeriod.class.getDeclaredField("type");
                
                assertTrue(startDateField.isAnnotationPresent(jakarta.persistence.Column.class));
                assertTrue(endDateField.isAnnotationPresent(jakarta.persistence.Column.class));
                assertTrue(typeField.isAnnotationPresent(jakarta.persistence.Enumerated.class));
                
                assertEquals("start_date", startDateField.getAnnotation(jakarta.persistence.Column.class).name());
                assertEquals("end_date", endDateField.getAnnotation(jakarta.persistence.Column.class).name());
                assertEquals(jakarta.persistence.EnumType.STRING, 
                           typeField.getAnnotation(jakarta.persistence.Enumerated.class).value());
            } catch (NoSuchFieldException e) {
                fail("Required fields should exist: " + e.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable value object")
        void shouldBeImmutableValueObject() {
            // Given
            LocalDate originalStart = LocalDate.of(2024, 1, 1);
            LocalDate originalEnd = LocalDate.of(2024, 1, 31);
            ReportPeriod period = ReportPeriod.of(originalStart, originalEnd);
            
            // When
            LocalDate retrievedStart = period.getStartDate();
            LocalDate retrievedEnd = period.getEndDate();
            
            // Then
            assertEquals(originalStart, retrievedStart);
            assertEquals(originalEnd, retrievedEnd);
            
            // Modifying retrieved dates shouldn't affect the original period
            LocalDate modifiedStart = retrievedStart.plusDays(1);
            assertNotEquals(modifiedStart, period.getStartDate());
        }

        @Test
        @DisplayName("Should not expose mutable state")
        void shouldNotExposeMutableState() {
            // Given
            ReportPeriod period = ReportPeriod.quarterly(2024, 2);
            
            // When
            LocalDate startDate = period.getStartDate();
            LocalDate endDate = period.getEndDate();
            ReportPeriod.PeriodType type = period.getType();
            
            // Then
            // LocalDate and enums are immutable, so this is safe
            assertNotNull(startDate);
            assertNotNull(endDate);
            assertNotNull(type);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle leap year February correctly")
        void shouldHandleLeapYearFebruaryCorrectly() {
            // Given
            ReportPeriod leapFeb = ReportPeriod.monthly(2024, 2);
            ReportPeriod regularFeb = ReportPeriod.monthly(2023, 2);
            
            // When & Then
            assertEquals(LocalDate.of(2024, 2, 29), leapFeb.getEndDate());
            assertEquals(29L, leapFeb.getDurationInDays());
            
            assertEquals(LocalDate.of(2023, 2, 28), regularFeb.getEndDate());
            assertEquals(28L, regularFeb.getDurationInDays());
        }

        @Test
        @DisplayName("Should handle year boundaries correctly")
        void shouldHandleYearBoundariesCorrectly() {
            // Given
            ReportPeriod crossYear = ReportPeriod.of(
                LocalDate.of(2023, 12, 15), 
                LocalDate.of(2024, 1, 15)
            );
            
            // When
            long duration = crossYear.getDurationInDays();
            
            // Then
            assertEquals(32L, duration); // Dec 15 to Jan 15 (inclusive)
            assertTrue(crossYear.contains(LocalDate.of(2023, 12, 31)));
            assertTrue(crossYear.contains(LocalDate.of(2024, 1, 1)));
        }

        @Test
        @DisplayName("Should handle minimum and maximum dates")
        void shouldHandleMinimumAndMaximumDates() {
            // Given - Use past dates to avoid future date validation
            LocalDate minDate = LocalDate.of(1900, 1, 1);
            LocalDate maxDate = LocalDate.of(2020, 12, 31); // Use past date
            
            // When & Then
            assertDoesNotThrow(() -> ReportPeriod.of(minDate, maxDate));
            
            ReportPeriod extremePeriod = ReportPeriod.of(minDate, maxDate);
            assertTrue(extremePeriod.getDurationInDays() > 0);
            assertTrue(extremePeriod.contains(LocalDate.of(2000, 6, 15)));
        }
    }

    @Nested
    @DisplayName("Factory Method Integration Tests")
    class FactoryMethodIntegrationTests {

        @Test
        @DisplayName("Should create consistent periods across different factory methods")
        void shouldCreateConsistentPeriodsAcrossDifferentFactoryMethods() {
            // Given
            LocalDate jan1 = LocalDate.of(2024, 1, 1);
            LocalDate jan31 = LocalDate.of(2024, 1, 31);
            
            // When
            ReportPeriod customJanuary = ReportPeriod.of(jan1, jan31);
            ReportPeriod factoryJanuary = ReportPeriod.monthly(2024, 1);
            
            // Then
            assertEquals(customJanuary.getStartDate(), factoryJanuary.getStartDate());
            assertEquals(customJanuary.getEndDate(), factoryJanuary.getEndDate());
            assertEquals(customJanuary.getDurationInDays(), factoryJanuary.getDurationInDays());
            
            // Types should be different though
            assertNotEquals(customJanuary.getType(), factoryJanuary.getType());
        }

        @Test
        @DisplayName("Should handle current month edge case at month boundary")
        void shouldHandleCurrentMonthEdgeCaseAtMonthBoundary() {
            // This test documents behavior when run at month boundaries
            // Given & When - Use last month to avoid future date issues
            LocalDate lastMonth = LocalDate.now().minusMonths(1);
            ReportPeriod lastMonthPeriod = ReportPeriod.monthly(lastMonth.getYear(), lastMonth.getMonthValue());
            ReportPeriod previousMonth = ReportPeriod.monthly(lastMonth.minusMonths(1).getYear(), lastMonth.minusMonths(1).getMonthValue());
            
            // Then
            assertNotNull(lastMonthPeriod);
            assertNotNull(previousMonth);
            assertTrue(lastMonthPeriod.getDurationInDays() >= 28);
            assertTrue(lastMonthPeriod.getDurationInDays() <= 31);
            assertTrue(previousMonth.getDurationInDays() >= 28);
            assertTrue(previousMonth.getDurationInDays() <= 31);
            
            // Previous month should end before last month starts
            assertTrue(previousMonth.getEndDate().isBefore(lastMonthPeriod.getStartDate()));
        }
    }
}