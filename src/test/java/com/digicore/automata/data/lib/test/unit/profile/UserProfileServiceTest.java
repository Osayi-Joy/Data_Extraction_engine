package com.digicore.automata.data.lib.test.unit.profile;

import com.digicore.automata.data.lib.modules.backoffice.authentication.model.BackOfficePasswordHistory;
import com.digicore.automata.data.lib.modules.backoffice.authentication.model.BackOfficeUserAuthProfile;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficePermission;
import com.digicore.automata.data.lib.modules.backoffice.profile.model.BackOfficeUserProfile;
import com.digicore.automata.data.lib.modules.backoffice.profile.repository.BackOfficeUserProfileRepository;
import com.digicore.automata.data.lib.modules.backoffice.profile.service.implementation.BackOfficeUserProfileServiceImpl;
import com.digicore.automata.data.lib.modules.backoffice.profile.specification.BackOfficeUserProfileSpecification;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserAuthProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserEditDTO;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.service.AuthProfileService;
import com.digicore.automata.data.lib.modules.common.authorization.dto.PermissionDTO;
import com.digicore.automata.data.lib.modules.common.authorization.projection.AuthProfileProjection;
import com.digicore.automata.data.lib.modules.common.authorization.service.PermissionService;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.common.util.ClientUtil;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import com.digicore.registhentication.registration.enums.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {
  //  mvn test -Dspring.profiles.active=test -Dtest="UserProfileServiceTest"

  @Mock private BackOfficeUserProfileRepository backOfficeUserProfileRepository;
  @Mock private AuthProfileService<UserAuthProfileDTO> backOfficeUserAuthServiceImpl;

  @Mock private BackOfficeUserProfileSpecification backOfficeUserProfileSpecification;

  @InjectMocks private BackOfficeUserProfileServiceImpl userProfileService;
  @Mock private ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
  @Mock private SettingService settingService;
  @Mock private PermissionService<PermissionDTO, BackOfficePermission> backOfficePermissionServiceImpl;

  @Test
  public void testRetrieveLoggedInUserProfile() {
    String loggedInUsername = ClientUtil.getLoggedInUsername();

    BackOfficeUserAuthProfile authProfile = new BackOfficeUserAuthProfile();
    authProfile.setUsername(loggedInUsername);
    authProfile.setStatus(Status.ACTIVE);
    authProfile.setAssignedRole("USER");

    BackOfficeUserProfile userProfile = new BackOfficeUserProfile();
    userProfile.setFirstName("Joy");
    userProfile.setLastName("Osayi");
    authProfile.setBackOfficeUserProfile(userProfile);

    BackOfficePasswordHistory passwordHistory = new BackOfficePasswordHistory();
    passwordHistory.setLastModifiedDate(LocalDateTime.now());

    when(backOfficeUserAuthServiceImpl.retrieveAuthProfileForPasswordReset(ArgumentMatchers.anyString()))
            .thenReturn(authProfile);
    UserProfileDTO result = userProfileService.retrieveLoggedInUserProfile();

    verify(backOfficeUserAuthServiceImpl, times(1)).retrieveAuthProfileForPasswordReset(loggedInUsername);

    assertEquals(loggedInUsername, result.getUsername());
    assertEquals(loggedInUsername, result.getEmail());
    assertEquals("Joy", result.getFirstName());
    assertEquals("Osayi", result.getLastName());
    assertEquals(Status.ACTIVE, result.getStatus());
    assertEquals("USER", result.getAssignedRole());
  }
  @Test
  void retrieveAllBackOfficeUserProfiles() {
    // Prepare test data

    BackOfficeUserProfile backOfficeUserProfile = new BackOfficeUserProfile();
    BackOfficeUserProfile backOfficeUserProfile2 = new BackOfficeUserProfile();
    backOfficeUserProfile.setEmail("Oluwatobi@gmail.com");
    backOfficeUserProfile2.setEmail("Joy@gmail.com");

    // Mock response
    List<BackOfficeUserProfile> backOfficeUserProfiles =
        Arrays.asList(backOfficeUserProfile, backOfficeUserProfile2);
    Page<BackOfficeUserProfile> rolePage = new PageImpl<>(backOfficeUserProfiles);

    when(backOfficeUserProfileRepository.findAllByIsDeleted(
            ArgumentMatchers.eq(false), ArgumentMatchers.any()))
        .thenReturn(rolePage);

    // Arrange
    // Add some mock BackOfficeUserAuthProfile objects to the list

    AuthProfileProjection authProfileProjection =
        new AuthProfileProjection("Oluwatobi", "Ogunwuyi", "MAKER", Status.ACTIVE);
    when(backOfficeUserAuthServiceImpl.retrieveUserRole(any())).thenReturn(authProfileProjection);

    // Call service
    PaginatedResponseDTO<UserProfileDTO> result = userProfileService.retrieveAllUserProfiles(0, 10);

    // Do assertions
    assertEquals(2, result.getContent().size());
    assertEquals(1, result.getCurrentPage());
    assertTrue(result.getIsFirstPage());
    assertTrue(result.getIsLastPage());
    assertEquals(2, result.getTotalItems());
  }

  @Test
  void search() {

    UserAuthProfileDTO backOfficeUserAuthProfile = new UserAuthProfileDTO();
    UserAuthProfileDTO backOfficeUserAuthProfile2 = new UserAuthProfileDTO();
    UserProfileDTO userProfileDTO = new UserProfileDTO();
    UserProfileDTO userProfileDTO2 = new UserProfileDTO();
    userProfileDTO.setEmail("Oluwatobi@gmail.com");
    userProfileDTO2.setEmail("Joy@gmail.com");
    backOfficeUserAuthProfile.setUserProfile(userProfileDTO);
    backOfficeUserAuthProfile2.setUserProfile(userProfileDTO2);

    // Mock response
    List<UserAuthProfileDTO> backOfficeUserAuthProfiles =
        Arrays.asList(backOfficeUserAuthProfile, backOfficeUserAuthProfile2);
    Page<UserAuthProfileDTO> authProfileDTOS = new PageImpl<>(backOfficeUserAuthProfiles);

    BackOfficeUserProfile backOfficeUserProfile = new BackOfficeUserProfile();
    BackOfficeUserProfile backOfficeUserProfile2 = new BackOfficeUserProfile();
    backOfficeUserProfile.setEmail("Oluwatobi@gmail.com");
    backOfficeUserProfile2.setEmail("Joy@gmail.com");

    // Mock response
    List<BackOfficeUserProfile> backOfficeUserProfiles =
        Arrays.asList(backOfficeUserProfile, backOfficeUserProfile2);
    Page<BackOfficeUserProfile> rolePage = new PageImpl<>(backOfficeUserProfiles);

    // Arrange
    // Add some mock BackOfficeUserAuthProfile objects to the list

    AutomataSearchRequest automataSearchRequest = new AutomataSearchRequest();
    automataSearchRequest.setKey("email");
    automataSearchRequest.setValue("Oluwatobi@gmail.com");
    automataSearchRequest.setPage(0);
    automataSearchRequest.setSize(2);

    when(backOfficeUserAuthServiceImpl.retrieveUserByRoleOrUsername(any(), any(), any()))
        .thenReturn(authProfileDTOS);

    // Call service
    PaginatedResponseDTO<UserProfileDTO> result =
        userProfileService.filterOrSearch(automataSearchRequest);

    // Do assertions
    assertEquals(2, result.getContent().size());
    assertEquals(1, result.getCurrentPage());
    assertTrue(result.getIsFirstPage());
    assertTrue(result.getIsLastPage());
    assertEquals(2, result.getTotalItems());
  }

  @Test
  void filter() {
    UserAuthProfileDTO backOfficeUserProfile = new UserAuthProfileDTO();
    UserAuthProfileDTO backOfficeUserProfile2 = new UserAuthProfileDTO();
    UserProfileDTO userProfileDTO = new UserProfileDTO();
    UserProfileDTO userProfileDTO2 = new UserProfileDTO();
    userProfileDTO.setEmail("Oluwatobi@gmail.com");
    userProfileDTO2.setEmail("Joy@gmail.com");
    backOfficeUserProfile.setUserProfile(userProfileDTO);
    backOfficeUserProfile2.setUserProfile(userProfileDTO2);

    // Mock response
    List<UserAuthProfileDTO> backOfficeUserProfiles =
        Arrays.asList(backOfficeUserProfile, backOfficeUserProfile2);
    Page<UserAuthProfileDTO> rolePage = new PageImpl<>(backOfficeUserProfiles);

    // Arrange
    // Add some mock BackOfficeUserAuthProfile objects to the list

    //    AuthProfileProjection authProfileProjection =
    //        new AuthProfileProjection("Oluwatobi", "Ogunwuyi", "MAKER", Status.ACTIVE);
    when(backOfficeUserAuthServiceImpl.retrieveUserByStatusOrCreatedDate(
            any(), any(), any(), any()))
        .thenReturn(rolePage);

    AutomataSearchRequest automataSearchRequest = new AutomataSearchRequest();
    automataSearchRequest.setKey("email");
    automataSearchRequest.setValue("Oluwatobi@gmail.com");
    automataSearchRequest.setPage(0);
    automataSearchRequest.setSize(2);
    automataSearchRequest.setForFilter(true);
    automataSearchRequest.setStartDate("2021-01-01");
    automataSearchRequest.setEndDate("2023-12-01");

    // Call service
    PaginatedResponseDTO<UserProfileDTO> result =
        userProfileService.filterOrSearch(automataSearchRequest);

    // Do assertions
    assertEquals(2, result.getContent().size());
    assertEquals(1, result.getCurrentPage());
    assertTrue(result.getIsFirstPage());
    assertTrue(result.getIsLastPage());
    assertEquals(2, result.getTotalItems());
  }

  @Test
  void testDeleteBackOfficeUserProfile() {
    BackOfficeUserProfile userProfile = new BackOfficeUserProfile();
    userProfile.setEmail("test@example.com");
    userProfile.setProfileId("123");

    when(backOfficeUserProfileRepository.findFirstByEmailOrderByCreatedDate(any()))
        .thenReturn(Optional.of(userProfile));

    assertDoesNotThrow(() -> userProfileService.deleteUserProfile("test@example.com"));

  }

  @Test
  void testRetrieveUserProfile() {
    BackOfficeUserProfile userProfile = new BackOfficeUserProfile();
    userProfile.setEmail("test@example.com");
    userProfile.setProfileId("123");



    UserAuthProfileDTO userAuthProfileDTO = new UserAuthProfileDTO();
    userAuthProfileDTO.setStatus(Status.ACTIVE);
    userAuthProfileDTO.setUsername("test@example.com");
    when(backOfficeUserAuthServiceImpl.retrieveAuthProfile("test@example.com"))
            .thenReturn(userAuthProfileDTO);


    when(backOfficeUserProfileRepository.findFirstByEmailOrderByCreatedDate(any()))
        .thenReturn(Optional.of(userProfile));

    UserProfileDTO result = userProfileService.retrieveUserProfile("test@example.com");

    assertEquals(userProfile.getEmail(),result.getEmail());
  }

  @Test
  void testEnableBackOfficeInActiveProfile() {

    UserAuthProfileDTO userAuthProfileDTO = new UserAuthProfileDTO();
    userAuthProfileDTO.setStatus(Status.INACTIVE);
    userAuthProfileDTO.setUsername("test@example.com");
    when(backOfficeUserAuthServiceImpl.retrieveAuthProfile("test@example.com"))
        .thenReturn(userAuthProfileDTO);

    doNothing()
        .when(backOfficeUserAuthServiceImpl)
        .updateAuthProfile(any(UserAuthProfileDTO.class));

    userProfileService.enableUserProfile("test@example.com");

    verify(backOfficeUserAuthServiceImpl, times(1))
        .updateAuthProfile(any(UserAuthProfileDTO.class));

    verify(backOfficeUserAuthServiceImpl, times(1)).retrieveAuthProfile(eq("test@example.com"));
  }

  @Test
  void testDisableBackOfficeActiveProfile() {
    UserAuthProfileDTO userAuthProfileDTO = new UserAuthProfileDTO();
    userAuthProfileDTO.setStatus(Status.ACTIVE);
    userAuthProfileDTO.setUsername("test@example.com");
    when(backOfficeUserAuthServiceImpl.retrieveAuthProfile("test@example.com"))
        .thenReturn(userAuthProfileDTO);

    doNothing()
        .when(backOfficeUserAuthServiceImpl)
        .updateAuthProfile(any(UserAuthProfileDTO.class));

    userProfileService.disableUserProfile("test@example.com");

    verify(backOfficeUserAuthServiceImpl).updateAuthProfile(any(UserAuthProfileDTO.class));

    verify(backOfficeUserAuthServiceImpl, times(1)).retrieveAuthProfile(eq("test@example.com"));
  }

  @Test
  void testEditUserProfile() {
    UserEditDTO userProfileDTO = new UserEditDTO();
    userProfileDTO.setEmail("test@example.com");
    userProfileDTO.setFirstName("John");
    userProfileDTO.setLastName("Doe");
    userProfileDTO.setAssignedRole("ROLE_USER");


    BackOfficeUserProfile backOfficeUserProfile = new BackOfficeUserProfile();
    backOfficeUserProfile.setId(1L);

    when(backOfficeUserProfileRepository.findFirstByEmailOrderByCreatedDate(anyString()))
        .thenReturn(Optional.of(backOfficeUserProfile));



    when(backOfficeUserAuthServiceImpl.retrieveAuthProfile(any())).thenReturn(new UserAuthProfileDTO());

    userProfileService.editUserProfile(userProfileDTO);


    assertDoesNotThrow(() -> userProfileService.editUserProfile(userProfileDTO));

  }
}
