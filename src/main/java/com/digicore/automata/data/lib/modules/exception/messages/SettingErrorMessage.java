package com.digicore.automata.data.lib.modules.exception.messages;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

public class SettingErrorMessage {
    private SettingErrorMessage() {
    }
    public static final String SETTING_NOT_FOUND_MESSAGE = "Settings Not Found due to invalid key please provide the right key to fetch the settings details";
    public static final String SETTING_NOT_FOUND_CODE = "ST_01";

    public static final String SETTING_KEY_IS_REQUIRED = "setting key is required";
    public static final String SETTING_TYPE_IS_REQUIRED = "setting type is required";
    public static final String SETTING_VALUE_IS_REQUIRED = "setting value is required";
    public static final String SETTING_DESCRIPTION_IS_REQUIRED = "setting description is required";
}
