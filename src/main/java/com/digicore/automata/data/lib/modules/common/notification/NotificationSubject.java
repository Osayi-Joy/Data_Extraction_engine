package com.digicore.automata.data.lib.modules.common.notification;

import com.digicore.automata.data.lib.modules.common.util.services.SystemUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSubject {
 /**
  * Please take note of the followings
  * 1. You are expected to create the message, code and key fields just as seen below.
  * 2. You are to use the key fields not the message and code field.
  * 3. The key fields value should always be the message or code field name.
  * 4. Ensure to study what had been done below before adding more errors message and code.
  */

 // email-subjects
 public static final String INVITE_BACKOFFICE_USER_SUBJECT = "Invitation to the backoffice automata Platform";
 public static final String INVITE_BACKOFFICE_USER_SUBJECT_KEY = "INVITE_BACKOFFICE_USER_SUBJECT";
 public static final String LOGIN_SUCCESSFUL_SUBJECT = "Login successful";
 public static final String LOGIN_SUCCESSFUL_SUBJECT_KEY = "LOGIN_SUCCESSFUL_SUBJECT";
 public static final String PASSWORD_RESET_SUCCESSFUL_SUBJECT = "Password reset successful";
 public static final String PASSWORD_RESET_SUCCESSFUL_SUBJECT_KEY = "PASSWORD_RESET_SUCCESSFUL_SUBJECT";


 private final SystemUtil systemUtil;

 @PostConstruct
 public void loadStaticFieldsIntoDB() {
  systemUtil.loadStaticFieldsIntoDB(NotificationSubject.class,"EMAIL_SUBJECT");
 }
}
