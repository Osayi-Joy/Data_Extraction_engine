package com.digicore.automata.data.lib.modules.common.authorization.service;


import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleCreationDTO;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleDTO;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleDTOWithTeamMembers;
import com.digicore.automata.data.lib.modules.common.authorization.projection.RoleProjection;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;

import java.util.List;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
public interface RoleService<T, V> {
    PaginatedResponseDTO<RoleDTOWithTeamMembers> retrieveAllRoles(int pageNumber, int pageSize);

    CsvDto<RoleDTO> prepareRolesCSV(CsvDto<RoleDTO> parameter);

    V retrieveRole(String name);

    RoleDTOWithTeamMembers retrieveSystemRole(String name);

    default V retrieveRole(String name, String email) {
        return null;
    }

    default <U> V retrieveRole(String name, U profile) {
        return null;
    }

    void roleCheck(String name);

    void checkIfRoleIsNotSystemRole(String name);

    default void checkSystemDefaultRolesStatus() {
    }

    <K> T createNewRole(K role);


    void updateExistingRole(RoleCreationDTO role);

    default void systemRolesChecks() {
    }

    V mapRoleDTOToEntity(T permission);

    T mapRoleEntityToDTO(V permission);

    void deleteRole(String name);

    void checkRoleStatus(String name);

    default void checkRoleStatus(String name, String email) {

    }

    void disableRole(String name);

    void enableRole(String name);

    List<RoleProjection> retrieveAllRoles();

    default PaginatedResponseDTO<RoleDTOWithTeamMembers> filterRoles(AutomataSearchRequest searchRequest) {
        return null;
    }

    default <T> CsvDto<T> prepareCSV(CsvDto<T> parameter) {
        return null;
    }

    default void permissionCheck(RoleCreationDTO roleCreationDTO) {
    }

    default void checkRoleIfExist(String roleName) {
    }

    default void updateRolePermissions(RoleCreationDTO role, String username) {
    }

    default PaginatedResponseDTO<RoleDTOWithTeamMembers> searchRoles(AutomataSearchRequest automataSearchRequest) {
        return null;
    }

}
