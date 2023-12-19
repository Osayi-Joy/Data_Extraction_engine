package com.digicore.automata.data.lib.modules.common.authentication.service;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import com.digicore.automata.data.lib.modules.common.authentication.dto.UserAuthProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserProfileDTO;
import com.digicore.automata.data.lib.modules.common.authorization.projection.AuthProfileProjection;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.registhentication.registration.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuthProfileService<T> {

    T retrieveAuthProfile(String email);
    <V> V retrieveAuthProfileForPasswordReset(String email);

     default void updateAuthProfile(T authProfile){}
     void deleteAuthProfile(String username);


    <V,K,U> void saveNewAuthProfile(
            K registrationRequest,
            U role,
            V userProfile);

    default void disableAuthProfile(String username){}
    default void enableAuthProfile(String username){}
    default AuthProfileProjection retrieveUserRole(Long userProfileId){
       return null;
   }
   default Page<T> retrieveUserByRoleOrUsername(String role, String username, Pageable pageable){
       return null;
   }
   default Page<T> retrieveUserByStatusOrCreatedDate(Status status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable){
       return null;
   }

    default PaginatedResponseDTO<UserProfileDTO> filterProfileByStatusOrDateCreated(
            AutomataSearchRequest automataSearchRequest){
        return null;
    }



}
