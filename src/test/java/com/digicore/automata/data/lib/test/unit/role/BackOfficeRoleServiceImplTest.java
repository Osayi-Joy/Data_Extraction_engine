package com.digicore.automata.data.lib.test.unit.role;

import com.digicore.api.helper.exception.ZeusRuntimeException;
import com.digicore.automata.data.lib.modules.backoffice.authentication.repository.BackOfficeUserAuthProfileRepository;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficePermission;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficeRole;
import com.digicore.automata.data.lib.modules.backoffice.authorization.repository.BackOfficeRoleRepository;
import com.digicore.automata.data.lib.modules.backoffice.authorization.service.implementation.BackOfficeRoleServiceImpl;
import com.digicore.automata.data.lib.modules.backoffice.authorization.specification.BackOfficeRoleSpecification;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserAuthProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.service.AuthProfileService;
import com.digicore.automata.data.lib.modules.common.authorization.dto.PermissionDTO;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleCreationDTO;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleDTO;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleDTOWithTeamMembers;
import com.digicore.automata.data.lib.modules.common.authorization.projection.AuthProfileProjection;
import com.digicore.automata.data.lib.modules.common.authorization.projection.RoleProjection;
import com.digicore.automata.data.lib.modules.common.authorization.service.PermissionService;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
@ExtendWith(MockitoExtension.class)
class BackOfficeRoleServiceImplTest {

  @Mock private BackOfficeRoleRepository backOfficeRoleRepository;
  @Mock private PermissionService<PermissionDTO, BackOfficePermission> backOfficePermissionServiceImpl;
  @Mock private ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;

  @Mock private BackOfficeUserAuthProfileRepository backOfficeUserAuthProfileRepository;

  @Mock private AuthProfileService<UserAuthProfileDTO> backOfficeUserAuthServiceImpl;
  @Mock private SettingService settingService;
  @Mock private BackOfficeRoleSpecification backOfficeRoleSpecification;
  @InjectMocks private BackOfficeRoleServiceImpl roleService;

  private static RoleTestData getRoleTestData() {
    BackOfficeRole role = new BackOfficeRole();
    BackOfficeRole role1 = new BackOfficeRole();
    role.setName("test-role");
    role1.setName("test-role1");

    role.setActive(true);
    role.setDeleted(false);
    role1.setActive(false);
    role1.setDeleted(false);
    return new RoleTestData(role, role1);
  }

  public static PermissionTestData getPermissionTestData() {
    PermissionDTO permissionDTO = new PermissionDTO();
    permissionDTO.setName("approve-test-edit-user");
    BackOfficePermission permission = new BackOfficePermission();
    permission.setName("approve-test-edit-user");
    PermissionDTO permissionDTO1 = new PermissionDTO();
    permissionDTO1.setName("test-create-user");
    BackOfficePermission permission1 = new BackOfficePermission();
    permission1.setName("approve-test-edit-user");
    return new PermissionTestData(permissionDTO, permission, permissionDTO1, permission1);
  }

//  @Test
//  void searchRolesTest() {
//    AutomataSearchRequest searchRequest = new AutomataSearchRequest();
//    PageRequest pageRequest = PageRequest.of(0, 10);
//
//    List<BackOfficeRole> roles = new ArrayList<>();
//    Page<BackOfficeRole> rolePage = new PageImpl<>(roles, pageRequest, roles.size());
//
//    BackOfficeRoleSpecification specification = backOfficeRoleSpecification.buildSpecification(searchRequest);
//    when(backOfficeRoleSpecification.buildSpecification(searchRequest)).thenReturn(specification);
//    when(backOfficeRoleRepository.findAll(specification, pageRequest)).thenReturn(rolePage);
//
//    PaginatedResponseDTO<RoleDTOWithTeamMembers> result = roleService.searchRoles(searchRequest);
//    // Assert that result is not null and contains the expected values
//    assertNotNull(result);
//    assertEquals(0, result.getCurrentPage());
//    assertEquals(10, result.getTotalItems());
//// Add more assertions based on your expected result
//
//  }


  @Test
  void testEditRole() {
    PermissionTestData permissionTestData = getPermissionTestData();
    RoleCreationDTO roleDTO = new RoleCreationDTO();
    roleDTO.setName("SampleRole");
    roleDTO.setPermissions(Set.of(permissionTestData.permissionDTO().getName(), permissionTestData.permissionDTO1().getName()));

    BackOfficeRole existingRole = new BackOfficeRole();
    when(backOfficeRoleRepository.findFirstByNameAndIsDeletedOrderByCreatedDate("SampleRole", false))
            .thenReturn(Optional.of(existingRole));


     roleService.updateExistingRole(roleDTO);

    assertDoesNotThrow(() -> roleService.updateExistingRole(roleDTO));
  }

  @Test
  void retrieveAllRoles() {
    // Prepare test data
    RoleTestData roleTestData = getRoleTestData();

    // Mock response
    List<BackOfficeRole> roles = Arrays.asList(roleTestData.role(), roleTestData.role1());
    Page<BackOfficeRole> rolePage = new PageImpl<>(roles);

    when(backOfficeRoleRepository.findAllByIsDeleted(ArgumentMatchers.eq(false), ArgumentMatchers.any()))
        .thenReturn(rolePage);

    // Arrange
    List<AuthProfileProjection> mockProfiles = new ArrayList<>();
    // Add some mock BackOfficeUserAuthProfile objects to the list


    AuthProfileProjection backOfficeUserProfile  = new AuthProfileProjection("Oluwatobi","Ogunwuyi","", Status.ACTIVE);
    AuthProfileProjection backOfficeUserProfile2  = new AuthProfileProjection("Joy","Osayi","", Status.ACTIVE);
    mockProfiles.add(backOfficeUserProfile);
    mockProfiles.add(backOfficeUserProfile2);
    when(backOfficeUserAuthProfileRepository.findAllByAssignedRole(any())).thenReturn(mockProfiles);



    // Call service
    PaginatedResponseDTO<RoleDTOWithTeamMembers> result = roleService.retrieveAllRoles(0, 10);

    // Do assertions
    assertEquals(2, result.getContent().size());
    assertEquals(0, result.getCurrentPage());
    assertEquals(2, result.getContent().get(0).getTotalTeamMemberCount());
    assertTrue(result.getContent().get(0).isActive());
    assertFalse(result.getContent().get(1).isActive());
    assertTrue(result.getIsFirstPage());
    assertTrue(result.getIsLastPage());
    assertEquals(2, result.getTotalItems());
  }





  @Test()
  void testCreateNewRole() {
    // Prepare test data
    PermissionTestData permissionTestData = getPermissionTestData();
    RoleCreationDTO roleDTO = new RoleCreationDTO();
    roleDTO.setName("TestRole");
    roleDTO.setPermissions(
        Set.of(permissionTestData.permissionDTO().getName(), permissionTestData.permissionDTO1().getName()));
    roleDTO.setDescription("This is test role description");

    // Mock response

    BackOfficeRole role = roleService.mapRoleDTOToEntity(roleDTO);
    when(backOfficeRoleRepository.save(any(BackOfficeRole.class))).thenReturn(role);

    // Call service
    RoleDTO result = roleService.createNewRole(roleDTO);

    assertNotNull(result);
    assertTrue(result.isActive());
    assertEquals(roleDTO.getName(), result.getName());
  }
  @Test
  void deleteRole(){
    // Prepare test data
    PermissionTestData permissionTestData = getPermissionTestData();
    RoleCreationDTO roleDTO = new RoleCreationDTO();
    roleDTO.setName("TestRole");
    roleDTO.setPermissions(
            Set.of(permissionTestData.permissionDTO().getName(), permissionTestData.permissionDTO1().getName()));
    roleDTO.setDescription("This is test role description");

    // Mock response

    BackOfficeRole role = roleService.mapRoleDTOToEntity(roleDTO);
    when(backOfficeRoleRepository.findFirstByNameAndIsDeletedOrderByCreatedDate(roleDTO.getName(),false)).thenReturn(Optional.ofNullable(role));

    roleService.deleteRole(roleDTO.getName());
    when(backOfficeRoleRepository.findFirstByNameAndIsDeletedOrderByCreatedDate(roleDTO.getName(),false)).thenThrow(new ZeusRuntimeException(""));

    assertThrows(ZeusRuntimeException.class,()-> roleService.retrieveRole(roleDTO.getName()));
  }


  @Test
  void retrieveAllRolesWithoutPagination() {
    // Prepare test data
    RoleTestData roleTestData = getRoleTestData();

    // Mock response
    List<RoleProjection> roles = Arrays.asList(new RoleProjection("test"), new RoleProjection("test2"));


    when(backOfficeRoleRepository.findAllByActive(true))
            .thenReturn(roles);



    // Call service
    List<RoleProjection> result = roleService.retrieveAllRoles();

    // Do assertions
    assertEquals(2, result.size());

  }

  @Test
  void testDisableRole(){
    RoleTestData roleTestData = getRoleTestData();

    when(backOfficeRoleRepository.findFirstByNameAndActiveOrderByCreatedDate(roleTestData.role.getName(),true)).thenReturn(Optional.of(roleTestData.role));
    roleService.disableRole(roleTestData.role.getName());
    assertFalse(roleTestData.role.isActive());
    assertFalse(roleTestData.role1.isActive());
  }

  @Test
  void testEnableRole(){
    RoleTestData roleTestData = getRoleTestData();

    when(backOfficeRoleRepository.findFirstByNameAndActiveOrderByCreatedDate(roleTestData.role1.getName(),false)).thenReturn(Optional.of(roleTestData.role1));
    roleService.enableRole(roleTestData.role1.getName());
    assertTrue(roleTestData.role.isActive());
    assertTrue(roleTestData.role1.isActive());
  }



  private record RoleTestData(BackOfficeRole role, BackOfficeRole role1) {}

  private record PermissionTestData(
      PermissionDTO permissionDTO,
      BackOfficePermission permission,
      PermissionDTO permissionDTO1,
      BackOfficePermission permission1) {}
}
