package com.digicore.automata.data.lib.modules.backoffice.registration.services;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

public interface BackOfficeServiceUserRegistrationService {

  void systemUsersChecks();
  default void doProfileCheck(String email) {
  }
}
