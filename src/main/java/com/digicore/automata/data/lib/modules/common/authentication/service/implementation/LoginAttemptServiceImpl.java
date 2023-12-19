package com.digicore.automata.data.lib.modules.common.authentication.service.implementation;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import com.digicore.automata.data.lib.modules.common.authentication.model.LoginAttempt;
import com.digicore.automata.data.lib.modules.common.authentication.repository.LoginAttemptRepository;
import com.digicore.automata.data.lib.modules.common.constants.AuditLogActivity;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.config.properties.PropertyConfig;
import com.digicore.registhentication.authentication.services.LoginAttemptService;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import java.time.LocalDateTime;
import java.util.Optional;

import com.digicore.request.processor.processors.AuditLogProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static com.digicore.automata.data.lib.modules.exception.messages.LoginErrorMessage.*;


/** The type Login attempt service. */
@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {
  private final PropertyConfig propertyConfig;

  private final LoginAttemptRepository loginAttemptRepository;
  private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
  private final SettingService settingService;
  private final AuditLogProcessor auditLogProcessor;

  private void systemLockUser(LoginAttempt loginAttempt) {
    loginAttempt.setLoginAccessDenied(true);
    loginAttempt.setAutomatedUnlockTime(
        LocalDateTime.now().plusMinutes(propertyConfig.getLoginAttemptAutoUnlockDuration()));
    this.save(loginAttempt);
  }

  private LoginAttempt getOrCreateByUsername(String username) {
    return this.findByUsername(username)
        .orElseGet(
            () -> {
              LoginAttempt loginAttempt = new LoginAttempt();
              loginAttempt.setFailedAttemptCount(0);
              loginAttempt.setLoginAccessDenied(false);
              loginAttempt.setAutomatedUnlockTime(LocalDateTime.now());
              loginAttempt.setUsername(username);
              return this.save(loginAttempt);
            });
  }

  private LoginAttempt save(LoginAttempt userLoginAttempt) {
    return this.loginAttemptRepository.save(userLoginAttempt);
  }

  private Optional<LoginAttempt> findByUsername(String username) {
    return this.loginAttemptRepository.findFirstByUsernameOrderByCreatedDate(username);
  }

  /**
   * Unlock user.
   *
   * @param username the username
   */
  public void unlockUser(String username) {
    LoginAttempt userLoginAttempt = this.getOrCreateByUsername(username);
    this.unlock(userLoginAttempt);
  }

  private void unlock(LoginAttempt userLoginAttempt) {
    userLoginAttempt.setFailedAttemptCount(0);
    userLoginAttempt.setLoginAccessDenied(false);
    userLoginAttempt.setAutomatedUnlockTime(LocalDateTime.now());
    this.save(userLoginAttempt);
  }

  @Override
  public void verifyLoginAccess(String username, boolean credentialMatches) {
    LoginAttempt loginAttempt = this.getOrCreateByUsername(username);
    if (!credentialMatches) {
      if (loginAttempt.isLoginAccessDenied()
          && this.shouldNotAutomaticallyUnlockProfile(loginAttempt)) {
        exceptionHandler.processCustomException(
            settingService.retrieveValue(LOGIN_ACCESS_DENIED_MESSAGE_KEY).replace(
                "{}", String.valueOf(propertyConfig.getLoginAttemptAutoUnlockDuration())),
                settingService.retrieveValue(LOGIN_ACCESS_DENIED_CODE_KEY),
            HttpStatus.UNAUTHORIZED,
                settingService.retrieveValue(LOGIN_ACCESS_DENIED_CODE_KEY));
        auditLogProcessor.saveAuditWithDescription(AuditLogActivity.LOGIN_FAILURE,AuditLogActivity.BACKOFFICE,String.format(AuditLogActivity.LOGIN_FAILURE_DESCRIPTION,username,"login currently disabled due to ".concat(String.valueOf(loginAttempt.getFailedAttemptCount())).concat("failed attempts")));

      }

      loginAttempt.setFailedAttemptCount(loginAttempt.getFailedAttemptCount() + 1);
      if (loginAttempt.getFailedAttemptCount() >= propertyConfig.getLoginAttemptMaxCount()) {
        this.systemLockUser(loginAttempt);
      }

      this.save(loginAttempt);
    } else {
      if (this.shouldNotAutomaticallyUnlockProfile(loginAttempt)) {
        exceptionHandler.processCustomException(
                settingService.retrieveValue(LOGIN_ACCESS_DENIED_MESSAGE_KEY).replace(
                "{}", String.valueOf(propertyConfig.getLoginAttemptAutoUnlockDuration())),
            settingService.retrieveValue(LOGIN_ACCESS_DENIED_CODE_KEY),
            HttpStatus.FORBIDDEN,
            settingService.retrieveValue(LOGIN_ACCESS_DENIED_CODE_KEY));
      }

      this.unlock(loginAttempt);
    }
  }

  private boolean shouldNotAutomaticallyUnlockProfile(LoginAttempt loginAttempt) {
    return !LocalDateTime.now().isAfter(loginAttempt.getAutomatedUnlockTime());
  }
}
