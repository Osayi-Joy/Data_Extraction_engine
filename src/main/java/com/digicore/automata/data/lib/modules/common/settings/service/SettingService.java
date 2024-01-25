package com.digicore.automata.data.lib.modules.common.settings.service;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import com.digicore.automata.data.lib.modules.common.settings.dto.SettingDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SettingService {

 SettingDTO updateSetting(SettingDTO settingDTO);
 void updateSetting(Set<SettingDTO> settingDTO);
 boolean settingExists(String settingKey);

 Optional<SettingDTO> retrieveSetting(String key);
 String retrieveValue(String key);

 List<SettingDTO> retrieveAllSettings();

}
