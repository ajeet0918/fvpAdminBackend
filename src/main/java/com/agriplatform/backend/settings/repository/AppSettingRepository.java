package com.agriplatform.backend.settings.repository;

import com.agriplatform.backend.settings.model.AppSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {

    List<AppSetting> findAllByOrderByCategoryAscSettingKeyAsc();

    List<AppSetting> findByCategoryIgnoreCaseOrderBySettingKeyAsc(String category);

    Optional<AppSetting> findBySettingKey(String settingKey);

    Optional<AppSetting> findBySettingKeyAndActiveTrue(String settingKey);
}

