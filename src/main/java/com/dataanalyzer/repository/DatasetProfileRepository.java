package com.dataanalyzer.repository;

import com.dataanalyzer.entity.DatasetProfileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DatasetProfileRepository extends JpaRepository<DatasetProfileEntity, Long> {

    List<DatasetProfileEntity> findAllByUserEmailOrderByCreatedAtDesc(String userEmail);

    Optional<DatasetProfileEntity> findByIdAndUserEmail(Long id, String userEmail);

    Page<DatasetProfileEntity> findAllByUserEmailOrderByCreatedAtDesc(String userEmail, Pageable pageable);

    Page<DatasetProfileEntity> findAllByUserEmailAndFileNameContainingIgnoreCaseOrderByCreatedAtDesc(String userEmail, String fileName, Pageable pageable);

    Page<DatasetProfileEntity> findAllByUserEmailAndHealthLabelOrderByCreatedAtDesc(String userEmail, String healthLabel, Pageable pageable);

    Page<DatasetProfileEntity> findAllByUserEmailAndFileNameContainingIgnoreCaseAndHealthLabelOrderByCreatedAtDesc(String userEmail, String fileName, String healthLabel, Pageable pageable);
}
