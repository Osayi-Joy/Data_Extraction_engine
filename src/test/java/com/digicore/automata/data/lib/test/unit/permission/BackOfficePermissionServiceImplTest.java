package com.digicore.automata.data.lib.test.unit.permission;


import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficePermission;
import com.digicore.automata.data.lib.modules.backoffice.authorization.repository.BackOfficePermissionRepository;
import com.digicore.automata.data.lib.modules.backoffice.authorization.service.implementation.BackOfficePermissionServiceImpl;
import com.digicore.automata.data.lib.modules.common.authorization.dto.PermissionDTO;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
@ExtendWith(MockitoExtension.class)
class BackOfficePermissionServiceImplTest {

 @Mock
 private BackOfficePermissionRepository permissionRepository;
 @Mock
 private  ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
 @InjectMocks
 private BackOfficePermissionServiceImpl permissionService;

 @Test
 void retrieveAllSystemPermissionsTest(){
  BackOfficePermission permission = new BackOfficePermission();
  BackOfficePermission permission1 = new BackOfficePermission();
  BackOfficePermission permission2 = new BackOfficePermission();

  permission.setName("test ");
  permission1.setName("test 1");
  permission2.setName("test 2");

  permission.setPermissionType("USERS");
  permission1.setPermissionType("ROLES");
  permission2.setPermissionType("BILLERS");

    when(permissionRepository.findAll()).thenReturn(List.of(permission,permission1,permission2));

    Set<PermissionDTO> result = permissionService.retrieveAllSystemPermissions();

  assertEquals(3, result.size());
 }
}
