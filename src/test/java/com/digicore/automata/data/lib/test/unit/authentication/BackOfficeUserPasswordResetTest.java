package com.digicore.automata.data.lib.test.unit.authentication;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import com.digicore.automata.data.lib.modules.backoffice.authentication.model.BackOfficeUserAuthProfile;
import com.digicore.automata.data.lib.modules.backoffice.authentication.repository.BackOfficeUserAuthProfileRepository;
import com.digicore.automata.data.lib.modules.backoffice.authentication.repository.BackOfficeUserPasswordHistoryRepository;
import com.digicore.automata.data.lib.modules.backoffice.authentication.service.implementation.BackOfficeUserAuthProfileServiceImpl;
import com.digicore.automata.data.lib.modules.backoffice.authentication.service.implementation.PasswordResetServiceImpl;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserAuthProfileDTO;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.common.util.ClientUtil;
import com.digicore.otp.enums.OtpType;
import com.digicore.otp.service.OtpService;
import com.digicore.registhentication.authentication.dtos.request.ResetPasswordSecondBaseRequestDTO;
import com.digicore.registhentication.authentication.dtos.request.UpdatePasswordRequestDTO;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackOfficeUserPasswordResetTest {

  @Mock private BackOfficeUserAuthProfileServiceImpl userAuthService;
  @Mock private ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
  @Mock private OtpService otpService;

  @Mock private SettingService settingService;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private BackOfficeUserAuthProfileRepository backOfficeUserAuthProfileRepository;


  @Mock private BackOfficeUserPasswordHistoryRepository backOfficeUserPasswordHistoryRepository;

  @InjectMocks private PasswordResetServiceImpl passwordResetService;

  @Test
  void testDefaultPasswordUpdate() {
    ResetPasswordSecondBaseRequestDTO passwordFirstBaseRequestDTO =
        new ResetPasswordSecondBaseRequestDTO();
    passwordFirstBaseRequestDTO.setEmail("test@unittest.com");
    passwordFirstBaseRequestDTO.setOtp("1111");
    passwordFirstBaseRequestDTO.setNewPassword("tester@12ece432");
    doNothing()
        .when(otpService)
        .effect(
            passwordFirstBaseRequestDTO.getEmail(),
            OtpType.PASSWORD_UPDATE,
            passwordFirstBaseRequestDTO.getOtp());
    UserAuthProfileDTO backOfficeUserAuthProfile = new UserAuthProfileDTO();
    backOfficeUserAuthProfile.setUsername(passwordFirstBaseRequestDTO.getEmail());
    when(userAuthService.retrieveAuthProfile(passwordFirstBaseRequestDTO.getEmail()))
        .thenReturn(backOfficeUserAuthProfile);

    passwordResetService.updateAccountPassword(passwordFirstBaseRequestDTO);

    // Verify that the method was called
    Mockito.verify(otpService)
        .effect(
            passwordFirstBaseRequestDTO.getEmail(),
            OtpType.PASSWORD_UPDATE,
            passwordFirstBaseRequestDTO.getOtp());
  }

  @Test
  void testUpdateAccountPasswordWithoutVerification() {
    ResetPasswordSecondBaseRequestDTO passwordFirstBaseRequestDTO =
        new ResetPasswordSecondBaseRequestDTO();
    passwordFirstBaseRequestDTO.setEmail("test@unittest.com");
    passwordFirstBaseRequestDTO.setOtp("1111");
    passwordFirstBaseRequestDTO.setNewPassword("tester@12ece432");
    BackOfficeUserAuthProfile backOfficeUserAuthProfile = new BackOfficeUserAuthProfile();
    UserAuthProfileDTO userAuthProfileDTO = new UserAuthProfileDTO();
    backOfficeUserAuthProfile.setUsername(passwordFirstBaseRequestDTO.getEmail());
    userAuthProfileDTO.setUsername(passwordFirstBaseRequestDTO.getEmail());
    when(userAuthService.retrieveAuthProfileForPasswordReset(passwordFirstBaseRequestDTO.getEmail()))
        .thenReturn(backOfficeUserAuthProfile);


    when(userAuthService.retrieveAuthProfile(passwordFirstBaseRequestDTO.getEmail()))
            .thenReturn(userAuthProfileDTO);

    passwordResetService.updateAccountPasswordWithoutVerification(
        passwordFirstBaseRequestDTO.getEmail(), passwordFirstBaseRequestDTO.getNewPassword());

    // Verify that the method was not called
    Mockito.verify(otpService, Mockito.never())
        .effect(
            passwordFirstBaseRequestDTO.getEmail(),
            OtpType.PASSWORD_UPDATE,
            passwordFirstBaseRequestDTO.getOtp());
  }

  @Test
  void testUpdateAccountPassword() {
    UpdatePasswordRequestDTO updatePasswordRequestDTO = new UpdatePasswordRequestDTO();
    updatePasswordRequestDTO.setOldPassword("12@hhhhhhh.com");
    updatePasswordRequestDTO.setNewPassword("tester@12ece432");

    BackOfficeUserAuthProfile backOfficeUserAuthProfile = new BackOfficeUserAuthProfile();
    backOfficeUserAuthProfile.setUsername(ClientUtil.getLoggedInUsername());
    backOfficeUserAuthProfile.setPassword(updatePasswordRequestDTO.getOldPassword());
    backOfficeUserAuthProfile.setAssignedRole("USER");

    when(userAuthService.retrieveAuthProfileForPasswordReset(ClientUtil.getLoggedInUsername()))
            .thenReturn(backOfficeUserAuthProfile);

    passwordResetService.updateAccountPassword(updatePasswordRequestDTO);

    Mockito.verify(userAuthService, times(1)).retrieveAuthProfileForPasswordReset(any(String.class));
  }

}
