package com.hisabkitab.backend.notification.infrastructure;

import com.hisabkitab.backend.notification.domain.UserDeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceTokenRepository
        extends JpaRepository<UserDeviceTokenEntity, Long> {

    Optional<UserDeviceTokenEntity> findByUserIdAndDeviceId(
            Long userId,
            String deviceId
    );

    List<UserDeviceTokenEntity> findAllByUserIdAndIsActiveTrue(Long userId);
}
