package org.example.backend.repository;

import org.example.backend.model.OutcomeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutcomeCategoryRepository extends JpaRepository<OutcomeCategory, Integer> {

}
