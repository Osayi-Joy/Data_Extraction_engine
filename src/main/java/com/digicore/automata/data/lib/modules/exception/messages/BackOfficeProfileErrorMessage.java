package com.digicore.automata.data.lib.modules.exception.messages;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import com.digicore.automata.data.lib.modules.common.util.services.SystemUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackOfficeProfileErrorMessage {
  /**
   * Please take note of the followings
   * 1. You are expected to create the message, code and key fields just as seen below.
   * 2. You are to use the key fields not the message and code field.
   * 3. The key fields value should always be the message or code field name.
   * 4. Ensure to study what had been done below before adding more errors message and code.
   */

  public static final String BACKOFFICE_PROFILE_NOT_FOUND_MESSAGE = "BackOffice Profile Not Found";
  public static final String BACKOFFICE_PROFILE_NOT_FOUND_MESSAGE_KEY = "BACKOFFICE_PROFILE_NOT_FOUND_MESSAGE";

  public static final String BACKOFFICE_PROFILE_NOT_FOUND_CODE = "BP_002";
  public static final String BACKOFFICE_PROFILE_NOT_FOUND_CODE_KEY = "BACKOFFICE_PROFILE_NOT_FOUND_CODE";
  public static final String BACKOFFICE_PROFILE_ALREADY_ACTIVE_MESSAGE =
      "BackOffice Profile is already active and cannot be enabled";
  public static final String BACKOFFICE_PROFILE_ALREADY_ACTIVE_MESSAGE_KEY =
      "BACKOFFICE_PROFILE_ALREADY_ACTIVE_MESSAGE";
  public static final String BACKOFFICE_PROFILE_ALREADY_ACTIVE_CODE = "BP_003";
  public static final String BACKOFFICE_PROFILE_ALREADY_ACTIVE_CODE_KEY = "BACKOFFICE_PROFILE_ALREADY_ACTIVE_CODE";
  public static final String BACKOFFICE_PROFILE_ALREADY_DISABLED_MESSAGE =
      "BackOffice Profile is already disable and cannot be disabled";
  public static final String BACKOFFICE_PROFILE_ALREADY_DISABLED_MESSAGE_KEY =
      "BACKOFFICE_PROFILE_ALREADY_DISABLED_MESSAGE";
  public static final String BACKOFFICE_PROFILE_ALREADY_DISABLED_CODE = "BP_004";
  public static final String BACKOFFICE_PROFILE_ALREADY_DISABLED_CODE_KEY = "BACKOFFICE_PROFILE_ALREADY_DISABLED_CODE";

  public static final String BACKOFFICE_PROFILE_DISABLED_MESSAGE =
          "BackOffice Profile is disabled, contact admin.";
  public static final String BACKOFFICE_PROFILE_DISABLED_MESSAGE_KEY =
          "BACKOFFICE_PROFILE_DISABLED_MESSAGE";
  public static final String BACKOFFICE_PROFILE_DISABLED_CODE = "BP_005";
  public static final String BACKOFFICE_PROFILE_DISABLED_CODE_KEY = "BACKOFFICE_PROFILE_DISABLED_CODE";

  public static final String BACKOFFICE_PASSWORD_ALREADY_USED_MESSAGE =
      "Password has already been previously used";

  public static final String BACKOFFICE_PASSWORD_ALREADY_USED_MESSAGE_KEY = "BACKOFFICE_PASSWORD_ALREADY_USED_MESSAGE";
  public static final String BACKOFFICE_PASSWORD_ALREADY_USED_CODE = "BP_OO5";

  public static final String BACKOFFICE_PASSWORD_ALREADY_USED_CODE_KEY = "BACKOFFICE_PASSWORD_ALREADY_USED_CODE";

  public static final String BACKOFFICE_PASSWORD_DEFAULT_USER_PASSWORD_CANT_BE_UPDATED_MESSAGE =
          "Default system users password can't be updated";

  public static final String BACKOFFICE_PASSWORD_DEFAULT_USER_PASSWORD_CANT_BE_UPDATED_MESSAGE_KEY = "BACKOFFICE_PASSWORD_DEFAULT_USER_PASSWORD_CANT_BE_UPDATED_MESSAGE";
  public static final String BACKOFFICE_PASSWORD_DEFAULT_USER_PASSWORD_CANT_BE_UPDATED_CODE = "BP_OO6";
  public static final String BACKOFFICE_PASSWORD_WRONG_OLD_PASSWORD_MESSAGE = "The old password provided is wrong";
  public static final String BACKOFFICE_PASSWORD_DEFAULT_USER_PASSWORD_CANT_BE_UPDATED_CODE_KEY = "BACKOFFICE_PASSWORD_DEFAULT_USER_PASSWORD_CANT_BE_UPDATED_CODE";
  public static final String BACKOFFICE_PASSWORD_WRONG_OLD_PASSWORD_MESSAGE_KEY = "BACKOFFICE_PASSWORD_WRONG_OLD_PASSWORD_MESSAGE";
  public static final String BACKOFFICE_PASSWORD_WRONG_OLD_PASSWORD_CODE = "BP_OO7";

  public static final String BACKOFFICE_PASSWORD_WRONG_OLD_PASSWORD_CODE_KEY = "BACKOFFICE_PASSWORD_WRONG_OLD_PASSWORD_CODE";
  public static final String EXPORT_FORMANT_NOT_SUPPORTED_MESSAGE =
          "the export format supplied is not supported";
  public static final String EXPORT_FORMANT_NOT_SUPPORTED_CODE = "GEN_007";
  private final SystemUtil systemUtil;

  @PostConstruct
  public void loadStaticFieldsIntoDB() {
    systemUtil.loadStaticFieldsIntoDB(BackOfficeProfileErrorMessage.class,"BACKOFFICE_PROFILE_ERROR_MESSAGE");
  }
}
