package com.abac.repository;

import com.abac.entity.AttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AttributeRepository extends JpaRepository<AttributeEntity, Long> {

    Optional<AttributeEntity> findByName(String name);

    List<AttributeEntity> findByCategoryOrderByName(AttributeEntity.AttributeCategory category);

    List<AttributeEntity> findAllByOrderByCategoryAscNameAsc();
}
