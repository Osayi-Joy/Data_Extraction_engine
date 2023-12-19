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
public class AuthorizationErrorMessage {

  /**
   * Please take note of the followings
   * 1. You are expected to create the message, code and key fields just as seen below.
   * 2. You are to use the key fields not the message and code field.
   * 3. The key fields value should always be the message or code field name.
   * 4. Ensure to study what had been done below before adding more errors message and code.
   */

  // Role Error
  public static final String ROLE_SHOULD_NOT_CONTAIN_A_CHECKER_AND_A_MAKER_PERMISSION_MESSAGE =
      "you can't assign a checker and a maker permission under one role";
  public static final String ROLE_SHOULD_NOT_CONTAIN_A_CHECKER_AND_A_MAKER_PERMISSION_MESSAGE_KEY =
      "ROLE_SHOULD_NOT_CONTAIN_A_CHECKER_AND_A_MAKER_PERMISSION_MESSAGE";
  public static final String ROLE_SHOULD_NOT_CONTAIN_A_CHECKER_AND_A_MAKER_PERMISSION_CODE =
      "RO_001";
  public static final String ROLE_SHOULD_NOT_CONTAIN_A_CHECKER_AND_A_MAKER_PERMISSION_CODE_KEY =
      "ROLE_SHOULD_NOT_CONTAIN_A_CHECKER_AND_A_MAKER_PERMISSION_CODE";

  public static final String ROLE_ALREADY_EXIST_MESSAGE = "supplied role already exist";
  public static final String ROLE_ALREADY_EXIST_MESSAGE_KEY = "ROLE_ALREADY_EXIST_MESSAGE";
  public static final String ROLE_ALREADY_EXIST_CODE = "RO_002";
  public static final String ROLE_ALREADY_EXIST_CODE_KEY = "ROLE_ALREADY_EXIST_CODE";

  public static final String SYSTEM_ROLE_NOT_USABLE_MESSAGE =
      "supplied role can't be used, this is a system role";
  public static final String SYSTEM_ROLE_NOT_USABLE_MESSAGE_KEY =
      "SYSTEM_ROLE_NOT_USABLE_MESSAGE";
  public static final String SYSTEM_ROLE_NOT_USABLE_CODE = "RO_003";
  public static final String SYSTEM_ROLE_NOT_USABLE_CODE_KEY = "SYSTEM_ROLE_NOT_USABLE_CODE";

  public static final String ROLE_SHOULD_CONTAIN_TREAT_REQUEST_PERMISSION_MESSAGE =
      "you need to add the treat-requests permissions.";
  public static final String ROLE_SHOULD_CONTAIN_TREAT_REQUEST_PERMISSION_MESSAGE_KEY =
      "ROLE_SHOULD_CONTAIN_TREAT_REQUEST_PERMISSION_MESSAGE";
  public static final String ROLE_SHOULD_CONTAIN_TREAT_REQUEST_PERMISSION_CODE = "RO_004";
  public static final String ROLE_SHOULD_CONTAIN_TREAT_REQUEST_PERMISSION_CODE_KEY = "ROLE_SHOULD_CONTAIN_TREAT_REQUEST_PERMISSION_CODE";

  public static final String INVALID_ROLE_MESSAGE = "invalid role";
  public static final String INVALID_ROLE_MESSAGE_KEY = "INVALID_ROLE_MESSAGE";
  public static final String INVALID_ROLE_CODE = "R0_005";

  public static final String ROLE_ALREADY_ACTIVE_MESSAGE= "ROLE_ALREADY_ACTIVE_MESSAGE";
  public static final String ROLE_ALREADY_ACTIVE_MESSAGE_KEY= "Role is already active";
  public static final String ROLE_ALREADY_ACTIVE_CODE= "RO_006";
  public static final String ROLE_ALREADY_ACTIVE_CODE_KEY= "ROLE_ALREADY_ACTIVE_CODE";
  public static final String INVALID_ROLE_CODE_KEY = "INVALID_ROLE_CODE";
  // Permission Error
  public static final String PERMISSION_NOT_IN_SYSTEM_MESSAGE = "this {} permission is not valid";
  public static final String PERMISSION_NOT_IN_SYSTEM_MESSAGE_KEY = "PERMISSION_NOT_IN_SYSTEM_MESSAGE";
  public static final String PERMISSION_NOT_IN_SYSTEM_CODE = "PE_001";
  public static final String PERMISSION_NOT_IN_SYSTEM_CODE_KEY = "PERMISSION_NOT_IN_SYSTEM_CODE";
  public static final String PERMISSIONS_REQUIRED_MESSAGE = "permissions are required";
  public static final String PERMISSIONS_REQUIRED_MESSAGE_KEY = "PERMISSIONS_REQUIRED_MESSAGE";
  public static final String PERMISSIONS_REQUIRED_CODE = "PE_002";
  public static final String PERMISSIONS_REQUIRED_CODE_KEY = "PERMISSIONS_REQUIRED_CODE";

  public static final String PERMISSION_NOT_BELONGING_TO_THE_ROLE_MESSAGE = "You can't assign this permission %s to a user because it is not assigned under this role %s";
  public static final String PERMISSION_NOT_BELONGING_TO_THE_ROLE_MESSAGE_KEY = "PERMISSION_NOT_BELONGING_TO_THE_ROLE_MESSAGE";
  public static final String PERMISSION_NOT_BELONGING_TO_THE_ROLE_CODE = "PE_003";
  public static final String PERMISSION_NOT_BELONGING_TO_THE_ROLE_CODE_KEY = "PERMISSION_NOT_BELONGING_TO_THE_ROLE_CODE";

  public static final String NO_RECORD_FOUND_MESSAGE = "No record found";
  public static final String NO_RECORD_FOUND_MESSAGE_KEY = "NO_RECORD_FOUND_MESSAGE";
  public static final String NO_RECORD_CODE = "GEN_006";
  public static final String NO_RECORD_CODE_KEY = "NO_RECORD_CODE";


  private final SystemUtil systemUtil;


  @PostConstruct
  public void loadStaticFieldsIntoDB() {
    systemUtil.loadStaticFieldsIntoDB(AuthorizationErrorMessage.class,"AUTHORIZATION_ERROR_MESSAGE");
  }
}
