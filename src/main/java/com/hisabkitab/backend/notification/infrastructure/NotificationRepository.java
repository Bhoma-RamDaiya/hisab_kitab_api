package com.hisabkitab.backend.notification.infrastructure;

import com.hisabkitab.backend.notification.domain.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository
        extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findAllByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    long countByUserIdAndIsReadFalse(Long userId);
}
