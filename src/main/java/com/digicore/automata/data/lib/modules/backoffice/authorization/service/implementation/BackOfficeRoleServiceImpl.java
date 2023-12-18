package com.digicore.automata.data.lib.modules.backoffice.authorization.service.implementation;

import static com.digicore.automata.data.lib.modules.common.util.PageableUtil.*;
import static com.digicore.automata.data.lib.modules.exception.messages.AuthorizationErrorMessage.*;

import com.digicore.automata.data.lib.modules.backoffice.authentication.repository.BackOfficeUserAuthProfileRepository;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficePermission;
import com.digicore.automata.data.lib.modules.backoffice.authorization.specification.BackOfficeRoleSpecification;
import com.digicore.automata.data.lib.modules.common.authorization.dto.PermissionDTO;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleCreationDTO;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleDTO;
import com.digicore.automata.data.lib.modules.common.authorization.dto.RoleDTOWithTeamMembers;
import com.digicore.automata.data.lib.modules.common.authorization.projection.AuthProfileProjection;
import com.digicore.automata.data.lib.modules.common.authorization.projection.RoleProjection;
import com.digicore.automata.data.lib.modules.common.authorization.service.PermissionService;
import com.digicore.automata.data.lib.modules.common.authorization.service.RoleService;
import com.digicore.automata.data.lib.modules.common.constants.SystemConstants;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.automata.data.lib.modules.backoffice.authorization.model.BackOfficeRole;
import com.digicore.automata.data.lib.modules.backoffice.authorization.repository.BackOfficeRoleRepository;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.registhentication.exceptions.ExceptionHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
@Service
@RequiredArgsConstructor
public class BackOfficeRoleServiceImpl implements RoleService<RoleDTO, BackOfficeRole> {

    private final BackOfficeRoleRepository backOfficeRoleRepository;
    private final BackOfficeUserAuthProfileRepository backOfficeUserAuthProfileRepository;
    private final PermissionService<PermissionDTO, BackOfficePermission>
            backOfficePermissionServiceImpl;
    private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
    private final SettingService settingService;
    private final BackOfficeRoleSpecification backOfficeRoleSpecification;


    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public PaginatedResponseDTO<RoleDTOWithTeamMembers> retrieveAllRoles(
            int pageNumber, int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
        Page<BackOfficeRole> rolePage = backOfficeRoleRepository.findAllByIsDeleted(false, pageable);

        return getRoleDTOWithTeamMembers(rolePage);
    }


    @Override
    public PaginatedResponseDTO<RoleDTOWithTeamMembers> searchRoles(
            AutomataSearchRequest automataSearchRequest) {
        Specification<BackOfficeRole> specification =
                backOfficeRoleSpecification.buildSpecification(automataSearchRequest);
        Page<BackOfficeRole> rolePage = backOfficeRoleRepository.findAll(
                specification,
                getPageable(automataSearchRequest.getPage(), automataSearchRequest.getSize()));

        return getRoleDTOWithTeamMembers(rolePage);
    }
    @Override
    public PaginatedResponseDTO<RoleDTOWithTeamMembers> filterRoles(AutomataSearchRequest searchRequest){
        Page<BackOfficeRole> rolePage = backOfficeRoleRepository.findAllByActiveAndCreatedDateBetween(
                searchRequest.isForFilter(),
                toStartOfDay(searchRequest.getStartDate()), toEndOfDay(searchRequest.getEndDate()),
                getPageable(searchRequest.getPage(), searchRequest.getSize()));

        return getRoleDTOWithTeamMembers(rolePage);
    }

    @Override
    public CsvDto<RoleDTO> prepareRolesCSV(CsvDto<RoleDTO> parameter) {

        List<RoleDTO> data = backOfficeRoleRepository.findAllByActiveAndCreatedDateBetween(
                        parameter.getAutomataSearchRequest().isForFilter(),
                        toStartOfDay(parameter.getAutomataSearchRequest().getStartDate()),
                        toEndOfDay(parameter.getAutomataSearchRequest().getEndDate()),
                        getPageable(parameter.getAutomataSearchRequest().getPage(),
                        parameter.getAutomataSearchRequest().getSize()))
                .getContent().stream()
                .map(this::mapRoleEntityToDTO)
                .toList();

        if (data.isEmpty()) {
            throw exceptionHandler.processCustomException("No record found", "GEN_006", HttpStatus.NOT_FOUND);
        } else {
            parameter.setCsvHeader(new String[] {
                    "Role Name",
                    "Role Description",
                    "Active",
                    "Permissions"});
            parameter.getFieldMappings().put("Role Name", RoleDTO::getName);
            parameter.getFieldMappings().put("Role Description", RoleDTO::getDescription);
            parameter.getFieldMappings().put("Active", roleDTO -> Boolean.toString(roleDTO.isActive()));
            parameter.getFieldMappings().put("Permissions", roleDTO -> {
                Set<String> permissionNames = roleDTO.getPermissions().stream()
                        .map(PermissionDTO::getName)
                        .collect(Collectors.toSet());
                return String.join(", ", permissionNames);
            });
            parameter.setData(data);

            LocalDateTime currentDateTime = LocalDateTime.now();
            String fileName = "Roles-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(currentDateTime);
            parameter.setFileName(fileName);

            return parameter;
        }
    }


    @Override
    public BackOfficeRole retrieveRole(String name) {
        return backOfficeRoleRepository
                .findFirstByNameAndIsDeletedOrderByCreatedDate(name, false)
                .orElseThrow(
                        () ->
                                exceptionHandler.processBadRequestException(
                                        settingService.retrieveValue(INVALID_ROLE_MESSAGE_KEY),
                                        settingService.retrieveValue(INVALID_ROLE_CODE_KEY)));
    }

    @Override
    public RoleDTOWithTeamMembers retrieveSystemRole(String name) {
        BackOfficeRole backOfficeRole = retrieveRole(name);
        RoleDTOWithTeamMembers dtoWithTeamMembers = new RoleDTOWithTeamMembers();
        List<String> teamMembers = retrieveAllAuthProfileWithRole(backOfficeRole.getName());
        dtoWithTeamMembers.setTeamMembers(teamMembers);
        dtoWithTeamMembers.setName(backOfficeRole.getName());
        dtoWithTeamMembers.setTotalTeamMemberCount(teamMembers.size());
        dtoWithTeamMembers.setActive(backOfficeRole.isActive());
        dtoWithTeamMembers.setDescription(backOfficeRole.getDescription());
        dtoWithTeamMembers.setPermissions(
                backOfficeRole.getPermissions().stream()
                        .map(backOfficePermissionServiceImpl::mapEntityToDTO)
                        .collect(Collectors.toSet()));
        return dtoWithTeamMembers;
    }

    @Override
    public void roleCheck(String name) {
        if (!checkIfRoleExists(name))
            exceptionHandler.processBadRequestException(
                    settingService.retrieveValue(INVALID_ROLE_MESSAGE_KEY),
                    settingService.retrieveValue(INVALID_ROLE_CODE_KEY),
                    settingService.retrieveValue(INVALID_ROLE_CODE_KEY));
    }


    private boolean checkIfRoleExists(String name) {
        return backOfficeRoleRepository.existsByName(name);
    }

    private Optional<BackOfficeRole> retrieveRoleWithoutThrowingExceptionIfNotFound(String name) {
        return backOfficeRoleRepository.findFirstByNameAndActiveOrderByCreatedDate(name, true);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public <K> RoleDTO createNewRole(K roleDTO) {
        RoleCreationDTO roleCreationDTO = (RoleCreationDTO) roleDTO;
        roleCreationDTO.setActive(true);
        BackOfficeRole newRole = mapRoleDTOToEntity(roleCreationDTO);
        newRole.setPermissions(
                backOfficePermissionServiceImpl.getValidPermissions(roleCreationDTO.getPermissions()));
        return mapRoleEntityToDTO(backOfficeRoleRepository.save(newRole));
    }

    @Override
    public void checkRoleIfExist(String roleName) {
        if (checkIfRoleExists(roleName))
            exceptionHandler.processBadRequestException(
                    settingService.retrieveValue(ROLE_ALREADY_EXIST_MESSAGE_KEY),
                    settingService.retrieveValue(ROLE_ALREADY_EXIST_CODE_KEY),
                    settingService.retrieveValue(ROLE_ALREADY_EXIST_CODE_KEY));
    }

    @Override
    public void permissionCheck(RoleCreationDTO roleCreationDTO) {
        if (roleCreationDTO.getPermissions() == null || roleCreationDTO.getPermissions().isEmpty())
            exceptionHandler.processBadRequestException(settingService.retrieveValue(PERMISSIONS_REQUIRED_MESSAGE_KEY), settingService.retrieveValue(PERMISSIONS_REQUIRED_CODE_KEY), settingService.retrieveValue(PERMISSIONS_REQUIRED_CODE_KEY));
    }

    @Override
    public void checkIfRoleIsNotSystemRole(String name) {
        if (SystemConstants.MAKER_ROLE_NAME.equalsIgnoreCase(name) || SystemConstants.CHECKER_ROLE_NAME.equalsIgnoreCase(name))
            exceptionHandler.processBadRequestException(
                    settingService.retrieveValue(SYSTEM_ROLE_NOT_USABLE_MESSAGE_KEY),
                    settingService.retrieveValue(SYSTEM_ROLE_NOT_USABLE_CODE_KEY),
                    settingService.retrieveValue(SYSTEM_ROLE_NOT_USABLE_CODE_KEY));
    }

    @Override
    public void updateExistingRole(RoleCreationDTO roleDTO) {
        checkIfRoleIsNotSystemRole(roleDTO.getName());
        BackOfficeRole role = retrieveRole(roleDTO.getName());
        role.setActive(true);
        role.setDescription(roleDTO.getDescription());
        role.setPermissions(backOfficePermissionServiceImpl.getValidPermissions(roleDTO.getPermissions()));
        backOfficeRoleRepository.save(role);
        // updateAllUsersPermissions(roleDTO.getName(),role.getPermissions());

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public void systemRolesChecks() {
        long count = backOfficeRoleRepository.count();
        if (count >= 4) return;

        Optional<BackOfficeRole> systemMakerRole =
                backOfficeRoleRepository.findFirstByNameOrderByCreatedDate(SystemConstants.MAKER_ROLE_NAME);
        Optional<BackOfficeRole> systemCheckerRole =
                backOfficeRoleRepository.findFirstByNameOrderByCreatedDate(SystemConstants.CHECKER_ROLE_NAME);
        if (systemMakerRole.isEmpty()) {
            createSystemDefaultRole(
                    SystemConstants.MAKER_ROLE_NAME,
                    "This is a system default maker role used to invite just one backoffice user with an invite privilege",
                    Set.of(
                            "invite-backoffice-user",
                            "resend-invite-email",
                            "create-roles",
                            "edit-role",
                            "edit-backoffice-user-details",
                            "view-permissions",
                            "view-roles"));
        }

        if (systemCheckerRole.isEmpty()) {
            createSystemDefaultRole(
                    SystemConstants.CHECKER_ROLE_NAME,
                    "This is a system default checker role used to accept invite of just one backoffice user with a privilege to approve invited backoffice users",
                    Set.of("approve-invite-backoffice-user", "approve-create-roles", "treat-requests"));
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public void createSystemDefaultRole(
            String makerRoleName, String description, Set<String> permissions) {
        BackOfficeRole role = new BackOfficeRole();
        role.setName(makerRoleName);
        role.setActive(true);
        role.setDescription(description);
        role.setDeleted(false);
        role.setPermissions(backOfficePermissionServiceImpl.getValidPermissions(permissions));
        backOfficeRoleRepository.save(role);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public void checkSystemDefaultRolesStatus() {
        if (backOfficeRoleRepository.count() >= 5) {
            Optional<BackOfficeRole> systemMakerRole =
                    retrieveRoleWithoutThrowingExceptionIfNotFound(SystemConstants.MAKER_ROLE_NAME);
            Optional<BackOfficeRole> systemCheckerRole =
                    retrieveRoleWithoutThrowingExceptionIfNotFound(SystemConstants.CHECKER_ROLE_NAME);
            if (systemMakerRole.isPresent()) {
                systemMakerRole.get().setActive(false);
                systemMakerRole.get().setDeleted(true);
                backOfficeRoleRepository.save(systemMakerRole.get());
            }
            if (systemCheckerRole.isPresent()) {
                systemCheckerRole.get().setActive(false);
                systemCheckerRole.get().setDeleted(true);
                backOfficeRoleRepository.save(systemCheckerRole.get());
            }
        }
    }

    public RoleDTO mapRoleEntityToDTO(BackOfficeRole role) {
        RoleDTO roleDTO = new RoleDTO();
        BeanUtils.copyProperties(role, roleDTO);
        roleDTO.setPermissions(
                role.getPermissions().stream()
                        .map(backOfficePermissionServiceImpl::mapEntityToDTO)
                        .collect(Collectors.toSet()));
        return roleDTO;
    }

    @Override
    public void deleteRole(String name) {
        BackOfficeRole role = retrieveRole(name);
        role.setDeleted(true);
        role.setName(role.getName().concat("_deleted_").concat(LocalDateTime.now().toString()));
        role.setActive(false);
        backOfficeRoleRepository.save(role);
    }

    @Override
    public void checkRoleStatus(String name) {
        retrieveRole(name);
    }

    @Override
    public void disableRole(String name) {
        BackOfficeRole role = retrieveRole(name);
        role.setActive(false);
        backOfficeRoleRepository.save(role);
    }

    @Override
    public void enableRole(String name) {
        BackOfficeRole role = backOfficeRoleRepository.findFirstByNameAndActiveOrderByCreatedDate(name, false)
                .orElseThrow(() -> exceptionHandler.processBadRequestException(
                        ROLE_ALREADY_ACTIVE_MESSAGE, ROLE_ALREADY_ACTIVE_CODE));
        role.setActive(true);
        backOfficeRoleRepository.save(role);
    }


    @Override
    public List<RoleProjection> retrieveAllRoles() {
        return backOfficeRoleRepository.findAllByActive(true);
    }

    public List<String> retrieveAllAuthProfileWithRole(String role) {
        List<AuthProfileProjection> backOfficeUserAuthProfiles =
                backOfficeUserAuthProfileRepository.findAllByAssignedRole(role);
        return backOfficeUserAuthProfiles.stream()
                .map(
                        backOfficeUserAuthProfile ->
                                backOfficeUserAuthProfile
                                        .getFirstName()
                                        .concat(" ")
                                        .concat(backOfficeUserAuthProfile.getLastName()))
                .toList();
    }

    public BackOfficeRole mapRoleDTOToEntity(RoleDTO roleDTO) {
        BackOfficeRole role = new BackOfficeRole();
        BeanUtils.copyProperties(roleDTO, role);
        role.setPermissions(
                roleDTO.getPermissions().stream()
                        .map(backOfficePermissionServiceImpl::mapDTOToEntity)
                        .collect(Collectors.toSet()));
        return role;
    }

    public BackOfficeRole mapRoleDTOToEntity(RoleCreationDTO roleDTO) {
        BackOfficeRole role = new BackOfficeRole();
        BeanUtils.copyProperties(roleDTO, role);
        return role;
    }

    private PaginatedResponseDTO<RoleDTOWithTeamMembers> getRoleDTOWithTeamMembers(Page<BackOfficeRole> rolePage) {
        return PaginatedResponseDTO.<RoleDTOWithTeamMembers>builder()
                .content(
                        rolePage.getContent().stream()
                                .map(
                                        role -> {
                                            RoleDTO roleDTO = mapRoleEntityToDTO(role);
                                            RoleDTOWithTeamMembers dtoWithTeamMembers = new RoleDTOWithTeamMembers();
                                            List<String> teamMembers = retrieveAllAuthProfileWithRole(role.getName());
                                            dtoWithTeamMembers.setTeamMembers(teamMembers);
                                            dtoWithTeamMembers.setName(role.getName());
                                            dtoWithTeamMembers.setTotalTeamMemberCount(teamMembers.size());
                                            dtoWithTeamMembers.setActive(role.isActive());
                                            dtoWithTeamMembers.setDescription(role.getDescription());
                                            dtoWithTeamMembers.setPermissions(roleDTO.getPermissions());
                                            return dtoWithTeamMembers;
                                        })
                                .toList())
                .currentPage(rolePage.getNumber())
                .isFirstPage(rolePage.isFirst())
                .isLastPage(rolePage.isLast())
                .totalItems(rolePage.getTotalElements())
                .totalPages(rolePage.getTotalPages())
                .build();
    }



}
