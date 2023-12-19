package com.digicore.automata.data.lib.modules.backoffice.authorization.service.implementation;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import static com.digicore.automata.data.lib.modules.exception.messages.AuthorizationErrorMessage.*;

import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficePermission;
import com.digicore.automata.data.lib.modules.common.authorization.dto.PermissionDTO;
import com.digicore.automata.data.lib.modules.common.authorization.service.PermissionService;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.automata.data.lib.modules.backoffice.authorization.repository.BackOfficePermissionRepository;
import com.digicore.common.util.BeanUtilWrapper;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackOfficePermissionServiceImpl
    implements PermissionService<PermissionDTO, BackOfficePermission> {
  private final BackOfficePermissionRepository permissionRepository;
  private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
  private final SettingService settingService;

  @Override
  public Set<PermissionDTO> retrieveAllSystemPermissions() {
    return permissionRepository.findAll().stream()
        .map(this::mapEntityToDTO)
        .collect(Collectors.toSet());
  }

  @Override
  public PermissionDTO retrieveSystemPermissionByName(String name) {
    return mapEntityToDTO(
        permissionRepository
            .findFirstByNameOrderByCreatedDate(name)
            .orElseThrow(
                () ->
                    exceptionHandler.processBadRequestException(
                        settingService.retrieveValue(
                            PERMISSION_NOT_IN_SYSTEM_MESSAGE_KEY.replace("{}", name)),
                        settingService.retrieveValue(PERMISSION_NOT_IN_SYSTEM_CODE_KEY))));
  }

  @Override
  public BackOfficePermission retrieveSystemPermission(String name) {
    return permissionRepository
        .findFirstByNameOrderByCreatedDate(name)
        .orElseThrow(
            () ->
                exceptionHandler.processBadRequestException(
                    settingService.retrieveValue(
                        PERMISSION_NOT_IN_SYSTEM_MESSAGE_KEY.replace("{}", name)),
                    settingService.retrieveValue(PERMISSION_NOT_IN_SYSTEM_CODE_KEY)));
  }

  @Override
  public void addSystemPermissions(Set<BackOfficePermission> newPermissions) {
    newPermissions.forEach(
        permission -> {
          Optional<BackOfficePermission> existingPermission =
              permissionRepository.findFirstByNameOrderByCreatedDate(permission.getName());
          if (existingPermission.isPresent()) {
            permission.setId(existingPermission.get().getId());
            permission.setPermissionType(existingPermission.get().getPermissionType());
            permission.setName(existingPermission.get().getName());
            permission.setDeleted(existingPermission.get().isDeleted());
            permission.setDescription(existingPermission.get().getDescription());
            permission.setPermissionType(existingPermission.get().getPermissionType());
          }
        });
    permissionRepository.saveAll(newPermissions);
  }

  @Override
  public Set<BackOfficePermission> getValidPermissions(Set<String> permission) {
    return permissionCheck(permission);
  }

  @Override
  public void verifyPermissions(Set<String> permission) {
    permissionCheck(permission);
  }

  private Set<BackOfficePermission> permissionCheck(Set<String> selectedPermissions) {
    Set<BackOfficePermission> permissionSet = new HashSet<>();
    boolean treatRequestAdded = false;
    for (String permission : selectedPermissions) {
      permissionSet.add(retrieveSystemPermission(permission));
      if (permission.startsWith("approve-")) {
        String remainingWords = permission.substring("approve-".length());
        if (selectedPermissions.contains(remainingWords)) {
          exceptionHandler.processBadRequestException(
              settingService.retrieveValue(
                  ROLE_SHOULD_NOT_CONTAIN_A_CHECKER_AND_A_MAKER_PERMISSION_MESSAGE_KEY),
              settingService.retrieveValue(
                  ROLE_SHOULD_NOT_CONTAIN_A_CHECKER_AND_A_MAKER_PERMISSION_CODE_KEY),
              settingService.retrieveValue(
                  ROLE_SHOULD_NOT_CONTAIN_A_CHECKER_AND_A_MAKER_PERMISSION_CODE_KEY));
        }
        if (!selectedPermissions.contains("treat-requests") && !treatRequestAdded) {
          permissionSet.add(retrieveSystemPermission("treat-requests"));
          treatRequestAdded = true;
          }
      }
    }
    return permissionSet;
  }

  public PermissionDTO mapEntityToDTO(BackOfficePermission permission) {
    PermissionDTO permissionDTO = new PermissionDTO();
    BeanUtilWrapper.copyNonNullProperties(permission, permissionDTO);
    return permissionDTO;
  }

  @Override
  public BackOfficePermission mapDTOToEntity(PermissionDTO permissionDTO) {
    BackOfficePermission permission = new BackOfficePermission();
    BeanUtilWrapper.copyNonNullProperties(permissionDTO, permission);
    return permission;
  }
}
