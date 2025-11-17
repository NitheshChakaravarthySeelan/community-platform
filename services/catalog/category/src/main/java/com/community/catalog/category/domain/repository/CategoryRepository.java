package com.community.catalog.category.domain.repository;

import com.community.catalog.category.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {}
