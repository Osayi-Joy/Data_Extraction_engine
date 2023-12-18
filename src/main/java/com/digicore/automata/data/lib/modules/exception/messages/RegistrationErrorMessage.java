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
public class RegistrationErrorMessage {

 /**
  * Please take note of the followings
  * 1. You are expected to create the message, code and key fields just as seen below.
  * 2. You are to use the key fields not the message and code field.
  * 3. The key fields value should always be the message or code field name.
  * 4. Ensure to study what had been done below before adding more errors message and code.
  */
 public static final String PROFILE_EXISTS_MESSAGE = "profile already exists";
 public static final String PROFILE_EXISTS_MESSAGE_KEY = "PROFILE_EXISTS_MESSAGE";
 public static final String PROFILE_EXISTS_CODE = "RE_002";
 public static final String PROFILE_EXISTS_CODE_KEY = "PROFILE_EXISTS_CODE";
 public static final String PROFILE_NOT_EXIST_MESSAGE = "profile do not exist";
 public static final String PROFILE_NOT_EXIST_MESSAGE_KEY = "PROFILE_NOT_EXIST_MESSAGE";
 public static final String PROFILE_NOT_EXIST_CODE = "RE_003";
 public static final String PROFILE_NOT_EXIST_CODE_KEY = "PROFILE_NOT_EXIST_CODE";
 public static final String INVITE_ALREADY_ACCEPTED_MESSAGE = "the user already accepted invite";
 public static final String INVITE_ALREADY_ACCEPTED_MESSAGE_KEY = "INVITE_ALREADY_ACCEPTED_MESSAGE";
 public static final String INVITE_ALREADY_ACCEPTED_CODE = "RE_004";
 public static final String INVITE_ALREADY_ACCEPTED_CODE_KEY = "INVITE_ALREADY_ACCEPTED_CODE";
 public static final String INVALID_EMAIL_CODE = "RE_005";
 public static final String INVALID_EMAIL_CODE_KEY = "INVALID_EMAIL_CODE";
 public static final String USERNAME_EXISTS_MESSAGE = "username already exists";
 public static final String USERNAME_EXISTS_MESSAGE_KEY = "USERNAME_EXISTS_MESSAGE";
 public static final String USERNAME_EXISTS_CODE = "RE_006";
 public static final String USERNAME_EXISTS_CODE_KEY = "USERNAME_EXISTS_CODE";
 public static final String COMPANY_NAME_REQUIRED_VALIDATOR_MESSAGE = "company name is required";
 public static final String REGISTRATION_CODE_REQUIRED_VALIDATOR_MESSAGE = "registration code is required";
 public static final String PASSWORD_REQUIRED_VALIDATOR_MESSAGE = "password is required, it has to be at least 12 characters long which should contain at least one uppercase letter, one lowercase letter, one digit, and one special character.";
 public static final String PASSWORD_PATTERN = "^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{12,}$";
 private final SystemUtil systemUtil;

 @PostConstruct
 public void loadStaticFieldsIntoDB() {
  systemUtil.loadStaticFieldsIntoDB(RegistrationErrorMessage.class,"REGISTRATION_ERROR_MESSAGE");
 }

}
