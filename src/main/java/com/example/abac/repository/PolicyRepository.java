package com.example.abac.repository;

import com.example.abac.entity.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    List<Policy> findByEnabledTrueOrderByPriorityDesc();

    @Query("SELECT p FROM Policy p WHERE (:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND (:enabled IS NULL OR p.enabled = :enabled)")
    Page<Policy> filter(@Param("name") String name, @Param("enabled") Boolean enabled, Pageable pageable);
}
