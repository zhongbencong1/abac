package com.example.abac.repository;

import com.example.abac.entity.ResourceAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceAttributeRepository extends JpaRepository<ResourceAttribute, Long> {

    List<ResourceAttribute> findByResourceTypeAndResourceId(String resourceType, String resourceId);

    void deleteByResourceTypeAndResourceId(String resourceType, String resourceId);
}
