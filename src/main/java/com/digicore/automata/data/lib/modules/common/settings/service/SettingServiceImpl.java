package com.digicore.automata.data.lib.modules.common.settings.service;

import com.digicore.automata.data.lib.modules.common.settings.model.Setting;
import com.digicore.automata.data.lib.modules.common.settings.dto.SettingDTO;
import com.digicore.automata.data.lib.modules.common.settings.repository.SettingRepository;
import com.digicore.common.util.BeanUtilWrapper;
import com.digicore.registhentication.exceptions.ExceptionHandler;

import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SettingServiceImpl implements SettingService {
  private final SettingRepository settingRepository;

  private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;

  private final Map<String, String> availableSettings = new HashMap<>();
  @EventListener(ContextRefreshedEvent.class)
  public void loadSettingsIntoMemoryOnStartUp(){
    retrieveAllSettings().forEach(settingDTO -> availableSettings.put(settingDTO.getKey(),settingDTO.getValue()));
  }

  @Override
  public synchronized SettingDTO updateSetting(SettingDTO settingDTO) {
    Setting setting =
        settingRepository
            .findFirstByKeyOrderByCreatedDateDesc(settingDTO.getKey())
            .orElse(new Setting());
    BeanUtilWrapper.copyNonNullProperties(settingDTO, setting);
    settingRepository.save(setting);
    availableSettings.put(setting.getKey(),setting.getValue());
    return settingDTO;
  }

  @Override
  public void updateSetting(Set<SettingDTO> settingDTO) {
    Set<Setting> settings = settingDTO.stream().map(settingDTO1 -> {
      Setting setting = new Setting();
      BeanUtilWrapper.copyNonNullProperties(settingDTO1, setting);
      return setting;
    }).collect(Collectors.toSet());
    settingRepository.saveAll(settings);
  }

  @Override
  public boolean settingExists(String settingKey) {
    return settingRepository.existsByKey(settingKey);
  }

  @Override
  public Optional<SettingDTO> retrieveSetting(String key) {
    Optional<Setting> setting = settingRepository.findFirstByKeyOrderByCreatedDateDesc(key);
    if (setting.isPresent()){
      SettingDTO settingDTO = new SettingDTO();
      BeanUtilWrapper.copyNonNullProperties(setting.get(), settingDTO);
      settingDTO.setSettingVisible(false);
      return Optional.of(settingDTO);
    }
   return Optional.empty();
  }

  @Override
  public String retrieveValue(String key) {
    return availableSettings.get(key);
  }

  @Override
  public List<SettingDTO> retrieveAllSettings() {
    return settingRepository.findAll().stream()
        .map(
            setting -> {
              SettingDTO settingDTO = new SettingDTO();
              BeanUtilWrapper.copyNonNullProperties(setting, settingDTO);
              settingDTO.setSettingVisible(false);
              return settingDTO;
            })
        .toList();
  }


  @Override
  public synchronized SettingDTO createUserSetting(SettingDTO settingDTO) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getPrincipal() instanceof UserDetails) {
      UserDetails userDetails = (UserDetails) auth.getPrincipal();
      String username = userDetails.getUsername();

      Setting setting =
              settingRepository
                      .findFirstByCreatedByAndKeyOrderByCreatedDateDesc(username, settingDTO.getKey())
                      .orElse(new Setting());
      BeanUtilWrapper.copyNonNullProperties(settingDTO, setting);
      setting.setCreatedBy(username);
      settingRepository.save(setting);
      //availableSettings.put(setting.getKey(),setting.getValue());
    }
    return settingDTO;
  }

  @Override
  public synchronized SettingDTO updateUserSetting(SettingDTO settingDTO) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getPrincipal() instanceof UserDetails) {
      UserDetails userDetails = (UserDetails) auth.getPrincipal();
      String username = userDetails.getUsername();

      Setting setting =
              settingRepository
                      .findFirstByCreatedByAndKeyOrderByCreatedDateDesc(username, settingDTO.getKey())
                      .orElse(new Setting());
      BeanUtilWrapper.copyNonNullProperties(settingDTO, setting);
      settingRepository.save(setting);
      //availableSettings.put(setting.getKey(),setting.getValue());
    }
    return settingDTO;
  }

  @Override
  public boolean userSettingExists(String settingKey) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getPrincipal() instanceof UserDetails) {
      UserDetails userDetails = (UserDetails) auth.getPrincipal();
      String username = userDetails.getUsername();

      return settingRepository.existsByCreatedByAndKey(username, settingKey);
    }
    return false;
  }

  @Override
  public Optional<SettingDTO> retrieveUserSetting(String key) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getPrincipal() instanceof UserDetails) {
      UserDetails userDetails = (UserDetails) auth.getPrincipal();
      String username = userDetails.getUsername();

      Optional<Setting> setting = settingRepository.findFirstByCreatedByAndKeyOrderByCreatedDateDesc(username, key);
      if (setting.isPresent()){
        SettingDTO settingDTO = new SettingDTO();
        BeanUtilWrapper.copyNonNullProperties(setting.get(), settingDTO);
        settingDTO.setSettingVisible(false);
        return Optional.of(settingDTO);
      }
    }
    return Optional.empty();
  }

  @Override
  public List<SettingDTO> retrieveAllUserSettings() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getPrincipal() instanceof UserDetails) {
      UserDetails userDetails = (UserDetails) auth.getPrincipal();
      String username = userDetails.getUsername();

      settingRepository.findAllByCreatedBy(username).stream()
              .map(
                      setting -> {
                        SettingDTO settingDTO = new SettingDTO();
                        BeanUtilWrapper.copyNonNullProperties(setting, settingDTO);
                        settingDTO.setSettingVisible(false);
                        return settingDTO;
                      })
              .toList();
    }

    return List.of();
  }

}
