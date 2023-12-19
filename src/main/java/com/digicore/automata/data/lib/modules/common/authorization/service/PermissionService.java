package com.digicore.automata.data.lib.modules.common.authorization.service;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import java.util.Set;

public interface PermissionService<T, V> {
  Set<T> retrieveAllSystemPermissions();

  T retrieveSystemPermissionByName(String name);

  V retrieveSystemPermission(String name);

  void addSystemPermissions(Set<V> newPermissions);

  Set<V> getValidPermissions(Set<String> permissionDTOS);

  void verifyPermissions(Set<String> permissionDTOS);


  V mapDTOToEntity(T permission);

  T mapEntityToDTO(V permission);
}
