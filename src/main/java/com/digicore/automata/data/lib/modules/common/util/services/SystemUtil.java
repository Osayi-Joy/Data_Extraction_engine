package com.digicore.automata.data.lib.modules.common.util.services;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import com.digicore.automata.data.lib.modules.common.dto.SystemIDGeneratorResult;
import com.digicore.automata.data.lib.modules.common.settings.dto.SettingDTO;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemUtil {
    private static final String COUNTER = "COUNTER";
    private final SettingService settingService;

    public  synchronized SystemIDGeneratorResult generateUniqueSystemId(String prefix, String lastCounter) {
        long counter = Long.parseLong(lastCounter);

        // Increment counter and generate a new ID
        counter++;
        String formattedCounter = String.format("%08d", counter); // Format the counter
        String generatedId = prefix.concat(formattedCounter);

        return new SystemIDGeneratorResult(String.valueOf(counter), generatedId);
    }

    public static Set<String> makerPermissions = Set.of("view-dashboard",
            "view-self-user-details",
            "view-backoffice-users",
            "view-backoffice-user-details",
            "export-backoffice-users",
            "invite-backoffice-user",
            "resend-invite-email",
            "edit-backoffice-user-details",
            "delete-backoffice-profile",
            "enable-backoffice-profile",
            "disable-backoffice-profile",
            "create-roles",
            "view-permissions",
            "view-roles",
            "delete-role",
            "edit-role",
            "disable-role",
            "enable-role",
            "view-role-details",
            "view-self-audit-trails",
            "view-all-audit-trails",
            "create-issuer",
            "view-issuers",
            "delete-issuer",
            "edit-issuer",
            "disable-issuer",
            "enable-issuer",
            "view-issuer-details");

    public static Set<String> checkerPermissions =Set.of("treat-requests",
            "view-dashboard",
            "view-self-user-details",
            "view-backoffice-users",
            "view-backoffice-user-details",
            "export-backoffice-users",
            "approve-invite-backoffice-user",
            "approve-edit-backoffice-user-details",
            "approve-delete-backoffice-profile",
            "approve-enable-backoffice-profile",
            "approve-disable-backoffice-profile",
            "approve-create-roles",
            "view-permissions",
            "view-roles",
            "approve-delete-role",
            "approve-edit-role",
            "approve-disable-role",
            "approve-enable-role",
            "view-role-details",
            "view-self-audit-trails",
            "view-all-audit-trails",
            "approve-create-issuer",
            "view-issuers",
            "approve-delete-issuer",
            "approve-edit-issuer",
            "approve-disable-issuer",
            "approve-enable-issuer",
            "view-issuer-details");

    public synchronized void saveLastCounterValue(String prefix, String counter) {
        SettingDTO settingDTO = new SettingDTO();
        settingDTO.setKey(prefix.concat(COUNTER));
        settingDTO.setValue(counter);
        settingService.updateSetting(settingDTO);
    }

    public synchronized SettingDTO getLastCounterValueForPrefix(String prefix) {
        return createCounterSetting(prefix.concat(COUNTER));
    }

    private SettingDTO createCounterSetting(String key) {
        Optional<SettingDTO> settingDTOOptional = settingService.retrieveSetting(key);
        if (settingDTOOptional.isEmpty()){
            SettingDTO settingDTO = new SettingDTO();
            settingDTO.setSettingVisible(false);
            settingDTO.setKey(key);
            settingDTO.setValue("1");
            settingDTO.setSettingType(COUNTER);
            settingDTO.setDescription("This is used to keep the counter for the system generated IDs");
            return settingService.updateSetting(settingDTO);
        }
        return settingDTOOptional.get();
    }

    public <T> void loadStaticFieldsIntoDB(Class<T> clazz, String settingType) {
        Field[] fields = clazz.getDeclaredFields();
        HashSet<SettingDTO> settingDTOList = new HashSet<>();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(String.class)) {
                try {
                    String key = field.getName();
                    String value = (String) field.get(null);
                    if (!key.contains("_VALIDATOR") && !key.contains("_KEY") && !key.contains("_PATTERN") && (!settingService.settingExists(key))) {
                        SettingDTO settingDTO = new SettingDTO();
                        settingDTO.setSettingVisible(true);
                        settingDTO.setKey(key);
                        settingDTO.setValue(value);
                        settingDTO.setSettingType(settingType);
                        settingDTO.setDescription(
                                "This is system generated description, you should update it.");
                        settingDTOList.add(settingDTO);

                    }
                } catch (IllegalAccessException e) {
                    log.error("unable to load setting because : {}",e.getMessage() );
                    // Handle exception
                }
            }
        }

        if (!settingDTOList.isEmpty())
            settingService.updateSetting(settingDTOList);
    }

}
