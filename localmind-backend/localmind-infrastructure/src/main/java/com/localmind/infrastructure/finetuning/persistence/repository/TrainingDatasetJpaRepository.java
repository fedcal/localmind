package com.localmind.infrastructure.finetuning.persistence.repository;

import com.localmind.infrastructure.finetuning.persistence.entity.TrainingDatasetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TrainingDatasetJpaRepository extends JpaRepository<TrainingDatasetEntity, UUID> {
}
