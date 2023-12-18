package com.digicore.automata.data.lib.test.unit.registration;

import com.digicore.automata.data.lib.modules.backoffice.authentication.service.implementation.BackOfficeUserAuthProfileServiceImpl;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficePermission;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficeRole;
import com.digicore.automata.data.lib.modules.backoffice.profile.repository.BackOfficeUserProfileRepository;
import com.digicore.automata.data.lib.modules.backoffice.registration.services.implementation.BackOfficeUserRegistrationServiceImpl;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleDTO;
import com.digicore.automata.data.lib.modules.common.authorization.service.RoleService;
import com.digicore.automata.data.lib.modules.common.registration.dto.UserRegistrationDTO;
import com.digicore.registhentication.common.dto.response.ProfileDTO;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static com.digicore.automata.data.lib.modules.common.constants.SystemConstants.PERMISSION_TYPE_USERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
@ExtendWith(MockitoExtension.class)
class BackOfficeUserRegistrationTest {

  @Mock private BackOfficeUserProfileRepository userProfileRepository;
  @Mock private BackOfficeUserAuthProfileServiceImpl userAuthProfileService;
  @Mock private RoleService<RoleDTO, BackOfficeRole> backOfficeRoleServiceImpl;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
  @InjectMocks private BackOfficeUserRegistrationServiceImpl backOfficeUserRegistrationService;

  @Test
  void testBackOfficeProfileCreation() {
    // Mock data
    BackOfficeRole role = new BackOfficeRole();
    BackOfficePermission permission = new BackOfficePermission();
    permission.setId(1L);
    permission.setPermissionType(PERMISSION_TYPE_USERS);
    permission.setName("test-permission");
    permission.setDeleted(false);
    role.setName("test-role");
    role.setPermissions(Collections.singleton(permission));
    when(userProfileRepository.existsByEmail("test@unittest.com")).thenReturn(false);
    when(backOfficeRoleServiceImpl.retrieveRole("test-role")).thenReturn(role);

    UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
    userRegistrationDTO.setPassword("P@ssw0rd");
    userRegistrationDTO.setUsername("test@unittest.com");
    userRegistrationDTO.setLastName("test");
    userRegistrationDTO.setFirstName("unit");
    userRegistrationDTO.setAssignedRole("test-role");
    userRegistrationDTO.setEmail("test@unittest.com");
    userRegistrationDTO.setPhoneNumber("07087982874");

    // Invoke the method
    ProfileDTO profileDTO = backOfficeUserRegistrationService.createProfile(userRegistrationDTO);

    // Assertions
    assertEquals(userRegistrationDTO.getAssignedRole(), profileDTO.getAssignedRole());
  }
}
