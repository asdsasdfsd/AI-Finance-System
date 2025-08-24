// backend/src/main/java/org/example/backend/controller/FinancialGroupingController.java
// FIXED Financial Grouping Controller - Complete and properly structured

package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.backend.application.dto.GenerateReportCommand;
import org.example.backend.domain.valueobject.ReportType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.example.backend.application.service.ReportApplicationService;
import org.example.backend.application.dto.GenerateReportCommand;
import org.example.backend.domain.valueobject.ReportType;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/financial-grouping")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class FinancialGroupingController {

    // Required repository dependencies for database access
    private final org.example.backend.repository.TransactionRepository transactionRepository;
    private final org.example.backend.repository.CategoryRepository categoryRepository;
    private final org.example.backend.repository.DepartmentRepository departmentRepository;
    private final org.example.backend.repository.CompanyRepository companyRepository;

    // Inject ReportApplicationService
    private final org.example.backend.application.service.ReportApplicationService reportApplicationService;

    /**
     * Generate enhanced financial grouping report preview with hierarchical structure
     */
    @GetMapping("/generate")
    public ResponseEntity<?> generateFinancialGroupingPreview(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Generating enhanced financial grouping preview for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Start date cannot be after end date")
                );
            }
            
            // Generate enhanced financial grouping data with hierarchical structure
            Map<String, Object> response = generateEnhancedFinancialGroupingData(companyId, startDate, endDate);
            
            log.info("Enhanced financial grouping preview generated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to generate financial grouping preview: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Failed to generate financial grouping report")
            );
        }
    }

    /**
     * Get financial grouping data in JSON format
     */
    @GetMapping("/json")
    public ResponseEntity<?> getFinancialGroupingReport(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Getting financial grouping JSON for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            Map<String, Object> data = generateEnhancedFinancialGroupingData(companyId, startDate, endDate);
            
            log.info("Financial grouping JSON generated successfully");
            return ResponseEntity.ok(data);
            
        } catch (Exception e) {
            log.error("Failed to get financial grouping JSON: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Failed to generate financial grouping data")
            );
        }
    }

    /**
     * Export enhanced financial grouping report as Excel
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportFinancialGrouping(
            @RequestParam Integer companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Exporting financial grouping for company {} from {} to {}", 
                     companyId, startDate, endDate);
            
            // Generate enhanced Excel data
            byte[] excelData = generateEnhancedExcelData(companyId, startDate, endDate);
            String filename = String.format("Financial_Grouping_%d_%s_to_%s.xlsx", 
                                           companyId, startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType
                    .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            headers.setContentLength(excelData.length);
            
            log.info("Financial grouping exported successfully, file size: {} bytes", excelData.length);
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Failed to export financial grouping: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate enhanced financial grouping data with hierarchical structure using real database data
     */
    private Map<String, Object> generateEnhancedFinancialGroupingData(Integer companyId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get company information from database
            String companyName = getCompanyNameFromDatabase(companyId);
            
            // Get real transaction data from database
            List<Map<String, Object>> categoryGrouping = getCategoryGroupingFromDatabase(companyId, startDate, endDate);
            List<Map<String, Object>> departmentGrouping = getDepartmentGroupingFromDatabase(companyId, startDate, endDate);
            List<Map<String, Object>> transactionTypeGrouping = getTransactionTypeGroupingFromDatabase(companyId, startDate, endDate);
            List<Map<String, Object>> monthlyTrend = getMonthlyTrendFromDatabase(companyId, startDate, endDate);
            
            // Calculate summary statistics from real data
            Map<String, Object> summary = calculateSummaryFromRealData(companyId, startDate, endDate);
            
            response.put("companyId", companyId);
            response.put("companyName", companyName);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("categoryGrouping", categoryGrouping);
            response.put("departmentGrouping", departmentGrouping);
            response.put("transactionTypeGrouping", transactionTypeGrouping);
            response.put("monthlyTrend", monthlyTrend);
            response.put("summary", summary);
            response.put("reportType", "FINANCIAL_GROUPING");
            response.put("generatedAt", LocalDate.now());
            
        } catch (Exception e) {
            log.error("Error generating financial grouping data for company {}: {}", companyId, e.getMessage());
            throw new RuntimeException("Failed to generate financial grouping data", e);
        }
        
        return response;
    }

    /**
     * Get company name from database
     */
    private String getCompanyNameFromDatabase(Integer companyId) {
        return companyRepository.findById(companyId)
                .map(company -> company.getCompanyName())
                .orElse("Unknown Company");
    }


    /**
     * Get subcategories from transaction descriptions
     */
    private List<Map<String, Object>> getSubcategoriesFromTransactions(List<org.example.backend.model.Transaction> transactions) {
        Map<String, List<org.example.backend.model.Transaction>> groupedByDescription = transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getDescription().substring(0, Math.min(t.getDescription().length(), 20)) + "..."
                ));
        
        List<Map<String, Object>> subcategories = new ArrayList<>();
        int index = 0;
        
        for (Map.Entry<String, List<org.example.backend.model.Transaction>> entry : groupedByDescription.entrySet()) {
            if (index >= 5) break; // Limit to top 5 subcategories
            
            Map<String, Object> subcategory = new HashMap<>();
            List<org.example.backend.model.Transaction> subTransactions = entry.getValue();
            
            BigDecimal subAmount = subTransactions.stream()
                    .map(org.example.backend.model.Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalCategoryAmount = transactions.stream()
                    .map(org.example.backend.model.Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal subPercentage = totalCategoryAmount.compareTo(BigDecimal.ZERO) > 0 
                    ? subAmount.divide(totalCategoryAmount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;
            
            subcategory.put("id", "subcat_" + index);
            subcategory.put("name", entry.getKey());
            subcategory.put("amount", subAmount);
            subcategory.put("transactionCount", subTransactions.size());
            subcategory.put("percentage", subPercentage.setScale(1, RoundingMode.HALF_UP) + "%");
            
            subcategories.add(subcategory);
            index++;
        }
        
        return subcategories;
    }

    /**
     * Get transaction type grouping from database
     */
    private List<Map<String, Object>> getTransactionTypeGroupingFromDatabase(Integer companyId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> typeData = new ArrayList<>();
        
        try {
            // Get all transactions for the company in the date range
            List<org.example.backend.model.Transaction> allTransactions = transactionRepository
                    .findByCompanyIdAndTransactionDateBetween(companyId, startDate, endDate);
            
            // Group by transaction type
            Map<String, List<org.example.backend.model.Transaction>> groupedByType = allTransactions.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            t -> t.getTransactionType().toString()
                    ));
            
            BigDecimal grandTotal = allTransactions.stream()
                    .map(org.example.backend.model.Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            for (Map.Entry<String, List<org.example.backend.model.Transaction>> entry : groupedByType.entrySet()) {
                Map<String, Object> typeItem = new HashMap<>();
                List<org.example.backend.model.Transaction> typeTransactions = entry.getValue();
                
                BigDecimal totalAmount = typeTransactions.stream()
                        .map(org.example.backend.model.Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal averageAmount = typeTransactions.size() > 0
                        ? totalAmount.divide(new BigDecimal(typeTransactions.size()), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                
                BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                        ? totalAmount.divide(grandTotal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO;
                
                typeItem.put("type", entry.getKey());
                typeItem.put("label", entry.getKey() + " Transactions");
                typeItem.put("totalAmount", totalAmount);
                typeItem.put("transactionCount", typeTransactions.size());
                typeItem.put("averageAmount", averageAmount);
                typeItem.put("percentage", percentage.setScale(1, RoundingMode.HALF_UP) + "%");
                
                typeData.add(typeItem);
            }
            
        } catch (Exception e) {
            log.error("Error getting transaction type grouping from database: {}", e.getMessage());
        }
        
        return typeData;
    }

    /**
     * Get monthly trend data from database
     */
    private List<Map<String, Object>> getMonthlyTrendFromDatabase(Integer companyId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        
        try {
            // Get first day of start month and last day of end month
            LocalDate currentMonth = startDate.withDayOfMonth(1);
            LocalDate endMonth = endDate.withDayOfMonth(1);
            
            while (!currentMonth.isAfter(endMonth)) {
                LocalDate monthStart = currentMonth;
                LocalDate monthEnd = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());
                
                // Get transactions for this month
                List<org.example.backend.model.Transaction> monthTransactions = transactionRepository
                        .findByCompanyIdAndTransactionDateBetween(companyId, monthStart, monthEnd);
                
                // Calculate income and expenses for the month
                BigDecimal income = monthTransactions.stream()
                        .filter(t -> "INCOME".equals(t.getTransactionType().toString()))
                        .map(org.example.backend.model.Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal expenses = monthTransactions.stream()
                        .filter(t -> "EXPENSE".equals(t.getTransactionType().toString()))
                        .map(org.example.backend.model.Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal netIncome = income.subtract(expenses);
                
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                monthData.put("monthName", currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
                monthData.put("income", income);
                monthData.put("expenses", expenses);
                monthData.put("netIncome", netIncome);
                monthData.put("transactionCount", monthTransactions.size());
                
                trendData.add(monthData);
                
                // Move to next month
                currentMonth = currentMonth.plusMonths(1);
            }
            
        } catch (Exception e) {
            log.error("Error getting monthly trend from database: {}", e.getMessage());
        }
        
        return trendData;
    }

    /**
     * Calculate summary statistics from real database data
     */
    private Map<String, Object> calculateSummaryFromRealData(Integer companyId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            // Get all transactions for the period
            List<org.example.backend.model.Transaction> allTransactions = transactionRepository
                    .findByCompanyIdAndTransactionDateBetween(companyId, startDate, endDate);
            
            BigDecimal totalIncome = allTransactions.stream()
                    .filter(t -> "INCOME".equals(t.getTransactionType().toString()))
                    .map(org.example.backend.model.Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalExpenses = allTransactions.stream()
                    .filter(t -> "EXPENSE".equals(t.getTransactionType().toString()))
                    .map(org.example.backend.model.Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal netIncome = totalIncome.subtract(totalExpenses);
            
            summary.put("totalIncome", totalIncome);
            summary.put("totalExpenses", totalExpenses);
            summary.put("netIncome", netIncome);
            
            // Calculate profit margin
            String profitMargin = totalIncome.compareTo(BigDecimal.ZERO) > 0
                    ? netIncome.divide(totalIncome, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) + "%"
                    : "0.0%";
            summary.put("profitMargin", profitMargin);
            
            // Calculate expense ratio
            String expenseRatio = totalIncome.compareTo(BigDecimal.ZERO) > 0
                    ? totalExpenses.divide(totalIncome, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) + "%"
                    : "0.0%";
            summary.put("expenseRatio", expenseRatio);
            
            summary.put("totalTransactions", allTransactions.size());
            
        } catch (Exception e) {
            log.error("Error calculating summary from real data: {}", e.getMessage());
            // Return default values on error
            summary.put("totalIncome", BigDecimal.ZERO);
            summary.put("totalExpenses", BigDecimal.ZERO);
            summary.put("netIncome", BigDecimal.ZERO);
            summary.put("profitMargin", "0.0%");
            summary.put("expenseRatio", "0.0%");
            summary.put("totalTransactions", 0);
        }
        
        return summary;
    }

    /**
     * Generate enhanced Excel file with multiple worksheets
     */
    private byte[] generateEnhancedExcelData(Integer companyId, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            
            Map<String, Object> data = generateEnhancedFinancialGroupingData(companyId, startDate, endDate);
            String companyName = (String) data.get("companyName");
            
            // Create worksheets
            createSummarySheet(workbook, data, companyName);
            createCategoryAnalysisSheet(workbook, data, companyName);
            createDepartmentAnalysisSheet(workbook, data, companyName);
            createMonthlyTrendSheet(workbook, data, companyName);
            createDetailedTransactionsSheet(workbook, data, companyName);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("Failed to generate Excel file", e);
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    /**
     * Create summary worksheet
     */
    private void createSummarySheet(Workbook workbook, Map<String, Object> data, String companyName) {
        Sheet sheet = workbook.createSheet("Executive Summary");
        
        // Create styles
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        int rowNum = 0;
        
        // Title section
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Financial Grouping Analysis - Executive Summary");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        
        rowNum++; // Empty row
        
        // Company and date info
        Row companyRow = sheet.createRow(rowNum++);
        companyRow.createCell(0).setCellValue("Company:");
        companyRow.createCell(1).setCellValue(companyName);
        
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Report Period:");
        dateRow.createCell(1).setCellValue(data.get("startDate") + " to " + data.get("endDate"));
        
        rowNum++; // Empty row
        
        // Summary statistics
        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) data.get("summary");
        
        Row summaryHeaderRow = sheet.createRow(rowNum++);
        summaryHeaderRow.createCell(0).setCellValue("Financial Summary");
        summaryHeaderRow.getCell(0).setCellStyle(headerStyle);
        
        Row[] summaryRows = {
            sheet.createRow(rowNum++),
            sheet.createRow(rowNum++),
            sheet.createRow(rowNum++),
            sheet.createRow(rowNum++),
            sheet.createRow(rowNum++)
        };
        
        summaryRows[0].createCell(0).setCellValue("Total Income:");
        summaryRows[0].createCell(1).setCellValue(((BigDecimal) summary.get("totalIncome")).doubleValue());
        summaryRows[0].getCell(1).setCellStyle(currencyStyle);
        
        summaryRows[1].createCell(0).setCellValue("Total Expenses:");
        summaryRows[1].createCell(1).setCellValue(((BigDecimal) summary.get("totalExpenses")).doubleValue());
        summaryRows[1].getCell(1).setCellStyle(currencyStyle);
        
        summaryRows[2].createCell(0).setCellValue("Net Income:");
        summaryRows[2].createCell(1).setCellValue(((BigDecimal) summary.get("netIncome")).doubleValue());
        summaryRows[2].getCell(1).setCellStyle(currencyStyle);
        
        summaryRows[3].createCell(0).setCellValue("Profit Margin:");
        summaryRows[3].createCell(1).setCellValue((String) summary.get("profitMargin"));
        
        summaryRows[4].createCell(0).setCellValue("Expense Ratio:");
        summaryRows[4].createCell(1).setCellValue((String) summary.get("expenseRatio"));
        
        // Auto-size columns
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }

        /**
     * Create department analysis worksheet
     */
    private void createDepartmentAnalysisSheet(Workbook workbook, Map<String, Object> data, String companyName) {
        Sheet sheet = workbook.createSheet("Department Analysis");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 0;
        
        // Title section
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Department Budget Analysis - " + companyName);
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));
        
        rowNum++; // Empty row
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Department", "Budget Allocated", "Actual Spent", "Budget Utilization", "Transaction Count", "Avg Transaction Size"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> departmentData = (List<Map<String, Object>>) data.get("departmentGrouping");
        
        if (departmentData != null && !departmentData.isEmpty()) {
            for (Map<String, Object> dept : departmentData) {
                Row dataRow = sheet.createRow(rowNum++);
                
                // Department name
                Cell deptCell = dataRow.createCell(0);
                deptCell.setCellValue((String) dept.get("department"));
                deptCell.setCellStyle(dataStyle);
                
                // Budget Allocated
                Cell budgetCell = dataRow.createCell(1);
                Object budgetObj = dept.get("budgetAllocated");
                if (budgetObj instanceof BigDecimal) {
                    budgetCell.setCellValue(((BigDecimal) budgetObj).doubleValue());
                } else {
                    budgetCell.setCellValue(0.0);
                }
                budgetCell.setCellStyle(currencyStyle);
                
                // Actual Spent
                Cell spentCell = dataRow.createCell(2);
                Object spentObj = dept.get("actualSpent");
                if (spentObj instanceof BigDecimal) {
                    spentCell.setCellValue(((BigDecimal) spentObj).doubleValue());
                } else {
                    spentCell.setCellValue(0.0);
                }
                spentCell.setCellStyle(currencyStyle);
                
                // Budget Utilization
                Cell utilizationCell = dataRow.createCell(3);
                String utilization = (String) dept.get("budgetUtilization");
                utilizationCell.setCellValue(utilization != null ? utilization : "0.0%");
                utilizationCell.setCellStyle(dataStyle);
                
                // Transaction Count
                Cell countCell = dataRow.createCell(4);
                Object countObj = dept.get("transactionCount");
                if (countObj instanceof Integer) {
                    countCell.setCellValue((Integer) countObj);
                } else {
                    countCell.setCellValue(0);
                }
                countCell.setCellStyle(dataStyle);
                
                // Average Transaction Size
                Cell avgCell = dataRow.createCell(5);
                Object avgObj = dept.get("averageTransactionSize");
                if (avgObj instanceof BigDecimal) {
                    avgCell.setCellValue(((BigDecimal) avgObj).doubleValue());
                } else {
                    avgCell.setCellValue(0.0);
                }
                avgCell.setCellStyle(currencyStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create monthly trend worksheet
     */
    private void createMonthlyTrendSheet(Workbook workbook, Map<String, Object> data, String companyName) {
        Sheet sheet = workbook.createSheet("Monthly Trend");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 0;
        
        // Title section
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Monthly Financial Trends - " + companyName);
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));
        
        rowNum++; // Empty row
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Month", "Income", "Expenses", "Net Income", "Transaction Count"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> monthlyData = (List<Map<String, Object>>) data.get("monthlyTrend");
        
        if (monthlyData != null && !monthlyData.isEmpty()) {
            for (Map<String, Object> month : monthlyData) {
                Row dataRow = sheet.createRow(rowNum++);
                
                // Month name
                Cell monthCell = dataRow.createCell(0);
                monthCell.setCellValue((String) month.get("monthName"));
                monthCell.setCellStyle(dataStyle);
                
                // Income
                Cell incomeCell = dataRow.createCell(1);
                Object incomeObj = month.get("income");
                if (incomeObj instanceof BigDecimal) {
                    incomeCell.setCellValue(((BigDecimal) incomeObj).doubleValue());
                } else {
                    incomeCell.setCellValue(0.0);
                }
                incomeCell.setCellStyle(currencyStyle);
                
                // Expenses
                Cell expensesCell = dataRow.createCell(2);
                Object expensesObj = month.get("expenses");
                if (expensesObj instanceof BigDecimal) {
                    expensesCell.setCellValue(((BigDecimal) expensesObj).doubleValue());
                } else {
                    expensesCell.setCellValue(0.0);
                }
                expensesCell.setCellStyle(currencyStyle);
                
                // Net Income
                Cell netIncomeCell = dataRow.createCell(3);
                Object netIncomeObj = month.get("netIncome");
                if (netIncomeObj instanceof BigDecimal) {
                    netIncomeCell.setCellValue(((BigDecimal) netIncomeObj).doubleValue());
                } else {
                    netIncomeCell.setCellValue(0.0);
                }
                netIncomeCell.setCellStyle(currencyStyle);
                
                // Transaction Count
                Cell countCell = dataRow.createCell(4);
                Object countObj = month.get("transactionCount");
                if (countObj instanceof Integer) {
                    countCell.setCellValue((Integer) countObj);
                } else {
                    countCell.setCellValue(0);
                }
                countCell.setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    /**
     * Get category grouping data from database with real transaction data - FIXED for JPA relationships
     */
    private List<Map<String, Object>> getCategoryGroupingFromDatabase(Integer companyId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> categoryData = new ArrayList<>();
        
        try {
            // Get all categories for the company
            List<org.example.backend.model.Category> categories = categoryRepository.findByCompanyIdAndIsActiveTrue(companyId);
            
            for (org.example.backend.model.Category category : categories) {
                // Get all transactions and filter by category object
                List<org.example.backend.model.Transaction> allTransactions = transactionRepository
                        .findByCompanyIdAndTransactionDateBetween(companyId, startDate, endDate);
                
                // FIXED: Filter transactions by category object instead of categoryId
                List<org.example.backend.model.Transaction> categoryTransactions = allTransactions.stream()
                        .filter(t -> t.getCategory() != null && category.getCategoryId().equals(t.getCategory().getCategoryId()))
                        .collect(java.util.stream.Collectors.toList());
                
                if (!categoryTransactions.isEmpty()) {
                    Map<String, Object> categoryItem = new HashMap<>();
                    categoryItem.put("id", "category_" + category.getCategoryId());
                    categoryItem.put("category", category.getName());
                    categoryItem.put("type", category.getType().toString());
                    
                    // Calculate totals from real transactions
                    BigDecimal totalAmount = categoryTransactions.stream()
                            .map(org.example.backend.model.Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    int transactionCount = categoryTransactions.size();
                    
                    categoryItem.put("totalAmount", totalAmount);
                    categoryItem.put("transactionCount", transactionCount);
                    
                    // Calculate percentage (will be updated after all categories are processed)
                    categoryItem.put("percentage", "0.0%");
                    
                    // Get subcategory breakdown (group by description patterns)
                    List<Map<String, Object>> subcategories = getSubcategoriesFromTransactions(categoryTransactions);
                    categoryItem.put("subcategories", subcategories);
                    
                    categoryData.add(categoryItem);
                }
            }
            
            // Calculate percentages based on total amount
            BigDecimal grandTotal = categoryData.stream()
                    .map(cat -> (BigDecimal) cat.get("totalAmount"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
                for (Map<String, Object> category : categoryData) {
                    BigDecimal amount = (BigDecimal) category.get("totalAmount");
                    BigDecimal percentage = amount.divide(grandTotal, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    category.put("percentage", percentage.setScale(1, RoundingMode.HALF_UP) + "%");
                }
            }
            
        } catch (Exception e) {
            log.error("Error getting category grouping from database: {}", e.getMessage());
        }
        
        return categoryData;
    }

    /**
     * Get department grouping data from database - FIXED for JPA relationships
     */
    private List<Map<String, Object>> getDepartmentGroupingFromDatabase(Integer companyId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> departmentData = new ArrayList<>();
        
        try {
            // Get all departments for the company
            List<org.example.backend.model.Department> departments = departmentRepository.findByCompanyIdAndIsActiveTrue(companyId);
            
            for (org.example.backend.model.Department department : departments) {
                // Get all transactions and filter by department
                List<org.example.backend.model.Transaction> allTransactions = transactionRepository
                        .findByCompanyIdAndTransactionDateBetween(companyId, startDate, endDate);
                
                // FIXED: Filter transactions by department object instead of departmentId
                List<org.example.backend.model.Transaction> departmentTransactions = allTransactions.stream()
                        .filter(t -> t.getDepartment() != null && department.getDepartmentId().equals(t.getDepartment().getDepartmentId()))
                        .collect(java.util.stream.Collectors.toList());
                
                Map<String, Object> deptItem = new HashMap<>();
                deptItem.put("id", "dept_" + department.getDepartmentId());
                deptItem.put("department", department.getName());
                deptItem.put("budgetAllocated", department.getBudget());
                
                // Calculate actual spent from transactions
                BigDecimal actualSpent = departmentTransactions.stream()
                        .filter(t -> "EXPENSE".equals(t.getTransactionType().toString()))
                        .map(org.example.backend.model.Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                deptItem.put("actualSpent", actualSpent);
                
                // Calculate budget utilization
                BigDecimal budget = department.getBudget();
                String budgetUtilization = budget.compareTo(BigDecimal.ZERO) > 0
                        ? actualSpent.divide(budget, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) + "%"
                        : "0.0%";
                deptItem.put("budgetUtilization", budgetUtilization);
                
                deptItem.put("transactionCount", departmentTransactions.size());
                
                // Calculate average transaction size
                BigDecimal averageTransactionSize = departmentTransactions.size() > 0
                        ? actualSpent.divide(new BigDecimal(departmentTransactions.size()), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                deptItem.put("averageTransactionSize", averageTransactionSize);
                
                departmentData.add(deptItem);
            }
            
        } catch (Exception e) {
            log.error("Error getting department grouping from database: {}", e.getMessage());
        }
        
        return departmentData;
    }

    /**
     * Create detailed transactions worksheet - FIXED for JPA relationships
     */
    private void createDetailedTransactionsSheet(Workbook workbook, Map<String, Object> data, String companyName) {
        Sheet sheet = workbook.createSheet("Transaction Details");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 0;
        
        // Title section
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Detailed Transaction Report - " + companyName);
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 6));
        
        rowNum++; // Empty row
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Date", "Description", "Category", "Department", "Type", "Amount", "Reference"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Get transaction details from database
        Integer companyId = (Integer) data.get("companyId");
        LocalDate startDate = (LocalDate) data.get("startDate");
        LocalDate endDate = (LocalDate) data.get("endDate");
        
        try {
            List<org.example.backend.model.Transaction> transactions = transactionRepository
                    .findByCompanyIdAndTransactionDateBetween(companyId, startDate, endDate);
            
            for (org.example.backend.model.Transaction transaction : transactions) {
                Row dataRow = sheet.createRow(rowNum++);
                
                // Date
                Cell dateCell = dataRow.createCell(0);
                dateCell.setCellValue(transaction.getTransactionDate());
                dateCell.setCellStyle(dateStyle);
                
                // Description
                Cell descCell = dataRow.createCell(1);
                descCell.setCellValue(transaction.getDescription());
                descCell.setCellStyle(dataStyle);
                
                // Category - FIXED: Use category object instead of categoryId
                Cell categoryCell = dataRow.createCell(2);
                String categoryName = "";
                if (transaction.getCategory() != null) {
                    categoryName = transaction.getCategory().getName();
                }
                categoryCell.setCellValue(categoryName);
                categoryCell.setCellStyle(dataStyle);
                
                // Department - FIXED: Use department object instead of departmentId
                Cell departmentCell = dataRow.createCell(3);
                String departmentName = "";
                if (transaction.getDepartment() != null) {
                    departmentName = transaction.getDepartment().getName();
                }
                departmentCell.setCellValue(departmentName);
                departmentCell.setCellStyle(dataStyle);
                
                // Type
                Cell typeCell = dataRow.createCell(4);
                typeCell.setCellValue(transaction.getTransactionType().toString());
                typeCell.setCellStyle(dataStyle);
                
                // Amount
                Cell amountCell = dataRow.createCell(5);
                amountCell.setCellValue(transaction.getAmount().doubleValue());
                amountCell.setCellStyle(currencyStyle);
                
                // Reference
                Cell referenceCell = dataRow.createCell(6);
                referenceCell.setCellValue(transaction.getReferenceNumber() != null ? transaction.getReferenceNumber() : "");
                referenceCell.setCellStyle(dataStyle);
            }
            
        } catch (Exception e) {
            log.error("Error adding transaction details to Excel: {}", e.getMessage());
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Save financial grouping report using ReportApplicationService
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveFinancialGroupingReport(
            @RequestBody Map<String, Object> request) {
        
        try {
            // Extract parameters from request
            Integer companyId = (Integer) request.get("companyId");
            String reportName = (String) request.get("reportName");
            String startDateStr = (String) request.get("startDate");
            String endDateStr = (String) request.get("endDate");
            Boolean aiAnalysisEnabled = (Boolean) request.getOrDefault("aiAnalysisEnabled", false);
            
            // Validate required parameters
            if (companyId == null || reportName == null || reportName.trim().isEmpty() || 
                startDateStr == null || endDateStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Missing required parameters: companyId, reportName, startDate, endDate"
                ));
            }
            
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            
            log.info("Saving financial grouping report: {} for company {} from {} to {}", 
                    reportName, companyId, startDate, endDate);
            
            // Create GenerateReportCommand for ReportApplicationService
            GenerateReportCommand command = GenerateReportCommand.builder()
                    .reportType(ReportType.FINANCIAL_GROUPING)
                    .reportName(reportName.trim())
                    .startDate(startDate)
                    .endDate(endDate)
                    .tenantId(companyId)
                    .createdBy(1) // Default user - should be extracted from JWT in production
                    .aiAnalysisEnabled(aiAnalysisEnabled)
                    .build();
            
            // Generate and save report using ReportApplicationService
            String reportId = reportApplicationService.generateReport(command);
            
            log.info("Financial grouping report saved successfully with ID: {}", reportId);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", Map.of("reportId", reportId),
                "message", "Financial grouping report saved successfully"
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters for financial grouping report save: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Failed to save financial grouping report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to save financial grouping report. Please try again."
            ));
        }
    }

    /**
     * Alternative save method using GenerateReportCommand directly
     */
    @PostMapping("/save-direct")
    public ResponseEntity<Map<String, Object>> saveFinancialGroupingReportDirect(
            @RequestBody GenerateReportCommand command) {
        
        try {
            // Set report type to FINANCIAL_GROUPING
            command.setReportType(ReportType.FINANCIAL_GROUPING);
            
            // Set defaults if not provided
            if (command.getTenantId() == null) {
                command.setTenantId(1); // Should be extracted from JWT in production
            }
            if (command.getCreatedBy() == null) {
                command.setCreatedBy(1); // Should be extracted from JWT in production
            }
            
            log.info("Saving financial grouping report directly: {} for tenant {}", 
                    command.getReportName(), command.getTenantId());
            
            // Generate and save report
            String reportId = reportApplicationService.generateReport(command);
            
            log.info("Financial grouping report saved successfully with ID: {}", reportId);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", Map.of("reportId", reportId),
                "message", "Financial grouping report saved successfully"
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid command for financial grouping report save: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Failed to save financial grouping report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to save financial grouping report: " + e.getMessage()
            ));
        }
    }

    /**
     * Create category analysis worksheet - FIXED complete method
     */
    private void createCategoryAnalysisSheet(Workbook workbook, Map<String, Object> data, String companyName) {
        Sheet sheet = workbook.createSheet("Category Analysis");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 0;
        
        // Title section
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Category Analysis - " + companyName);
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));
        
        rowNum++; // Empty row
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Category", "Type", "Total Amount", "Transaction Count", "Avg Transaction", "Percentage"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categoryData = (List<Map<String, Object>>) data.get("categoryGrouping");
        
        if (categoryData != null && !categoryData.isEmpty()) {
            for (Map<String, Object> category : categoryData) {
                Row dataRow = sheet.createRow(rowNum++);
                
                // Category name
                Cell categoryCell = dataRow.createCell(0);
                categoryCell.setCellValue((String) category.get("category"));
                categoryCell.setCellStyle(dataStyle);
                
                // Type
                Cell typeCell = dataRow.createCell(1);
                typeCell.setCellValue((String) category.get("type"));
                typeCell.setCellStyle(dataStyle);
                
                // Total Amount
                Cell totalAmountCell = dataRow.createCell(2);
                Object totalAmountObj = category.get("totalAmount");
                if (totalAmountObj instanceof BigDecimal) {
                    totalAmountCell.setCellValue(((BigDecimal) totalAmountObj).doubleValue());
                } else if (totalAmountObj instanceof Number) {
                    totalAmountCell.setCellValue(((Number) totalAmountObj).doubleValue());
                } else {
                    totalAmountCell.setCellValue(0.0);
                }
                totalAmountCell.setCellStyle(currencyStyle);
                
                // Transaction Count
                Cell countCell = dataRow.createCell(3);
                Object countObj = category.get("transactionCount");
                if (countObj instanceof Integer) {
                    countCell.setCellValue((Integer) countObj);
                } else if (countObj instanceof Number) {
                    countCell.setCellValue(((Number) countObj).intValue());
                } else {
                    countCell.setCellValue(0);
                }
                countCell.setCellStyle(dataStyle);
                
                // Calculate and set average transaction amount
                Cell avgCell = dataRow.createCell(4);
                try {
                    BigDecimal total = (BigDecimal) category.get("totalAmount");
                    Integer count = (Integer) category.get("transactionCount");
                    if (total != null && count != null && count > 0) {
                        double average = total.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP).doubleValue();
                        avgCell.setCellValue(average);
                    } else {
                        avgCell.setCellValue(0.0);
                    }
                } catch (Exception e) {
                    avgCell.setCellValue(0.0);
                }
                avgCell.setCellStyle(currencyStyle);
                
                // Percentage
                Cell percentageCell = dataRow.createCell(5);
                String percentage = (String) category.get("percentage");
                percentageCell.setCellValue(percentage != null ? percentage : "0.0%");
                percentageCell.setCellStyle(dataStyle);
                
                // Add subcategory details if available
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> subcategories = (List<Map<String, Object>>) category.get("subcategories");
                if (subcategories != null && !subcategories.isEmpty()) {
                    for (Map<String, Object> subcat : subcategories) {
                        Row subcatRow = sheet.createRow(rowNum++);
                        
                        // Subcategory name (indented)
                        Cell subcatNameCell = subcatRow.createCell(0);
                        subcatNameCell.setCellValue("  • " + subcat.get("name"));
                        subcatNameCell.setCellStyle(dataStyle);
                        
                        // Empty type cell for subcategory
                        Cell subcatTypeCell = subcatRow.createCell(1);
                        subcatTypeCell.setCellValue("");
                        subcatTypeCell.setCellStyle(dataStyle);
                        
                        // Subcategory amount
                        Cell subcatAmountCell = subcatRow.createCell(2);
                        Object subcatAmountObj = subcat.get("amount");
                        if (subcatAmountObj instanceof BigDecimal) {
                            subcatAmountCell.setCellValue(((BigDecimal) subcatAmountObj).doubleValue());
                        } else if (subcatAmountObj instanceof Number) {
                            subcatAmountCell.setCellValue(((Number) subcatAmountObj).doubleValue());
                        } else {
                            subcatAmountCell.setCellValue(0.0);
                        }
                        subcatAmountCell.setCellStyle(currencyStyle);
                        
                        // Subcategory transaction count
                        Cell subcatCountCell = subcatRow.createCell(3);
                        Object subcatCountObj = subcat.get("transactionCount");
                        if (subcatCountObj instanceof Integer) {
                            subcatCountCell.setCellValue((Integer) subcatCountObj);
                        } else if (subcatCountObj instanceof Number) {
                            subcatCountCell.setCellValue(((Number) subcatCountObj).intValue());
                        } else {
                            subcatCountCell.setCellValue(0);
                        }
                        subcatCountCell.setCellStyle(dataStyle);
                        
                        // Average for subcategory (calculated)
                        Cell subcatAvgCell = subcatRow.createCell(4);
                        try {
                            BigDecimal subcatTotal = (BigDecimal) subcat.get("amount");
                            Integer subcatCount = (Integer) subcat.get("transactionCount");
                            if (subcatTotal != null && subcatCount != null && subcatCount > 0) {
                                double subcatAvg = subcatTotal.divide(new BigDecimal(subcatCount), 2, RoundingMode.HALF_UP).doubleValue();
                                subcatAvgCell.setCellValue(subcatAvg);
                            } else {
                                subcatAvgCell.setCellValue(0.0);
                            }
                        } catch (Exception e) {
                            subcatAvgCell.setCellValue(0.0);
                        }
                        subcatAvgCell.setCellStyle(currencyStyle);
                        
                        // Subcategory percentage
                        Cell subcatPercentageCell = subcatRow.createCell(5);
                        String subcatPercentage = (String) subcat.get("percentage");
                        subcatPercentageCell.setCellValue(subcatPercentage != null ? subcatPercentage : "0.0%");
                        subcatPercentageCell.setCellStyle(dataStyle);
                    }
                    
                    // Add empty row after each category for better readability
                    rowNum++;
                }
            }
        } else {
            // No data available
            Row noDataRow = sheet.createRow(rowNum++);
            Cell noDataCell = noDataRow.createCell(0);
            noDataCell.setCellValue("No category data available for the selected period");
            noDataCell.setCellStyle(dataStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));
        }
        
        // Auto-size columns for better readability
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            // Set minimum width to ensure readability
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.max(currentWidth, 3000)); // Minimum 30 characters
        }
        
        // Add totals row if there's data
        if (categoryData != null && !categoryData.isEmpty()) {
            rowNum++; // Empty row before totals
            
            Row totalsRow = sheet.createRow(rowNum++);
            Cell totalLabelCell = totalsRow.createCell(0);
            totalLabelCell.setCellValue("TOTAL");
            totalLabelCell.setCellStyle(headerStyle);
            
            // Calculate totals
            BigDecimal grandTotal = BigDecimal.ZERO;
            int totalTransactions = 0;
            
            for (Map<String, Object> category : categoryData) {
                Object amountObj = category.get("totalAmount");
                if (amountObj instanceof BigDecimal) {
                    grandTotal = grandTotal.add((BigDecimal) amountObj);
                }
                
                Object countObj = category.get("transactionCount");
                if (countObj instanceof Integer) {
                    totalTransactions += (Integer) countObj;
                }
            }
            
            Cell totalTypeCell = totalsRow.createCell(1);
            totalTypeCell.setCellValue("ALL");
            totalTypeCell.setCellStyle(headerStyle);
            
            Cell totalAmountCell = totalsRow.createCell(2);
            totalAmountCell.setCellValue(grandTotal.doubleValue());
            totalAmountCell.setCellStyle(currencyStyle);
            
            Cell totalCountCell = totalsRow.createCell(3);
            totalCountCell.setCellValue(totalTransactions);
            totalCountCell.setCellStyle(headerStyle);
            
            Cell totalAvgCell = totalsRow.createCell(4);
            if (totalTransactions > 0) {
                double overallAvg = grandTotal.divide(new BigDecimal(totalTransactions), 2, RoundingMode.HALF_UP).doubleValue();
                totalAvgCell.setCellValue(overallAvg);
            } else {
                totalAvgCell.setCellValue(0.0);
            }
            totalAvgCell.setCellStyle(currencyStyle);
            
            Cell totalPercentageCell = totalsRow.createCell(5);
            totalPercentageCell.setCellValue("100.0%");
            totalPercentageCell.setCellStyle(headerStyle);
        }
    }

    // ========== Style Creation Methods (Following unified format) ==========

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setFontName("Arial");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Professional borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        
        // Light professional background
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Arial");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Industry standard header background
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Complete borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("¥#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("yyyy-mm-dd"));
        return style;
    }
}