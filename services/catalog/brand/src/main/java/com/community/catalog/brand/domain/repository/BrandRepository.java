package com.community.catalog.brand.domain.repository;

import com.community.catalog.brand.domain.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {}
