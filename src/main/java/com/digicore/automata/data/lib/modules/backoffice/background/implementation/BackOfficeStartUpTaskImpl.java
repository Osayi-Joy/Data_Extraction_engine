package com.digicore.automata.data.lib.modules.backoffice.background.implementation;

import com.digicore.automata.data.lib.modules.backoffice.authentication.service.implementation.BackOfficeUserAuthServiceImpl;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficePermission;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficeRole;
import com.digicore.automata.data.lib.modules.backoffice.registration.services.implementation.BackOfficeUserRegistrationServiceImpl;
import com.digicore.automata.data.lib.modules.common.authorization.dto.PermissionDTO;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleDTO;
import com.digicore.automata.data.lib.modules.common.authorization.service.PermissionService;
import com.digicore.automata.data.lib.modules.common.authorization.service.RoleService;
import com.digicore.automata.data.lib.modules.common.background.services.BackGroundService;
import com.digicore.common.util.ClientUtil;
import com.digicore.config.properties.PropertyConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
@Profile("backOffice")
public class BackOfficeStartUpTaskImpl implements BackGroundService {
  private final PropertyConfig propertyConfig;
  private final RoleService<RoleDTO, BackOfficeRole> backOfficeRoleServiceImpl;
  private final PermissionService<PermissionDTO, BackOfficePermission> backOfficePermissionServiceImpl;
  private final BackOfficeUserRegistrationServiceImpl backOfficeUserRegistrationService;
  private final BackOfficeUserAuthServiceImpl backOfficeUserAuthServiceImpl;

  @Override
  @EventListener(ContextRefreshedEvent.class)
  public void runSystemStartUpTask() {
    updateSystemPermissions();
  }
  @Override
  @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
  public void disableInactiveAccounts() {
    LocalDate thresholdDate = LocalDate.now().minusDays(propertyConfig.getInactivityDays());
    backOfficeUserAuthServiceImpl.disableInactiveAccounts(thresholdDate);
  }

  private void updateSystemPermissions() {
    File file = getSystemFile(propertyConfig.getSystemDefinedPermissions());
    Set<BackOfficePermission> newAuthorities;
    try {
      newAuthorities = ClientUtil.getObjectMapper().readValue(file, new TypeReference<>() {});
    } catch (IOException ignored) {
      log.trace("no update required");
      return;
    }
    if ("systemPermissionUpdate.json".equals(file.getName())) {
      backOfficePermissionServiceImpl.addSystemPermissions(newAuthorities);
      backOfficeRoleServiceImpl.systemRolesChecks();
      backOfficeUserRegistrationService.systemUsersChecks();
    }
  }




  private File getSystemFile(String filePath) {
    Path path = Paths.get(filePath);
    return path.toFile();
  }

}
