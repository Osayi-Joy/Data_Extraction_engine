package com.digicore.automata.data.lib.modules.exception.messages;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
import com.digicore.automata.data.lib.modules.common.util.services.SystemUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginErrorMessage {

  /**
   * Please take note of the followings
   * 1. You are expected to create the message, code and key fields just as seen below.
   * 2. You are to use the key fields not the message and code field.
   * 3. The key fields value should always be the message or code field name.
   * 4. Ensure to study what had been done below before adding more errors message and code.
   */

  public static final String LOGIN_ACCESS_DENIED_MESSAGE =
      "Your login access is temporarily denied because we detected multiple failed attempt on login, check back in {} minutes or contact support for help.";
  public static final String LOGIN_ACCESS_DENIED_MESSAGE_KEY = "LOGIN_ACCESS_DENIED_MESSAGE";
  public static final String LOGIN_ACCESS_DENIED_CODE = "LA_001";
  public static final String LOGIN_ACCESS_DENIED_CODE_KEY = "LOGIN_ACCESS_DENIED_CODE";
  public static final String LOGIN_FAILED_MESSAGE = "invalid username or password";
  public static final String LOGIN_FAILED_MESSAGE_KEY = "LOGIN_FAILED_MESSAGE";
  public static final String LOGIN_FAILED_USER_NOT_FOUND_CODE = "LOG_001";
  public static final String LOGIN_FAILED_USER_NOT_FOUND_CODE_KEY =
      "LOGIN_FAILED_USER_NOT_FOUND_CODE";
  public static final String LOGIN_FAILED_CODE = "LOG_002";
  public static final String LOGIN_FAILED_CODE_KEY = "LOGIN_FAILED_CODE";
  private final SystemUtil systemUtil;

  @PostConstruct
  public void loadStaticFieldsIntoDB() {
    systemUtil.loadStaticFieldsIntoDB(LoginErrorMessage.class,"AUTHENTICATION_ERROR_MESSAGE");
  }
}
