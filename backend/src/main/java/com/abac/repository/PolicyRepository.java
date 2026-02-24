package com.abac.repository;

import com.abac.entity.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PolicyRepository extends JpaRepository<PolicyEntity, Long> {

    List<PolicyEntity> findByEnabledTrueOrderByPriorityAscIdAsc();
}
