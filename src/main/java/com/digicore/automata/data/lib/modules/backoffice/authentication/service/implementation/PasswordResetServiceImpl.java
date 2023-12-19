package com.digicore.automata.data.lib.modules.backoffice.authentication.service.implementation;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import static com.digicore.automata.data.lib.modules.exception.messages.BackOfficeProfileErrorMessage.*;
import static com.digicore.automata.data.lib.modules.exception.messages.RegistrationErrorMessage.*;

import com.digicore.automata.data.lib.modules.common.authentication.dto.UserAuthProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.service.AuthProfileService;
import com.digicore.automata.data.lib.modules.common.constants.SystemConstants;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.automata.data.lib.modules.backoffice.authentication.model.BackOfficePasswordHistory;
import com.digicore.automata.data.lib.modules.backoffice.authentication.model.BackOfficeUserAuthProfile;
import com.digicore.automata.data.lib.modules.backoffice.authentication.repository.BackOfficeUserAuthProfileRepository;
import com.digicore.automata.data.lib.modules.backoffice.authentication.repository.BackOfficeUserPasswordHistoryRepository;
import com.digicore.common.util.ClientUtil;
import com.digicore.otp.enums.OtpType;
import com.digicore.otp.service.OtpService;
import com.digicore.registhentication.authentication.dtos.request.ResetPasswordSecondBaseRequestDTO;
import com.digicore.registhentication.authentication.dtos.request.UpdatePasswordRequestDTO;
import com.digicore.registhentication.authentication.services.PasswordResetService;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import com.digicore.registhentication.registration.enums.Status;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {
  private final AuthProfileService<UserAuthProfileDTO> backOfficeUserAuthProfileServiceImpl;
  private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
  private final OtpService otpService;
  private final SettingService settingService;

  private final PasswordEncoder passwordEncoder;
  private final BackOfficeUserPasswordHistoryRepository backOfficeUserPasswordHistoryRepository;
  private final BackOfficeUserAuthProfileRepository backOfficeUserAuthProfileRepository;

  @Override
  public void updateAccountPassword(ResetPasswordSecondBaseRequestDTO passwordFirstBaseRequestDTO) {
    otpService.effect(
        passwordFirstBaseRequestDTO.getEmail(),
        OtpType.PASSWORD_UPDATE,
        passwordFirstBaseRequestDTO.getOtp());
    updateAuthProfilePassword(
        passwordFirstBaseRequestDTO.getEmail(), passwordFirstBaseRequestDTO.getNewPassword(), true);
  }

  @Override
  public void updateAccountPasswordWithoutVerification(String email, String plainPassword) {
    BackOfficeUserAuthProfile backOfficeAuthProfile =
        backOfficeUserAuthProfileServiceImpl.retrieveAuthProfileForPasswordReset(email);
    passwordIntegrityChecks(plainPassword, backOfficeAuthProfile);
    updateAuthProfilePassword(email, plainPassword, false);
  }

  @Override
  public void updateAccountPassword(UpdatePasswordRequestDTO updatePasswordRequestDTO) {
    BackOfficeUserAuthProfile backOfficeAuthProfile =
            backOfficeUserAuthProfileServiceImpl.retrieveAuthProfileForPasswordReset(ClientUtil.getLoggedInUsername());
    if (passwordEncoder.matches(updatePasswordRequestDTO.getOldPassword(), backOfficeAuthProfile.getPassword())) {
      passwordIntegrityChecks(updatePasswordRequestDTO.getNewPassword(), backOfficeAuthProfile);
      updateAuthProfilePassword(ClientUtil.getLoggedInUsername(), updatePasswordRequestDTO.getNewPassword(), false);
      return;
    }

    exceptionHandler.processBadRequestException(
              settingService.retrieveValue(BACKOFFICE_PASSWORD_WRONG_OLD_PASSWORD_MESSAGE_KEY),
              settingService.retrieveValue(BACKOFFICE_PASSWORD_WRONG_OLD_PASSWORD_CODE_KEY),
              settingService.retrieveValue(BACKOFFICE_PASSWORD_WRONG_OLD_PASSWORD_CODE_KEY));

  }


  private void passwordIntegrityChecks(
      String plainPassword, BackOfficeUserAuthProfile backOfficeUserAuthProfile) {
    List<BackOfficePasswordHistory> passwordHistories =
        new ArrayList<>(backOfficeUserAuthProfile.getBackOfficePasswordHistories());

    if (passwordEncoder.matches(plainPassword, backOfficeUserAuthProfile.getPassword())
        || passwordHistories.stream()
            .anyMatch(s -> passwordEncoder.matches(plainPassword, s.getOldPassword())))
      throw exceptionHandler.processBadRequestException(
          settingService.retrieveValue(BACKOFFICE_PASSWORD_ALREADY_USED_MESSAGE_KEY),
          settingService.retrieveValue(BACKOFFICE_PASSWORD_ALREADY_USED_CODE_KEY));

    if (backOfficeUserAuthProfile.getBackOfficePasswordHistories().size() > 5) {
      long id = passwordHistories.get(0).getId();
      backOfficeUserAuthProfile.getBackOfficePasswordHistories().remove(passwordHistories.get(0));
      passwordHistories.get(0).setBackOfficeUserAuthProfile(null);
      passwordHistories.remove(0);
      backOfficeUserAuthProfileRepository.save(backOfficeUserAuthProfile);
      backOfficeUserPasswordHistoryRepository.deleteById(id);
    }

    String encodePassword = passwordEncoder.encode(plainPassword);
    BackOfficePasswordHistory passwordHistory = new BackOfficePasswordHistory();
    passwordHistory.setOldPassword(encodePassword);
    passwordHistory.setBackOfficeUserAuthProfile(backOfficeUserAuthProfile);
    passwordHistories.add(passwordHistory);
    backOfficeUserPasswordHistoryRepository.save(passwordHistory);

    backOfficeUserAuthProfile.setBackOfficePasswordHistories(new HashSet<>(passwordHistories));
    backOfficeUserAuthProfile.setPassword(encodePassword);
  }

  private void updateAuthProfilePassword(
      String email, String plainPassword, boolean isForDefaultPasswordRest) {
    if (SystemConstants.MAKER_EMAIL.equalsIgnoreCase(email) || SystemConstants.CHECKER_EMAIL.equalsIgnoreCase(email)) {
      throw exceptionHandler.processBadRequestException(
              settingService.retrieveValue(BACKOFFICE_PASSWORD_DEFAULT_USER_PASSWORD_CANT_BE_UPDATED_MESSAGE_KEY),
              settingService.retrieveValue(BACKOFFICE_PASSWORD_DEFAULT_USER_PASSWORD_CANT_BE_UPDATED_CODE_KEY));
    }
    UserAuthProfileDTO backOfficeUserAuthProfile =
        backOfficeUserAuthProfileServiceImpl.retrieveAuthProfile(email);
    if (Status.PENDING_INVITE_ACCEPTANCE.equals(backOfficeUserAuthProfile.getStatus())) {
      backOfficeUserAuthProfile.setPassword(plainPassword);
      if (isForDefaultPasswordRest) {
        backOfficeUserAuthProfile.setDefaultPassword(false);
        backOfficeUserAuthProfile.setStatus(Status.ACTIVE);
      }
      backOfficeUserAuthProfileServiceImpl.updateAuthProfile(backOfficeUserAuthProfile);
      return;
    } else if (Status.ACTIVE.equals(backOfficeUserAuthProfile.getStatus())) {
      backOfficeUserAuthProfile.setPassword(plainPassword);
      backOfficeUserAuthProfileServiceImpl.updateAuthProfile(backOfficeUserAuthProfile);
      return;
    }
    exceptionHandler.processBadRequestException(
        settingService.retrieveValue(INVITE_ALREADY_ACCEPTED_MESSAGE_KEY),
        settingService.retrieveValue(INVITE_ALREADY_ACCEPTED_CODE_KEY),
        settingService.retrieveValue(INVITE_ALREADY_ACCEPTED_CODE_KEY));
  }
}
