package com.digicore.automata.data.lib.modules.common.profile.services;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import com.digicore.automata.data.lib.modules.common.authentication.dto.UserEditDTO;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserProfileDTO;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;

public interface UserProfileService<T> {
  T retrieveLoggedInUserProfile();
  PaginatedResponseDTO<T> retrieveAllUserProfiles(int pageNumber, int pageSize);

  PaginatedResponseDTO<T> filterOrSearch(AutomataSearchRequest automataSearchRequest);

  void deleteUserProfile(String email);

  T retrieveUserProfile(String email);

  void enableUserProfile(String email);

  void disableUserProfile(String email);

  void profileExistenceCheckByEmail(String email);

 void editUserProfile(UserEditDTO userEditDTO);

 default CsvDto<UserProfileDTO> prepareUserProfileCSV(CsvDto<UserProfileDTO> parameter){return null;}

}
