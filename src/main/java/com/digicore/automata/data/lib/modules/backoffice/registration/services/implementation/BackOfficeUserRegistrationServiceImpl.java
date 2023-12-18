package com.digicore.automata.data.lib.modules.backoffice.registration.services.implementation;

import static com.digicore.automata.data.lib.modules.exception.messages.RegistrationErrorMessage.*;

import com.digicore.automata.data.lib.modules.backoffice.registration.services.BackOfficeServiceUserRegistrationService;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserAuthProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.service.AuthProfileService;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleDTO;
import com.digicore.automata.data.lib.modules.common.authorization.service.RoleService;
import com.digicore.automata.data.lib.modules.common.constants.SystemConstants;
import com.digicore.automata.data.lib.modules.common.registration.dto.UserRegistrationDTO;
import com.digicore.automata.data.lib.modules.common.registration.util.RegistrationUtil;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficeRole;
import com.digicore.automata.data.lib.modules.backoffice.profile.model.BackOfficeUserProfile;
import com.digicore.automata.data.lib.modules.backoffice.profile.repository.BackOfficeUserProfileRepository;
import com.digicore.common.util.BeanUtilWrapper;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import com.digicore.registhentication.registration.services.RegistrationService;
import com.digicore.registhentication.util.IDGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
@Service
@RequiredArgsConstructor
public class BackOfficeUserRegistrationServiceImpl
    implements RegistrationService<UserProfileDTO, UserRegistrationDTO>,
        BackOfficeServiceUserRegistrationService {
  private final BackOfficeUserProfileRepository userProfileRepository;
  private final AuthProfileService<UserAuthProfileDTO> backOfficeUserAuthProfileServiceImpl;
  private final RoleService<RoleDTO, BackOfficeRole> backOfficeRoleServiceImpl;
  private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
  private final SettingService settingService;



  @Override
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
  public UserProfileDTO createProfile(UserRegistrationDTO registrationRequest) {
    doProfileCheck(registrationRequest.getEmail());
    if (!SystemConstants.MAKER_EMAIL.equalsIgnoreCase(registrationRequest.getEmail())
        && !SystemConstants.CHECKER_EMAIL.equalsIgnoreCase(registrationRequest.getEmail())) {
      backOfficeRoleServiceImpl.checkSystemDefaultRolesStatus();
      backOfficeRoleServiceImpl.checkIfRoleIsNotSystemRole(registrationRequest.getAssignedRole());
      }
    backOfficeUserAuthProfileServiceImpl.saveNewAuthProfile(
        registrationRequest,
            backOfficeRoleServiceImpl.retrieveRole(registrationRequest.getAssignedRole()),
        createBackOfficeUserProfile(registrationRequest));
    return RegistrationUtil.getUserProfileDTO(registrationRequest);
  }
  @Override
  public void doProfileCheck(String email) {
    if (profileExistenceCheckByEmail(email))
      exceptionHandler.processBadRequestException(
              settingService.retrieveValue(PROFILE_EXISTS_MESSAGE_KEY),
              settingService.retrieveValue(PROFILE_EXISTS_CODE_KEY),
              settingService.retrieveValue(PROFILE_EXISTS_CODE_KEY));
  }

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
  public BackOfficeUserProfile createBackOfficeUserProfile(
      UserRegistrationDTO registrationRequest) {
    BackOfficeUserProfile backOfficeUserProfile = new BackOfficeUserProfile();
    BeanUtilWrapper.copyNonNullProperties(registrationRequest, backOfficeUserProfile);
    backOfficeUserProfile.setProfileId(IDGeneratorUtil.generateProfileId("BAC-").toUpperCase());
    backOfficeUserProfile.setReferralCode(IDGeneratorUtil.generateRefId());
    userProfileRepository.save(backOfficeUserProfile);
    return backOfficeUserProfile;
  }

  @Override
  public boolean profileExistenceCheckByEmail(String email) {
    return userProfileRepository.existsByEmail(email);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
  public void systemUsersChecks() {
    if (profileExistenceCheckByEmail(SystemConstants.MAKER_EMAIL) || profileExistenceCheckByEmail(SystemConstants.CHECKER_EMAIL))
      return;
    UserRegistrationDTO makerPayload = new UserRegistrationDTO();
    UserRegistrationDTO checkerPayload = new UserRegistrationDTO();

    makerPayload.setAssignedRole(SystemConstants.MAKER_ROLE_NAME);
    makerPayload.setEmail(SystemConstants.MAKER_EMAIL);
    makerPayload.setFirstName("SYSTEM DEFAULT MAKER");
    makerPayload.setLastName("SYSTEM DEFAULT MAKER");
    makerPayload.setPassword(SystemConstants.SYSTEM_DEFAULT_PASSWORD);
    makerPayload.setUsername(SystemConstants.MAKER_EMAIL);

    checkerPayload.setAssignedRole(SystemConstants.CHECKER_ROLE_NAME);
    checkerPayload.setEmail(SystemConstants.CHECKER_EMAIL);
    checkerPayload.setFirstName("SYSTEM DEFAULT CHECKER");
    checkerPayload.setLastName("SYSTEM DEFAULT CHECKER");
    checkerPayload.setPassword(SystemConstants.SYSTEM_DEFAULT_PASSWORD);
    checkerPayload.setUsername(SystemConstants.CHECKER_EMAIL);

    createProfile(checkerPayload);
    createProfile(makerPayload);
  }
}
