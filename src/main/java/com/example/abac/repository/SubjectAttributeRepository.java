package com.example.abac.repository;

import com.example.abac.entity.SubjectAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectAttributeRepository extends JpaRepository<SubjectAttribute, Long> {

    List<SubjectAttribute> findBySubjectId(String subjectId);

    void deleteBySubjectIdAndAttrKey(String subjectId, String attrKey);

    void deleteBySubjectId(String subjectId);
}
