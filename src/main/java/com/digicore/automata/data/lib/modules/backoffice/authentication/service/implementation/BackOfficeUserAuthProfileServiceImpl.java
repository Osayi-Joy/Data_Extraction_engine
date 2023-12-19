package com.digicore.automata.data.lib.modules.backoffice.authentication.service.implementation;
/*
 * @author Oluwatobi Ogunwuyi
 * @createdOn Jun-26(Mon)-2023
 */

import static com.digicore.automata.data.lib.modules.exception.messages.AuthorizationErrorMessage.PERMISSION_NOT_BELONGING_TO_THE_ROLE_CODE_KEY;
import static com.digicore.automata.data.lib.modules.exception.messages.AuthorizationErrorMessage.PERMISSION_NOT_BELONGING_TO_THE_ROLE_MESSAGE_KEY;
import static com.digicore.automata.data.lib.modules.exception.messages.RegistrationErrorMessage.*;

import com.digicore.automata.data.lib.modules.common.authentication.dto.UserAuthProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.service.AuthProfileService;
import com.digicore.automata.data.lib.modules.common.authorization.dto.PermissionDTO;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleDTO;
import com.digicore.automata.data.lib.modules.common.authorization.projection.AuthProfileProjection;
import com.digicore.automata.data.lib.modules.common.authorization.service.PermissionService;
import com.digicore.automata.data.lib.modules.common.authorization.service.RoleService;
import com.digicore.automata.data.lib.modules.common.registration.dto.UserRegistrationDTO;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.automata.data.lib.modules.backoffice.authentication.model.BackOfficeUserAuthProfile;
import com.digicore.automata.data.lib.modules.backoffice.authentication.repository.BackOfficeUserAuthProfileRepository;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficePermission;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficeRole;
import com.digicore.automata.data.lib.modules.backoffice.profile.model.BackOfficeUserProfile;
import com.digicore.common.util.BeanUtilWrapper;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import com.digicore.registhentication.registration.enums.Status;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BackOfficeUserAuthProfileServiceImpl implements AuthProfileService<UserAuthProfileDTO> {

  private final BackOfficeUserAuthProfileRepository backOfficeUserAuthProfileRepository;
  private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
  private final PasswordEncoder passwordEncoder;
  private final PermissionService<PermissionDTO, BackOfficePermission>
      backOfficePermissionServiceImpl;
  private final RoleService<RoleDTO, BackOfficeRole> backOfficeRoleServiceImpl;
  private final SettingService settingService;

  private static UserAuthProfileDTO getProfileDTO(
      BackOfficeUserAuthProfile backOfficeUserAuthProfile) {
    UserAuthProfileDTO backOfficeUserAuthProfileDTO = new UserAuthProfileDTO();
    UserProfileDTO userProfileDTO = new UserProfileDTO();
    userProfileDTO.setUsername(backOfficeUserAuthProfile.getUsername());
    userProfileDTO.setStatus(backOfficeUserAuthProfile.getStatus());
    userProfileDTO.setAssignedRole(backOfficeUserAuthProfile.getAssignedRole());
    userProfileDTO.setPhoneNumber(
        StringUtils.isBlank(backOfficeUserAuthProfile.getBackOfficeUserProfile().getPhoneNumber())
            ? "N/S"
            : backOfficeUserAuthProfile.getBackOfficeUserProfile().getPhoneNumber());
    userProfileDTO.setEmail(backOfficeUserAuthProfile.getBackOfficeUserProfile().getEmail());
    userProfileDTO.setFirstName(
        backOfficeUserAuthProfile.getBackOfficeUserProfile().getFirstName());
    userProfileDTO.setLastName(backOfficeUserAuthProfile.getBackOfficeUserProfile().getLastName());
    userProfileDTO.setReferralCode(
        backOfficeUserAuthProfile.getBackOfficeUserProfile().getReferralCode());
    userProfileDTO.setProfileId(
        backOfficeUserAuthProfile.getBackOfficeUserProfile().getProfileId());
    userProfileDTO.setPassword(null);
    userProfileDTO.setPin(null);
    backOfficeUserAuthProfileDTO.setUserProfile(userProfileDTO);
    return backOfficeUserAuthProfileDTO;
  }

  @Override
  public UserAuthProfileDTO retrieveAuthProfile(String email) {
    BackOfficeUserAuthProfile backOfficeUserAuthProfile =
        backOfficeUserAuthProfileRepository
            .findFirstByUsernameOrderByCreatedDate(email)
            .orElseThrow(
                () ->
                    exceptionHandler.processBadRequestException(
                        settingService.retrieveValue(PROFILE_NOT_EXIST_MESSAGE_KEY),
                        settingService.retrieveValue(PROFILE_NOT_EXIST_CODE_KEY)));
    return mapBackOfficeUserAuthProfileEntityToDTO(backOfficeUserAuthProfile);
  }

  @Override
  public BackOfficeUserAuthProfile retrieveAuthProfileForPasswordReset(String email) {
    return backOfficeUserAuthProfileRepository
        .findFirstByUsernameOrderByCreatedDate(email)
        .orElseThrow(
            () ->
                exceptionHandler.processBadRequestException(
                    settingService.retrieveValue(PROFILE_NOT_EXIST_MESSAGE_KEY),
                    settingService.retrieveValue(PROFILE_NOT_EXIST_CODE_KEY)));
  }

  @Override
  public void updateAuthProfile(UserAuthProfileDTO authProfile) {
    backOfficeRoleServiceImpl.checkIfRoleIsNotSystemRole(authProfile.getAssignedRole());
    backOfficeRoleServiceImpl.roleCheck(authProfile.getAssignedRole());
    backOfficeUserAuthProfileRepository.save(ensureAuthProfileInfoRemainsValid(authProfile));
  }

  @Override
  public void deleteAuthProfile(String username) {
    BackOfficeUserAuthProfile existingAuthProfile =
        backOfficeUserAuthProfileRepository
            .findFirstByUsernameOrderByCreatedDate(username)
            .orElseThrow(
                () ->
                    exceptionHandler.processBadRequestException(
                        settingService.retrieveValue(PROFILE_NOT_EXIST_MESSAGE_KEY),
                        settingService.retrieveValue(PROFILE_NOT_EXIST_CODE_KEY)));

    existingAuthProfile.setDeleted(true);
    existingAuthProfile.setStatus(Status.DELETED);
    existingAuthProfile.setUsername(
        username.concat("_deleted_").concat(LocalDateTime.now().toString()));
    backOfficeUserAuthProfileRepository.save(existingAuthProfile);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
  public <V, K, U> void saveNewAuthProfile(K registrationDTO, U role, V userProfile) {
    UserRegistrationDTO registrationRequest = (UserRegistrationDTO) registrationDTO;
    BackOfficeRole backOfficeRole = (BackOfficeRole) role;
    BackOfficeUserProfile backOfficeUserProfile = (BackOfficeUserProfile) userProfile;
    doProfileCheck(registrationRequest.getUsername());
    BackOfficeUserAuthProfile backOfficeUserAuthProfile = new BackOfficeUserAuthProfile();
    BeanUtilWrapper.copyNonNullProperties(registrationRequest, backOfficeUserAuthProfile);
    backOfficeUserAuthProfile.setBackOfficeUserProfile(backOfficeUserProfile);
    backOfficeUserAuthProfile.setPin("N/A");
    backOfficeUserAuthProfile.setPassword(
        passwordEncoder.encode(registrationRequest.getPassword()));
    backOfficeUserAuthProfile.setDefaultPassword(true);
    backOfficeUserAuthProfile.setStatus(Status.PENDING_INVITE_ACCEPTANCE);
    backOfficeUserAuthProfile.setAssignedRole(backOfficeRole.getName());
    backOfficeUserAuthProfile.setPermissions(new HashSet<>(backOfficeRole.getPermissions()));
    backOfficeUserAuthProfileRepository.save(backOfficeUserAuthProfile);
  }

  private BackOfficeUserAuthProfile ensureAuthProfileInfoRemainsValid(
      UserAuthProfileDTO authProfile) {
    BackOfficeUserAuthProfile existingAuthProfile =
        backOfficeUserAuthProfileRepository
            .findFirstByUsernameOrderByCreatedDate(authProfile.getUsername())
            .orElseThrow(
                () ->
                    exceptionHandler.processBadRequestException(
                        settingService.retrieveValue(PROFILE_NOT_EXIST_MESSAGE_KEY),
                        settingService.retrieveValue(PROFILE_NOT_EXIST_CODE_KEY)));

    if (null != authProfile.getAssignedRole()
        && !authProfile.getAssignedRole().isEmpty()
        && (!existingAuthProfile
            .getAssignedRole()
            .equalsIgnoreCase(authProfile.getAssignedRole()))) {
      //        existingAuthProfile.setPermissions(
     //         backOfficeRoleServiceImpl.retrieveRole(authProfile.getAssignedRole()).getPermissions());
      existingAuthProfile.setAssignedRole(authProfile.getAssignedRole());

    }

    if (null != authProfile.getPassword() && !authProfile.getPassword().isEmpty())
      existingAuthProfile.setPassword(passwordEncoder.encode(authProfile.getPassword()));

    if (null != authProfile.getStatus()) existingAuthProfile.setStatus(authProfile.getStatus());

    if (null != authProfile.getUsername() && !authProfile.getUsername().isEmpty())
      existingAuthProfile.setUsername(authProfile.getUsername());

    existingAuthProfile.setDefaultPassword(authProfile.isDefaultPassword());

    return existingAuthProfile;
  }

  //do not delete
  private void ensureOnlyPermissionAssignedUnderTheSpecifiedRoleIsAllowed(
      UserAuthProfileDTO authProfile, BackOfficeUserAuthProfile existingAuthProfile) {
    if (null != authProfile.getPermissions() && !authProfile.getPermissions().isEmpty()) {
      if (existingAuthProfile.getAssignedRole().equalsIgnoreCase(authProfile.getAssignedRole())) {
        Set<String> existingPermissions =
            existingAuthProfile.getPermissions().stream()
                .map(BackOfficePermission::getName)
                .collect(Collectors.toSet());
        authProfile
            .getPermissions()
            .forEach(
                permissionDTO -> {
                  if (!existingPermissions.contains(permissionDTO.getName()))
                    throw exceptionHandler.processBadRequestException(
                        String.format(
                            settingService.retrieveValue(
                                PERMISSION_NOT_BELONGING_TO_THE_ROLE_MESSAGE_KEY),
                            permissionDTO.getName(),
                            authProfile.getAssignedRole()),
                        settingService.retrieveValue(
                            PERMISSION_NOT_BELONGING_TO_THE_ROLE_CODE_KEY));
                });
        existingAuthProfile.setPermissions(
            backOfficePermissionServiceImpl.getValidPermissions(
                authProfile.getPermissions().stream()
                    .map(PermissionDTO::getName)
                    .collect(Collectors.toSet())));
      } else {
        BackOfficeRole backOfficeRole =
            backOfficeRoleServiceImpl.retrieveRole(authProfile.getAssignedRole());
        Set<String> newPermissions =
            backOfficeRole.getPermissions().stream()
                .map(BackOfficePermission::getName)
                .collect(Collectors.toSet());
        authProfile
            .getPermissions()
            .forEach(
                permissionDTO -> {
                  if (!newPermissions.contains(permissionDTO.getName()))
                    throw exceptionHandler.processBadRequestException(
                        String.format(
                            settingService.retrieveValue(
                                PERMISSION_NOT_BELONGING_TO_THE_ROLE_MESSAGE_KEY),
                            permissionDTO.getName(),
                            authProfile.getAssignedRole()),
                        settingService.retrieveValue(
                            PERMISSION_NOT_BELONGING_TO_THE_ROLE_CODE_KEY));
                });
        existingAuthProfile.setPermissions(
            backOfficePermissionServiceImpl.getValidPermissions(
                authProfile.getPermissions().stream()
                    .map(PermissionDTO::getName)
                    .collect(Collectors.toSet())));
      }
    }
  }

  @Override
  public AuthProfileProjection retrieveUserRole(Long backOfficeUserProfileId) {
    Optional<AuthProfileProjection> backOfficeUserAuthProfileProjection =
        backOfficeUserAuthProfileRepository.findFirstByBackOfficeUserProfileId(
            backOfficeUserProfileId);
    return backOfficeUserAuthProfileProjection.orElse(null);
  }

  @Override
  public Page<UserAuthProfileDTO> retrieveUserByRoleOrUsername(
      String role, String username, Pageable pageable) {
    return backOfficeUserAuthProfileRepository
        .findAllByIsDeletedFalseAndAssignedRoleContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrderByCreatedDate(
            role, username, pageable)
        .map(BackOfficeUserAuthProfileServiceImpl::getProfileDTO);
  }

  @Override
  public Page<UserAuthProfileDTO> retrieveUserByStatusOrCreatedDate(
      Status status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
    return backOfficeUserAuthProfileRepository
        .findAllByStatusAndIsDeletedFalseAndCreatedDateBetweenOrderByCreatedDate(
            status, startDate, endDate, pageable)
        .map(BackOfficeUserAuthProfileServiceImpl::getProfileDTO);
  }

  private void doProfileCheck(String username) {
    if (backOfficeUserAuthProfileRepository.existsByUsername(username))
      exceptionHandler.processBadRequestException(
          settingService.retrieveValue(PROFILE_EXISTS_MESSAGE_KEY),
          settingService.retrieveValue(PROFILE_EXISTS_CODE_KEY),
          settingService.retrieveValue(PROFILE_EXISTS_CODE_KEY));
  }

  private UserAuthProfileDTO mapBackOfficeUserAuthProfileEntityToDTO(
      BackOfficeUserAuthProfile backOfficeUserAuthProfile) {
    UserAuthProfileDTO userAuthProfileDTO = new UserAuthProfileDTO();
    userAuthProfileDTO.setPermissions(
            backOfficeRoleServiceImpl.retrieveRole(backOfficeUserAuthProfile.getAssignedRole()).getPermissions().stream()
            .map(backOfficePermissionServiceImpl::mapEntityToDTO)
            .collect(Collectors.toSet()));
    BeanUtilWrapper.copyNonNullProperties(backOfficeUserAuthProfile, userAuthProfileDTO);
    UserProfileDTO userProfileDTO = new UserProfileDTO();
    BeanUtilWrapper.copyNonNullProperties(backOfficeUserAuthProfile.getBackOfficeUserProfile(),userProfileDTO);
    userAuthProfileDTO.setUserProfile(userProfileDTO);
    return userAuthProfileDTO;
  }
}
