package org.example.backend.service;

import org.example.backend.model.Outcome;
import org.example.backend.repository.OutcomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutcomeService {
    @Autowired
    private OutcomeRepository outcomeRepository;

    public List<Outcome> findAll() { return outcomeRepository.findAll(); }
    public Outcome findById(Integer id) { return outcomeRepository.findById(id).orElse(null); }
    public Outcome save(Outcome outcome) { return outcomeRepository.save(outcome); }
    public void deleteById(Integer id) { outcomeRepository.deleteById(id); }
}