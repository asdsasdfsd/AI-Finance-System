package org.example.backend.service;

import org.example.backend.model.Company;
import org.example.backend.model.Fund;
import org.example.backend.repository.FundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class FundServiceTest {

    @Mock
    private FundRepository fundRepository;

    @InjectMocks
    private FundService fundService;

    private Fund sampleFund;
    private Company sampleCompany;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleCompany = new Company();
        sampleCompany.setCompanyId(1);
        sampleCompany.setCompanyName("Test Company");

        sampleFund = new Fund();
        sampleFund.setFundId(100);
        sampleFund.setCompany(sampleCompany);
        sampleFund.setIsActive(true);
    }

    @Test
    void testFindAll() {
        when(fundRepository.findAll()).thenReturn(Collections.singletonList(sampleFund));
        List<Fund> result = fundService.findAll();
        assertEquals(1, result.size());
        verify(fundRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        when(fundRepository.findById(100)).thenReturn(Optional.of(sampleFund));
        Fund result = fundService.findById(100);
        assertNotNull(result);
        assertEquals(100, result.getFundId());
        verify(fundRepository).findById(100);
    }

    @Test
    void testFindByCompanyId() {
        when(fundRepository.findByCompanyCompanyId(1)).thenReturn(List.of(sampleFund));
        List<Fund> result = fundService.findByCompanyId(1);
        assertEquals(1, result.size());
        verify(fundRepository).findByCompanyCompanyId(1);
    }

    @Test
    void testFindActiveByCompany() {
        when(fundRepository.findByCompanyAndIsActive(sampleCompany, true)).thenReturn(List.of(sampleFund));
        List<Fund> result = fundService.findActiveByCompany(sampleCompany);
        assertFalse(result.isEmpty());
    }

    @Test
    void testSaveShouldSetTimestamps() {
        sampleFund.setCreatedAt(null); // simulate first-time save
        when(fundRepository.save(any(Fund.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Fund saved = fundService.save(sampleFund);
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void testDeleteById() {
        doNothing().when(fundRepository).deleteById(100);
        fundService.deleteById(100);
        verify(fundRepository).deleteById(100);
    }
}

