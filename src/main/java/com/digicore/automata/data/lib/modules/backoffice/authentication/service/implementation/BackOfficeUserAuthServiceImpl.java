package com.digicore.automata.data.lib.modules.backoffice.authentication.service.implementation;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import com.digicore.automata.data.lib.modules.common.authentication.dto.UserProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.service.implementation.LoginServiceHelper;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleDTO;
import com.digicore.automata.data.lib.modules.common.authorization.service.RoleService;
import com.digicore.automata.data.lib.modules.common.settings.dto.SettingDTO;
import com.digicore.automata.data.lib.modules.common.settings.model.Setting;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.automata.data.lib.modules.backoffice.authentication.model.BackOfficeUserAuthProfile;
import com.digicore.automata.data.lib.modules.backoffice.authentication.repository.BackOfficeUserAuthProfileRepository;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficePermission;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficeRole;
import com.digicore.common.util.BeanUtilWrapper;
import com.digicore.registhentication.authentication.dtos.request.LoginRequestDTO;
import com.digicore.registhentication.authentication.dtos.response.LoginResponse;
import com.digicore.registhentication.authentication.enums.AuthenticationType;
import com.digicore.registhentication.authentication.services.LoginAttemptService;
import org.jboss.aerogear.security.otp.Totp;
import com.digicore.registhentication.authentication.services.LoginService;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import com.digicore.registhentication.registration.enums.Status;
import lombok.RequiredArgsConstructor;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.*;

import static com.digicore.automata.data.lib.modules.exception.messages.BackOfficeProfileErrorMessage.BACKOFFICE_PROFILE_DISABLED_CODE_KEY;
import static com.digicore.automata.data.lib.modules.exception.messages.BackOfficeProfileErrorMessage.BACKOFFICE_PROFILE_DISABLED_MESSAGE_KEY;
import static com.digicore.automata.data.lib.modules.exception.messages.LoginErrorMessage.*;
import static com.digicore.automata.data.lib.modules.exception.messages.LoginErrorMessage.LOGIN_FAILED_CODE_KEY;
import static com.digicore.automata.data.lib.modules.exception.messages.RegistrationErrorMessage.PROFILE_NOT_EXIST_CODE_KEY;
import static com.digicore.automata.data.lib.modules.exception.messages.RegistrationErrorMessage.PROFILE_NOT_EXIST_MESSAGE_KEY;

@Service
@RequiredArgsConstructor
public class BackOfficeUserAuthServiceImpl implements UserDetailsService,
        LoginService<LoginResponse, LoginRequestDTO> {

 private final BackOfficeUserAuthProfileRepository backOfficeUserAuthProfileRepository;
 private final SettingService settingService;
 private final RoleService<RoleDTO, BackOfficeRole> backOfficeRoleServiceImpl;
 private final LoginServiceHelper loginServiceHelper;
 private final LoginAttemptService loginAttemptService;
 private final PasswordEncoder passwordEncoder;
 private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;

 @Override
 public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
  BackOfficeUserAuthProfile userFoundInDB =
          backOfficeUserAuthProfileRepository
                  .findFirstByUsernameOrderByCreatedDate(username)
                  .orElseThrow(
                          () ->
                                  exceptionHandler.processCustomException(
                                          settingService.retrieveValue(LOGIN_FAILED_MESSAGE_KEY),
                                          settingService.retrieveValue(LOGIN_FAILED_USER_NOT_FOUND_CODE_KEY),
                                          HttpStatus.UNAUTHORIZED));
  if (Status.INACTIVE.equals(userFoundInDB.getStatus()))
   throw exceptionHandler.processCustomException(
           settingService.retrieveValue(BACKOFFICE_PROFILE_DISABLED_MESSAGE_KEY),
           settingService.retrieveValue(BACKOFFICE_PROFILE_DISABLED_CODE_KEY),
           HttpStatus.UNAUTHORIZED);

  return getUserProfileDTO(username, userFoundInDB);
 }

 @Override
 public LoginResponse authenticate(LoginRequestDTO loginRequestDTO) {
  UserProfileDTO userDetails =
          (UserProfileDTO) this.loadUserByUsername(loginRequestDTO.getUsername());
  backOfficeRoleServiceImpl.checkRoleStatus(userDetails.getAssignedRole());
   if (passwordEncoder.matches(loginRequestDTO.getPassword(), userDetails.getPassword())) {
    loginAttemptService.verifyLoginAccess(userDetails.getUsername(), true);
    if(userDetails.isEnabled2FA()){
     return loginServiceHelper.get2faEnabledLoginResponse();
    }
    return loginServiceHelper.getLoginResponse(loginRequestDTO, userDetails);
   }

  loginAttemptService.verifyLoginAccess(userDetails.getUsername(), false);
  exceptionHandler.processCustomException(
          settingService.retrieveValue(LOGIN_FAILED_MESSAGE_KEY),
          settingService.retrieveValue(LOGIN_FAILED_CODE_KEY),
          HttpStatus.UNAUTHORIZED,
          settingService.retrieveValue(LOGIN_FAILED_CODE_KEY));
  return null;

 }

 private Set<SimpleGrantedAuthority> getGrantedAuthorities(Collection<BackOfficePermission> privileges) {
  Set<SimpleGrantedAuthority> authorities = new HashSet<>();
  for (BackOfficePermission permission : privileges) {
   authorities.add(new SimpleGrantedAuthority(permission.getName()));
  }
  return authorities;
 }

 private Set<SimpleGrantedAuthority> getGrantedAuthorities(String assignedRole) {
  BackOfficeRole backOfficeRole = backOfficeRoleServiceImpl.retrieveRole(assignedRole);
  Set<SimpleGrantedAuthority> authorities = new HashSet<>();
  for (BackOfficePermission permission : backOfficeRole.getPermissions()) {
   authorities.add(new SimpleGrantedAuthority(permission.getName()));
  }
  return authorities;
 }


 public void disableInactiveAccounts(LocalDate thresholdDate) {
  List<BackOfficeUserAuthProfile> inactiveUsers = backOfficeUserAuthProfileRepository
          .findByLastLoginDateBeforeAndStatus(thresholdDate.atStartOfDay(), Status.ACTIVE);

  inactiveUsers.forEach(user -> user.setStatus(Status.INACTIVE));
  backOfficeUserAuthProfileRepository.saveAll(inactiveUsers);
 }


 private UserProfileDTO getUserProfileDTO(
         String username, BackOfficeUserAuthProfile userFoundInDB) {
  UserProfileDTO userProfileDTO = new UserProfileDTO();
  userProfileDTO.setUsername(username);
  userProfileDTO.setFirstName(userFoundInDB.getBackOfficeUserProfile().getFirstName());
  userProfileDTO.setLastName(userFoundInDB.getBackOfficeUserProfile().getLastName());
  userProfileDTO.setAssignedRole(userFoundInDB.getAssignedRole());
  userProfileDTO.setEmail(userFoundInDB.getBackOfficeUserProfile().getEmail());
  userProfileDTO.setProfileId(userFoundInDB.getBackOfficeUserProfile().getProfileId());
  userProfileDTO.setReferralCode(userFoundInDB.getBackOfficeUserProfile().getReferralCode());
  userProfileDTO.setPassword(userFoundInDB.getPassword());
  userProfileDTO.setPin(userFoundInDB.getPin());
  userProfileDTO.setDefaultPassword(userFoundInDB.isDefaultPassword());
  userProfileDTO.setEnabled2FA(userFoundInDB.isEnabled2fa());
  userProfileDTO.setPermissions(getGrantedAuthorities(userFoundInDB.getAssignedRole()));

  return userProfileDTO;
 }

}
