package com.digicore.automata.data.lib.modules.backoffice.profile.service.implementation;


import com.digicore.automata.data.lib.modules.backoffice.authentication.model.BackOfficePasswordHistory;
import com.digicore.automata.data.lib.modules.backoffice.authentication.model.BackOfficeUserAuthProfile;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficePermission;
import com.digicore.automata.data.lib.modules.backoffice.profile.model.BackOfficeUserProfile;
import com.digicore.automata.data.lib.modules.backoffice.profile.repository.BackOfficeUserProfileRepository;
import com.digicore.automata.data.lib.modules.backoffice.profile.specification.BackOfficeUserProfileSpecification;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserAuthProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserEditDTO;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.service.AuthProfileService;
import com.digicore.automata.data.lib.modules.common.authorization.dto.PermissionDTO;
import com.digicore.automata.data.lib.modules.common.authorization.projection.AuthProfileProjection;
import com.digicore.automata.data.lib.modules.common.authorization.service.PermissionService;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.profile.services.UserProfileService;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.automata.data.lib.modules.common.util.PageableUtil;

import com.digicore.common.util.BeanUtilWrapper;
import com.digicore.common.util.ClientUtil;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import com.digicore.registhentication.registration.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.digicore.automata.data.lib.modules.exception.messages.BackOfficeProfileErrorMessage.*;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BackOfficeUserProfileServiceImpl
    implements UserProfileService<UserProfileDTO> {

  private final BackOfficeUserProfileRepository backOfficeUserProfileRepository;
  private final AuthProfileService<UserAuthProfileDTO> backOfficeUserAuthProfileServiceImpl;
  private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
  private final BackOfficeUserProfileSpecification backOfficeUserProfileSpecification;
  private final SettingService settingService;
  private final PermissionService<PermissionDTO, BackOfficePermission> backOfficePermissionServiceImpl;
    @Override
    public UserProfileDTO retrieveLoggedInUserProfile() {
        UserProfileDTO profileDto = new UserProfileDTO();
        BackOfficeUserAuthProfile backOfficeUserAuthProfile =
                backOfficeUserAuthProfileServiceImpl.retrieveAuthProfileForPasswordReset(
                        ClientUtil.getLoggedInUsername());

        profileDto.setUsername(backOfficeUserAuthProfile.getUsername());
        profileDto.setEmail(backOfficeUserAuthProfile.getUsername());
        profileDto.setFirstName(backOfficeUserAuthProfile.getBackOfficeUserProfile().getFirstName());
        profileDto.setLastName(backOfficeUserAuthProfile.getBackOfficeUserProfile().getLastName());
        profileDto.setStatus(backOfficeUserAuthProfile.getStatus());
        profileDto.setAssignedRole(backOfficeUserAuthProfile.getAssignedRole());

        BackOfficePasswordHistory latestPasswordHistory = backOfficeUserAuthProfile.getBackOfficePasswordHistories()
                .stream()
                .max(Comparator.comparing(BackOfficePasswordHistory::getLastModifiedDate))
                .orElse(null);

        if (latestPasswordHistory != null) {
            profileDto.setLastPasswordUpdatedDate(latestPasswordHistory.getLastModifiedDate());
        } else {
            profileDto.setLastPasswordUpdatedDate(
                    backOfficeUserAuthProfile.getCreatedDate()
            );
        }
        profileDto.setUserPermissions(
                backOfficeUserAuthProfile.getPermissions()
                        .stream()
                        .map(backOfficePermissionServiceImpl::mapEntityToDTO)
                        .collect(Collectors.toSet()));
        profileDto.setPassword(null);
        profileDto.setPin(null);
        return profileDto;
    }


    @Override
  public PaginatedResponseDTO<UserProfileDTO> retrieveAllUserProfiles(
      int pageNumber, int pageSize) {

    Page<BackOfficeUserProfile> backOfficeUserProfiles =
        backOfficeUserProfileRepository.findAllByIsDeleted(
            false, PageableUtil.getPageable(pageNumber, pageSize));
    return PaginatedResponseDTO.<UserProfileDTO>builder()
        .content(
            backOfficeUserProfiles.stream()
                .map(this::getUserProfileDTO)
                .toList())
        .totalPages(backOfficeUserProfiles.getTotalPages())
        .totalItems(backOfficeUserProfiles.getTotalElements())
        .currentPage(backOfficeUserProfiles.getNumber() + 1)
        .isFirstPage(backOfficeUserProfiles.isFirst())
        .isLastPage(backOfficeUserProfiles.isLast())
        .build();
  }

    @Override
    public PaginatedResponseDTO<UserProfileDTO> filterOrSearch(
            AutomataSearchRequest automataSearchRequest) {
        if (automataSearchRequest.isForFilter()) {
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;
      if (automataSearchRequest.getStartDate() != null
          || automataSearchRequest.getEndDate() != null) {
        startDate =
            LocalDate.parse(
                    automataSearchRequest.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atStartOfDay();
        endDate = PageableUtil.dateChecker(automataSearchRequest.getEndDate(), startDate);
      }
            Page<UserAuthProfileDTO> backOfficeUserAuthProfileDTOS =
                    backOfficeUserAuthProfileServiceImpl.retrieveUserByStatusOrCreatedDate(
                            automataSearchRequest.getStatus(),
                            startDate,
                            endDate,
                            PageableUtil.getPageable(
                                    automataSearchRequest.getPage(), automataSearchRequest.getSize()));
            return getUserProfileDTOPaginatedResponseDTO(backOfficeUserAuthProfileDTOS);
        }

        Page<UserAuthProfileDTO> backOfficeUserAuthProfileDTOS =
                backOfficeUserAuthProfileServiceImpl.retrieveUserByRoleOrUsername(
                        automataSearchRequest.getValue(),
                        automataSearchRequest.getValue(),
                        PageableUtil.getPageable(
                                automataSearchRequest.getPage(), automataSearchRequest.getSize()));
        PaginatedResponseDTO<UserProfileDTO> responseDTO =
                getUserProfileDTOPaginatedResponseDTO(backOfficeUserAuthProfileDTOS);
        if (responseDTO.getContent().isEmpty()) {
            Specification<BackOfficeUserProfile> specification =
                    backOfficeUserProfileSpecification.buildSpecification(automataSearchRequest);
            Page<BackOfficeUserProfile> backOfficeUserProfiles =
                    backOfficeUserProfileRepository.findAll(
                            specification,
                            PageableUtil.getPageable(
                                    automataSearchRequest.getPage(), automataSearchRequest.getSize()));
            return PaginatedResponseDTO.<UserProfileDTO>builder()
                    .content(backOfficeUserProfiles.stream().map(this::getUserProfileDTO).toList())
                    .totalPages(backOfficeUserProfiles.getTotalPages())
                    .totalItems(backOfficeUserProfiles.getTotalElements())
                    .currentPage(backOfficeUserProfiles.getNumber() + 1)
                    .isFirstPage(backOfficeUserProfiles.isFirst())
                    .isLastPage(backOfficeUserProfiles.isLast())
                    .build();
        }
        return responseDTO;
    }

  @Override
  public CsvDto<UserProfileDTO> prepareUserProfileCSV(CsvDto<UserProfileDTO> parameter) {
        Pageable pageable = PageRequest.of(parameter.getPage(), parameter.getPageSize(), Sort.by("createdDate").descending());
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (parameter.getAutomataSearchRequest().getStartDate() != null || parameter.getAutomataSearchRequest().getEndDate() != null) {
            startDate = LocalDate.parse(parameter.getAutomataSearchRequest().getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
            endDate = PageableUtil.dateChecker(parameter.getAutomataSearchRequest().getEndDate(), startDate);
        }

        List<UserProfileDTO> data = getUserProfileDTO(parameter, pageable, startDate, endDate);
        if (data.isEmpty()) {
            throw exceptionHandler.processCustomException(
                    BACKOFFICE_PROFILE_NOT_FOUND_MESSAGE,
                    BACKOFFICE_PROFILE_NOT_FOUND_CODE,
                    HttpStatus.NOT_FOUND);
        } else {
            parameter.setCsvHeader(new String[]{"Profile ID", "First Name", "Last Name", "Phone Number", "Email Address", "Status", "Role"});
            parameter.getFieldMappings().put("Profile ID", UserProfileDTO::getProfileId);
            parameter.getFieldMappings().put("First Name", UserProfileDTO::getFirstName);
            parameter.getFieldMappings().put("Last Name", UserProfileDTO::getLastName);
            parameter.getFieldMappings().put("Phone Number", UserProfileDTO::getPhoneNumber);
            parameter.getFieldMappings().put("Email Address", UserProfileDTO::getEmail);
            parameter.getFieldMappings().put("Role", UserProfileDTO::getAssignedRole);
            parameter.getFieldMappings().put("Status", obj -> obj.getStatus().toString());
            parameter.setData(data);
            parameter.setFileName("Backoffice-users-".concat(LocalDateTime.now().toString()));
            return parameter;
        }
    }

    private List<UserProfileDTO> getUserProfileDTO(CsvDto<UserProfileDTO> parameter, Pageable pageable, LocalDateTime startDate, LocalDateTime endDate) {
        return backOfficeUserAuthProfileServiceImpl.retrieveUserByStatusOrCreatedDate(parameter.getAutomataSearchRequest().getStatus(), startDate, endDate, pageable)
                .map(backOfficeUserAuthProfileDTO -> {
                    UserProfileDTO profileDto = new UserProfileDTO();
                    BeanUtilWrapper.copyNonNullProperties(
                            backOfficeUserAuthProfileDTO.getUserProfile(), profileDto);
                    return profileDto;
                })
                .toList();
    }

    private UserProfileDTO getUserProfileDTO(BackOfficeUserProfile backOfficeUserProfile) {
        UserProfileDTO profileDto = new UserProfileDTO();
        AuthProfileProjection profileProjection =
                backOfficeUserAuthProfileServiceImpl.retrieveUserRole(
                backOfficeUserProfile.getId());
        BeanUtilWrapper.copyNonNullProperties(backOfficeUserProfile, profileDto);
        profileDto.setUsername(backOfficeUserProfile.getEmail());
        profileDto.setAssignedRole(profileProjection.getAssignedRole());
        profileDto.setStatus(profileProjection.getStatus());
        profileDto.setPassword(null);
        profileDto.setPin(null);
        profileDto.setPhoneNumber(
            StringUtils.isBlank(backOfficeUserProfile.getPhoneNumber())
                ? "N/S"
                : backOfficeUserProfile.getPhoneNumber());
        return profileDto;
    }

    private UserProfileDTO getUserProfileDTOWithPermissions(BackOfficeUserProfile backOfficeUserProfile) {
        UserProfileDTO profileDto = new UserProfileDTO();
        UserAuthProfileDTO userAuthProfileDTO =
                backOfficeUserAuthProfileServiceImpl.retrieveAuthProfile(
                        backOfficeUserProfile.getEmail());
        BeanUtilWrapper.copyNonNullProperties(backOfficeUserProfile, profileDto);
        profileDto.setUsername(backOfficeUserProfile.getEmail());
        profileDto.setAssignedRole(userAuthProfileDTO.getAssignedRole());
        profileDto.setStatus(userAuthProfileDTO.getStatus());
        profileDto.setPassword(null);
        profileDto.setUserPermissions(userAuthProfileDTO.getPermissions());
        profileDto.setPin(null);
        profileDto.setPhoneNumber(
                StringUtils.isBlank(backOfficeUserProfile.getPhoneNumber())
                        ? "N/S"
                        : backOfficeUserProfile.getPhoneNumber());
        return profileDto;
    }

    private static PaginatedResponseDTO<UserProfileDTO> getUserProfileDTOPaginatedResponseDTO(Page<UserAuthProfileDTO> backOfficeUserAuthProfileDTOS) {
        return PaginatedResponseDTO.<UserProfileDTO>builder()
                .content(
                        backOfficeUserAuthProfileDTOS
                                .map(
                                        backOfficeUserAuthProfileDTO -> {
                                            UserProfileDTO profileDto = new UserProfileDTO();
                                            BeanUtilWrapper.copyNonNullProperties(
                                                    backOfficeUserAuthProfileDTO.getUserProfile(), profileDto);
                                            return profileDto;
                                        })
                                .toList())
                .totalPages(backOfficeUserAuthProfileDTOS.getTotalPages())
                .totalItems(backOfficeUserAuthProfileDTOS.getTotalElements())
                .currentPage(backOfficeUserAuthProfileDTOS.getNumber() + 1)
                .isFirstPage(backOfficeUserAuthProfileDTOS.isFirst())
                .isLastPage(backOfficeUserAuthProfileDTOS.isLast())
                .build();
    }

    @Override
    public void deleteUserProfile(String email) {
        BackOfficeUserProfile backOfficeUserProfile = retrieveProfile(email);
        backOfficeUserProfile.setDeleted(true);
        backOfficeUserProfile.setProfileId(backOfficeUserProfile.getProfileId().concat("_deleted_").concat(LocalDateTime.now().toString()));
        backOfficeUserProfile.setEmail(backOfficeUserProfile.getEmail().concat("_deleted_").concat(LocalDateTime.now().toString()));
        backOfficeUserProfileRepository.save(backOfficeUserProfile);
        backOfficeUserAuthProfileServiceImpl.deleteAuthProfile(email);

    }

    @Override
    public UserProfileDTO retrieveUserProfile(String email) {
      return getUserProfileDTOWithPermissions(retrieveProfile(email));
    }

    private BackOfficeUserProfile retrieveProfile(String email) {
        return backOfficeUserProfileRepository.findFirstByEmailOrderByCreatedDate(email).orElseThrow(() ->
                exceptionHandler.processBadRequestException(
                        settingService.retrieveValue(BACKOFFICE_PROFILE_NOT_FOUND_MESSAGE_KEY),
                        settingService.retrieveValue(BACKOFFICE_PROFILE_NOT_FOUND_CODE_KEY))
        );
    }

    @Override
    public void enableUserProfile(String email) {
        UserAuthProfileDTO userAuthProfileDTO = backOfficeUserAuthProfileServiceImpl
                .retrieveAuthProfile(email);
        if (userAuthProfileDTO.getStatus().equals(Status.ACTIVE)) {
            throw exceptionHandler.processBadRequestException(
                    settingService.retrieveValue(BACKOFFICE_PROFILE_ALREADY_ACTIVE_MESSAGE_KEY),
                    settingService.retrieveValue(BACKOFFICE_PROFILE_ALREADY_ACTIVE_CODE_KEY));
        } else {
            userAuthProfileDTO.setStatus(Status.ACTIVE);
            userAuthProfileDTO.setPassword(null);
            backOfficeUserAuthProfileServiceImpl.updateAuthProfile(userAuthProfileDTO);
        }
    }
    @Override
    public void disableUserProfile(String email) {
        UserAuthProfileDTO userAuthProfileDTO = backOfficeUserAuthProfileServiceImpl
                .retrieveAuthProfile(email);
        if (userAuthProfileDTO.getStatus().equals(Status.INACTIVE)) {
            throw exceptionHandler.processBadRequestException(
                    settingService.retrieveValue(BACKOFFICE_PROFILE_ALREADY_DISABLED_MESSAGE_KEY),
                    settingService.retrieveValue(BACKOFFICE_PROFILE_ALREADY_DISABLED_CODE_KEY));
        } else {
            userAuthProfileDTO.setStatus(Status.INACTIVE);
            userAuthProfileDTO.setPassword(null);
            backOfficeUserAuthProfileServiceImpl.updateAuthProfile(userAuthProfileDTO);
        }
    }

    @Override
    public void editUserProfile(UserEditDTO userEditDTO) {
        BackOfficeUserProfile backOfficeUserProfileToUpdate = this.retrieveProfile(userEditDTO.getEmail());
        BeanUtilWrapper.copyNonNullProperties(userEditDTO, backOfficeUserProfileToUpdate);
        backOfficeUserProfileRepository.save(backOfficeUserProfileToUpdate);
        UserAuthProfileDTO userAuthProfileDTO = backOfficeUserAuthProfileServiceImpl
                .retrieveAuthProfile(userEditDTO.getEmail());
        userAuthProfileDTO.setUsername(userEditDTO.getEmail());
        userAuthProfileDTO.setUserProfile(new UserProfileDTO());
        BeanUtilWrapper.copyNonNullProperties(
                backOfficeUserProfileToUpdate, userAuthProfileDTO.getUserProfile());
        userAuthProfileDTO.setPassword(null);
        userAuthProfileDTO.setAssignedRole(userEditDTO.getAssignedRole());
        backOfficeUserAuthProfileServiceImpl.updateAuthProfile(userAuthProfileDTO);

    }

    @Override
    public void profileExistenceCheckByEmail(String email) {
        if(!checkIfUserProfileExists(email)){
            throw exceptionHandler.processBadRequestException(
                    settingService.retrieveValue(BACKOFFICE_PROFILE_NOT_FOUND_MESSAGE_KEY),
                    settingService.retrieveValue(BACKOFFICE_PROFILE_NOT_FOUND_CODE_KEY)
            );
        }
    }

    private boolean checkIfUserProfileExists(String email){
        return backOfficeUserProfileRepository.existsByEmail(email);
    }
}
