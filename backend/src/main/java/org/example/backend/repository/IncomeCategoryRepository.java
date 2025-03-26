package org.example.backend.repository;

import org.example.backend.model.IncomeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface IncomeCategoryRepository extends JpaRepository<IncomeCategory, Integer> {

}
