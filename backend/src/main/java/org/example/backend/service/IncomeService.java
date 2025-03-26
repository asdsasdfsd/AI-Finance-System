package org.example.backend.service;

import org.example.backend.model.Income;
import org.example.backend.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncomeService {
    @Autowired
    private IncomeRepository incomeRepository;

    public List<Income> findAll() {
        return incomeRepository.findAll();
    }

    public Income findById(Integer id) {
        return incomeRepository.findById(id).orElse(null);
    }

    public Income save(Income income) {
        return incomeRepository.save(income);
    }

    public void deleteById(Integer id) {
        incomeRepository.deleteById(id);
    }
}
